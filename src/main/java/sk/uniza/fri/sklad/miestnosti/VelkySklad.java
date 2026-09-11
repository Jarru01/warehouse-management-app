package sk.uniza.fri.sklad.miestnosti;

import sk.uniza.fri.sklad.predmety.Regal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * VelkySklad je potomok Miestnoti, ktory obsahuje zoznam regalov s tovarom, ktory je mozne upravovat.
 * @author Juraj
 */
public class VelkySklad extends Miestnost implements ISkladovaMiestnost {
    private static final long serialVersionUID = 2L;

    private final List<Regal> zoznamRegalov;   //zoznam regalov v miestnosti

    /**
     * Vytvori velky sklad bez regalov s popisom z parametra.
     * @param popisMiestnosti popis miestnosti
     */
    public VelkySklad(String popisMiestnosti) {
        super(popisMiestnosti);
        this.zoznamRegalov = new ArrayList<>();
    }

    /**
     * Vrati prvy volny regal v miestnosti alebo null, ak volny nie je.
     * @return volny regal alebo null
     */
    @Override
    public Regal getRegal() {
        for (Regal regal : this.zoznamRegalov) {
            if (regal.maVolneMiesto()) {
                return regal;
            }
        }
        return null;
    }

    /**
     * Vrati regal s danym indexom z parametra.
     * @param index index regalu (cislovanie od 1)
     * @return regal alebo null, ak index neexistuje
     */
    public Regal getRegal(int index) {
        if (index > 0 && index <= this.zoznamRegalov.size()) {
            return this.zoznamRegalov.get(index - 1);
        }
        return null;
    }

    /**
     * Prida novy regal s danou kapacitou z parametra.
     * @param kapacita kapacita regala
     * @return true ak bol regal pridany
     */
    public boolean pridajRegal(int kapacita) {
        if (kapacita <= 0) {
            return false;
        }
        this.zoznamRegalov.add(new Regal(kapacita));
        return true;
    }

    /**
     * Odstrani regal s danym indexom z parametra, ak je prazdny.
     * @param index index regalu (cislovanie od 1)
     * @return true ak bol regal odstraneny
     */
    public boolean odstranRegal(int index) {
        if (index <= 0 || index > this.zoznamRegalov.size()) {
            return false;
        }
        if (!this.zoznamRegalov.get(index - 1).jePrazdny()) {
            return false;
        }
        this.zoznamRegalov.remove(index - 1);
        return true;
    }

    /**
     * Vrati nemodifikovatelny zoznam regalov v miestnosti.
     * @return zoznam regalov
     */
    public List<Regal> getZoznamRegalov() {
        return Collections.unmodifiableList(this.zoznamRegalov);
    }

    /**
     * Vrati string obsahujuci informacie o regaloch v miestnosti a tovaru v nich.
     * @return string obsahujuci informacie o regaloch v miestnosti a tovaru v nich
     */
    @Override
    public String vypisZoznamuRegalov() {
        StringBuilder zoznam = new StringBuilder();
        if (this.zoznamRegalov.isEmpty()) {
            zoznam.append("V miestnosti sa nenachadzaju regale.");
        }
        for (int i = 0; i < this.zoznamRegalov.size(); i++) {
            zoznam.append(i + 1).append(". ").append(this.zoznamRegalov.get(i)).append('\n');
        }
        return "Vypis regalov v miestnosti " + this.getPopisMiestnosti() + ":" + '\n'
                + zoznam.toString().indent(2);
    }
}
