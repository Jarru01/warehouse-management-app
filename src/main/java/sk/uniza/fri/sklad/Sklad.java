package sk.uniza.fri.sklad;

import sk.uniza.fri.sklad.miestnosti.MalySklad;
import sk.uniza.fri.sklad.miestnosti.Miestnost;
import sk.uniza.fri.sklad.miestnosti.VelkySklad;
import sk.uniza.fri.sklad.osoby.Pracovnik;
import sk.uniza.fri.sklad.osoby.Riaditel;
import sk.uniza.fri.sklad.pracaSoSuborom.ZapisovacSuboru;
import sk.uniza.fri.sklad.terminaly.TerminalPracovnika;
import sk.uniza.fri.sklad.terminaly.TerminalRiaditela;
import sk.uniza.fri.sklad.terminaly.TerminalZakaznika;
import sk.uniza.fri.sklad.terminaly.Vstup;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Trieda Sklad vytvara zakladnu strukturu skladu (miestnosti, zoznam pracovnikov, riaditela skladu), spusta
 * interakciu s pouzivatelom a obsahuje metodu na ulozenie konfiguracie skladu do suboru.
 * @author Juraj
 */
@SuppressWarnings("serial")
public class Sklad implements Serializable {
    private static final long serialVersionUID = 2L;

    public static final String PREDVOLENE_ID_RIADITELA = "123";
    public static final String MIESTNOST_SKLAD_TOVARU = "skladTovaru";
    public static final String MIESTNOST_PRIJEM_TOVARU = "prijemTovaru";
    public static final String MIESTNOST_VYDAJ_TOVARU = "vydajTovaru";

    private final Map<String, Miestnost> zoznamMiestnosti;      //zoznam miestnosti skladu
    private final Map<String, Pracovnik> zoznamPracovnikov;     //zoznam pracovnikov skladu
    private final Riaditel riaditelSkladu;                      //riaditel skladu

    /**
     * Vytvori prazdny zoznam pracovnikov, naplni zoznam miestnosti s miestnostami skladu a vytvori riaditela.
     */
    @SuppressWarnings("this-escape")
    public Sklad() {
        this.zoznamMiestnosti = new HashMap<>();
        this.zoznamPracovnikov = new HashMap<>();
        this.riaditelSkladu = new Riaditel("Juraj", "Solensky", this, PREDVOLENE_ID_RIADITELA);

        this.zoznamMiestnosti.put(MIESTNOST_SKLAD_TOVARU, new VelkySklad(MIESTNOST_SKLAD_TOVARU));
        this.zoznamMiestnosti.put(MIESTNOST_PRIJEM_TOVARU, new MalySklad(MIESTNOST_PRIJEM_TOVARU));
        this.zoznamMiestnosti.put(MIESTNOST_VYDAJ_TOVARU, new MalySklad(MIESTNOST_VYDAJ_TOVARU));
    }

    /**
     * Spusti hlavny cyklus programu. Menu sa zobrazuje, kym pouzivatel nezada ukoncenie.
     * @param vstup zdielany vstup konzoly
     */
    public void spusti(Vstup vstup) {
        while (this.vyberPouzivatela(vstup)) {
            // cyklus pokracuje, kym pouzivatel neukonci program
        }
        System.out.println("Program bol ukonceny.");
    }

    /**
     * Vypise uvodne menu a spusti terminal zvolenej role. Po odhlaseni sa vrati do hlavneho cyklu.
     * @param vstup zdielany vstup konzoly
     * @return true ak sa ma hlavne menu zobrazit znovu
     */
    public boolean vyberPouzivatela(Vstup vstup) {
        System.out.println(
                """

                                           _____         _    \s
                                          |_   _|       | |   \s
                         _____ __ ___   __ _| | ___  ___| |__ \s
                        |_  / '_ ` _ \\ / _` | |/ _ \\/ __| '_ \\\s
                         / /| | | | | | (_| | |  __/ (__| | | |
                        /___|_| |_| |_|\\__,_\\_/\\___|\\___|_| |_|""");

        System.out.println("Vyber prosim, za koho sa chces prihlasit do systemu:");
        System.out.println("-1- Pracovnik");
        System.out.println("-2- Zakaznik");
        System.out.println("-3- Riaditel");
        System.out.println("Pre ukoncenie zadaj lubovolny iny vstup.");

        switch (vstup.nacitaj()) {
            case "1" -> new TerminalPracovnika(this, vstup).spustiTerminal();
            case "2" -> new TerminalZakaznika(this, vstup).spustiTerminal();
            case "3" -> new TerminalRiaditela(this, vstup).spustiTerminal();
            default -> {
                return false;
            }
        }
        return true;
    }

    /**
     * Vracia nemodifikovatelny zoznam miestnosti skladu.
     * @return zoznam miestnosti
     */
    public Map<String, Miestnost> getZoznamMiestnosti() {
        return Collections.unmodifiableMap(this.zoznamMiestnosti);
    }

    /**
     * Vracia nemodifikovatelny zoznam pracovnikov skladu.
     * @return zoznam pracovnikov
     */
    public Map<String, Pracovnik> getZoznamPracovnikov() {
        return Collections.unmodifiableMap(this.zoznamPracovnikov);
    }

    /**
     * Vracia riaditela skladu.
     * @return riaditel skladu
     */
    public Riaditel getRiaditelSkladu() {
        return this.riaditelSkladu;
    }

    /**
     * Vracia miestnost s danym klucom alebo null, ak neexistuje.
     * @param kluc kluc miestnosti
     * @return miestnost alebo null
     */
    public Miestnost getMiestnost(String kluc) {
        return this.zoznamMiestnosti.get(kluc);
    }

    /**
     * Vracia velky sklad tovaru.
     * @return velky sklad tovaru
     */
    public VelkySklad getSkladTovaru() {
        return (VelkySklad)this.zoznamMiestnosti.get(MIESTNOST_SKLAD_TOVARU);
    }

    /**
     * Vracia miestnost na prijem tovaru.
     * @return miestnost na prijem tovaru
     */
    public MalySklad getPrijemTovaru() {
        return (MalySklad)this.zoznamMiestnosti.get(MIESTNOST_PRIJEM_TOVARU);
    }

    /**
     * Vracia miestnost na vydaj tovaru.
     * @return miestnost na vydaj tovaru
     */
    public MalySklad getVydajTovaru() {
        return (MalySklad)this.zoznamMiestnosti.get(MIESTNOST_VYDAJ_TOVARU);
    }

    /**
     * Prida pracovnika do zoznamu skladu a umiestni ho do hlavneho skladu tovaru.
     * @param pracovnik pracovnik na pridanie
     * @return true ak bol pracovnik pridany
     */
    public boolean pridajPracovnika(Pracovnik pracovnik) {
        if (pracovnik == null || this.zoznamPracovnikov.containsKey(pracovnik.getId())) {
            return false;
        }
        this.zoznamPracovnikov.put(pracovnik.getId(), pracovnik);
        pracovnik.premiestniDo(this.getSkladTovaru());
        return true;
    }

    /**
     * Odoberie pracovnika zo zoznamu skladu a z miestnosti, v ktorej sa nachadza.
     * @param id id pracovnika
     * @return odobrany pracovnik alebo null
     */
    public Pracovnik odoberPracovnika(String id) {
        Pracovnik pracovnik = this.zoznamPracovnikov.remove(id);
        if (pracovnik != null) {
            pracovnik.opustiMiestnost();
        }
        return pracovnik;
    }

    /**
     * Ulozi do suboru aktualnu konfiguraciu skladu.
     */
    public void ulozZmeny() {
        new ZapisovacSuboru().zapis(this);
    }
}
