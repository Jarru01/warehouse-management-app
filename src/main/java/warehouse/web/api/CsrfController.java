package warehouse.web.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Vrati CSRF token pre klientov REST rozhrania. Token sa posiela v hlavicke pri kazdej zmenovej poziadavke.
 * @author Juraj
 */
@RestController
@Tag(name = "Ucet", description = "Prihlasenie a CSRF token pre REST klientov")
public class CsrfController {

    /**
     * Vrati CSRF token aktualnej session spolu s nazvom hlavicky a parametra.
     * @param token CSRF token
     * @return nazov hlavicky, nazov parametra a hodnota tokenu
     */
    @Operation(summary = "Vrati CSRF token aktualnej session")
    @GetMapping("/api/csrf")
    public Map<String, String> csrf(CsrfToken token) {
        return Map.of("headerName", token.getHeaderName(), "parameterName", token.getParameterName(),
                "token", token.getToken());
    }
}
