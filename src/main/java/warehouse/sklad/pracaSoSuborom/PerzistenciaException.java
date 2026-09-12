package warehouse.sklad.pracaSoSuborom;

/**
 * Vynimka oznacujuca neplatny alebo nepodporovany obsah suboru skladu.
 * @author Juraj
 */
public class PerzistenciaException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * Vytvori vynimku so spravou z parametra.
     * @param sprava opis chyby
     */
    public PerzistenciaException(String sprava) {
        super(sprava);
    }

    /**
     * Vytvori vynimku so spravou a pricinou z parametrov.
     * @param sprava opis chyby
     * @param pricina povodna vynimka
     */
    public PerzistenciaException(String sprava, Throwable pricina) {
        super(sprava, pricina);
    }
}
