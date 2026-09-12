package warehouse.web.api;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Zakladny stavovy endpoint overujuci dostupnost aplikacie a databazy.
 * @author Juraj
 */
@RestController
public class StatusController {
    private final JdbcTemplate jdbcTemplate;

    /**
     * Vytvori kontroler s pristupom k databaze.
     * @param jdbcTemplate nastroj na jednoduche SQL dotazy
     */
    public StatusController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Vrati stav aplikacie a spojenia s databazou.
     * @return stav aplikacie a databazy
     */
    @GetMapping("/api/status")
    public Map<String, String> status() {
        Integer vysledok = this.jdbcTemplate.queryForObject("select 1", Integer.class);
        String databaza = vysledok != null && vysledok == 1 ? "ok" : "chyba";
        return Map.of("application", "warehouse-management-system", "database", databaza);
    }
}
