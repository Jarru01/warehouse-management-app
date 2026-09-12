package warehouse.web.view;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import warehouse.domain.Pracovnik;
import warehouse.domain.Riaditel;
import warehouse.domain.NenajdenaEntitaException;
import warehouse.service.SkladService;
import warehouse.web.api.DtoMapper;

/**
 * Prihlasenie a odhlasenie pouzivatelov.
 * @author Juraj
 */
@Controller
public class LoginController {
    private final SkladService skladService;
    private final DtoMapper mapper;

    /**
     * Vytvori kontroler so servisom a mapovacom.
     * @param skladService servis skladu
     * @param mapper prevodnik na DTO
     */
    public LoginController(SkladService skladService, DtoMapper mapper) {
        this.skladService = skladService;
        this.mapper = mapper;
    }

    /**
     * Zobrazi uvodnu stranku s vyberom roly. Vybrana rola sa prenesie do sablony.
     * @param rola kluc zvolenej roly
     * @param model model sablony
     * @return nazov sablony
     */
    @GetMapping("/")
    public String index(@RequestParam(name = "rola", required = false, defaultValue = "riaditel") String rola,
                        Model model) {
        String vybranaRola = switch (rola) {
            case "pracovnik", "zakaznik", "riaditel" -> rola;
            default -> "riaditel";
        };
        model.addAttribute("rola", vybranaRola);
        return "index";
    }

    /**
     * Prihlasi riaditela podla id.
     * @param id id riaditela
     * @param session HTTP session
     * @param attributes flash atributy
     * @return presmerovanie
     */
    @PostMapping("/prihlasenie/riaditel")
    public String prihlasRiaditela(@RequestParam String id, HttpSession session, RedirectAttributes attributes) {
        try {
            Riaditel riaditel = this.skladService.riaditel(id);
            this.ulozPrihlasenie(session, "RIADITEL", riaditel.getId(), riaditel.getMeno(), riaditel.getPriezvisko());
            return "redirect:/riaditel";
        } catch (NenajdenaEntitaException e) {
            attributes.addFlashAttribute("chyba", e.getMessage());
            return "redirect:/";
        }
    }

    /**
     * Prihlasi pracovnika podla id.
     * @param id id pracovnika
     * @param session HTTP session
     * @param attributes flash atributy
     * @return presmerovanie
     */
    @PostMapping("/prihlasenie/pracovnik")
    public String prihlasPracovnika(@RequestParam String id, HttpSession session, RedirectAttributes attributes) {
        try {
            Pracovnik pracovnik = this.skladService.pracovnik(id);
            this.ulozPrihlasenie(session, "PRACOVNIK", pracovnik.getId(), pracovnik.getMeno(), pracovnik.getPriezvisko());
            return "redirect:/pracovnik";
        } catch (NenajdenaEntitaException e) {
            attributes.addFlashAttribute("chyba", e.getMessage());
            return "redirect:/";
        }
    }

    /**
     * Prihlasi zakaznika s udajmi zadanymi vo formulari.
     * @param id id zakaznika
     * @param meno meno zakaznika
     * @param priezvisko priezvisko zakaznika
     * @param session HTTP session
     * @param attributes flash atributy
     * @return presmerovanie
     */
    @PostMapping("/prihlasenie/zakaznik")
    public String prihlasZakaznika(@RequestParam String id, @RequestParam String meno, @RequestParam String priezvisko,
                                   HttpSession session, RedirectAttributes attributes) {
        if (!id.matches("\\d+") || !meno.matches("\\p{L}+") || !priezvisko.matches("\\p{L}+")) {
            attributes.addFlashAttribute("chyba", "Zadaj ID v tvare cisla a meno s priezviskom z pismen.");
            return "redirect:/";
        }
        this.ulozPrihlasenie(session, "ZAKAZNIK", id, meno, priezvisko);
        return "redirect:/zakaznik";
    }

    /**
     * Odhlasi pouzivatela a zrusi session.
     * @param session HTTP session
     * @return presmerovanie na uvodnu stranku
     */
    @PostMapping("/odhlasenie")
    public String odhlasenie(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    private void ulozPrihlasenie(HttpSession session, String rola, String identita, String meno, String priezvisko) {
        session.setAttribute(SessionKluce.ROLA, rola);
        session.setAttribute(SessionKluce.IDENTITA, identita);
        session.setAttribute(SessionKluce.MENO, meno);
        session.setAttribute(SessionKluce.PRIEZVISKO, priezvisko);
    }
}
