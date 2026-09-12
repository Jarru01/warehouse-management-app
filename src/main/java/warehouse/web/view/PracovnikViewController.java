package warehouse.web.view;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import warehouse.domain.Pracovnik;
import warehouse.service.NenajdenaEntitaException;
import warehouse.service.NeplatnaOperaciaException;
import warehouse.service.SkladService;
import warehouse.web.api.DtoMapper;
import warehouse.web.api.dto.MiestnostResponse;
import warehouse.web.api.dto.RegalResponse;

import java.util.List;

/**
 * Obrazovky pracovnika.
 * @author Juraj
 */
@Controller
@RequestMapping("/pracovnik")
public class PracovnikViewController {
    private final SkladService skladService;
    private final DtoMapper mapper;

    /**
     * Vytvori kontroler so servisom a mapovacom.
     * @param skladService servis skladu
     * @param mapper prevodnik na DTO
     */
    public PracovnikViewController(SkladService skladService, DtoMapper mapper) {
        this.skladService = skladService;
        this.mapper = mapper;
    }

    /**
     * Zobrazi panel pracovnika s aktualnou miestnostou a drzanych tovarom.
     * @param session HTTP session
     * @param attributes flash atributy
     * @param model model sablony
     * @return nazov sablony alebo presmerovanie
     */
    @GetMapping
    public String panel(HttpSession session, RedirectAttributes attributes, Model model) {
        String id = (String) session.getAttribute(SessionKluce.IDENTITA);
        try {
            Pracovnik pracovnik = this.skladService.pracovnik(id);
            List<MiestnostResponse> miestnosti = this.skladService.miestnosti().stream()
                    .map(this.mapper::naMiestnost).toList();
            List<RegalResponse> regale = this.skladService.regale(pracovnik.getMiestnost().getKluc()).stream()
                    .map(regal -> this.mapper.naRegal(regal, this.skladService.tovarVRegali(regal.getId())))
                    .toList();
            model.addAttribute("pracovnik", this.mapper.naPracovnika(pracovnik));
            model.addAttribute("miestnosti", miestnosti);
            model.addAttribute("regale", regale);
            model.addAttribute("drzanyTovar",
                    pracovnik.getDrzanyTovar() == null ? null : this.mapper.naTovar(pracovnik.getDrzanyTovar()));
            return "pracovnik";
        } catch (NenajdenaEntitaException e) {
            session.invalidate();
            attributes.addFlashAttribute("chyba", e.getMessage());
            return "redirect:/";
        }
    }

    /**
     * Presunie pracovnika do zvolenej miestnosti.
     * @param miestnost kluc miestnosti
     * @param session HTTP session
     * @param attributes flash atributy
     * @return presmerovanie na panel
     */
    @PostMapping("/presun")
    public String presun(@RequestParam String miestnost, HttpSession session, RedirectAttributes attributes) {
        String id = (String) session.getAttribute(SessionKluce.IDENTITA);
        try {
            this.skladService.presunPracovnika(id, miestnost);
            attributes.addFlashAttribute("sprava", "Presun bol vykonany.");
        } catch (NenajdenaEntitaException | NeplatnaOperaciaException e) {
            attributes.addFlashAttribute("chyba", e.getMessage());
        }
        return "redirect:/pracovnik";
    }

    /**
     * Pracovnik zoberie tovar z aktualnej miestnosti.
     * @param tovarId id tovaru
     * @param session HTTP session
     * @param attributes flash atributy
     * @return presmerovanie na panel
     */
    @PostMapping("/zober")
    public String zober(@RequestParam String tovarId, HttpSession session, RedirectAttributes attributes) {
        String id = (String) session.getAttribute(SessionKluce.IDENTITA);
        try {
            this.skladService.zoberTovar(id, tovarId);
            attributes.addFlashAttribute("sprava", "Tovar bol zobraty.");
        } catch (NenajdenaEntitaException | NeplatnaOperaciaException e) {
            attributes.addFlashAttribute("chyba", e.getMessage());
        }
        return "redirect:/pracovnik";
    }

    /**
     * Pracovnik ulozi drzany tovar.
     * @param session HTTP session
     * @param attributes flash atributy
     * @return presmerovanie na panel
     */
    @PostMapping("/uloz")
    public String uloz(HttpSession session, RedirectAttributes attributes) {
        String id = (String) session.getAttribute(SessionKluce.IDENTITA);
        try {
            this.skladService.ulozTovar(id);
            attributes.addFlashAttribute("sprava", "Tovar bol ulozeny.");
        } catch (NenajdenaEntitaException | NeplatnaOperaciaException e) {
            attributes.addFlashAttribute("chyba", e.getMessage());
        }
        return "redirect:/pracovnik";
    }
}
