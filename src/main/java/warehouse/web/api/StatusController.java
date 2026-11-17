package warehouse.web.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Zakladny stavovy endpoint overujuci dostupnost aplikacie a databazy.
 * @author Juraj
 */
@RestController
@Tag(name = "Stav", description = "Stav aplikacie a databazy")
public class StatusController {
    private static final String APLIKACIA = "warehouse-management-app";

    private final JdbcTemplate jdbcTemplate;

    /**
     * Vytvori kontroler s pristupom k databaze.
     * @param jdbcTemplate nastroj na jednoduche SQL dotazy
     */
    public StatusController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Vrati stav aplikacie a spojenia s databazou. Ak databaza nie je dostupna, vrati 503 so stavom "chyba".
     * @return stav aplikacie a databazy
     */
    @Operation(summary = "Stav aplikacie a databazy (503 pri vypadku databazy)")
    @GetMapping("/api/status")
    public ResponseEntity<Map<String, String>> status() {
        boolean databazaOk;
        try {
            Integer vysledok = this.jdbcTemplate.queryForObject("select 1", Integer.class);
            databazaOk = vysledok != null && vysledok == 1;
        } catch (DataAccessException e) {
            databazaOk = false;
        }
        HttpStatus stav = databazaOk ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
        return ResponseEntity.status(stav)
                .body(Map.of("application", APLIKACIA, "database", databazaOk ? "ok" : "chyba"));
    }
}
