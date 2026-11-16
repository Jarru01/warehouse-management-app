package warehouse.web.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Poziadavka na prijatie pracovnika.
 * @param id id pracovnika
 * @param meno meno pracovnika
 * @param priezvisko priezvisko pracovnika
 * @author Juraj
 */
public record PrijmoutPracovnikaRequest(
        @NotBlank(message = "ID je povinne.")
        @Size(max = 50, message = "ID moze mat najviac 50 znakov.")
        @Pattern(regexp = "\\d+", message = "ID musi obsahovat iba cislice.") String id,
        @NotBlank(message = "Meno je povinne.")
        @Size(max = 100, message = "Meno moze mat najviac 100 znakov.")
        @Pattern(regexp = "\\p{L}+", message = "Meno musi obsahovat iba pismena.") String meno,
        @NotBlank(message = "Priezvisko je povinne.")
        @Size(max = 100, message = "Priezvisko moze mat najviac 100 znakov.")
        @Pattern(regexp = "\\p{L}+", message = "Priezvisko musi obsahovat iba pismena.") String priezvisko) {
}
