package warehouse.web.api;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Popis REST rozhrania pre OpenAPI a Swagger UI. Prihlasenie je session z uvodnej stranky; zmenove poziadavky
 * potrebuju CSRF token v hlavicke X-CSRF-TOKEN, ktory sa v Swagger UI zada cez tlacidlo "Authorize".
 * @author Juraj
 */
@Configuration
public class OpenApiConfig {
    private static final String CSRF = "csrf";

    /**
     * Zakladne udaje o rozhrani a schema CSRF tokenu.
     * @return popis rozhrania
     */
    @Bean
    public OpenAPI warehouseOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Warehouse App API")
                        .version("2.0.0")
                        .description("""
                                REST rozhranie skladu. Pouziva rovnake prihlasenie (session) ako webove stranky: \
                                najprv sa prihlas na uvodnej stranke. Pre POST a DELETE ziskaj token z \
                                GET /api/csrf a zadaj ho cez Authorize ako X-CSRF-TOKEN. Chyby su vo formate \
                                RFC 9457 ProblemDetail."""))
                .components(new Components().addSecuritySchemes(CSRF, new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .name("X-CSRF-TOKEN")
                        .description("CSRF token z GET /api/csrf (pole token).")))
                .addSecurityItem(new SecurityRequirement().addList(CSRF));
    }
}
