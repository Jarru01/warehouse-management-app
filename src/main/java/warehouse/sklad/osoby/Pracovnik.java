package warehouse.sklad.osoby;

import warehouse.sklad.IIdentifikovatelny;
import warehouse.sklad.Sklad;
import warehouse.sklad.miestnosti.ISkladovaMiestnost;
import warehouse.sklad.miestnosti.MalySklad;
import warehouse.sklad.miestnosti.Miestnost;
import warehouse.sklad.miestnosti.VelkySklad;
import warehouse.sklad.predmety.Regal;
import warehouse.sklad.predmety.Tovar;

/**
 * Pracovnik je potomok Osoby, ktory obsahuje dalsie atributy a metody specificke pre pracovnika. Pracovnik sa vie
 * pohybovat po sklade a interagovat s regalmi/tovarom.
 * @author Juraj
 */
public class Pracovnik extends Osoba implements IIdentifikovatelny {
    private final Sklad sklad;              //sklad v ktorom pracovnik pracuje
    private final String id;                //id pracovnika
    private Miestnost aktualnaMiestnost;    //miestnost v ktorej sa pracovnik nachadza
    private Tovar aktualnyTovar;            //tovar ktory pracovnik drzi

    /**
     * Vytvara pracovnika s menom, priezivskom, skladom a ID z parametra. Pracovnik nie je umiestneny v ziadnej
     * miestnosti, kym ho riaditel neprijme.
     * @param meno meno pracovnika
     * @param priezvisko priezvisko pracovnika
     * @param sklad sklad v ktorom pracovnik pracuje
     * @param id id pracovnika
     */
    public Pracovnik(String meno, String priezvisko, Sklad sklad, String id) {
        super(meno, priezvisko);
        this.sklad = sklad;
        this.id = id;
        this.aktualnaMiestnost = null;
        this.aktualnyTovar = null;
    }

    /**
     * Vracia id pracovnika.
     * @return id
     */
    @Override
    public String getId() {
        return this.id;
    }

    /**
     * Vracia aktualnu miestnost v ktorej sa pracovnik nachadza.
     * @return aktualna miestnost
     */
    public Miestnost getAktualnaMiestnost() {
        return this.aktualnaMiestnost;
    }

    /**
     * Vracia tovar ktory pracovnik drzi alebo null.
     * @return aktualny tovar
     */
    public Tovar getAktualnyTovar() {
        return this.aktualnyTovar;
    }

    /**
     * Pyta sa pracovnika ci drzi tovar. Ak ano, vrati true.
     * @return boolean
     */
    public boolean mamTovar() {
        return this.aktualnyTovar != null;
    }

    /**
     * Presunie pracovnika do miestnosti s danym klucom.
     * @param kluc kluc cielovej miestnosti
     * @return true ak sa presun podaril
     */
    public boolean chodDoMiestnosti(String kluc) {
        Miestnost ciel = this.sklad.getMiestnost(kluc);
        if (ciel == null) {
            return false;
        }
        this.premiestniDo(ciel);
        return true;
    }

    /**
     * Odoberie pracovnika z aktualnej miestnosti a umiestni ho do miestnosti z parametra.
     * @param miestnost miestnost do ktorej ma pracovnik ist
     */
    public void premiestniDo(Miestnost miestnost) {
        if (miestnost == null) {
            return;
        }
        if (this.aktualnaMiestnost != null) {
            this.aktualnaMiestnost.odoberPracovnika(this);
        }
        this.aktualnaMiestnost = miestnost;
        this.aktualnaMiestnost.pridajPracovnika(this);
    }

    /**
     * Odoberie pracovnika z aktualnej miestnosti.
     */
    public void opustiMiestnost() {
        if (this.aktualnaMiestnost != null) {
            this.aktualnaMiestnost.odoberPracovnika(this);
            this.aktualnaMiestnost = null;
        }
    }

    /**
     * Obnovi stav pracovnika pri nacitani zo suboru. Urcene vyhradne pre perzistenciu.
     * @param miestnost miestnost, v ktorej sa pracovnik nachadza
     * @param drzanyTovar tovar, ktory pracovnik drzi, alebo null
     */
    public void obnovStav(Miestnost miestnost, Tovar drzanyTovar) {
        this.premiestniDo(miestnost);
        this.aktualnyTovar = drzanyTovar;
    }

    /**
     * Ak je pracovnik v malom sklade, pokusi sa zobrat tovar so zadanym id z regalu.
     * @param id id tovaru
     * @return true ak pracovnik tovar zobral
     */
    public boolean zoberTovar(String id) {
        if (this.aktualnyTovar != null) {
            return false;
        }
        if (this.aktualnaMiestnost instanceof MalySklad) {
            Regal regal = ((ISkladovaMiestnost)this.aktualnaMiestnost).getRegal();
            Tovar tovar = regal.getTovar(id);
            if (tovar != null && regal.odoberTovar(id)) {
                this.aktualnyTovar = tovar;
                return true;
            }
        }
        return false;
    }

    /**
     * Ak je pracovnik vo velkom sklade, pokusi sa zobrat tovar s id zo zadaneho regalu.
     * @param id id tovaru
     * @param indexRegalu index regalu
     * @return true ak pracovnik tovar zobral
     */
    public boolean zoberTovar(String id, int indexRegalu) {
        if (this.aktualnyTovar != null) {
            return false;
        }
        if (this.aktualnaMiestnost instanceof VelkySklad) {
            Regal regal = ((VelkySklad)this.aktualnaMiestnost).getRegal(indexRegalu);
            if (regal == null) {
                return false;
            }
            Tovar tovar = regal.getTovar(id);
            if (tovar != null && regal.odoberTovar(id)) {
                this.aktualnyTovar = tovar;
                return true;
            }
        }
        return false;
    }

    /**
     * Pokusi sa ulozit drzany tovar do volneho regalu v aktualnej miestnosti.
     * @return true ak sa tovar podarilo ulozit
     */
    public boolean ulozTovar() {
        if (this.aktualnyTovar == null || !(this.aktualnaMiestnost instanceof ISkladovaMiestnost)) {
            return false;
        }
        Regal regal = ((ISkladovaMiestnost)this.aktualnaMiestnost).getRegal();
        if (regal == null || !regal.ulozTovar(this.aktualnyTovar)) {
            return false;
        }
        this.aktualnyTovar = null;
        return true;
    }

    /**
     * Vrati string so zoznamom regalov a tovaru v aktualnej miestnosti.
     * @return zoznam regalov v aktualnej miestnosti
     */
    public String zoznamRegalovAktualnejMiestnosti() {
        return ((ISkladovaMiestnost)this.aktualnaMiestnost).vypisZoznamuRegalov();
    }

    /**
     * Vrati string s udajmi o pracovnikovi.
     * @return string s udajmi o pracovnikovi
     */
    @Override
    public String toString() {
        return "[Meno: " + this.getMeno() + ", " + "Priezvisko: " + this.getPriezvisko() + ", " + "ID: " + this.getId()
                + "]";
    }
}
