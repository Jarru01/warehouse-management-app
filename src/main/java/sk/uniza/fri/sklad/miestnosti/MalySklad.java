package sk.uniza.fri.sklad.miestnosti;

import sk.uniza.fri.sklad.predmety.Regal;

/**
 * MalySklad je potomok Miestnoti, ktory obsahuje regal s fixnou kapacitou.
 * @author Juraj
 */
public class MalySklad extends Miestnost implements ISkladovaMiestnost {
    private final Regal regal;  //regal na ukladanie tovaru

    /**
     * Vytvori maly sklad s popisom miestnosti z parametra, inicializuje atribut regal s fixnou kapacitou.
     * @param popisMiestnosti popis miestnosti
     */
    public MalySklad(String popisMiestnosti) {
        super(popisMiestnosti);
        this.regal = new Regal(5);
    }

    /**
     * Vrati regal v miestnosti.
     * @return regal
     */
    @Override
    public Regal getRegal() {
        return this.regal;
    }

    /**
     * Vrati string obsahujuci zoznam regalov a tovaru v miestnosti.
     * @return string obsahujuci zoznam regalov a tovaru v miestnosti
     */
    @Override
    public String vypisZoznamuRegalov() {
        String out = "Vypis regalov v miestnosti " + this.getPopisMiestnosti() + ":" + '\n';
        String zoznam = 1 + ". " + this.regal;
        zoznam = zoznam.indent(2);
        out = out + zoznam;
        return out;
    }
}
