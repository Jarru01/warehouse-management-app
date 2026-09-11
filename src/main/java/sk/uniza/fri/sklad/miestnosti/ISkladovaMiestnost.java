package sk.uniza.fri.sklad.miestnosti;

import sk.uniza.fri.sklad.predmety.Regal;

/**
 * Interface ktory implementuju miestnosti, v ktorych sa nachadzaju regale.
 * @author Juraj
 */
public interface ISkladovaMiestnost {
    /**
     * Vrati volny regal v miestnosti.
     * @return regal
     */
    Regal getRegal();

    /**
     * Vrati string obsahujuci zoznam regalov a ich tovar v danej miestnosti.
     * @return string obsahujuci zoznam regalov a ich tovar v danej miestnosti
     */
    String vypisZoznamuRegalov();
}
