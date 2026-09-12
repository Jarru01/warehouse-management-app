package warehouse.domain;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Velky sklad s lubovolnym poctom regalov.
 * @author Juraj
 */
@Entity
@DiscriminatorValue("VELKY")
public class VelkySklad extends Miestnost {

    protected VelkySklad() {
    }

    /**
     * Vytvori velky sklad s klucom z parametra.
     * @param kluc kluc miestnosti
     */
    public VelkySklad(String kluc) {
        super(kluc);
    }

    /**
     * Vrati typ miestnosti.
     * @return typ VELKY
     */
    @Override
    public TypMiestnosti getTyp() {
        return TypMiestnosti.VELKY;
    }
}
