package warehouse.web.view;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import warehouse.security.PrihlasenyPouzivatel;

/**
 * Spolocne udaje pre vsetky obrazovky: prihlaseny pouzivatel a CSRF token.
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
}
