package warehouse.web.api.dto;

/**
 * Pracovnik v odpovedi.
 * @param id id pracovnika
 * @param meno meno pracovnika
 * @param priezvisko priezvisko pracovnika
 * @param miestnost kluc miestnosti, v ktorej sa pracovnik nachadza, alebo null
 * @param drzanyTovar id drzaneho tovaru alebo null
 * @author Juraj
 */
public record PracovnikResponse(String id, String meno, String priezvisko, String miestnost, String drzanyTovar) {
}
