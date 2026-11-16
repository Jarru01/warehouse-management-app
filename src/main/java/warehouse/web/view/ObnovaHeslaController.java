package warehouse.web.view;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;
import warehouse.domain.NeplatnaOperaciaException;
import warehouse.security.ObnovaHeslaService;
import warehouse.web.api.dto.NoveHesloRequest;

import java.util.Optional;

/**
 * Obrazovky na obnovu zabudnuteho hesla zakaznika.
 * @author Juraj
 */
@Controller
public class ObnovaHeslaController {
    private static final String NA_PRIHLASENIE = "redirect:/?rola=zakaznik";

    private final ObnovaHeslaService obnovaHeslaService;
    private final FormularValidator validator;

    /**
     * Vytvori kontroler obnovy hesla.
     * @param obnovaHeslaService servis obnovy hesla
     * @param validator validator formularov
     */
    public ObnovaHeslaController(ObnovaHeslaService obnovaHeslaService, FormularValidator validator) {
        this.obnovaHeslaService = obnovaHeslaService;
        this.validator = validator;
    }

    /**
     * Zobrazi formular na zadanie e-mailu.
     * @return nazov sablony
     */
    @GetMapping("/heslo/zabudnute")
    public String formular() {
        return "zabudnute-heslo";
    }

    /**
     * Posle odkaz na obnovu, ak e-mail patri zakaznikovi. Odpoved je vzdy rovnaka.
     * @param email zadany e-mail
     * @param attributes flash atributy
     * @return presmerovanie na prihlasenie zakaznika
     */
    @PostMapping("/heslo/zabudnute")
    public String poziadaj(@RequestParam String email, RedirectAttributes attributes) {
        if (email.isBlank() || email.length() > 254) {
            attributes.addFlashAttribute("chyba", "Zadaj e-mail, ktory si uviedol pri registracii.");
            return "redirect:/heslo/zabudnute";
        }
        this.obnovaHeslaService.poziadaj(email);
        attributes.addFlashAttribute("sprava",
                "Ak k e-mailu existuje ucet, poslali sme nan odkaz na obnovu hesla. Plati 30 minut.");
        return NA_PRIHLASENIE;
    }

    /**
     * Zobrazi formular na nove heslo, ak je odkaz platny.
     * @param token token z odkazu
     * @param model model sablony
     * @param attributes flash atributy
     * @return nazov sablony alebo presmerovanie
     */
    @GetMapping("/heslo/obnova")
    public String noveHeslo(@RequestParam(required = false) String token, Model model, RedirectAttributes attributes) {
        if (!this.obnovaHeslaService.jePlatny(token)) {
            attributes.addFlashAttribute("chyba", "Odkaz na obnovu hesla je neplatny alebo expiroval.");
            return NA_PRIHLASENIE;
        }
        model.addAttribute("token", token);
        return "obnova-hesla";
    }

    /**
     * Nastavi nove heslo podla odkazu.
     * @param token token z odkazu
     * @param noveHeslo nove heslo
     * @param noveHesloZnova zopakovane nove heslo
     * @param attributes flash atributy
     * @return presmerovanie
     */
    @PostMapping("/heslo/obnova")
    public String obnov(@RequestParam String token, @RequestParam String noveHeslo,
                        @RequestParam String noveHesloZnova, RedirectAttributes attributes) {
        Optional<String> chyby = this.validator.chyby(new NoveHesloRequest(noveHeslo));
        if (chyby.isEmpty() && !noveHeslo.equals(noveHesloZnova)) {
            chyby = Optional.of("Nove hesla sa nezhoduju.");
        }
        if (chyby.isPresent()) {
            attributes.addFlashAttribute("chyba", chyby.get());
            return "redirect:" + UriComponentsBuilder.fromPath("/heslo/obnova").queryParam("token", token)
                    .encode().toUriString();
        }
        try {
            this.obnovaHeslaService.obnov(token, noveHeslo);
        } catch (NeplatnaOperaciaException e) {
            attributes.addFlashAttribute("chyba", e.getMessage());
            return NA_PRIHLASENIE;
        }
        attributes.addFlashAttribute("sprava", "Heslo bolo zmenene. Teraz sa mozes prihlasit.");
        return NA_PRIHLASENIE;
    }
}
