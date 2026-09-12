package warehouse.web.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Udaje zakaznika v poziadavke.
 * @param id id zakaznika
 * @param meno meno zakaznika
 * @param priezvisko priezvisko zakaznika
 * @author Juraj
 */
public record ZakaznikRequest(
        @NotBlank @Pattern(regexp = "\\d+", message = "ID musi obsahovat iba cislice.") String id,
        @NotBlank @Pattern(regexp = "\\p{L}+", message = "Meno musi obsahovat iba pismena.") String meno,
        @NotBlank @Pattern(regexp = "\\p{L}+", message = "Priezvisko musi obsahovat iba pismena.") String priezvisko) {
}
