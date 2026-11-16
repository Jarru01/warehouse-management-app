package warehouse.web.api;

/**
 * Vynimka pre poziadavku na REST rozhranie bez prihlasenia.
 * @author Juraj
 */
public class NeprihlasenyException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * Vytvori vynimku so spravou z parametra.
     * @param sprava opis chyby
     */
    public NeprihlasenyException(String sprava) {
        super(sprava);
    }
}
