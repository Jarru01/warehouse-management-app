package warehouse.web.api;

/**
 * Vynimka pre poziadavku prihlaseneho pouzivatela, ktory na operaciu nema opravnenie.
 * @author Juraj
 */
public class ZakazanyPristupException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * Vytvori vynimku so spravou z parametra.
     * @param sprava opis chyby
     */
    public ZakazanyPristupException(String sprava) {
        super(sprava);
    }
}
