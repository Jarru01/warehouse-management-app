package warehouse.web.view;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import warehouse.domain.NenajdenaEntitaException;
import warehouse.domain.NeplatnaOperaciaException;
import warehouse.security.AktualnePrihlasenie;
import warehouse.security.PrihlasenyPouzivatel;
import warehouse.security.SecurityConfig;
import warehouse.security.UcetService;
import warehouse.web.api.dto.ZmenaHeslaRequest;

import java.util.Optional;

/**
 * Obrazovka na zmenu vlastneho hesla pre vsetky roly.
 * @author Juraj
 */
@Controller
public class UcetViewController {
    private final UcetService ucetService;
    private final AktualnePrihlasenie aktualnePrihlasenie;
    private final FormularValidator validator;

    /**
     * Vytvori kontroler zmeny hesla.
     * @param ucetService servis uctov
     * @param aktualnePrihlasenie pomocnik na upravu prihlasenia
     * @param validator validator formularov
     */
    public UcetViewController(UcetService ucetService, AktualnePrihlasenie aktualnePrihlasenie,
                              FormularValidator validator) {
        this.ucetService = ucetService;
        this.aktualnePrihlasenie = aktualnePrihlasenie;
        this.validator = validator;
    }

    /**
     * Zobrazi formular na zmenu hesla.
     * @return nazov sablony
     */
    @GetMapping(SecurityConfig.ZMENA_HESLA)
    public String formular() {
        return "heslo";
    }

    /**
     * Zmeni heslo prihlaseneho pouzivatela a vrati ho na jeho domovsku stranku.
     * @param sucasneHeslo sucasne heslo
     * @param noveHeslo nove heslo
     * @param noveHesloZnova zopakovane nove heslo
     * @param pouzivatel prihlaseny pouzivatel
     * @param request poziadavka
     * @param response odpoved
     * @param attributes flash atributy
     * @return presmerovanie
     */
    @PostMapping(SecurityConfig.ZMENA_HESLA)
    public String zmenHeslo(@RequestParam String sucasneHeslo, @RequestParam String noveHeslo,
                            @RequestParam String noveHesloZnova,
                            @AuthenticationPrincipal PrihlasenyPouzivatel pouzivatel,
                            HttpServletRequest request, HttpServletResponse response,
                            RedirectAttributes attributes) {
        Optional<String> chyby = this.validator.chyby(new ZmenaHeslaRequest(sucasneHeslo, noveHeslo));
        if (chyby.isEmpty() && !noveHeslo.equals(noveHesloZnova)) {
            chyby = Optional.of("Nove hesla sa nezhoduju.");
        }
        if (chyby.isPresent()) {
            attributes.addFlashAttribute("chyba", chyby.get());
            return "redirect:" + SecurityConfig.ZMENA_HESLA;
        }
        try {
            this.ucetService.zmenHeslo(pouzivatel.rola(), pouzivatel.id(), sucasneHeslo, noveHeslo);
        } catch (NenajdenaEntitaException | NeplatnaOperaciaException e) {
            attributes.addFlashAttribute("chyba", e.getMessage());
            return "redirect:" + SecurityConfig.ZMENA_HESLA;
        }
        this.aktualnePrihlasenie.poZmeneHesla(request, response);
        attributes.addFlashAttribute("sprava", "Heslo bolo zmenene.");
        return "redirect:" + pouzivatel.rola().getDomovskaStranka();
    }
}
