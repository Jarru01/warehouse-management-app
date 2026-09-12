package warehouse.web.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

/**
 * Poziadavka na vytvorenie tovaru zakaznikom.
 * @param id id tovaru
 * @param nazov nazov tovaru
 * @param vaha vaha tovaru v gramoch
 * @param odosielatel udaje o odosielatelovi
 * @param prijemca udaje o prijemcovi
 * @author Juraj
 */
public record VlozitTovarRequest(
        @NotBlank @Pattern(regexp = "\\d+", message = "ID tovaru musi obsahovat iba cislice.") String id,
        @NotBlank @Pattern(regexp = "[\\p{L}\\p{N}]+", message = "Nazov moze obsahovat iba pismena a cislice.") String nazov,
        @Positive(message = "Vaha musi byt kladna.") int vaha,
        @NotNull @Valid ZakaznikRequest odosielatel,
        @NotNull @Valid ZakaznikRequest prijemca) {
}
