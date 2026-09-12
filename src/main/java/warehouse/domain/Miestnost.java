package warehouse.domain;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

/**
 * Miestnost skladu s jedinecnym klucom a menom. Potomkovia rozlisuju typ miestnosti.
 * @author Juraj
 */
@Entity
@Table(name = "miestnost")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "typ", discriminatorType = DiscriminatorType.STRING)
public abstract class Miestnost {
    public static final String SKLAD_TOVARU = "skladTovaru";
    public static final String PRIJEM_TOVARU = "prijemTovaru";
    public static final String VYDAJ_TOVARU = "vydajTovaru";
    public static final int KAPACITA_MALEHO_SKLADU = 5;

    @Id
    @Column(name = "kluc", nullable = false, length = 50)
    private String kluc;

    protected Miestnost() {
    }

    protected Miestnost(String kluc) {
        this.kluc = kluc;
    }

    /**
     * Vrati kluc miestnosti.
     * @return kluc miestnosti
     */
    public String getKluc() {
        return this.kluc;
    }

    /**
     * Vrati typ miestnosti.
     * @return typ miestnosti
     */
    public abstract TypMiestnosti getTyp();
}
