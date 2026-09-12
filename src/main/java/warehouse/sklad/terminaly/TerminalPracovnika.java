package warehouse.sklad.terminaly;

import warehouse.sklad.Sklad;
import warehouse.sklad.Validacia;
import warehouse.sklad.miestnosti.VelkySklad;
import warehouse.sklad.osoby.Pracovnik;
import warehouse.sklad.predmety.Tovar;

/**
 *  Terminal pracovnika sluzi na citanie vstupu z terminalu a nasledne spracovanie tohto vstupu. Terminal pracuje
 *  s pracovnikom ktory sa do neho prihlasil, ktory vykonava zadane prikazy.
 * @author Juraj
 */
public class TerminalPracovnika implements ITerminal {

    private final Sklad sklad;              //sklad s ktorym terminal pracuje
    private final Vstup vstup;              //zdielany vstup konzoly
    private Pracovnik aktualnyPracovnik;    //prihlaseny pracovnik
    private int pocetPokusov;               //pocet pokusov na prihlasenie
    private boolean aktivny;                //ci je terminal otvoreny

    /**
     * Nastavi pociatocne hodnoty atributov a naplni atributy z parametrov.
     * @param sklad sklad s ktorym terminal pracuje
     * @param vstup zdielany vstup konzoly
     */
    public TerminalPracovnika(Sklad sklad, Vstup vstup) {
        this.pocetPokusov = 0;
        this.sklad = sklad;
        this.aktualnyPracovnik = null;
        this.vstup = vstup;
        this.aktivny = false;
    }

    /**
     * Nacita vstup z terminalu.
     * @return vstup
     */
    @Override
    public String nacitajVstup() {
        return this.vstup.nacitaj();
    }

    /**
     * Poziada pracovnika o identifikaciu a po uspesnom prihlaseni spracuva jeho prikazy, kym sa neodhlasi.
     */
    @Override
    public void spustiTerminal() {
        if (!this.prihlas()) {
            return;
        }
        this.aktivny = true;
        while (this.aktivny) {
            this.obsluzMenu();
        }
        this.sklad.ulozZmeny();
    }

    /**
     * Poziada pracovnika o ID. Neplatny vstup je mozne zadat najviac trikrat.
     * @return true ak sa prihlasenie podarilo
     */
    private boolean prihlas() {
        if (this.sklad.getZoznamPracovnikov().isEmpty()) {
            System.out.println("Sklad nema pracovnikov.");
            return false;
        }

        this.pocetPokusov = 0;
        this.aktualnyPracovnik = null;
        while (this.aktualnyPracovnik == null && this.pocetPokusov < 3) {
            System.out.println("\nZadaj prosim svoje ID: ");
            String id = this.nacitajVstup();
            this.aktualnyPracovnik = this.sklad.getZoznamPracovnikov().get(id);
            if (this.aktualnyPracovnik == null) {
                this.pocetPokusov++;
                System.out.println("Pracovnik so zadanym ID neexistuje.");
            }
        }

        if (this.aktualnyPracovnik == null) {
            System.out.println("Zadal si prilis vela neplatnych pokusov o prihlasenie.");
            return false;
        }
        return true;
    }

    /**
     * Uvita prihlaseneho pracovnika, vypise zoznam moznych prikazov a spracuje zvoleny prikaz.
     */
    @Override
    public void obsluzMenu() {
        System.out.println("\nVitaj " + this.aktualnyPracovnik.getMeno() + ".");
        System.out.println("Prave sa nachadzas v miestnosti " +
                this.aktualnyPracovnik.getAktualnaMiestnost().getPopisMiestnosti() + ". Vyber cinnost, ktoru chces vykonat:");
        System.out.println("-1- Chod do miestnosti");
        System.out.println("-2- Zober tovar");
        System.out.println("-3- Uloz tovar");
        System.out.println("-4- Vypis tovar v miestnosti");
        System.out.println("-5- Vypis moj tovar");
        System.out.println("Pre odhlasenie zadaj lubovolny iny vstup.");

        switch (this.nacitajVstup()) {
            case "1" -> this.chodDoMiestnosti();
            case "2" -> this.zoberTovar();
            case "3" -> this.ulozTovar();
            case "4" -> System.out.println(this.aktualnyPracovnik.zoznamRegalovAktualnejMiestnosti());
            case "5" -> this.vypisMojTovar();
            default -> this.odhlasit();
        }
        this.sklad.ulozZmeny();
    }

    /**
     * Odhlasi pracovnika, ak nedrzi ziaden tovar. Inak ho vyzve na ulozenie tovaru.
     */
    private void odhlasit() {
        if (this.aktualnyPracovnik.mamTovar()) {
            System.out.println("Najskor musim ulozit tovar.");
        } else {
            this.zatvorTerminal();
        }
    }

    /**
     * Zatvori terminal pracovnika.
     */
    @Override
    public void zatvorTerminal() {
        this.aktivny = false;
    }

    /**
     * Ak je pracovnik vo velkom sklade, vyziada si index regalu a ID tovaru. Ak je v malom sklade, vyziada si iba ID
     * tovaru. Nasledne sa pokusi tovar zobrat.
     */
    private void zoberTovar() {
        System.out.println();
        if (this.aktualnyPracovnik.mamTovar()) {
            System.out.println("Mam plne ruky.");
            return;
        }

        if (this.aktualnyPracovnik.getAktualnaMiestnost() instanceof VelkySklad) {
            System.out.println("Zadaj cislo regalu: ");
            String cisloRegalu = this.nacitajVstup();
            if (!Validacia.jeCislo(cisloRegalu)) {
                System.out.println("Zadal si neplatny vstup.");
                return;
            }
            int cislo = Integer.parseInt(cisloRegalu);
            if (cislo < 1 || cislo > ((VelkySklad)this.aktualnyPracovnik.getAktualnaMiestnost()).getZoznamRegalov().size()) {
                System.out.println("Zadal si cislo neexistujuceho regalu.");
                return;
            }
            System.out.println("Zadaj ID tovaru: ");
            String id = this.nacitajVstup();
            if (!this.aktualnyPracovnik.zoberTovar(id, cislo)) {
                System.out.println("Tovar nebol najdeny.");
            }
        } else {
            System.out.println("Zadaj ID tovaru: ");
            String id = this.nacitajVstup();
            if (!this.aktualnyPracovnik.zoberTovar(id)) {
                System.out.println("Tovar nebol najdeny.");
            }
        }
    }

    /**
     * Pokusi sa ulozit drzany tovar do aktualnej miestnosti.
     */
    private void ulozTovar() {
        System.out.println();
        if (!this.aktualnyPracovnik.mamTovar()) {
            System.out.println("Nemam ziaden tovar.");
            return;
        }
        if (this.aktualnyPracovnik.ulozTovar()) {
            System.out.println("Tovar bol ulozeny.");
        } else {
            System.out.println("Tovar sa nepodarilo ulozit.");
        }
    }

    /**
     * Vypise informacie o tovare, ktory pracovnik drzi.
     */
    private void vypisMojTovar() {
        System.out.println();
        Tovar tovar = this.aktualnyPracovnik.getAktualnyTovar();
        if (tovar == null) {
            System.out.println("Nemam ziaden tovar.");
        } else {
            System.out.println("Moj aktualny tovar:");
            System.out.println(tovar);
        }
    }

    /**
     * Vypise menu s moznymi miestnostami, poziada o vstup a presunie pracovnika do vybranej miestnosti.
     */
    private void chodDoMiestnosti() {
        System.out.println();
        System.out.println("Vyber miestnost do ktorej chces ist: ");
        System.out.println("-1- Sklad tovaru");
        System.out.println("-2- Prijem tovaru");
        System.out.println("-3- Vydaj tovaru");
        System.out.println("Pre zrusenie zadaj lubovolny iny vstup.");

        switch (this.nacitajVstup()) {
            case "1" -> this.presun(Sklad.MIESTNOST_SKLAD_TOVARU);
            case "2" -> this.presun(Sklad.MIESTNOST_PRIJEM_TOVARU);
            case "3" -> this.presun(Sklad.MIESTNOST_VYDAJ_TOVARU);
            default -> {
                // zrusenie vyberu
            }
        }
    }

    /**
     * Presunie pracovnika do miestnosti s danym klucom a vypise vysledok.
     * @param kluc kluc cielovej miestnosti
     */
    private void presun(String kluc) {
        if (this.aktualnyPracovnik.chodDoMiestnosti(kluc)) {
            System.out.println("Presiel si do miestnosti "
                    + this.aktualnyPracovnik.getAktualnaMiestnost().getPopisMiestnosti() + ".");
        } else {
            System.out.println("Miestnost neexistuje.");
        }
    }
}
