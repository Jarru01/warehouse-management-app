package warehouse.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Regal s pevnou kapacitou, ktory patri do miestnosti.
 * @author Juraj
 */
@Entity
@Table(name = "regal")
public class Regal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "miestnost_kluc", nullable = false)
    private Miestnost miestnost;

    @Column(nullable = false)
    private int kapacita;

    protected Regal() {
    }

    /**
     * Vytvori regal v miestnosti s kapacitou z parametrov.
     * @param miestnost miestnost, do ktorej regal patri
     * @param kapacita kapacita regala
     */
    public Regal(Miestnost miestnost, int kapacita) {
        if (kapacita < 1) {
            throw new IllegalArgumentException("Kapacita regala musi byt kladna.");
        }
        this.miestnost = miestnost;
        this.kapacita = kapacita;
    }

    /**
     * Vrati identifikator regalu.
     * @return id regalu
     */
    public Long getId() {
        return this.id;
    }

    /**
     * Vrati miestnost, do ktorej regal patri.
     * @return miestnost
     */
    public Miestnost getMiestnost() {
        return this.miestnost;
    }

    /**
     * Vrati kapacitu regalu.
     * @return kapacita
     */
    public int getKapacita() {
        return this.kapacita;
    }
}
