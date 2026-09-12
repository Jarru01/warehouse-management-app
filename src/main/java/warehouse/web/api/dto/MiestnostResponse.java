package warehouse.web.api.dto;

/**
 * Miestnost v odpovedi.
 * @param kluc kluc miestnosti
 * @param typ typ miestnosti
 * @author Juraj
 */
public record MiestnostResponse(String kluc, String typ) {
}
