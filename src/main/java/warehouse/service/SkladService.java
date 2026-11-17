package warehouse.service;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import warehouse.domain.AuditAkcia;
import warehouse.domain.Miestnost;
import warehouse.domain.NenajdenaEntitaException;
import warehouse.domain.NeplatnaOperaciaException;
import warehouse.domain.Pracovnik;
import warehouse.domain.Regal;
import warehouse.domain.Riaditel;
import warehouse.domain.Tovar;
import warehouse.domain.TypMiestnosti;
import warehouse.domain.Zakaznik;
import warehouse.domain.ZakaznikInfo;
import warehouse.repository.MiestnostRepository;
import warehouse.repository.PracovnikRepository;
import warehouse.repository.RegalRepository;
import warehouse.repository.RiaditelRepository;
import warehouse.repository.TovarRepository;
import warehouse.repository.ZakaznikRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servisna vrstva so vsetkymi operaciami skladu. Kazda zmena prebieha v transakcii.
 * @author Juraj
 */
@Service
public class SkladService {
    private final MiestnostRepository miestnostRepository;
    private final RegalRepository regalRepository;
    private final TovarRepository tovarRepository;
    private final PracovnikRepository pracovnikRepository;
    private final RiaditelRepository riaditelRepository;
    private final ZakaznikRepository zakaznikRepository;
    private final HeslaService heslaService;
    private final AuditService auditService;

    /**
     * Vytvori servis s potrebnymi repozitarmi, servisom hesiel a auditom.
     * @param miestnostRepository repozitar miestnosti
     * @param regalRepository repozitar regalov
     * @param tovarRepository repozitar tovaru
     * @param pracovnikRepository repozitar pracovnikov
     * @param riaditelRepository repozitar riaditelov
     * @param zakaznikRepository repozitar zakaznikov
     * @param heslaService servis hesiel
     * @param auditService servis auditu
     */
    public SkladService(MiestnostRepository miestnostRepository, RegalRepository regalRepository,
                        TovarRepository tovarRepository, PracovnikRepository pracovnikRepository,
                        RiaditelRepository riaditelRepository, ZakaznikRepository zakaznikRepository,
                        HeslaService heslaService, AuditService auditService) {
        this.miestnostRepository = miestnostRepository;
        this.regalRepository = regalRepository;
        this.tovarRepository = tovarRepository;
        this.pracovnikRepository = pracovnikRepository;
        this.riaditelRepository = riaditelRepository;
        this.zakaznikRepository = zakaznikRepository;
        this.heslaService = heslaService;
        this.auditService = auditService;
    }

    /**
     * Vrati vsetky miestnosti zoradene podla kluca.
     * @return zoznam miestnosti
     */
    @Transactional(readOnly = true)
    public List<Miestnost> miestnosti() {
        return this.miestnostRepository.findAll(Sort.by("kluc"));
    }

    /**
     * Vrati miestnost s danym klucom alebo vyhodi vynimku.
     * @param kluc kluc miestnosti
     * @return miestnost
     */
    @Transactional(readOnly = true)
    public Miestnost miestnost(String kluc) {
        return this.miestnostRepository.findById(kluc)
                .orElseThrow(() -> new NenajdenaEntitaException("Miestnost " + kluc + " neexistuje."));
    }

    /**
     * Vrati vsetkych pracovnikov zoradenych podla id.
     * @return zoznam pracovnikov
     */
    @Transactional(readOnly = true)
    public List<Pracovnik> pracovnici() {
        return this.pracovnikRepository.findAllWithDetail();
    }

    /**
     * Vrati pracovnika s danym id alebo vyhodi vynimku.
     * @param id id pracovnika
     * @return pracovnik
     */
    @Transactional(readOnly = true)
    public Pracovnik pracovnik(String id) {
        return this.pracovnikRepository.findWithDetailById(id)
                .orElseThrow(() -> new NenajdenaEntitaException("Pracovnik " + id + " neexistuje."));
    }

    /**
     * Vrati riaditela s danym id alebo vyhodi vynimku.
     * @param id id riaditela
     * @return riaditel
     */
    @Transactional(readOnly = true)
    public Riaditel riaditel(String id) {
        return this.riaditelRepository.findById(id)
                .orElseThrow(() -> new NenajdenaEntitaException("Riaditel " + id + " neexistuje."));
    }

    /**
     * Vrati tovar s danym id alebo vyhodi vynimku.
     * @param id id tovaru
     * @return tovar
     */
    @Transactional(readOnly = true)
    public Tovar tovar(String id) {
        return this.tovarRepository.findWithDetailById(id)
                .orElseThrow(() -> new NenajdenaEntitaException("Tovar " + id + " neexistuje."));
    }

    /**
     * Vrati regale v miestnosti zoradene podla id.
     * @param kluc kluc miestnosti
     * @return zoznam regalov
     */
    @Transactional(readOnly = true)
    public List<Regal> regale(String kluc) {
        return this.regalRepository.findByMiestnostKlucWithDetail(kluc);
    }

    /**
     * Vrati tovar v regali zoradany podla slotu.
     * @param regalId id regalu
     * @return zoznam tovaru
     */
    @Transactional(readOnly = true)
    public List<Tovar> tovarVRegali(Long regalId) {
        return this.tovarRepository.findByRegalIdWithDetail(regalId);
    }

    /**
     * Vrati regale miestnosti s ich tovarom dvoma dotazmi bez ohladu na pocet regalov.
     * @param kluc kluc miestnosti
     * @return regale zoradene podla id, kazdy s tovarom zoradenym podla slotu
     */
    @Transactional(readOnly = true)
    public List<RegalSTovarom> regaleSTovarom(String kluc) {
        Map<Long, List<Tovar>> tovar = podlaRegalu(this.tovarRepository.findByMiestnostKlucWithDetail(kluc));
        return this.regalRepository.findByMiestnostKlucWithDetail(kluc).stream()
                .map(regal -> new RegalSTovarom(regal, tovar.getOrDefault(regal.getId(), List.of())))
                .toList();
    }

    /**
     * Vrati vsetky regale skladu s ich tovarom, zoskupene podla kluca miestnosti, dvoma dotazmi bez ohladu
     * na pocet miestnosti a regalov. Miestnost bez regalov v mape chyba.
     * @return regale podla kluca miestnosti
     */
    @Transactional(readOnly = true)
    public Map<String, List<RegalSTovarom>> prehladRegalov() {
        Map<Long, List<Tovar>> tovar = podlaRegalu(this.tovarRepository.findAllUlozeneWithDetail());
        Map<String, List<RegalSTovarom>> prehlad = new LinkedHashMap<>();
        for (Regal regal : this.regalRepository.findAllWithDetail()) {
            prehlad.computeIfAbsent(regal.getMiestnost().getKluc(), kluc -> new ArrayList<>())
                    .add(new RegalSTovarom(regal, tovar.getOrDefault(regal.getId(), List.of())));
        }
        return prehlad;
    }

    /**
     * Vrati tovar v miestnosti na vydaj tovaru, ktory patri zakaznikovi s danym id.
     * @param zakaznikId id zakaznika
     * @return zoznam tovaru
     */
    @Transactional(readOnly = true)
    public List<Tovar> tovarPreZakaznika(String zakaznikId) {
        return this.tovarRepository.findByMiestnostKlucAndPrijemcaId(Miestnost.VYDAJ_TOVARU, zakaznikId);
    }

    /**
     * Vrati registrovaneho zakaznika s danym id alebo vyhodi vynimku.
     * @param id id zakaznika
     * @return zakaznik
     */
    @Transactional(readOnly = true)
    public Zakaznik zakaznik(String id) {
        return this.zakaznikRepository.findById(id)
                .orElseThrow(() -> new NenajdenaEntitaException("Zakaznik " + id + " neexistuje."));
    }

    /**
     * Prijme noveho pracovnika s heslom na prihlasenie a umiestni ho do hlavneho skladu tovaru.
     * @param id id pracovnika
     * @param meno meno pracovnika
     * @param priezvisko priezvisko pracovnika
     * @param heslo docasne heslo pracovnika, ulozi sa iba jeho hash a pracovnik si ho musi zmenit
     * @return prijaty pracovnik
     */
    @Transactional
    public Pracovnik prijmiPracovnika(String id, String meno, String priezvisko, String heslo) {
        if (this.pracovnikRepository.existsById(id)) {
            throw new NeplatnaOperaciaException("Pracovnik so zadanym ID uz existuje.");
        }
        Pracovnik pracovnik = new Pracovnik(id, meno, priezvisko);
        pracovnik.nastavHeslo(this.heslaService.zahesuj(heslo), true);
        pracovnik.premiestniDo(this.miestnost(Miestnost.SKLAD_TOVARU));
        Pracovnik ulozeny = this.pracovnikRepository.save(pracovnik);
        this.auditService.zaznamenaj(AuditAkcia.PRIJATIE_PRACOVNIKA,
                "pracovnik " + id + " (" + meno + " " + priezvisko + ")");
        return ulozeny;
    }

    /**
     * Zaregistruje noveho zakaznika s heslom na prihlasenie.
     * @param id id zakaznika
     * @param meno meno zakaznika
     * @param priezvisko priezvisko zakaznika
     * @param email e-mail na obnovu hesla
     * @param heslo heslo zakaznika, ulozi sa iba jeho hash
     * @return registrovany zakaznik
     */
    @Transactional
    public Zakaznik registrujZakaznika(String id, String meno, String priezvisko, String email, String heslo) {
        if (this.zakaznikRepository.existsById(id)) {
            throw new NeplatnaOperaciaException("Zakaznik so zadanym ID uz existuje.");
        }
        if (this.zakaznikRepository.existsByEmail(Zakaznik.normalizujEmail(email))) {
            throw new NeplatnaOperaciaException("Zakaznik so zadanym e-mailom uz existuje.");
        }
        Zakaznik zakaznik = new Zakaznik(id, meno, priezvisko, email);
        zakaznik.nastavHeslo(this.heslaService.zahesuj(heslo), false);
        Zakaznik ulozeny = this.zakaznikRepository.save(zakaznik);
        this.auditService.zaznamenajPre("ZAKAZNIK:" + id, AuditAkcia.REGISTRACIA,
                "zakaznik " + id + " (" + meno + " " + priezvisko + ")");
        return ulozeny;
    }

    /**
     * Riaditel nastavi pracovnikovi nove docasne heslo, ktore si pracovnik musi pri prihlaseni zmenit.
     * @param id id pracovnika
     * @param noveHeslo nove docasne heslo
     */
    @Transactional
    public void resetujHesloPracovnika(String id, String noveHeslo) {
        Pracovnik pracovnik = this.pracovnik(id);
        pracovnik.nastavHeslo(this.heslaService.zahesuj(noveHeslo), true);
        this.auditService.zaznamenaj(AuditAkcia.RESET_HESLA, "pracovnik " + id);
    }

    /**
     * Vyluci pracovnika. Ak pracovnik drzi tovar, najskor sa ho pokusi ulozit.
     * @param id id pracovnika
     */
    @Transactional
    public void vylucPracovnika(String id) {
        Pracovnik pracovnik = this.pracovnik(id);
        if (pracovnik.drziTovar()) {
            this.ulozTovar(id);
        }
        this.pracovnikRepository.delete(pracovnik);
        this.auditService.zaznamenaj(AuditAkcia.VYLUCENIE_PRACOVNIKA, "pracovnik " + id);
    }

    /**
     * Prida novy regal do hlavneho skladu tovaru.
     * @param kapacita kapacita regalu
     * @return pridany regal
     */
    @Transactional
    public Regal pridajRegal(int kapacita) {
        Regal regal = this.regalRepository.save(new Regal(this.miestnost(Miestnost.SKLAD_TOVARU), kapacita));
        this.auditService.zaznamenaj(AuditAkcia.PRIDANIE_REGALU,
                "regal " + regal.getId() + ", kapacita " + kapacita);
        return regal;
    }

    /**
     * Odstrani prazdny regal. Jediny regal maleho skladu odstranit nie je mozne.
     * @param regalId id regalu
     */
    @Transactional
    public void odstranRegal(Long regalId) {
        Regal regal = this.regal(regalId);
        if (regal.getMiestnost().getTyp() == TypMiestnosti.MALY) {
            throw new NeplatnaOperaciaException("Regal maleho skladu nie je mozne odstranit.");
        }
        if (this.tovarRepository.countByRegalId(regalId) > 0) {
            throw new NeplatnaOperaciaException("Regal nie je prazdny.");
        }
        this.regalRepository.delete(regal);
        this.auditService.zaznamenaj(AuditAkcia.ODSTRANENIE_REGALU, "regal " + regalId);
    }

    /**
     * Presunie pracovnika do miestnosti s danym klucom.
     * @param pracovnikId id pracovnika
     * @param klucMiestnosti kluc cielovej miestnosti
     * @return presunuty pracovnik
     */
    @Transactional
    public Pracovnik presunPracovnika(String pracovnikId, String klucMiestnosti) {
        Pracovnik pracovnik = this.pracovnik(pracovnikId);
        pracovnik.premiestniDo(this.miestnost(klucMiestnosti));
        this.auditService.zaznamenaj(AuditAkcia.PRESUN_PRACOVNIKA,
                "pracovnik " + pracovnikId + " do " + klucMiestnosti);
        return pracovnik;
    }

    /**
     * Pracovnik zoberie tovar z regalu v miestnosti, v ktorej sa nachadza.
     * @param pracovnikId id pracovnika
     * @param tovarId id tovaru
     */
    @Transactional
    public void zoberTovar(String pracovnikId, String tovarId) {
        Pracovnik pracovnik = this.pracovnik(pracovnikId);
        if (pracovnik.drziTovar()) {
            throw new NeplatnaOperaciaException("Pracovnik uz drzi tovar.");
        }
        Tovar tovar = this.tovar(tovarId);
        if (tovar.getRegal() == null
                || !tovar.getRegal().getMiestnost().getKluc().equals(pracovnik.getMiestnost().getKluc())) {
            throw new NeplatnaOperaciaException("Tovar sa nenachadza v miestnosti pracovnika.");
        }
        String odkial = tovar.getRegal().getMiestnost().getKluc();
        tovar.vyberZRegalu();
        pracovnik.uchop(tovar);
        this.auditService.zaznamenaj(AuditAkcia.ZOBRATIE_TOVARU,
                "tovar " + tovarId + " z " + odkial + " pracovnikom " + pracovnikId);
    }

    /**
     * Pracovnik ulozi drzany tovar do prveho volneho slotu v miestnosti, v ktorej sa nachadza.
     * @param pracovnikId id pracovnika
     */
    @Transactional
    public void ulozTovar(String pracovnikId) {
        Pracovnik pracovnik = this.pracovnik(pracovnikId);
        if (!pracovnik.drziTovar()) {
            throw new NeplatnaOperaciaException("Pracovnik nedrzi ziadny tovar.");
        }
        VolneMiesto miesto = this.najdiVolneMiesto(pracovnik.getMiestnost().getKluc())
                .orElseThrow(() -> new NeplatnaOperaciaException("V miestnosti nie je volny regal."));
        Regal regal = miesto.regal();
        int slot = miesto.slot();
        Tovar tovar = pracovnik.getDrzanyTovar();
        tovar.ulozDoRegalu(regal, slot);
        pracovnik.poloz();
        this.auditService.zaznamenaj(AuditAkcia.ULOZENIE_TOVARU, "tovar " + tovar.getId() + " do "
                + pracovnik.getMiestnost().getKluc() + ", regal " + regal.getId() + ", slot " + slot);
    }

    /**
     * Zakaznik vytvori tovar, ktory sa ulozi do miestnosti na prijem tovaru. Prijemca musi byt registrovany
     * zakaznik so zhodnym menom a priezviskom, pri tovare sa ulozia jeho registrovane udaje.
     * @param id id tovaru
     * @param nazov nazov tovaru
     * @param vaha vaha tovaru v gramoch
     * @param odosielatel udaje o odosielatelovi
     * @param prijemca udaje o prijemcovi
     * @return vytvoreny tovar
     */
    @Transactional
    public Tovar vytvorTovar(String id, String nazov, int vaha, ZakaznikInfo odosielatel, ZakaznikInfo prijemca) {
        if (this.tovarRepository.existsById(id)) {
            throw new NeplatnaOperaciaException("Tovar so zadanym ID uz existuje.");
        }
        Zakaznik registrovanyPrijemca = this.zakaznikRepository.findById(prijemca.getId())
                .filter(zakaznik -> zakaznik.getMeno().equalsIgnoreCase(prijemca.getMeno())
                        && zakaznik.getPriezvisko().equalsIgnoreCase(prijemca.getPriezvisko()))
                .orElseThrow(() -> new NeplatnaOperaciaException("Prijemca so zadanymi udajmi nie je registrovany."));
        Miestnost prijem = this.miestnost(Miestnost.PRIJEM_TOVARU);
        List<Regal> regale = this.regalRepository.findByMiestnostKlucOrderByIdAsc(prijem.getKluc());
        if (regale.size() != 1) {
            throw new NeplatnaOperaciaException("Miestnost na prijem tovaru nema prave jeden regal.");
        }
        VolneMiesto miesto = this.najdiVolneMiesto(prijem.getKluc())
                .orElseThrow(() -> new NeplatnaOperaciaException("Regal v miestnosti na prijem tovaru je plny."));
        Tovar tovar = new Tovar(id, nazov, vaha, odosielatel, registrovanyPrijemca.naInfo());
        tovar.ulozDoRegalu(miesto.regal(), miesto.slot());
        Tovar ulozeny = this.tovarRepository.save(tovar);
        this.auditService.zaznamenaj(AuditAkcia.VLOZENIE_TOVARU,
                "tovar " + id + " od " + odosielatel.getId() + " pre " + prijemca.getId());
        return ulozeny;
    }

    /**
     * Zakaznik vyzdvihne svoj tovar z miestnosti na vydaj tovaru.
     * @param tovarId id tovaru
     * @param zakaznikId id zakaznika
     */
    @Transactional
    public void vyzdvihniTovar(String tovarId, String zakaznikId) {
        Tovar tovar = this.tovar(tovarId);
        if (tovar.getRegal() == null
                || !tovar.getRegal().getMiestnost().getKluc().equals(Miestnost.VYDAJ_TOVARU)) {
            throw new NeplatnaOperaciaException("Tovar nie je pripraveny na vydaj.");
        }
        if (!tovar.getPrijemca().getId().equals(zakaznikId)) {
            throw new NeplatnaOperaciaException("Tovar nepatri zadanemu zakaznikovi.");
        }
        this.tovarRepository.delete(tovar);
        this.auditService.zaznamenaj(AuditAkcia.VYZDVIHNUTIE_TOVARU, "tovar " + tovarId + " zakaznikom " + zakaznikId);
    }

    private Regal regal(Long regalId) {
        return this.regalRepository.findById(regalId)
                .orElseThrow(() -> new NenajdenaEntitaException("Regal " + regalId + " neexistuje."));
    }

    private record VolneMiesto(Regal regal, int slot) {
    }

    /**
     * Najde regal s najnizsim id, ktory ma volne miesto, a v nom najnizsi volny slot - jednym dotazom.
     * Hibernate pred nativnym dotazom zapise neulozene zmeny, takze dotaz vidi aj tovar zobraty v tej istej
     * transakcii.
     */
    private Optional<VolneMiesto> najdiVolneMiesto(String kluc) {
        return this.regalRepository.findPrvyVolnySlot(kluc)
                .map(volny -> new VolneMiesto(this.regal(volny.getRegalId()), volny.getSlot()));
    }

    private static Map<Long, List<Tovar>> podlaRegalu(List<Tovar> tovar) {
        return tovar.stream().collect(Collectors.groupingBy(t -> t.getRegal().getId(), LinkedHashMap::new,
                Collectors.toList()));
    }
}
