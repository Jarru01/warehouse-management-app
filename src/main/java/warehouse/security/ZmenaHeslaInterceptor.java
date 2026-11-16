package warehouse.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;
import warehouse.web.api.ZakazanyPristupException;

/**
 * Pouzivatel prihlaseny docasnym heslom moze otvorit iba zmenu hesla. Obrazovky presmeruje na zmenu hesla,
 * REST rozhranie odmietne s odpovedou 403.
 * @author Juraj
 */
public class ZmenaHeslaInterceptor implements HandlerInterceptor {

    /**
     * Overi, ci si prihlaseny pouzivatel nemusi najprv zmenit heslo.
     * @param request poziadavka
     * @param response odpoved
     * @param handler spracovavac
     * @return true ak moze poziadavka pokracovat
     * @throws Exception chyba pri presmerovani
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        Authentication prihlasenie = SecurityContextHolder.getContext().getAuthentication();
        if (prihlasenie == null || !(prihlasenie.getPrincipal() instanceof PrihlasenyPouzivatel pouzivatel)
                || !pouzivatel.zmenaHesla()) {
            return true;
        }
        String cesta = request.getRequestURI().substring(request.getContextPath().length());
        if (cesta.startsWith("/api/")) {
            throw new ZakazanyPristupException("Najprv si zmen docasne heslo.");
        }
        response.sendRedirect(request.getContextPath() + SecurityConfig.ZMENA_HESLA);
        return false;
    }
}
