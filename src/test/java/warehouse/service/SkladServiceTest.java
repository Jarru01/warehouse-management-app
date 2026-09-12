package warehouse.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import warehouse.AbstractIntegrationTest;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class SkladServiceTest extends AbstractIntegrationTest {

    @Autowired
    private SkladService skladService;

    @Autowired
    private TovarRepository tovarRepository;

    @Test
    void prijmiPracovnikaUmiestniHoDoSkladuTovaru() {
        Pracovnik pracovnik = this.skladService.prijmiPracovnika("1", "Peter", "Novak");

        assertEquals(Miestnost.SKLAD_TOVARU, pracovnik.getMiestnost().getKluc());
        assertThrows(NeplatnaOperaciaException.class,
                () -> this.skladService.prijmiPracovnika("1", "Druhy", "Pracovnik"));
    }

    @Test
    void pracovnikZoberieTovarAPresunieHoMedziMiestnostami() {
        this.vytvorTovar("100");
        this.skladService.prijmiPracovnika("1", "Peter", "Novak");
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
        this.skladService.prijmiPracovnika("1", "Peter", "Novak");

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
        this.skladService.prijmiPracovnika("1", "Peter", "Novak");
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
        this.skladService.prijmiPracovnika("1", "Peter", "Novak");
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
        this.skladService.prijmiPracovnika("1", "Peter", "Novak");
        this.skladService.presunPracovnika("1", Miestnost.PRIJEM_TOVARU);
        this.skladService.zoberTovar("1", "100");
        this.skladService.presunPracovnika("1", Miestnost.SKLAD_TOVARU);
        this.skladService.ulozTovar("1");

        assertThrows(NeplatnaOperaciaException.class, () -> this.skladService.odstranRegal(regal.getId()));
    }

    private void presunTovaruDoVydaja(String tovarId) {
        this.skladService.prijmiPracovnika("1", "Mover", "Tester");
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
