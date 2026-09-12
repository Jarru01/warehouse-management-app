package warehouse.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Riaditel skladu.
 * @author Juraj
 */
@Entity
@Table(name = "riaditel")
public class Riaditel extends Osoba {
    public static final String PREDVOLENE_ID = "123";
    public static final String PREDVOLENE_MENO = "Juraj";
    public static final String PREDVOLENE_PRIEZVISKO = "Solensky";

    @Id
    @Column(nullable = false, length = 50)
    private String id;

    protected Riaditel() {
    }

    /**
     * Vytvori riaditela z parametrov.
     * @param id id riaditela
     * @param meno meno riaditela
     * @param priezvisko priezvisko riaditela
     */
    public Riaditel(String id, String meno, String priezvisko) {
        super(meno, priezvisko);
        this.id = id;
    }

    /**
     * Vrati id riaditela.
     * @return id riaditela
     */
    public String getId() {
        return this.id;
    }
}
