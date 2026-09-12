package warehouse.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * Pracovnik, ktory sa nachadza v miestnosti a moze drzat tovar.
 * @author Juraj
 */
@Entity
@Table(name = "pracovnik")
public class Pracovnik {

    @Id
    @Column(nullable = false, length = 50)
    private String id;

    @Column(nullable = false, length = 100)
    private String meno;

    @Column(nullable = false, length = 100)
    private String priezvisko;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "miestnost_kluc")
    private Miestnost miestnost;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drzany_tovar_id", unique = true)
    private Tovar drzanyTovar;

    protected Pracovnik() {
    }

    /**
     * Vytvori pracovnika z parametrov bez miestnosti a bez tovaru.
     * @param id id pracovnika
     * @param meno meno pracovnika
     * @param priezvisko priezvisko pracovnika
     */
    public Pracovnik(String id, String meno, String priezvisko) {
        this.id = id;
        this.meno = meno;
        this.priezvisko = priezvisko;
    }

    /**
     * Presunie pracovnika do miestnosti z parametra.
     * @param miestnost cielova miestnost
     */
    public void premiestniDo(Miestnost miestnost) {
        this.miestnost = miestnost;
    }

    /**
     * Pracovnik uchopi tovar z parametra.
     * @param tovar tovar na uchopenie
     */
    public void uchop(Tovar tovar) {
        this.drzanyTovar = tovar;
    }

    /**
     * Pracovnik polozi drzany tovar.
     */
    public void poloz() {
        this.drzanyTovar = null;
    }

    /**
     * Vrati informaciu, ci pracovnik drzi tovar.
     * @return true ak pracovnik drzi tovar
     */
    public boolean drziTovar() {
        return this.drzanyTovar != null;
    }

    /**
     * Vrati id pracovnika.
     * @return id pracovnika
     */
    public String getId() {
        return this.id;
    }

    /**
     * Vrati meno pracovnika.
     * @return meno pracovnika
     */
    public String getMeno() {
        return this.meno;
    }

    /**
     * Vrati priezvisko pracovnika.
     * @return priezvisko pracovnika
     */
    public String getPriezvisko() {
        return this.priezvisko;
    }

    /**
     * Vrati miestnost, v ktorej sa pracovnik nachadza.
     * @return miestnost alebo null
     */
    public Miestnost getMiestnost() {
        return this.miestnost;
    }

    /**
     * Vrati tovar, ktory pracovnik drzi, alebo null.
     * @return tovar alebo null
     */
    public Tovar getDrzanyTovar() {
        return this.drzanyTovar;
    }
}
