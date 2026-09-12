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
public class Pracovnik extends Osoba {

    @Id
    @Column(nullable = false, length = 50)
    private String id;

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
        super(meno, priezvisko);
        this.id = id;
    }

    /**
     * Presunie pracovnika do miestnosti z parametra.
     * @param miestnost cielova miestnost
     */
    public void premiestniDo(Miestnost miestnost) {
        this.miestnost = miestnost;
    }

    /**
     * Pracovnik uchopi tovar z parametra. Ak uz nejaky tovar drzi alebo tovar neexistuje, operacia zlyha.
     * @param tovar tovar na uchopenie
     */
    public void uchop(Tovar tovar) {
        if (tovar == null) {
            throw new NeplatnaOperaciaException("Tovar na uchopenie neexistuje.");
        }
        if (this.drzanyTovar != null) {
            throw new NeplatnaOperaciaException("Pracovnik uz drzi tovar.");
        }
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

    /**
     * Vrati string s udajmi o pracovnikovi.
     * @return string s udajmi o pracovnikovi
     */
    @Override
    public String toString() {
        return "[Meno: " + this.getMeno() + ", Priezvisko: " + this.getPriezvisko() + ", ID: " + this.id + "]";
    }
}
