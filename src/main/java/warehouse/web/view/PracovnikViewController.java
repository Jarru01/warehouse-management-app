package warehouse.web.view;

import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import warehouse.domain.Pracovnik;
import warehouse.domain.NenajdenaEntitaException;
import warehouse.domain.NeplatnaOperaciaException;
import warehouse.security.PrihlasenyPouzivatel;
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
     * Zobrazi panel pracovnika s aktualnou miestnostou a drzanych tovarom. Ak bol pracovnik medzicasom
     * vyluceny, odhlasi ho.
     * @param pouzivatel prihlaseny pracovnik
     * @param session HTTP session
     * @param attributes flash atributy
     * @param model model sablony
     * @return nazov sablony alebo presmerovanie
     */
    @GetMapping
    public String panel(@AuthenticationPrincipal PrihlasenyPouzivatel pouzivatel, HttpSession session,
                        RedirectAttributes attributes, Model model) {
        try {
            Pracovnik pracovnik = this.skladService.pracovnik(pouzivatel.id());
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
     * @param pouzivatel prihlaseny pracovnik
     * @param attributes flash atributy
     * @return presmerovanie na panel
     */
    @PostMapping("/presun")
    public String presun(@RequestParam String miestnost, @AuthenticationPrincipal PrihlasenyPouzivatel pouzivatel,
                         RedirectAttributes attributes) {
        try {
            this.skladService.presunPracovnika(pouzivatel.id(), miestnost);
            attributes.addFlashAttribute("sprava", "Presun bol vykonany.");
        } catch (NenajdenaEntitaException | NeplatnaOperaciaException e) {
            attributes.addFlashAttribute("chyba", e.getMessage());
        }
        return "redirect:/pracovnik";
    }

    /**
     * Pracovnik zoberie tovar z aktualnej miestnosti.
     * @param tovarId id tovaru
     * @param pouzivatel prihlaseny pracovnik
     * @param attributes flash atributy
     * @return presmerovanie na panel
     */
    @PostMapping("/zober")
    public String zober(@RequestParam String tovarId, @AuthenticationPrincipal PrihlasenyPouzivatel pouzivatel,
                        RedirectAttributes attributes) {
        try {
            this.skladService.zoberTovar(pouzivatel.id(), tovarId);
            attributes.addFlashAttribute("sprava", "Tovar bol zobraty.");
        } catch (NenajdenaEntitaException | NeplatnaOperaciaException e) {
            attributes.addFlashAttribute("chyba", e.getMessage());
        }
        return "redirect:/pracovnik";
    }

    /**
     * Pracovnik ulozi drzany tovar.
     * @param pouzivatel prihlaseny pracovnik
     * @param attributes flash atributy
     * @return presmerovanie na panel
     */
    @PostMapping("/uloz")
    public String uloz(@AuthenticationPrincipal PrihlasenyPouzivatel pouzivatel, RedirectAttributes attributes) {
        try {
            this.skladService.ulozTovar(pouzivatel.id());
            attributes.addFlashAttribute("sprava", "Tovar bol ulozeny.");
        } catch (NenajdenaEntitaException | NeplatnaOperaciaException e) {
            attributes.addFlashAttribute("chyba", e.getMessage());
        }
        return "redirect:/pracovnik";
    }
}
