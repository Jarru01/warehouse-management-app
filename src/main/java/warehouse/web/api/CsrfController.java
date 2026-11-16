package warehouse.web.api;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Vrati CSRF token pre klientov REST rozhrania. Token sa posiela v hlavicke pri kazdej zmenovej poziadavke.
 * @author Juraj
 */
@RestController
public class CsrfController {

    /**
     * Vrati CSRF token aktualnej session spolu s nazvom hlavicky a parametra.
     * @param token CSRF token
     * @return nazov hlavicky, nazov parametra a hodnota tokenu
     */
    @GetMapping("/api/csrf")
    public Map<String, String> csrf(CsrfToken token) {
        return Map.of("headerName", token.getHeaderName(), "parameterName", token.getParameterName(),
                "token", token.getToken());
    }
}
