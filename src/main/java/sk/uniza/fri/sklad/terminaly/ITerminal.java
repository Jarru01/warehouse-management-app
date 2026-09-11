package sk.uniza.fri.sklad.terminaly;

/**
 * Interface ITerminal implementuju triedy ktore sluzia na interakciu s pouzivatelom.
 * @author Juraj
 */
public interface ITerminal {
    /**
     * Sluzi na nacitanie vstupu zadaneho do terminalu.
     * @return vstup
     */
    String nacitajVstup();

    /**
     * Prihlasi pouzivatela a spusti cyklus spracovania prikazov, kym sa neodhlasi.
     */
    void spustiTerminal();

    /**
     * Ukonci aktualny terminal a vrati pouzivatela do hlavneho menu.
     */
    void zatvorTerminal();

    /**
     * Spracuje jeden cyklus menu s moznostami, ktore ma pouzivatel k dispozicii.
     */
    void obsluzMenu();
}
