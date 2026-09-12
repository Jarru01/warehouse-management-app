package warehouse.sklad.terminaly;

import java.util.Scanner;

/**
 * Trieda Vstup obaluje zdielany scanner konzoly, aby vsetky terminaly a vytvarac tovaru citaili z jedneho zdroja.
 * @author Juraj
 */
public class Vstup {
    private final Scanner scanner;

    /**
     * Vytvori vstup so scannerom z parametra.
     * @param scanner scanner konzoly
     */
    public Vstup(Scanner scanner) {
        this.scanner = scanner;
    }

    /**
     * Vypise vyzvu a nacita dalsi vstup od pouzivatela.
     * @return nacitany vstup
     */
    public String nacitaj() {
        System.out.print("> ");
        return this.scanner.next();
    }
}
