package warehouse.sklad.pracaSoSuborom;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Datovy model regalu v subore skladu.
 * @param kapacita kapacita regala
 * @param tovar zoznam ID tovaru v regali
 * @author Juraj
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record RegalData(int kapacita, List<String> tovar) {
}
