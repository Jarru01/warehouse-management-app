package warehouse.web.view;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.support.RequestContextUtils;
import warehouse.security.PrihlasenyPouzivatel;
import warehouse.web.api.GlobalExceptionHandler;

/**
 * Spolocne udaje a spracovanie chyb pre vsetky obrazovky.
 * @author Juraj
 */
@ControllerAdvice(basePackageClasses = SpolocnyModelAdvice.class)
public class SpolocnyModelAdvice {

    /**
     * Spristupni prihlaseneho pouzivatela sablonam.
     * @param pouzivatel prihlaseny pouzivatel alebo null
     * @return prihlaseny pouzivatel alebo null
     */
    @ModelAttribute("pouzivatel")
    public PrihlasenyPouzivatel pouzivatel(@AuthenticationPrincipal PrihlasenyPouzivatel pouzivatel) {
        return pouzivatel;
    }

    /**
     * Vytvori CSRF token este pred vykreslenim stranky, aby sa ulozil do session skor, nez sa odpoved odosle.
     * @param token CSRF token alebo null
     */
    @ModelAttribute
    public void nacitajCsrfToken(CsrfToken token) {
        if (token != null) {
            token.getToken();
        }
    }

    /**
     * Konflikt pri ukladani (sucasna zmena alebo porusene obmedzenie databazy) vrati pouzivatela na jeho
     * obrazovku so zrozumitelnou chybou namiesto chybovej stranky.
     * @param request poziadavka
     * @return presmerovanie na obrazovku sekcie
     */
    @ExceptionHandler({DataIntegrityViolationException.class, OptimisticLockingFailureException.class})
    public String konflikt(HttpServletRequest request) {
        RequestContextUtils.getOutputFlashMap(request).put("chyba", GlobalExceptionHandler.SPRAVA_KONFLIKTU);
        String cesta = request.getRequestURI().substring(request.getContextPath().length());
        int koniecSekcie = cesta.indexOf('/', 1);
        return "redirect:" + (koniecSekcie > 0 ? cesta.substring(0, koniecSekcie) : cesta);
    }
}
