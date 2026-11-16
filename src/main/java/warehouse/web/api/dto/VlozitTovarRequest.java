package warehouse.web.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

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
        @NotBlank(message = "ID tovaru je povinne.")
        @Size(max = 50, message = "ID tovaru moze mat najviac 50 znakov.")
        @Pattern(regexp = "\\d+", message = "ID tovaru musi obsahovat iba cislice.") String id,
        @NotBlank(message = "Nazov je povinny.")
        @Size(max = 100, message = "Nazov moze mat najviac 100 znakov.")
        @Pattern(regexp = "[\\p{L}\\p{N}]+", message = "Nazov moze obsahovat iba pismena a cislice.") String nazov,
        @Positive(message = "Vaha musi byt kladna.") int vaha,
        @NotNull(message = "Odosielatel je povinny.") @Valid ZakaznikRequest odosielatel,
        @NotNull(message = "Prijemca je povinny.") @Valid ZakaznikRequest prijemca) {
}
