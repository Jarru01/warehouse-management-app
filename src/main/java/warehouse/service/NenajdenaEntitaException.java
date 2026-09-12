package warehouse.service;

/**
 * Vynimka pre poziadanie o entitu, ktora neexistuje.
 * @author Juraj
 */
public class NenajdenaEntitaException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * Vytvori vynimku so spravou z parametra.
     * @param sprava opis chyby
     */
    public NenajdenaEntitaException(String sprava) {
        super(sprava);
    }
}
