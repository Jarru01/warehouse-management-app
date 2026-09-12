package warehouse.web.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Poziadavka na prijatie pracovnika.
 * @param id id pracovnika
 * @param meno meno pracovnika
 * @param priezvisko priezvisko pracovnika
 * @author Juraj
 */
public record PrijmoutPracovnikaRequest(
        @NotBlank @Pattern(regexp = "\\d+", message = "ID musi obsahovat iba cislice.") String id,
        @NotBlank @Pattern(regexp = "\\p{L}+", message = "Meno musi obsahovat iba pismena.") String meno,
        @NotBlank @Pattern(regexp = "\\p{L}+", message = "Priezvisko musi obsahovat iba pismena.") String priezvisko) {
}
