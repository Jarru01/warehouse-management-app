package sk.uniza.fri.sklad.pracaSoSuborom;

import sk.uniza.fri.sklad.Sklad;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Trieda ZapisovacSuboru sluzi k ulozeniu beziacej konfiguracie skladu do suboru.
 * @author Juraj
 */
public class ZapisovacSuboru {
    public static final String PREDVOLENY_SUBOR = "sklad.dat";

    private final Path subor;

    /**
     * Vytvori zapisovac pracujuci s predvolenym suborom skladu.
     */
    public ZapisovacSuboru() {
        this(Paths.get(PREDVOLENY_SUBOR));
    }

    /**
     * Vytvori zapisovac pracujuci so suborom z parametra.
     * @param subor cesta k suboru skladu
     */
    public ZapisovacSuboru(Path subor) {
        this.subor = subor;
    }

    /**
     * Ulozi konfiguraciu skladu z parametra do suboru. Zapis prebieha do docasneho suboru, ktory sa nasledne
     * atomicky presunie na cielove miesto, aby sa predslo poskodeniu ulozenych dat.
     * @param sklad sklad na ulozenie
     */
    public void zapis(Sklad sklad) {
        Path docasnySubor = this.subor.resolveSibling(this.subor.getFileName() + ".tmp");
        try (ObjectOutputStream vystup = new ObjectOutputStream(Files.newOutputStream(docasnySubor))) {
            vystup.writeObject(sklad);
        } catch (IOException e) {
            System.err.println("Ulozenie skladu zlyhalo: " + e.getMessage());
            return;
        }

        try {
            Files.move(docasnySubor, this.subor, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            this.presunBezAtomiky(docasnySubor);
        } catch (IOException e) {
            System.err.println("Ulozenie skladu zlyhalo: " + e.getMessage());
        }
    }

    /**
     * Presunie docasny subor na cielove miesto bez atomickej operacie.
     * @param docasnySubor docasny subor s ulozenym skladom
     */
    private void presunBezAtomiky(Path docasnySubor) {
        try {
            Files.move(docasnySubor, this.subor, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.err.println("Ulozenie skladu zlyhalo: " + e.getMessage());
        }
    }
}
