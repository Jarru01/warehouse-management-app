package warehouse.security;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import warehouse.domain.NenajdenaEntitaException;
import warehouse.domain.NeplatnaOperaciaException;
import warehouse.domain.Osoba;
import warehouse.repository.PracovnikRepository;
import warehouse.repository.RiaditelRepository;
import warehouse.repository.ZakaznikRepository;
import warehouse.domain.AuditAkcia;
import warehouse.service.AuditService;
import warehouse.service.HeslaService;

import java.util.Optional;

/**
 * Pouzivatelske ucty bez ohladu na rolu: vyhladanie pre prihlasenie a zmena vlastneho hesla.
 * @author Juraj
 */
@Service
public class UcetService {
    private final RiaditelRepository riaditelRepository;
    private final PracovnikRepository pracovnikRepository;
    private final ZakaznikRepository zakaznikRepository;
    private final HeslaService heslaService;
    private final AuditService auditService;

    /**
     * Vytvori servis uctov.
     * @param riaditelRepository repozitar riaditelov
     * @param pracovnikRepository repozitar pracovnikov
     * @param zakaznikRepository repozitar zakaznikov
     * @param heslaService servis hesiel
     * @param auditService servis auditu
     */
    public UcetService(RiaditelRepository riaditelRepository, PracovnikRepository pracovnikRepository,
                       ZakaznikRepository zakaznikRepository, HeslaService heslaService,
                       AuditService auditService) {
        this.riaditelRepository = riaditelRepository;
        this.pracovnikRepository = pracovnikRepository;
        this.zakaznikRepository = zakaznikRepository;
        this.heslaService = heslaService;
        this.auditService = auditService;
    }

    /**
     * Najde osobu podla roly a id.
     * @param rola rola
     * @param id id v ramci roly
     * @return osoba alebo prazdny vysledok
     */
    @Transactional(readOnly = true)
    public Optional<Osoba> najdi(Rola rola, String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        return switch (rola) {
            case RIADITEL -> this.riaditelRepository.findById(id).map(Osoba.class::cast);
            case PRACOVNIK -> this.pracovnikRepository.findById(id).map(Osoba.class::cast);
            case ZAKAZNIK -> this.zakaznikRepository.findById(id).map(Osoba.class::cast);
        };
    }

    /**
     * Zmeni vlastne heslo po overeni sucasneho hesla.
     * @param rola rola pouzivatela
     * @param id id pouzivatela
     * @param sucasneHeslo sucasne heslo
     * @param noveHeslo nove heslo
     */
    @Transactional
    public void zmenHeslo(Rola rola, String id, String sucasneHeslo, String noveHeslo) {
        Osoba osoba = this.najdi(rola, id)
                .orElseThrow(() -> new NenajdenaEntitaException("Pouzivatel " + id + " neexistuje."));
        if (!this.heslaService.zhoduje(sucasneHeslo, osoba.getHesloHash())) {
            throw new NeplatnaOperaciaException("Sucasne heslo nie je spravne.");
        }
        if (sucasneHeslo.equals(noveHeslo)) {
            throw new NeplatnaOperaciaException("Nove heslo sa musi lisit od sucasneho.");
        }
        osoba.nastavHeslo(this.heslaService.zahesuj(noveHeslo), false);
        this.auditService.zaznamenajPre(rola.name() + ":" + id, AuditAkcia.ZMENA_HESLA, null);
    }
}
