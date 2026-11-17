package warehouse.service;

import warehouse.domain.Regal;
import warehouse.domain.Tovar;

import java.util.List;

/**
 * Regal spolu s tovarom, ktory je v nom ulozeny, nacitany bez dalsich dotazov.
 * @param regal regal
 * @param tovar tovar v regali zoradeny podla slotu
 * @author Juraj
 */
public record RegalSTovarom(Regal regal, List<Tovar> tovar) {
}
