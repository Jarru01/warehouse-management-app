package sk.uniza.fri.sklad.pracaSoSuborom;

import sk.uniza.fri.sklad.Sklad;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Trieda CitacSuboru sluzi k nacitaniu konfiguracie skladu zo suboru, v ktorom je serializovana.
 * @author Juraj
 */
public class CitacSuboru {
    private static final DateTimeFormatter FORMAT_CASU = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final Path subor;

    /**
     * Vytvori citac pracujuci s predvolenym suborom skladu.
     */
    public CitacSuboru() {
        this(Paths.get(ZapisovacSuboru.PREDVOLENY_SUBOR));
    }

    /**
     * Vytvori citac pracujuci so suborom z parametra.
     * @param subor cesta k suboru skladu
     */
    public CitacSuboru(Path subor) {
        this.subor = subor;
    }

    /**
     * Pokusi sa nacitat konfiguraciu skladu zo suboru. Ak subor neexistuje, vytvori novy sklad s pociatocnou
     * konfiguraciou a ulozi ho. Ak je subor poskodeny, zazalohuje ho a vrati novy sklad.
     * @return nacitany alebo novy sklad
     */
    public Sklad nacitaj() {
        if (!Files.exists(this.subor)) {
            Sklad novySklad = new Sklad();
            new ZapisovacSuboru(this.subor).zapis(novySklad);
            return novySklad;
        }
        try (ObjectInputStream vstup = new ObjectInputStream(Files.newInputStream(this.subor))) {
            return (Sklad)vstup.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Ulozeny sklad sa nepodarilo nacitat: " + e.getMessage());
            this.zalohujPoskodenySubor();
            System.err.println("Poskodeny subor bol zalohovany, spustam novy sklad.");
            return new Sklad();
        }
    }

    /**
     * Presunie poskodeny subor do zalohy s casovou znamkou.
     */
    private void zalohujPoskodenySubor() {
        String cas = LocalDateTime.now().format(FORMAT_CASU);
        Path zaloha = this.subor.resolveSibling(this.subor.getFileName() + ".korumpovany-" + cas);
        try {
            Files.move(this.subor, zaloha, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.err.println("Zalohovanie poskodeneho suboru zlyhalo: " + e.getMessage());
        }
    }
}
