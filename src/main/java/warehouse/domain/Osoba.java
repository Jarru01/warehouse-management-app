package warehouse.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

/**
 * Spolocny predok osob, ktore maju meno a priezvisko.
 * @author Juraj
 */
@MappedSuperclass
public abstract class Osoba {

    @Column(nullable = false, length = 100)
    private String meno;

    @Column(nullable = false, length = 100)
    private String priezvisko;

    protected Osoba() {
    }

    /**
     * Vytvori osobu s menom a priezviskom z parametrov.
     * @param meno meno osoby
     * @param priezvisko priezvisko osoby
     */
    protected Osoba(String meno, String priezvisko) {
        this.meno = meno;
        this.priezvisko = priezvisko;
    }

    /**
     * Vrati meno osoby.
     * @return meno osoby
     */
    public String getMeno() {
        return this.meno;
    }

    /**
     * Vrati priezvisko osoby.
     * @return priezvisko osoby
     */
    public String getPriezvisko() {
        return this.priezvisko;
    }

    /**
     * Vrati string s menom a priezviskom osoby.
     * @return string s udajmi o osobe
     */
    @Override
    public String toString() {
        return "[Meno: " + this.meno + ", Priezvisko: " + this.priezvisko + "]";
    }
}
