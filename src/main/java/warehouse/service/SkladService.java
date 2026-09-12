package warehouse.service;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import warehouse.domain.Miestnost;
import warehouse.domain.Pracovnik;
import warehouse.domain.Regal;
import warehouse.domain.Riaditel;
import warehouse.domain.Tovar;
import warehouse.domain.ZakaznikInfo;
import warehouse.repository.MiestnostRepository;
import warehouse.repository.PracovnikRepository;
import warehouse.repository.RegalRepository;
import warehouse.repository.RiaditelRepository;
import warehouse.repository.TovarRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    /**
     * Vytvori servis s potrebnymi repozitarmi.
     * @param miestnostRepository repozitar miestnosti
     * @param regalRepository repozitar regalov
     * @param tovarRepository repozitar tovaru
     * @param pracovnikRepository repozitar pracovnikov
     * @param riaditelRepository repozitar riaditelov
     */
    public SkladService(MiestnostRepository miestnostRepository, RegalRepository regalRepository,
                        TovarRepository tovarRepository, PracovnikRepository pracovnikRepository,
                        RiaditelRepository riaditelRepository) {
        this.miestnostRepository = miestnostRepository;
        this.regalRepository = regalRepository;
        this.tovarRepository = tovarRepository;
        this.pracovnikRepository = pracovnikRepository;
        this.riaditelRepository = riaditelRepository;
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
     * Prijme noveho pracovnika a umiestni ho do hlavneho skladu tovaru.
     * @param id id pracovnika
     * @param meno meno pracovnika
     * @param priezvisko priezvisko pracovnika
     * @return prijaty pracovnik
     */
    @Transactional
    public Pracovnik prijmiPracovnika(String id, String meno, String priezvisko) {
        if (this.pracovnikRepository.existsById(id)) {
            throw new NeplatnaOperaciaException("Pracovnik so zadanym ID uz existuje.");
        }
        Pracovnik pracovnik = new Pracovnik(id, meno, priezvisko);
        pracovnik.premiestniDo(this.miestnost(Miestnost.SKLAD_TOVARU));
        return this.pracovnikRepository.save(pracovnik);
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
    }

    /**
     * Prida novy regal do hlavneho skladu tovaru.
     * @param kapacita kapacita regalu
     * @return pridany regal
     */
    @Transactional
    public Regal pridajRegal(int kapacita) {
        return this.regalRepository.save(new Regal(this.miestnost(Miestnost.SKLAD_TOVARU), kapacita));
    }

    /**
     * Odstrani prazdny regal.
     * @param regalId id regalu
     */
    @Transactional
    public void odstranRegal(Long regalId) {
        Regal regal = this.regal(regalId);
        if (this.tovarRepository.countByRegalId(regalId) > 0) {
            throw new NeplatnaOperaciaException("Regal nie je prazdny.");
        }
        this.regalRepository.delete(regal);
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
        tovar.vyberZRegalu();
        pracovnik.uchop(tovar);
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
        Regal regal = this.najdiVolnyRegal(pracovnik.getMiestnost().getKluc());
        if (regal == null) {
            throw new NeplatnaOperaciaException("V miestnosti nie je volny regal.");
        }
        pracovnik.getDrzanyTovar().ulozDoRegalu(regal, this.najdiVolnySlot(regal));
        pracovnik.poloz();
    }

    /**
     * Zakaznik vytvori tovar, ktory sa ulozi do miestnosti na prijem tovaru.
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
        Miestnost prijem = this.miestnost(Miestnost.PRIJEM_TOVARU);
        List<Regal> regale = this.regalRepository.findByMiestnostKlucOrderByIdAsc(prijem.getKluc());
        if (regale.size() != 1) {
            throw new NeplatnaOperaciaException("Miestnost na prijem tovaru nema prave jeden regal.");
        }
        Regal regal = regale.get(0);
        if (this.tovarRepository.countByRegalId(regal.getId()) >= regal.getKapacita()) {
            throw new NeplatnaOperaciaException("Regal v miestnosti na prijem tovaru je plny.");
        }
        Tovar tovar = new Tovar(id, nazov, vaha, odosielatel, prijemca);
        tovar.ulozDoRegalu(regal, this.najdiVolnySlot(regal));
        return this.tovarRepository.save(tovar);
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
    }

    private Regal regal(Long regalId) {
        return this.regalRepository.findById(regalId)
                .orElseThrow(() -> new NenajdenaEntitaException("Regal " + regalId + " neexistuje."));
    }

    private Regal najdiVolnyRegal(String kluc) {
        for (Regal regal : this.regalRepository.findByMiestnostKlucOrderByIdAsc(kluc)) {
            if (this.tovarRepository.countByRegalId(regal.getId()) < regal.getKapacita()) {
                return regal;
            }
        }
        return null;
    }

    private int najdiVolnySlot(Regal regal) {
        Set<Integer> obsadene = new HashSet<>();
        for (Tovar tovar : this.tovarRepository.findByRegalIdOrderBySlotAsc(regal.getId())) {
            obsadene.add(tovar.getSlot());
        }
        for (int slot = 1; slot <= regal.getKapacita(); slot++) {
            if (!obsadene.contains(slot)) {
                return slot;
            }
        }
        throw new NeplatnaOperaciaException("Regal je plny.");
    }
}
