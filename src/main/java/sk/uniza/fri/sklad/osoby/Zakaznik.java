package sk.uniza.fri.sklad.osoby;

import sk.uniza.fri.sklad.IIdentifikovatelny;
import sk.uniza.fri.sklad.miestnosti.MalySklad;
import sk.uniza.fri.sklad.predmety.Tovar;

/**
 * Zakaznik je potomok Osoby, ktory obsahuje dalsie atributy a metody specificke pre zakaznika.
 * @author Juraj
 */
public class Zakaznik extends Osoba implements IIdentifikovatelny {
    private final String id;                //id zakaznika
    private final MalySklad prijemTovaru;   //miestnost na prijem tovaru
    private final MalySklad vydajTovaru;    //miestnost na vydaj tovaru

    /**
     * Vytvara zakaznika s ID, menom, priezivskom, prijmomTovaru a vydajomTovaru z parametra.
     * @param id id zakaznika
     * @param meno meno zakaznika
     * @param priezvisko priezvisko zakaznika
     * @param prijemTovaru miestnost na prijem tovaru
     * @param vydajTovaru miestnost na vydaj tovaru
     */
    public Zakaznik(String id, String meno, String priezvisko, MalySklad prijemTovaru, MalySklad vydajTovaru) {
        super(meno, priezvisko);
        this.id = id;
        this.prijemTovaru = prijemTovaru;
        this.vydajTovaru = vydajTovaru;
    }

    /**
     * Vracia id zakaznika.
     * @return id
     */
    @Override
    public String getId() {
        return this.id;
    }

    /**
     * Ulozi tovar z parametra do miestnosti urcenej na prijem tovaru.
     * @param tovar tovar na ulozenie
     * @return true ak bol tovar ulozeny
     */
    public boolean ulozTovar(Tovar tovar) {
        return this.prijemTovaru.getRegal().ulozTovar(tovar);
    }

    /**
     * Vyzdvihne tovar z parametra z miestnosti urcenej na vydaj tovaru.
     * @param tovar tovar na vyzdvihnutie
     * @return true ak bol tovar vyzdvihnuty
     */
    public boolean vyzdvihniTovar(Tovar tovar) {
        return this.vydajTovaru.getRegal().odoberTovar(tovar.getId());
    }
}
