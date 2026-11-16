package warehouse.web.view;

/**
 * Nazvy atributov v HTTP session a hodnoty rol.
 * @author Juraj
 */
public final class SessionKluce {
    public static final String ROLA = "rola";
    public static final String IDENTITA = "identita";
    public static final String MENO = "meno";
    public static final String PRIEZVISKO = "priezvisko";

    public static final String ROLA_RIADITEL = "RIADITEL";
    public static final String ROLA_PRACOVNIK = "PRACOVNIK";
    public static final String ROLA_ZAKAZNIK = "ZAKAZNIK";

    private SessionKluce() {
    }
}
