package warehouse.web.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Poziadavka na registraciu zakaznika.
 * @param id zvolene id zakaznika
 * @param meno meno zakaznika
 * @param priezvisko priezvisko zakaznika
 * @param heslo heslo na prihlasenie
 * @author Juraj
 */
public record RegistraciaZakaznikaRequest(
        @NotBlank(message = "ID je povinne.")
        @Size(max = 50, message = "ID moze mat najviac 50 znakov.")
        @Pattern(regexp = "\\d+", message = "ID musi obsahovat iba cislice.") String id,
        @NotBlank(message = "Meno je povinne.")
        @Size(max = 100, message = "Meno moze mat najviac 100 znakov.")
        @Pattern(regexp = "\\p{L}+", message = "Meno musi obsahovat iba pismena.") String meno,
        @NotBlank(message = "Priezvisko je povinne.")
        @Size(max = 100, message = "Priezvisko moze mat najviac 100 znakov.")
        @Pattern(regexp = "\\p{L}+", message = "Priezvisko musi obsahovat iba pismena.") String priezvisko,
        @NotBlank(message = "Heslo je povinne.")
        @Size(min = 8, max = 72, message = "Heslo musi mat 8 az 72 znakov.") String heslo) {

    /**
     * Skryje heslo pri vypise poziadavky.
     * @return textovy opis bez hesla
     */
    @Override
    public String toString() {
        return "RegistraciaZakaznikaRequest[id=" + this.id + ", meno=" + this.meno + ", priezvisko=" + this.priezvisko + "]";
    }
}
