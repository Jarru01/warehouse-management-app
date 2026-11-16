package warehouse.web.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Udaje zakaznika v poziadavke.
 * @param id id zakaznika
 * @param meno meno zakaznika
 * @param priezvisko priezvisko zakaznika
 * @author Juraj
 */
public record ZakaznikRequest(
        @NotBlank(message = "ID zakaznika je povinne.")
        @Size(max = 50, message = "ID zakaznika moze mat najviac 50 znakov.")
        @Pattern(regexp = "\\d+", message = "ID musi obsahovat iba cislice.") String id,
        @NotBlank(message = "Meno zakaznika je povinne.")
        @Size(max = 100, message = "Meno zakaznika moze mat najviac 100 znakov.")
        @Pattern(regexp = "\\p{L}+", message = "Meno musi obsahovat iba pismena.") String meno,
        @NotBlank(message = "Priezvisko zakaznika je povinne.")
        @Size(max = 100, message = "Priezvisko zakaznika moze mat najviac 100 znakov.")
        @Pattern(regexp = "\\p{L}+", message = "Priezvisko musi obsahovat iba pismena.") String priezvisko) {
}
