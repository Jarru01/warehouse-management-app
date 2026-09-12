package warehouse.web.view;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor, ktory zabezpeci, ze stranku moze otvorit iba prihlasena rola.
 * @author Juraj
 */
public class RolaInterceptor implements HandlerInterceptor {
    private final String rola;

    /**
     * Vytvori interceptor pre rolu z parametra.
     * @param rola pozadovana rola
     */
    public RolaInterceptor(String rola) {
        this.rola = rola;
    }

    /**
     * Overi rolu v session. Pri neopravnenom pristupe presmeruje na uvodnu stranku.
     * @param request poziadavka
     * @param response odpoved
     * @param handler spracovavac
     * @return true ak je pristup povoleny
     * @throws Exception chyba pri presmerovani
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        HttpSession session = request.getSession(false);
        Object prihlasenaRola = session == null ? null : session.getAttribute(SessionKluce.ROLA);
        if (this.rola.equals(prihlasenaRola)) {
            return true;
        }
        response.sendRedirect("/");
        return false;
    }
}
