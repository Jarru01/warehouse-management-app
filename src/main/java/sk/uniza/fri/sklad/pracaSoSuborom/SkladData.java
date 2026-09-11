package sk.uniza.fri.sklad.pracaSoSuborom;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Korenovy datovy model skladu v subore skladu.
 * @param verzia verzia formatu suboru
 * @param riaditel riaditel skladu
 * @param miestnosti zoznam miestnosti
 * @param tovar zoznam vsetkeho tovaru v sklade
 * @param pracovnici zoznam pracovnikov
 * @author Juraj
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SkladData(
        int verzia,
        OsobaData riaditel,
        List<MiestnostData> miestnosti,
        List<TovarData> tovar,
        List<PracovnikData> pracovnici) {
}
