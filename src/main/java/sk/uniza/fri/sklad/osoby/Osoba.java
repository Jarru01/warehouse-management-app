package sk.uniza.fri.sklad.osoby;

/**
 * Trieda Osoba reprezentuje fyzicku osobu s menom a priezviskom.
 * @author Juraj
 */

public abstract class Osoba {
    private final String meno;      //meno osoby
    private final String priezvisko;//priezvisko osoby

    /**
     * Inicializuje atributy meno a priezvisko z parametrov konstruktora.
     * @param meno meno osoby
     * @param priezvisko priezvisko osoby
     */
    public Osoba(String meno, String priezvisko) {
        this.meno = meno;
        this.priezvisko = priezvisko;
    }

    /**
     * Vrati meno osoby.
     * @return meno
     */
    public String getMeno() {
        return this.meno;
    }

    /**
     * Vrati priezvisko osoby.
     * @return priezvisko
     */
    public String getPriezvisko() {
        return this.priezvisko;
    }


    /**
     * Vrati string s informaciami o osobe.
     * @return string s informaciami o osobe
     */
    @Override
    public String toString() {
        return "[Meno: " + this.meno + ", " + "Priezvisko: " + this.priezvisko + "]";

    }
}
