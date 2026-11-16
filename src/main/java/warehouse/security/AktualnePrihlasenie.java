package warehouse.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;

/**
 * Upravi prihlasenie v aktualnej session, napriklad po zmene docasneho hesla.
 * @author Juraj
 */
@Component
public class AktualnePrihlasenie {
    private final SecurityContextRepository securityContextRepository;

    /**
     * Vytvori pomocnika s ulozistom bezpecnostneho kontextu.
     * @param securityContextRepository uloziste bezpecnostneho kontextu
     */
    public AktualnePrihlasenie(SecurityContextRepository securityContextRepository) {
        this.securityContextRepository = securityContextRepository;
    }

    /**
     * Zrusi povinnost zmenit heslo v prihlaseni aktualneho pouzivatela.
     * @param request poziadavka
     * @param response odpoved
     */
    public void poZmeneHesla(HttpServletRequest request, HttpServletResponse response) {
        Authentication sucasne = SecurityContextHolder.getContext().getAuthentication();
        if (sucasne == null || !(sucasne.getPrincipal() instanceof PrihlasenyPouzivatel pouzivatel)) {
            return;
        }
        Authentication nove = UsernamePasswordAuthenticationToken.authenticated(pouzivatel.poZmeneHesla(), null,
                sucasne.getAuthorities());
        SecurityContext kontext = SecurityContextHolder.createEmptyContext();
        kontext.setAuthentication(nove);
        SecurityContextHolder.setContext(kontext);
        this.securityContextRepository.saveContext(kontext, request, response);
    }
}
