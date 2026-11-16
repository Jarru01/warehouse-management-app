package warehouse.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import warehouse.AbstractIntegrationTest;
import warehouse.domain.AuditAkcia;
import warehouse.domain.Miestnost;
import warehouse.domain.NenajdenaEntitaException;
import warehouse.domain.NeplatnaOperaciaException;
import warehouse.domain.Pracovnik;
import warehouse.domain.Regal;
import warehouse.domain.Tovar;
import warehouse.domain.ZakaznikInfo;
import warehouse.repository.TovarRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class SkladServiceTest extends AbstractIntegrationTest {
    private static final String HESLO = "testovacie-heslo";

    @Autowired
    private SkladService skladService;

    @Autowired
    private TovarRepository tovarRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuditService auditService;

    @BeforeEach
    void zaregistrujZakaznikov() {
        this.skladService.registrujZakaznika("10", "Jan", "Novy", HESLO);
        this.skladService.registrujZakaznika("20", "Peter", "Novak", HESLO);
    }

    @Test
    void hesloPracovnikaSaUkladaIbaAkoHash() {
        Pracovnik pracovnik = this.skladService.prijmiPracovnika("1", "Peter", "Novak", HESLO);

        assertNotEquals(HESLO, pracovnik.getHesloHash());
        assertTrue(this.passwordEncoder.matches(HESLO, pracovnik.getHesloHash()));
        assertTrue(pracovnik.isZmenaHesla(), "heslo od riaditela je docasne");
        assertFalse(this.skladService.zakaznik("10").isZmenaHesla(), "zakaznik si heslo zvolil sam");
    }

    @Test
    void zmenySaZapisuDoAuditu() {
        this.skladService.prijmiPracovnika("1", "Peter", "Novak", HESLO);
        Regal regal = this.skladService.pridajRegal(4);

        assertEquals("pracovnik 1 (Peter Novak)",
                this.auditService.najnovsie(AuditAkcia.PRIJATIE_PRACOVNIKA, 5).get(0).getDetail());
        assertEquals("regal " + regal.getId() + ", kapacita 4",
                this.auditService.najnovsie(AuditAkcia.PRIDANIE_REGALU, 5).get(0).getDetail());
        assertEquals("ZAKAZNIK:20", this.auditService.najnovsie(AuditAkcia.REGISTRACIA, 5).get(0).getPouzivatel());
    }

    @Test
    void neuspesnaZmenaNezanechaZaznam() {
        Regal prijem = this.regalV(Miestnost.PRIJEM_TOVARU);

        assertThrows(NeplatnaOperaciaException.class, () -> this.skladService.odstranRegal(prijem.getId()));

        assertTrue(this.auditService.najnovsie(AuditAkcia.ODSTRANENIE_REGALU, 5).isEmpty());
    }

    @Test
    void resetHeslaNastaviNoveDocasneHeslo() {
        this.skladService.prijmiPracovnika("1", "Peter", "Novak", HESLO);

        this.skladService.resetujHesloPracovnika("1", "nove-docasne-heslo");

        Pracovnik pracovnik = this.skladService.pracovnik("1");
        assertTrue(this.passwordEncoder.matches("nove-docasne-heslo", pracovnik.getHesloHash()));
        assertFalse(this.passwordEncoder.matches(HESLO, pracovnik.getHesloHash()));
        assertTrue(pracovnik.isZmenaHesla());
        assertThrows(NenajdenaEntitaException.class,
                () -> this.skladService.resetujHesloPracovnika("99", "nove-docasne-heslo"));
    }

    @Test
    void kratkeHesloSaOdmietne() {
        assertThrows(NeplatnaOperaciaException.class,
                () -> this.skladService.prijmiPracovnika("1", "Peter", "Novak", "kratke"));
        assertThrows(NeplatnaOperaciaException.class,
                () -> this.skladService.registrujZakaznika("30", "Eva", "Mala", "kratke"));
    }

    @Test
    void duplicitnaRegistraciaZakaznikaZlyha() {
        assertThrows(NeplatnaOperaciaException.class,
                () -> this.skladService.registrujZakaznika("10", "Iny", "Clovek", HESLO));
        assertEquals("Jan", this.skladService.zakaznik("10").getMeno());
    }

    @Test
    void tovarPreNeregistrovanehoPrijemcuSaOdmietne() {
        assertThrows(NeplatnaOperaciaException.class, () -> this.skladService.vytvorTovar("100", "Tovar100", 100,
                new ZakaznikInfo("10", "Jan", "Novy"), new ZakaznikInfo("99", "Nikto", "Neznamy")));
        assertThrows(NeplatnaOperaciaException.class, () -> this.skladService.vytvorTovar("100", "Tovar100", 100,
                new ZakaznikInfo("10", "Jan", "Novy"), new ZakaznikInfo("20", "Ine", "Meno")));

        assertFalse(this.tovarRepository.existsById("100"));
    }

    @Test
    void prijmiPracovnikaUmiestniHoDoSkladuTovaru() {
        Pracovnik pracovnik = this.skladService.prijmiPracovnika("1", "Peter", "Novak", HESLO);

        assertEquals(Miestnost.SKLAD_TOVARU, pracovnik.getMiestnost().getKluc());
        assertThrows(NeplatnaOperaciaException.class,
                () -> this.skladService.prijmiPracovnika("1", "Druhy", "Pracovnik", HESLO));
    }

    @Test
    void pracovnikZoberieTovarAPresunieHoMedziMiestnostami() {
        this.vytvorTovar("100");
        this.skladService.prijmiPracovnika("1", "Peter", "Novak", HESLO);
        this.skladService.presunPracovnika("1", Miestnost.PRIJEM_TOVARU);

        this.skladService.zoberTovar("1", "100");

        assertTrue(this.skladService.pracovnik("1").drziTovar());
        assertEquals("100", this.skladService.pracovnik("1").getDrzanyTovar().getId());
        assertTrue(this.skladService.tovarVRegali(this.regalV(Miestnost.PRIJEM_TOVARU).getId()).isEmpty());

        this.skladService.presunPracovnika("1", Miestnost.VYDAJ_TOVARU);
        this.skladService.ulozTovar("1");

        Pracovnik pracovnik = this.skladService.pracovnik("1");
        assertFalse(pracovnik.drziTovar());
        List<Tovar> vVydaji = this.skladService.tovarVRegali(this.regalV(Miestnost.VYDAJ_TOVARU).getId());
        assertEquals(1, vVydaji.size());
        assertEquals("100", vVydaji.get(0).getId());
    }

    @Test
    void pracovnikNemozeZobratTovarMimoSvojejMiestnosti() {
        this.vytvorTovar("100");
        this.skladService.prijmiPracovnika("1", "Peter", "Novak", HESLO);

        assertThrows(NeplatnaOperaciaException.class, () -> this.skladService.zoberTovar("1", "100"));
    }

    @Test
    void plnyPrijemTovaruNeprijmeDalsiTovar() {
        for (int i = 1; i <= Miestnost.KAPACITA_MALEHO_SKLADU; i++) {
            this.vytvorTovar(String.valueOf(i));
        }

        assertThrows(NeplatnaOperaciaException.class, () -> this.vytvorTovar("99"));
    }

    @Test
    void vyzdvihnutieCudziehoTovaruZlyha() {
        this.vytvorTovar("100");
        this.presunTovaruDoVydaja("100");

        assertThrows(NeplatnaOperaciaException.class, () -> this.skladService.vyzdvihniTovar("100", "10"));

        assertTrue(this.tovarRepository.existsById("100"));
    }

    @Test
    void vyzdvihnutieVlastnehoTovaruHoOdstrani() {
        this.vytvorTovar("100");
        this.presunTovaruDoVydaja("100");

        this.skladService.vyzdvihniTovar("100", "20");

        assertFalse(this.tovarRepository.existsById("100"));
    }

    @Test
    void vyluceniePracovnikaUloziJehoTovar() {
        this.vytvorTovar("100");
        this.skladService.prijmiPracovnika("1", "Peter", "Novak", HESLO);
        this.skladService.presunPracovnika("1", Miestnost.PRIJEM_TOVARU);
        this.skladService.zoberTovar("1", "100");

        this.skladService.vylucPracovnika("1");

        assertTrue(this.tovarRepository.existsById("100"));
        assertEquals(Miestnost.PRIJEM_TOVARU,
                this.skladService.tovar("100").getRegal().getMiestnost().getKluc());
        assertTrue(this.skladService.pracovnici().stream().noneMatch(pracovnik -> pracovnik.getId().equals("1")));
    }

    @Test
    void vyluceniePracovnikaBezVolnehoMiestaZlyha() {
        this.vytvorTovar("100");
        this.skladService.prijmiPracovnika("1", "Peter", "Novak", HESLO);
        this.skladService.presunPracovnika("1", Miestnost.PRIJEM_TOVARU);
        this.skladService.zoberTovar("1", "100");
        this.skladService.presunPracovnika("1", Miestnost.SKLAD_TOVARU);

        assertThrows(NeplatnaOperaciaException.class, () -> this.skladService.vylucPracovnika("1"));

        assertEquals("100", this.skladService.pracovnik("1").getDrzanyTovar().getId());
    }

    @Test
    void odstranenieNePrazdnehoRegaluZlyha() {
        Regal regal = this.skladService.pridajRegal(3);
        this.vytvorTovar("100");
        this.skladService.prijmiPracovnika("1", "Peter", "Novak", HESLO);
        this.skladService.presunPracovnika("1", Miestnost.PRIJEM_TOVARU);
        this.skladService.zoberTovar("1", "100");
        this.skladService.presunPracovnika("1", Miestnost.SKLAD_TOVARU);
        this.skladService.ulozTovar("1");

        assertThrows(NeplatnaOperaciaException.class, () -> this.skladService.odstranRegal(regal.getId()));
    }

    @Test
    void odstranenieRegaluMalehoSkladuZlyha() {
        Regal prijem = this.regalV(Miestnost.PRIJEM_TOVARU);
        Regal vydaj = this.regalV(Miestnost.VYDAJ_TOVARU);

        assertThrows(NeplatnaOperaciaException.class, () -> this.skladService.odstranRegal(prijem.getId()));
        assertThrows(NeplatnaOperaciaException.class, () -> this.skladService.odstranRegal(vydaj.getId()));

        assertEquals(1, this.skladService.regale(Miestnost.PRIJEM_TOVARU).size());
        assertEquals(1, this.skladService.regale(Miestnost.VYDAJ_TOVARU).size());
    }

    @Test
    void prazdnyRegalVelkehoSkladuSaOdstrani() {
        Regal regal = this.skladService.pridajRegal(3);

        this.skladService.odstranRegal(regal.getId());

        assertTrue(this.skladService.regale(Miestnost.SKLAD_TOVARU).stream()
                .noneMatch(r -> r.getId().equals(regal.getId())));
    }

    private void presunTovaruDoVydaja(String tovarId) {
        this.skladService.prijmiPracovnika("1", "Mover", "Tester", HESLO);
        this.skladService.presunPracovnika("1", Miestnost.PRIJEM_TOVARU);
        this.skladService.zoberTovar("1", tovarId);
        this.skladService.presunPracovnika("1", Miestnost.VYDAJ_TOVARU);
        this.skladService.ulozTovar("1");
    }

    private void vytvorTovar(String id) {
        this.skladService.vytvorTovar(id, "Tovar" + id, 100,
                new ZakaznikInfo("10", "Jan", "Novy"), new ZakaznikInfo("20", "Peter", "Novak"));
    }

    private Regal regalV(String kluc) {
        return this.skladService.regale(kluc).get(0);
    }
}
