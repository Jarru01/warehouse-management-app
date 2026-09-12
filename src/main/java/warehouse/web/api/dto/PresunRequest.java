package warehouse.web.api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Poziadavka na presun pracovnika do miestnosti.
 * @param miestnost kluc cielovej miestnosti
 * @author Juraj
 */
public record PresunRequest(
        @NotBlank(message = "Miestnost nesmie byt prazdna.") String miestnost) {
}
