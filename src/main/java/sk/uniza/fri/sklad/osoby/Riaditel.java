package sk.uniza.fri.sklad.osoby;

import sk.uniza.fri.sklad.IIdentifikovatelny;
import sk.uniza.fri.sklad.Sklad;
import sk.uniza.fri.sklad.miestnosti.ISkladovaMiestnost;
import sk.uniza.fri.sklad.miestnosti.Miestnost;

import java.util.Map;

/**
 * Riaditel je potomok Osoby, ktory obsahuje dalsie atributy a metody specificke pre riaditela, ako napriklad prijatie
 * alebo vylucenie pracovnika.
 * @author Juraj
 */
public class Riaditel extends Osoba implements IIdentifikovatelny {
    private final Sklad sklad;   //sklad ktory riaditel spravuje
    private final String id;     //id riaditela

    /**
     * Vytvara riaditela s menom, priezivskom, skladom a ID z parametra.
     * @param meno meno riaditela
     * @param priezvisko priezvisko riaditela
     * @param sklad sklad ktory riaditel spravuje
     * @param id id riaditela
     */
    public Riaditel(String meno, String priezvisko, Sklad sklad, String id) {
        super(meno, priezvisko);
        this.sklad = sklad;
        this.id = id;
    }

    /**
     * Vrati id riaditela.
     * @return id
     */
    @Override
    public String getId() {
        return this.id;
    }

    /**
     * Vrati zoznam pracovnikov skladu.
     * @return zoznam pracovnikov
     */
    public Map<String, Pracovnik> getZoznamPracovnikov() {
        return this.sklad.getZoznamPracovnikov();
    }

    /**
     * Vrati string so zoznamom pracovnikov skladu vo vsetkych miestnostiach.
     * @return string so zoznamom pracovnikov
     */
    public String vypisZoznamPracovnikov() {
        if (this.sklad.getZoznamPracovnikov().isEmpty()) {
            return "Sklad nema pracovnikov.";
        }
        StringBuilder vypis = new StringBuilder();
        for (Miestnost miestnost : this.sklad.getZoznamMiestnosti().values()) {
            vypis.append(miestnost.vypisZoznamuPracovnikov()).append('\n');
        }
        return vypis.toString();
    }

    /**
     * Vrati string so zoznamom tovaru vo vsetkych miestnostiach.
     * @return string so zoznamom tovaru
     */
    public String vypisTovar() {
        StringBuilder vypis = new StringBuilder();
        for (Miestnost miestnost : this.sklad.getZoznamMiestnosti().values()) {
            vypis.append(((ISkladovaMiestnost)miestnost).vypisZoznamuRegalov()).append('\n');
        }
        return vypis.toString();
    }

    /**
     * Prida regal s danou kapacitou do velkeho skladu.
     * @param kapacita kapacita regalu
     * @return true ak bol regal pridany
     */
    public boolean pridajRegal(int kapacita) {
        return this.sklad.getSkladTovaru().pridajRegal(kapacita);
    }

    /**
     * Odstrani regal s danym indexom z velkeho skladu, ak je prazdny.
     * @param index index regalu
     * @return true ak bol regal odstraneny
     */
    public boolean odstranRegal(int index) {
        return this.sklad.getSkladTovaru().odstranRegal(index);
    }

    /**
     * Prijme noveho pracovnika skladu, umiestni ho do velkeho skladu.
     * @param pracovnik pracovnik na prijatie
     * @return true ak bol pracovnik prijaty
     */
    public boolean prijmiPracovnika(Pracovnik pracovnik) {
        return this.sklad.pridajPracovnika(pracovnik);
    }

    /**
     * Vyluci pracovnika so zadanym id. Ak pracovnik drzi tovar, najskor sa ho pokusi ulozit.
     * @param id id pracovnika
     * @return true ak bol pracovnik vyluceny
     */
    public boolean vylucPracovnika(String id) {
        Pracovnik pracovnik = this.sklad.getZoznamPracovnikov().get(id);
        if (pracovnik == null) {
            return false;
        }
        if (pracovnik.mamTovar() && !pracovnik.ulozTovar()) {
            return false;
        }
        this.sklad.odoberPracovnika(id);
        return true;
    }
}
