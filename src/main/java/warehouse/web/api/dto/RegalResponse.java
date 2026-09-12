package warehouse.web.api.dto;

import java.util.List;

/**
 * Regal v odpovedi.
 * @param id id regalu
 * @param miestnost kluc miestnosti
 * @param kapacita kapacita regalu
 * @param tovar zoznam tovaru v regali
 * @author Juraj
 */
public record RegalResponse(Long id, String miestnost, int kapacita, List<TovarResponse> tovar) {
}
