package warehouse.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;

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

    @Column(name = "zmena_hesla", nullable = false)
    private boolean zmenaHesla;

    @Version
    private Long verzia;

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
     * Vrati informaciu, ci si osoba musi pri najblizsom prihlaseni zmenit heslo.
     * @return true pre docasne heslo
     */
    public boolean isZmenaHesla() {
        return this.zmenaHesla;
    }

    /**
     * Nastavi hash hesla osoby. Heslo sa nikdy neuklada v citatelnej podobe.
     * @param hesloHash hash hesla
     * @param docasne true, ak heslo nastavil niekto iny a osoba si ho musi zmenit
     */
    public void nastavHeslo(String hesloHash, boolean docasne) {
        this.hesloHash = hesloHash;
        this.zmenaHesla = docasne;
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
