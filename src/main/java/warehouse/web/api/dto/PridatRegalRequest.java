package warehouse.web.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * Poziadavka na pridanie regalu.
 * @param kapacita kapacita regalu
 * @author Juraj
 */
public record PridatRegalRequest(
        @Min(value = 1, message = "Kapacita musi byt kladna.")
        @Max(value = 20, message = "Kapacita moze byt najviac 20.")
        int kapacita) {
}
