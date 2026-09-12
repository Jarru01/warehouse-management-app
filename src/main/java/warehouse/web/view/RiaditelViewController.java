package warehouse.web.view;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import warehouse.domain.Miestnost;
import warehouse.domain.NenajdenaEntitaException;
import warehouse.domain.NeplatnaOperaciaException;
import warehouse.service.SkladService;
import warehouse.web.api.DtoMapper;
import warehouse.web.api.dto.MiestnostResponse;
import warehouse.web.api.dto.PracovnikResponse;
import warehouse.web.api.dto.RegalResponse;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Obrazovky riaditela.
 * @author Juraj
 */
@Controller
@RequestMapping("/riaditel")
public class RiaditelViewController {
    private final SkladService skladService;
    private final DtoMapper mapper;

    /**
     * Vytvori kontroler so servisom a mapovacom.
     * @param skladService servis skladu
     * @param mapper prevodnik na DTO
     */
    public RiaditelViewController(SkladService skladService, DtoMapper mapper) {
        this.skladService = skladService;
        this.mapper = mapper;
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
        Map<String, List<RegalResponse>> regale = new LinkedHashMap<>();
        for (Miestnost miestnost : this.skladService.miestnosti()) {
            regale.put(miestnost.getKluc(), this.skladService.regale(miestnost.getKluc()).stream()
                    .map(regal -> this.mapper.naRegal(regal, this.skladService.tovarVRegali(regal.getId())))
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
        return "riaditel";
    }

    /**
     * Prijme pracovnika z formulara.
     * @param id id pracovnika
     * @param meno meno pracovnika
     * @param priezvisko priezvisko pracovnika
     * @param attributes flash atributy
     * @return presmerovanie na panel
     */
    @PostMapping("/pracovnici")
    public String prijmiPracovnika(@RequestParam String id, @RequestParam String meno, @RequestParam String priezvisko,
                                   RedirectAttributes attributes) {
        try {
            this.skladService.prijmiPracovnika(id, meno, priezvisko);
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
     * Prida regal z formulara.
     * @param kapacita kapacita regalu
     * @param attributes flash atributy
     * @return presmerovanie na panel
     */
    @PostMapping("/regale")
    public String pridajRegal(@RequestParam int kapacita, RedirectAttributes attributes) {
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
