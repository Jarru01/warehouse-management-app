package warehouse.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

/**
 * Spolocny predok osob, ktore maju meno, priezvisko a heslo na prihlasenie.
 * @author Juraj
 */
@MappedSuperclass
public abstract class Osoba {

    @Column(nullable = false, length = 100)
    private String meno;

    @Column(nullable = false, length = 100)
    private String priezvisko;

    @Column(name = "heslo_hash", length = 100)
    private String hesloHash;

    protected Osoba() {
    }

    /**
     * Vrati hash hesla osoby alebo null, ak osoba heslo nema.
     * @return hash hesla alebo null
     */
    public String getHesloHash() {
        return this.hesloHash;
    }

    /**
     * Nastavi hash hesla osoby. Heslo sa nikdy neuklada v citatelnej podobe.
     * @param hesloHash hash hesla
     */
    public void nastavHesloHash(String hesloHash) {
        this.hesloHash = hesloHash;
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
