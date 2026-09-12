package warehouse.sklad.terminaly;

import warehouse.sklad.Sklad;
import warehouse.sklad.Validacia;
import warehouse.sklad.VytvaracTovaru;
import warehouse.sklad.miestnosti.MalySklad;
import warehouse.sklad.osoby.Zakaznik;
import warehouse.sklad.predmety.Tovar;

import java.util.function.Predicate;

/**
 *  Terminal zakaznika sluzi na citanie vstupu z terminalu a nasledne spracovanie tohto vstupu. Terminal pracuje
 *  so zakaznikom ktory sa do neho prihlasil, ktory vykonava zadane prikazy.
 * @author Juraj
 */
public class TerminalZakaznika implements ITerminal {

    private final Sklad sklad;       //sklad s ktorym terminal pracuje
    private final Vstup vstup;       //zdielany vstup konzoly
    private Zakaznik zakaznik;       //prihlaseny zakaznik
    private boolean aktivny;         //ci je terminal otvoreny

    /**
     * Nastavi pociatocne hodnoty atributov a naplni atributy z parametrov.
     * @param sklad sklad s ktorym terminal pracuje
     * @param vstup zdielany vstup konzoly
     */
    public TerminalZakaznika(Sklad sklad, Vstup vstup) {
        this.sklad = sklad;
        this.vstup = vstup;
        this.zakaznik = null;
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
     * Poziada zakaznika o ID, meno a priezvisko, vytvori zakaznika a spracuva jeho prikazy, kym sa neodhlasi.
     * Vstup "ukonci" prerusuje prihlasenie.
     */
    @Override
    public void spustiTerminal() {
        String id = this.nacitajText("Zadaj prosim svoje ID:", "Zadal si neplatne ID.", Validacia::jeCislo);
        if (id == null) {
            return;
        }
        String meno = this.nacitajText("Zadaj prosim svoje meno:", "Zadal si neplatne meno.", Validacia::jeText);
        if (meno == null) {
            return;
        }
        String priezvisko = this.nacitajText("Zadaj prosim svoje priezvisko:", "Zadal si neplatne priezvisko.", Validacia::jeText);
        if (priezvisko == null) {
            return;
        }

        this.zakaznik = new Zakaznik(id, meno, priezvisko, this.sklad.getPrijemTovaru(), this.sklad.getVydajTovaru());
        this.aktivny = true;
        while (this.aktivny) {
            this.obsluzMenu();
        }
        this.sklad.ulozZmeny();
    }

    /**
     * Nacita textovy vstup a opakuje vyzvu, kym nie je zadany platny text. Vstup "ukonci" prerusuje nacitavanie.
     * @param vyzva vyzva zobrazena pouzivatelovi
     * @param chybovaSprava sprava zobrazena pri neplatnom vstupe
     * @param validacia pravidlo platnosti vstupu
     * @return platny text alebo null pri ukonceni
     */
    private String nacitajText(String vyzva, String chybovaSprava, Predicate<String> validacia) {
        while (true) {
            System.out.println(vyzva);
            String vstup = this.nacitajVstup();
            if (vstup.equals("ukonci")) {
                return null;
            }
            if (validacia.test(vstup)) {
                return vstup;
            }
            System.out.println(chybovaSprava);
        }
    }

    /**
     * Uvita prihlaseneho zakaznika, vypise zoznam moznych prikazov a spracuje zvoleny prikaz.
     */
    @Override
    public void obsluzMenu() {
        System.out.println("\n Vitaj " + this.zakaznik.getMeno() + ". Vyber prosim cinnost, ktoru chces vykonat:");
        System.out.println("-1- Uloz tovar");
        System.out.println("-2- Vyzdvihni tovar");
        System.out.println("Pre odhlasenie zadaj lubovolny iny vstup.");

        switch (this.nacitajVstup()) {
            case "1" -> this.ulozTovar();
            case "2" -> this.vyzdvihniTovar();
            default -> this.zatvorTerminal();
        }
        this.sklad.ulozZmeny();
    }

    /**
     * Zatvori terminal zakaznika.
     */
    @Override
    public void zatvorTerminal() {
        this.aktivny = false;
    }

    /**
     * Vytvori novy tovar pomocou konzolovych vyziev a pokusi sa ho ulozit v sklade.
     */
    private void ulozTovar() {
        System.out.println();
        VytvaracTovaru vytvarac = new VytvaracTovaru(this.sklad, this.vstup);
        Tovar tovar = vytvarac.vytvorTovar(this.zakaznik);
        if (tovar != null && this.zakaznik.ulozTovar(tovar)) {
            System.out.println("Tovar bol ulozeny.");
        }
    }

    /**
     * Zakaznik zada id tovaru, ktory chce vyzdvihnut. Ak sa tovar nachadza v miestnosti na vydaj tovaru a patri
     * prihlasenemu zakaznikovi, tovar vyzdvihne.
     */
    private void vyzdvihniTovar() {
        MalySklad vydajTovaru = this.sklad.getVydajTovaru();
        System.out.println();
        System.out.println("Zadaj id tovaru:");
        String id = this.nacitajVstup();
        if (!Validacia.jeCislo(id)) {
            System.out.println("Zadal si neplatne ID.");
            return;
        }
        Tovar tovar = vydajTovaru.getRegal().getTovar(id);
        if (tovar == null) {
            System.out.println("Tovar nebol najdeny.");
            return;
        }
        if (!tovar.getPrijemca().getId().equals(this.zakaznik.getId())) {
            System.out.println("Tovar ktory chces vyzdvihnut ti nepatri.");
            return;
        }
        if (this.zakaznik.vyzdvihniTovar(tovar)) {
            System.out.println("Tovar bol vyzdvihnuty.");
        }
    }
}
