package warehouse.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

/**
 * Doplnkove udaje prihlasovacieho formulara: okrem adresy a session aj zvolena rola.
 * @author Juraj
 */
public class PrihlasenieDetails extends WebAuthenticationDetails {
    private static final long serialVersionUID = 1L;

    private final String rola;

    /**
     * Nacita udaje z poziadavky na prihlasenie.
     * @param request poziadavka na prihlasenie
     */
    public PrihlasenieDetails(HttpServletRequest request) {
        super(request);
        this.rola = request.getParameter("rola");
    }

    /**
     * Vrati rolu zvolenu vo formulari.
     * @return nazov roly alebo null
     */
    public String getRola() {
        return this.rola;
    }
}
