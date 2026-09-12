package warehouse.web.api.dto;

/**
 * Tovar v odpovedi.
 * @param id id tovaru
 * @param nazov nazov tovaru
 * @param vaha vaha tovaru v gramoch
 * @param odosielatel udaje o odosielatelovi
 * @param prijemca udaje o prijemcovi
 * @param miestnost kluc miestnosti, v ktorej je tovar ulozeny, alebo null
 * @param regalId id regalu alebo null
 * @param slot slot v regali alebo null
 * @author Juraj
 */
public record TovarResponse(String id, String nazov, int vaha, ZakaznikResponse odosielatel,
                            ZakaznikResponse prijemca, String miestnost, Long regalId, Integer slot) {
}
