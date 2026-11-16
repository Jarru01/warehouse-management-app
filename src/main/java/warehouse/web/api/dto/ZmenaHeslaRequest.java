package warehouse.web.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Poziadavka na zmenu vlastneho hesla.
 * @param sucasneHeslo sucasne heslo
 * @param noveHeslo nove heslo
 * @author Juraj
 */
public record ZmenaHeslaRequest(
        @NotBlank(message = "Sucasne heslo je povinne.") String sucasneHeslo,
        @NotBlank(message = "Nove heslo je povinne.")
        @Size(min = 8, max = 72, message = "Nove heslo musi mat 8 az 72 znakov.") String noveHeslo) {

    /**
     * Skryje hesla pri vypise poziadavky.
     * @return textovy opis bez hesiel
     */
    @Override
    public String toString() {
        return "ZmenaHeslaRequest[]";
    }
}
