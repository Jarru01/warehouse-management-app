package warehouse;

import warehouse.sklad.Sklad;
import warehouse.sklad.pracaSoSuborom.CitacSuboru;
import warehouse.sklad.terminaly.Vstup;

import java.util.Scanner;

/**
 * Trieda main a jej metoda sluzi na spustenie programu.
 */
public class Main {

    /**
     * Zo suboru sa nacita ulozeny sklad a vypise sa uvodne menu.
     * @param args argumenty
     */
    public static void main(String[] args) {
        Sklad sklad = new CitacSuboru().nacitaj();
        Vstup vstup = new Vstup(new Scanner(System.in));
        sklad.spusti(vstup);
    }
}
