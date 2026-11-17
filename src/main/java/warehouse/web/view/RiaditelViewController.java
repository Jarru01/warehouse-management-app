package warehouse.web.view;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import warehouse.domain.AuditAkcia;
import warehouse.domain.AuditZaznam;
import warehouse.domain.NenajdenaEntitaException;
import warehouse.domain.NeplatnaOperaciaException;
import warehouse.service.AuditService;
import warehouse.service.RegalSTovarom;
import warehouse.service.SkladService;
import warehouse.web.api.DtoMapper;
import warehouse.web.api.dto.MiestnostResponse;
import warehouse.web.api.dto.NoveHesloRequest;
import warehouse.web.api.dto.PracovnikResponse;
import warehouse.web.api.dto.PridatRegalRequest;
import warehouse.web.api.dto.PrijmoutPracovnikaRequest;
import warehouse.web.api.dto.RegalResponse;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Obrazovky riaditela.
 * @author Juraj
 */
@Controller
@RequestMapping("/riaditel")
public class RiaditelViewController {
    private static final int POCET_UDALOSTI = 15;
    private static final DateTimeFormatter FORMAT_CASU = DateTimeFormatter.ofPattern("d. M. yyyy HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    private final SkladService skladService;
    private final AuditService auditService;
    private final DtoMapper mapper;
    private final FormularValidator validator;

    /**
     * Riadok auditu pripraveny na zobrazenie.
     * @param cas miestny cas udalosti
     * @param pouzivatel kto udalost vykonal
     * @param akcia citatelny nazov udalosti
     * @param detail doplnujuci opis
     * @param varovanie true pre neuspesne alebo zablokovane prihlasenie
     */
    public record AuditRiadok(String cas, String pouzivatel, String akcia, String detail, boolean varovanie) {
    }

    /**
     * Vytvori kontroler so servisom skladu, auditu, mapovacom a validatorom formularov.
     * @param skladService servis skladu
     * @param auditService servis auditu
     * @param mapper prevodnik na DTO
     * @param validator validator formularov
     */
    public RiaditelViewController(SkladService skladService, AuditService auditService, DtoMapper mapper,
                                  FormularValidator validator) {
        this.skladService = skladService;
        this.auditService = auditService;
        this.mapper = mapper;
        this.validator = validator;
    }

    /**
     * Zobrazi riaditelov panel s pracovnikmi a skladom.
     * @param model model sablony
     * @return nazov sablony
     */
    @GetMapping
    public String dashboard(Model model) {
        List<PracovnikResponse> pracovnici = this.skladService.pracovnici().stream()
                .map(this.mapper::naPracovnika).toList();
        List<MiestnostResponse> miestnosti = this.skladService.miestnosti().stream()
                .map(this.mapper::naMiestnost).toList();
        Map<String, List<RegalSTovarom>> prehlad = this.skladService.prehladRegalov();
        Map<String, List<RegalResponse>> regale = new LinkedHashMap<>();
        for (MiestnostResponse miestnost : miestnosti) {
            regale.put(miestnost.kluc(), prehlad.getOrDefault(miestnost.kluc(), List.of()).stream()
                    .map(polozka -> this.mapper.naRegal(polozka.regal(), polozka.tovar()))
                    .toList());
        }
        model.addAttribute("pracovnici", pracovnici);
        model.addAttribute("miestnosti", miestnosti);
        model.addAttribute("regale", regale);
        long pocetTovarov = regale.values().stream()
                .flatMap(List::stream)
                .mapToLong(regal -> regal.tovar().size())
                .sum();
        model.addAttribute("pocetPracovnikov", pracovnici.size());
        model.addAttribute("pocetMiestnosti", miestnosti.size());
        model.addAttribute("pocetTovarov", pocetTovarov);
        model.addAttribute("udalosti", this.auditService.najnovsie(POCET_UDALOSTI).stream()
                .map(RiaditelViewController::naRiadok).toList());
        return "riaditel";
    }

    private static AuditRiadok naRiadok(AuditZaznam zaznam) {
        AuditAkcia akcia = zaznam.getAkcia();
        boolean varovanie = akcia == AuditAkcia.NEUSPESNE_PRIHLASENIE || akcia == AuditAkcia.ZABLOKOVANE_PRIHLASENIE;
        String nazov = akcia.name().charAt(0) + akcia.name().substring(1).toLowerCase(Locale.ROOT).replace('_', ' ');
        return new AuditRiadok(FORMAT_CASU.format(zaznam.getCas()), zaznam.getPouzivatel(), nazov,
                zaznam.getDetail(), varovanie);
    }

    /**
     * Prijme pracovnika z formulara.
     * @param id id pracovnika
     * @param meno meno pracovnika
     * @param priezvisko priezvisko pracovnika
     * @param heslo pociatocne heslo pracovnika
     * @param attributes flash atributy
     * @return presmerovanie na panel
     */
    @PostMapping("/pracovnici")
    public String prijmiPracovnika(@RequestParam String id, @RequestParam String meno, @RequestParam String priezvisko,
                                   @RequestParam String heslo, RedirectAttributes attributes) {
        Optional<String> chyby = this.validator.chyby(new PrijmoutPracovnikaRequest(id, meno, priezvisko, heslo));
        if (chyby.isPresent()) {
            attributes.addFlashAttribute("chyba", chyby.get());
            return "redirect:/riaditel";
        }
        try {
            this.skladService.prijmiPracovnika(id, meno, priezvisko, heslo);
            attributes.addFlashAttribute("sprava", "Pracovnik bol uspesne prijaty.");
        } catch (NenajdenaEntitaException | NeplatnaOperaciaException e) {
            attributes.addFlashAttribute("chyba", e.getMessage());
        }
        return "redirect:/riaditel";
    }

    /**
     * Vyluci pracovnika z formulara.
     * @param id id pracovnika
     * @param attributes flash atributy
     * @return presmerovanie na panel
     */
    @PostMapping("/pracovnici/{id}/vylucenie")
    public String vylucPracovnika(@PathVariable String id, RedirectAttributes attributes) {
        try {
            this.skladService.vylucPracovnika(id);
            attributes.addFlashAttribute("sprava", "Pracovnik bol vyluceny.");
        } catch (NenajdenaEntitaException | NeplatnaOperaciaException e) {
            attributes.addFlashAttribute("chyba", e.getMessage());
        }
        return "redirect:/riaditel";
    }

    /**
     * Nastavi pracovnikovi nove docasne heslo.
     * @param id id pracovnika
     * @param heslo nove docasne heslo
     * @param attributes flash atributy
     * @return presmerovanie na panel
     */
    @PostMapping("/pracovnici/{id}/heslo")
    public String resetujHeslo(@PathVariable String id, @RequestParam String heslo, RedirectAttributes attributes) {
        Optional<String> chyby = this.validator.chyby(new NoveHesloRequest(heslo));
        if (chyby.isPresent()) {
            attributes.addFlashAttribute("chyba", chyby.get());
            return "redirect:/riaditel";
        }
        try {
            this.skladService.resetujHesloPracovnika(id, heslo);
            attributes.addFlashAttribute("sprava",
                    "Pracovnik " + id + " ma nove docasne heslo. Pri prihlaseni si ho musi zmenit.");
        } catch (NenajdenaEntitaException | NeplatnaOperaciaException e) {
            attributes.addFlashAttribute("chyba", e.getMessage());
        }
        return "redirect:/riaditel";
    }

    /**
     * Prida regal z formulara.
     * @param kapacita kapacita regalu
     * @param attributes flash atributy
     * @return presmerovanie na panel
     */
    @PostMapping("/regale")
    public String pridajRegal(@RequestParam int kapacita, RedirectAttributes attributes) {
        Optional<String> chyby = this.validator.chyby(new PridatRegalRequest(kapacita));
        if (chyby.isPresent()) {
            attributes.addFlashAttribute("chyba", chyby.get());
            return "redirect:/riaditel";
        }
        try {
            this.skladService.pridajRegal(kapacita);
            attributes.addFlashAttribute("sprava", "Regal bol pridany.");
        } catch (IllegalArgumentException | NeplatnaOperaciaException e) {
            attributes.addFlashAttribute("chyba", e.getMessage());
        }
        return "redirect:/riaditel";
    }

    /**
     * Odstrani prazdny regal.
     * @param id id regalu
     * @param attributes flash atributy
     * @return presmerovanie na panel
     */
    @PostMapping("/regale/{id}/odstranenie")
    public String odstranRegal(@PathVariable Long id, RedirectAttributes attributes) {
        try {
            this.skladService.odstranRegal(id);
            attributes.addFlashAttribute("sprava", "Regal bol odstraneny.");
        } catch (NenajdenaEntitaException | NeplatnaOperaciaException e) {
            attributes.addFlashAttribute("chyba", e.getMessage());
        }
        return "redirect:/riaditel";
    }
}
