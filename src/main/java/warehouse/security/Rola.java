package warehouse.security;

import java.util.Arrays;
import java.util.Optional;

/**
 * Rola prihlaseneho pouzivatela.
 * @author Juraj
 */
public enum Rola {
    RIADITEL("riaditel", "/riaditel"),
    PRACOVNIK("pracovnik", "/pracovnik"),
    ZAKAZNIK("zakaznik", "/zakaznik");

    private final String kluc;
    private final String domovskaStranka;

    Rola(String kluc, String domovskaStranka) {
        this.kluc = kluc;
        this.domovskaStranka = domovskaStranka;
    }

    /**
     * Vrati kluc roly pouzivany v adrese uvodnej stranky.
     * @return kluc roly
     */
    public String getKluc() {
        return this.kluc;
    }

    /**
     * Vrati stranku, na ktoru sa rola presmeruje po prihlaseni.
     * @return cesta k stranke
     */
    public String getDomovskaStranka() {
        return this.domovskaStranka;
    }

    /**
     * Vrati opravnenie roly v tvare pouzivanom Spring Security.
     * @return nazov opravnenia
     */
    public String getOpravnenie() {
        return "ROLE_" + this.name();
    }

    /**
     * Najde rolu podla nazvu z formulara.
     * @param nazov nazov roly, napriklad RIADITEL
     * @return rola alebo prazdny vysledok
     */
    public static Optional<Rola> podlaNazvu(String nazov) {
        return Arrays.stream(values()).filter(rola -> rola.name().equals(nazov)).findFirst();
    }
}
