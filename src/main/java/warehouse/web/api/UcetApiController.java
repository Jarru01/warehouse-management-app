package warehouse.web.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import warehouse.security.AktualnePrihlasenie;
import warehouse.security.PrihlasenyPouzivatel;
import warehouse.security.UcetService;
import warehouse.web.api.dto.ZmenaHeslaRequest;

/**
 * REST rozhranie pre vlastny ucet prihlaseneho pouzivatela.
 * @author Juraj
 */
@RestController
public class UcetApiController {
    private final UcetService ucetService;
    private final AktualnePrihlasenie aktualnePrihlasenie;

    /**
     * Vytvori kontroler uctu.
     * @param ucetService servis uctov
     * @param aktualnePrihlasenie pomocnik na upravu prihlasenia
     */
    public UcetApiController(UcetService ucetService, AktualnePrihlasenie aktualnePrihlasenie) {
        this.ucetService = ucetService;
        this.aktualnePrihlasenie = aktualnePrihlasenie;
    }

    /**
     * Zmeni heslo prihlaseneho pouzivatela.
     * @param poziadavka sucasne a nove heslo
     * @param pouzivatel prihlaseny pouzivatel
     * @param request poziadavka
     * @param response odpoved
     */
    @PostMapping("/api/heslo")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void zmenHeslo(@Valid @RequestBody ZmenaHeslaRequest poziadavka,
                          @AuthenticationPrincipal PrihlasenyPouzivatel pouzivatel,
                          HttpServletRequest request, HttpServletResponse response) {
        this.ucetService.zmenHeslo(pouzivatel.rola(), pouzivatel.id(), poziadavka.sucasneHeslo(),
                poziadavka.noveHeslo());
        this.aktualnePrihlasenie.poZmeneHesla(request, response);
    }
}
