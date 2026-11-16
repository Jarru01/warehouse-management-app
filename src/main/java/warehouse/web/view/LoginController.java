package warehouse.web.view;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import warehouse.domain.NeplatnaOperaciaException;
import warehouse.service.SkladService;
import warehouse.web.api.dto.RegistraciaZakaznikaRequest;

import java.util.Optional;

/**
 * Uvodna stranka s prihlasenim a registracia zakaznikov. Samotne prihlasenie a odhlasenie spracuje Spring Security.
 * @author Juraj
 */
@Controller
public class LoginController {
    private final SkladService skladService;
    private final FormularValidator validator;

    /**
     * Vytvori kontroler so servisom a validatorom formularov.
     * @param skladService servis skladu
     * @param validator validator formularov
     */
    public LoginController(SkladService skladService, FormularValidator validator) {
        this.skladService = skladService;
        this.validator = validator;
    }

    /**
     * Zobrazi uvodnu stranku s vyberom roly. Vybrana rola sa prenesie do sablony.
     * @param rola kluc zvolenej roly
     * @param chyba pritomny po neuspesnom prihlaseni
     * @param zablokovane pritomny, ak su dalsie pokusy docasne zablokovane
     * @param model model sablony
     * @return nazov sablony
     */
    @GetMapping("/")
    public String index(@RequestParam(name = "rola", required = false, defaultValue = "riaditel") String rola,
                        @RequestParam(name = "chyba", required = false) String chyba,
                        @RequestParam(name = "zablokovane", required = false) String zablokovane,
                        Model model) {
        String vybranaRola = switch (rola) {
            case "pracovnik", "zakaznik", "riaditel" -> rola;
            default -> "riaditel";
        };
        model.addAttribute("rola", vybranaRola);
        if (zablokovane != null) {
            model.addAttribute("chyba", "Prilis vela neuspesnych pokusov o prihlasenie. Skus to znova neskor.");
        } else if (chyba != null) {
            model.addAttribute("chyba", "Nespravne ID alebo heslo.");
        }
        return "index";
    }

    /**
     * Zaregistruje noveho zakaznika z formulara.
     * @param id zvolene id zakaznika
     * @param meno meno zakaznika
     * @param priezvisko priezvisko zakaznika
     * @param heslo heslo zakaznika
     * @param attributes flash atributy
     * @return presmerovanie na prihlasenie zakaznika
     */
    @PostMapping("/registracia")
    public String registracia(@RequestParam String id, @RequestParam String meno, @RequestParam String priezvisko,
                              @RequestParam String heslo, RedirectAttributes attributes) {
        Optional<String> chyby = this.validator.chyby(new RegistraciaZakaznikaRequest(id, meno, priezvisko, heslo));
        if (chyby.isPresent()) {
            attributes.addFlashAttribute("chyba", chyby.get());
            return "redirect:/?rola=zakaznik";
        }
        try {
            this.skladService.registrujZakaznika(id, meno, priezvisko, heslo);
            attributes.addFlashAttribute("sprava", "Registracia prebehla. Teraz sa mozes prihlasit.");
        } catch (NeplatnaOperaciaException e) {
            attributes.addFlashAttribute("chyba", e.getMessage());
        }
        return "redirect:/?rola=zakaznik";
    }
}
