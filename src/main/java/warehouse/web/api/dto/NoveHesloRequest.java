package warehouse.web.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Poziadavka riaditela na nove docasne heslo pracovnika.
 * @param heslo nove docasne heslo
 * @author Juraj
 */
public record NoveHesloRequest(
        @NotBlank(message = "Heslo je povinne.")
        @Size(min = 8, max = 72, message = "Heslo musi mat 8 az 72 znakov.") String heslo) {

    /**
     * Skryje heslo pri vypise poziadavky.
     * @return textovy opis bez hesla
     */
    @Override
    public String toString() {
        return "NoveHesloRequest[]";
    }
}
