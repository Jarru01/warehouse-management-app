package warehouse.sklad.terminaly;

import warehouse.sklad.Sklad;
import warehouse.sklad.Validacia;
import warehouse.sklad.osoby.Pracovnik;
import warehouse.sklad.osoby.Riaditel;

/**
 * Terminal riaditela sluzi na citanie vstupu z terminalu a nasledne spracovanie tohto vstupu. Terminal pracuje
 * s riaditelom ktory sa do neho prihlasil, ktory vykonava zadane prikazy.
 * @author Juraj
 */
public class TerminalRiaditela implements ITerminal {

    private final Sklad sklad;          //sklad s ktorym terminal pracuje
    private final Vstup vstup;          //zdielany vstup konzoly
    private Riaditel riaditel;          //prihlaseny riaditel
    private int pocetPokusov;           //pocet pokusov na prihlasenie
    private boolean aktivny;            //ci je terminal otvoreny

    /**
     * Nastavi pociatocne hodnoty atributov a naplni atributy z parametrov.
     * @param sklad sklad s ktorym terminal pracuje
     * @param vstup zdielany vstup konzoly
     */
    public TerminalRiaditela(Sklad sklad, Vstup vstup) {
        this.sklad = sklad;
        this.riaditel = null;
        this.vstup = vstup;
        this.pocetPokusov = 0;
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
     * Poziada riaditela o identifikaciu a po uspesnom prihlaseni spracuva jeho prikazy, kym sa neodhlasi.
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
     * Poziada riaditela o ID. Neplatny vstup je mozne zadat najviac trikrat.
     * @return true ak sa prihlasenie podarilo
     */
    private boolean prihlas() {
        this.pocetPokusov = 0;
        this.riaditel = null;
        while (this.riaditel == null && this.pocetPokusov < 3) {
            System.out.println("\nZadaj prosim svoje ID: ");
            String id = this.nacitajVstup();
            if (this.sklad.getRiaditelSkladu().getId().equals(id)) {
                this.riaditel = this.sklad.getRiaditelSkladu();
            } else {
                this.pocetPokusov++;
                System.out.println("Zadal si nespravne ID.");
            }
        }

        if (this.riaditel == null) {
            System.out.println("Zadal si prilis vela neplatnych pokusov o prihlasenie.");
            return false;
        }
        return true;
    }

    /**
     * Uvita prihlaseneho riaditela, vypise zoznam moznych prikazov a spracuje zvoleny prikaz.
     */
    @Override
    public void obsluzMenu() {
        System.out.println("\nVitaj " + this.riaditel.getMeno() + ". Vyber prosim cinnost, ktoru chces vykonat:");
        System.out.println("-1- Vypis pracovnikov");
        System.out.println("-2- Vypis tovar");
        System.out.println("-3- Pridaj regal");
        System.out.println("-4- Odstran regal");
        System.out.println("-5- Prijmi pracovnika");
        System.out.println("-6- Vyluc pracovnika");
        System.out.println("Pre odhlasenie zadaj lubovolny iny vstup.");

        switch (this.nacitajVstup()) {
            case "1" -> System.out.println(this.riaditel.vypisZoznamPracovnikov());
            case "2" -> System.out.println(this.riaditel.vypisTovar());
            case "3" -> this.pridajRegal();
            case "4" -> this.odstranRegal();
            case "5" -> this.prijmiPracovnika();
            case "6" -> this.vylucPracovnika();
            default -> this.zatvorTerminal();
        }
        this.sklad.ulozZmeny();
    }

    /**
     * Zatvori terminal riaditela.
     */
    @Override
    public void zatvorTerminal() {
        this.aktivny = false;
    }

    /**
     * Vyluci pracovnika so zadanym ID, ak existuje a da sa mu ulozit drzany tovar.
     */
    private void vylucPracovnika() {
        System.out.println();
        if (this.riaditel.getZoznamPracovnikov().isEmpty()) {
            System.out.println("Sklad nema pracovnikov.");
            return;
        }
        System.out.println("Zadaj ID pracovnika:");
        String id = this.nacitajVstup();
        if (!Validacia.jeCislo(id)) {
            System.out.println("Zadal si neplatne ID.");
            return;
        }
        if (!this.riaditel.getZoznamPracovnikov().containsKey(id)) {
            System.out.println("Pracovnik so zadanym ID neexistuje.");
            return;
        }
        if (this.riaditel.vylucPracovnika(id)) {
            System.out.println("Pracovnik bol vyluceny.");
        } else {
            System.out.println("Pracovnik nema kde ulozit tovar.");
        }
    }

    /**
     * Prijme pracovnika s ID, menom a priezviskom zadanymi riaditelom.
     */
    private void prijmiPracovnika() {
        System.out.println();
        System.out.println("Zadaj ID pracovnika:");
        String id = this.nacitajVstup();
        if (!Validacia.jeCislo(id)) {
            System.out.println("Zadal si neplatne ID.");
            return;
        }
        if (this.riaditel.getZoznamPracovnikov().containsKey(id)) {
            System.out.println("Pracovnik so zadanym ID uz existuje.");
            return;
        }
        System.out.println("Zadaj meno pracovnika:");
        String meno = this.nacitajVstup();
        if (!Validacia.jeText(meno)) {
            System.out.println("Zadal si neplatne meno.");
            return;
        }
        System.out.println("Zadaj priezvisko pracovnika:");
        String priezvisko = this.nacitajVstup();
        if (!Validacia.jeText(priezvisko)) {
            System.out.println("Zadal si neplatne priezvisko.");
            return;
        }
        if (this.riaditel.prijmiPracovnika(new Pracovnik(meno, priezvisko, this.sklad, id))) {
            System.out.println("Pracovnik bol uspesne prijaty.");
        }
    }

    /**
     * Prida regal do velkeho skladu s kapacitou zadanou riaditelom.
     */
    private void pridajRegal() {
        System.out.println();
        System.out.println("Zadaj kapacitu regala(max 20):");
        String kapacita = this.nacitajVstup();
        if (!Validacia.jeCislo(kapacita)) {
            System.out.println("Zadal si neplatny vstup.");
            return;
        }
        int cislo = Integer.parseInt(kapacita);
        if (cislo < 1 || cislo > 20) {
            System.out.println("Zadal si neplatnu kapacitu regala.");
            return;
        }
        if (this.riaditel.pridajRegal(cislo)) {
            System.out.println("Regal bol uspesne pridany.");
        } else {
            System.out.println("Regal sa nepodarilo pridat.");
        }
    }

    /**
     * Odstrani regal s indexom zadanym riaditelom z velkeho skladu.
     */
    private void odstranRegal() {
        System.out.println();
        System.out.println("Zadaj index regalu:");
        String index = this.nacitajVstup();
        if (!Validacia.jeCislo(index)) {
            System.out.println("Zadal si neplatny vstup.");
            return;
        }
        if (this.riaditel.odstranRegal(Integer.parseInt(index))) {
            System.out.println("Regal bol uspesne odstraneny.");
        } else {
            System.out.println("Regal neexistuje alebo nie je prazdny.");
        }
    }
}
