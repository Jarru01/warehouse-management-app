package sk.uniza.fri.sklad.pracaSoSuborom;

import com.fasterxml.jackson.databind.ObjectMapper;
import sk.uniza.fri.sklad.Sklad;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Trieda CitacSuboru sluzi k nacitaniu konfiguracie skladu z JSON suboru.
 * @author Juraj
 */
public class CitacSuboru {
    private final Path subor;
    private final ObjectMapper mapper;

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
        this.mapper = new ObjectMapper();
    }

    /**
     * Pokusi sa nacitat konfiguraciu skladu zo suboru. Ak subor neexistuje, vytvori novy sklad s pociatocnou
     * konfiguraciou a ulozi ho. Ak je subor necitatelny alebo neplatny, odstrani ho a vrati novy sklad.
     * @return nacitany alebo novy sklad
     */
    public Sklad nacitaj() {
        if (!Files.exists(this.subor)) {
            Sklad novySklad = new Sklad();
            new ZapisovacSuboru(this.subor).zapis(novySklad);
            return novySklad;
        }
        try {
            SkladData data = this.mapper.readValue(this.subor.toFile(), SkladData.class);
            return SkladMapper.naSklad(data);
        } catch (IOException | PerzistenciaException e) {
            System.err.println("Subor skladu sa nepodarilo nacitat: " + e.getMessage());
            this.odstranNeplatnySubor();
            System.err.println("Subor bol odstraneny, spustam novy sklad.");
            Sklad novySklad = new Sklad();
            new ZapisovacSuboru(this.subor).zapis(novySklad);
            return novySklad;
        }
    }

    /**
     * Odstrani neplatny subor skladu.
     */
    private void odstranNeplatnySubor() {
        try {
            Files.deleteIfExists(this.subor);
        } catch (IOException e) {
            System.err.println("Subor skladu sa nepodarilo odstranit: " + e.getMessage());
        }
    }
}
