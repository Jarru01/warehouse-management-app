package warehouse.domain;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Maly sklad s jednym regalom fixnej kapacity.
 * @author Juraj
 */
@Entity
@DiscriminatorValue("MALY")
public class MalySklad extends Miestnost {

    protected MalySklad() {
    }

    /**
     * Vytvori maly sklad s klucom z parametra.
     * @param kluc kluc miestnosti
     */
    public MalySklad(String kluc) {
        super(kluc);
    }
}
