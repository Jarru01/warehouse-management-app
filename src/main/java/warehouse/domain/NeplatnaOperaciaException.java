package warehouse.domain;

/**
 * Vynimka pre operaciu, ktoru nie je mozne vykonat.
 * @author Juraj
 */
public class NeplatnaOperaciaException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * Vytvori vynimku so spravou z parametra.
     * @param sprava opis chyby
     */
    public NeplatnaOperaciaException(String sprava) {
        super(sprava);
    }
}
