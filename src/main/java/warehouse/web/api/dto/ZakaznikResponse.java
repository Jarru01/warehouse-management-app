package warehouse.web.api.dto;

/**
 * Udaje zakaznika v odpovedi.
 * @param id id zakaznika
 * @param meno meno zakaznika
 * @param priezvisko priezvisko zakaznika
 * @author Juraj
 */
public record ZakaznikResponse(String id, String meno, String priezvisko) {
}
