package sk.uniza.fri.sklad.pracaSoSuborom;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Datovy model osoby v subore skladu.
 * @param id id osoby
 * @param meno meno osoby
 * @param priezvisko priezvisko osoby
 * @author Juraj
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OsobaData(String id, String meno, String priezvisko) {
}
