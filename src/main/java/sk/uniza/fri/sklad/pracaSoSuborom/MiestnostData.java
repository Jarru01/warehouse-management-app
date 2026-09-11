package sk.uniza.fri.sklad.pracaSoSuborom;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Datovy model miestnosti v subore skladu.
 * @param kluc kluc miestnosti
 * @param typ typ miestnosti ("velky" alebo "maly")
 * @param regale zoznam regalov v miestnosti
 * @author Juraj
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MiestnostData(String kluc, String typ, List<RegalData> regale) {
}
