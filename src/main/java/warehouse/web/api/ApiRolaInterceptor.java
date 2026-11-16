package warehouse.web.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import warehouse.web.view.SessionKluce;

import java.util.Map;
import java.util.Set;

/**
 * Interceptor REST rozhrania, ktory povoli poziadavku iba prihlasenej roli. Volitelne overi, ze premenna
 * v ceste (napriklad id pracovnika) sa zhoduje s identitou prihlaseneho pouzivatela. Pri neuspechu vyhodi
 * vynimku, ktoru {@link GlobalExceptionHandler} prevedie na odpoved 401 alebo 403.
 * @author Juraj
 */
public class ApiRolaInterceptor implements HandlerInterceptor {
    private final Set<String> roly;
    private final String premennaIdentity;

    /**
     * Vytvori interceptor pre povolene roly.
     * @param roly roly, ktore mozu poziadavku vykonat
     * @param premennaIdentity nazov premennej v ceste, ktora sa musi zhodovat s identitou v session, alebo null
     */
    public ApiRolaInterceptor(Set<String> roly, String premennaIdentity) {
        this.roly = Set.copyOf(roly);
        this.premennaIdentity = premennaIdentity;
    }

    /**
     * Overi rolu a pripadne identitu v session.
     * @param request poziadavka
     * @param response odpoved
     * @param handler spracovavac
     * @return true ak je pristup povoleny
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        HttpSession session = request.getSession(false);
        Object rola = session == null ? null : session.getAttribute(SessionKluce.ROLA);
        if (rola == null) {
            throw new NeprihlasenyException("Pristup vyzaduje prihlasenie.");
        }
        if (!this.roly.contains(rola)) {
            throw new ZakazanyPristupException("Prihlasena rola nema pristup k tejto operacii.");
        }
        if (this.premennaIdentity != null) {
            String hodnota = this.premennaVCeste(request);
            if (hodnota != null && !hodnota.equals(session.getAttribute(SessionKluce.IDENTITA))) {
                throw new ZakazanyPristupException("Operaciu je mozne vykonat iba pre vlastnu identitu.");
            }
        }
        return true;
    }

    private String premennaVCeste(HttpServletRequest request) {
        Object premenne = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (premenne instanceof Map<?, ?> mapa && mapa.get(this.premennaIdentity) instanceof String hodnota) {
            return hodnota;
        }
        return null;
    }
}
