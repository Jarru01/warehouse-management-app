package warehouse.sklad.pracaSoSuborom;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Datovy model pracovnika v subore skladu.
 * @param id id pracovnika
 * @param meno meno pracovnika
 * @param priezvisko priezvisko pracovnika
 * @param miestnost kluc miestnosti, v ktorej sa pracovnik nachadza
 * @param drzanyTovar id tovaru, ktory pracovnik drzi, alebo null
 * @author Juraj
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PracovnikData(String id, String meno, String priezvisko, String miestnost, String drzanyTovar) {
}
