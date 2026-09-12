package warehouse.web.view;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import warehouse.domain.ZakaznikInfo;
import warehouse.service.NenajdenaEntitaException;
import warehouse.service.NeplatnaOperaciaException;
import warehouse.service.SkladService;
import warehouse.web.api.DtoMapper;

/**
 * Obrazovky zakaznika.
 * @author Juraj
 */
@Controller
@RequestMapping("/zakaznik")
public class ZakaznikViewController {
    private final SkladService skladService;
    private final DtoMapper mapper;

    /**
     * Vytvori kontroler so servisom a mapovacom.
     * @param skladService servis skladu
     * @param mapper prevodnik na DTO
     */
    public ZakaznikViewController(SkladService skladService, DtoMapper mapper) {
        this.skladService = skladService;
        this.mapper = mapper;
    }

    /**
     * Zobrazi panel zakaznika s jeho tovarom pripravenym na vydaj.
     * @param session HTTP session
     * @param model model sablony
     * @return nazov sablony
     */
    @GetMapping
    public String panel(HttpSession session, Model model) {
        String id = (String) session.getAttribute(SessionKluce.IDENTITA);
        model.addAttribute("tovar", this.skladService.tovarPreZakaznika(id).stream()
                .map(this.mapper::naTovar).toList());
        return "zakaznik";
    }

    /**
     * Vlozi novy tovar z formulara.
     * @param id id tovaru
     * @param nazov nazov tovaru
     * @param vaha vaha tovaru
     * @param prijemcaId id prijemcu
     * @param prijemcaMeno meno prijemcu
     * @param prijemcaPriezvisko priezvisko prijemcu
     * @param session HTTP session
     * @param attributes flash atributy
     * @return presmerovanie na panel
     */
    @PostMapping("/tovar")
    public String vlozTovar(@RequestParam String id, @RequestParam String nazov, @RequestParam int vaha,
                            @RequestParam String prijemcaId, @RequestParam String prijemcaMeno,
                            @RequestParam String prijemcaPriezvisko, HttpSession session,
                            RedirectAttributes attributes) {
        ZakaznikInfo odosielatel = new ZakaznikInfo(
                (String) session.getAttribute(SessionKluce.IDENTITA),
                (String) session.getAttribute(SessionKluce.MENO),
                (String) session.getAttribute(SessionKluce.PRIEZVISKO));
        try {
            this.skladService.vytvorTovar(id, nazov, vaha, odosielatel,
                    new ZakaznikInfo(prijemcaId, prijemcaMeno, prijemcaPriezvisko));
            attributes.addFlashAttribute("sprava", "Tovar bol ulozeny.");
        } catch (NeplatnaOperaciaException e) {
            attributes.addFlashAttribute("chyba", e.getMessage());
        }
        return "redirect:/zakaznik";
    }

    /**
     * Vyzdvihne tovar pripraveny na vydaj.
     * @param tovarId id tovaru
     * @param session HTTP session
     * @param attributes flash atributy
     * @return presmerovanie na panel
     */
    @PostMapping("/vyzdvihnutie/{tovarId}")
    public String vyzdvihniTovar(@PathVariable String tovarId, HttpSession session, RedirectAttributes attributes) {
        try {
            this.skladService.vyzdvihniTovar(tovarId, (String) session.getAttribute(SessionKluce.IDENTITA));
            attributes.addFlashAttribute("sprava", "Tovar bol vyzdvihnuty.");
        } catch (NenajdenaEntitaException | NeplatnaOperaciaException e) {
            attributes.addFlashAttribute("chyba", e.getMessage());
        }
        return "redirect:/zakaznik";
    }
}
