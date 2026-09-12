package warehouse.web.api.dto;

/**
 * Riaditel v odpovedi.
 * @param id id riaditela
 * @param meno meno riaditela
 * @param priezvisko priezvisko riaditela
 * @author Juraj
 */
public record RiaditelResponse(String id, String meno, String priezvisko) {
}
