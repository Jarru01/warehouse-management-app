package warehouse.web.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Poziadavka na zobratie tovaru pracovnikom.
 * @param tovarId id tovaru
 * @author Juraj
 */
public record ZobratTovarRequest(
        @NotBlank @Pattern(regexp = "\\d+", message = "ID tovaru musi obsahovat iba cislice.") String tovarId) {
}
