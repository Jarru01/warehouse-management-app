package sk.uniza.fri.sklad.pracaSoSuborom;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sk.uniza.fri.sklad.Sklad;
import sk.uniza.fri.sklad.TestData;
import sk.uniza.fri.sklad.osoby.Pracovnik;
import sk.uniza.fri.sklad.predmety.Tovar;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PerzistenciaTest {

    @TempDir
    Path tempDir;

    @Test
    void ulozenySkladSaNacitaVTomIstomStave() {
        Path subor = this.tempDir.resolve("sklad.dat");
        Sklad sklad = new Sklad();
        Tovar tovar = TestData.tovar("42");
        sklad.getPrijemTovaru().getRegal().ulozTovar(tovar);
        sklad.getRiaditelSkladu().prijmiPracovnika(new Pracovnik("Peter", "Novy", sklad, "5"));

        new ZapisovacSuboru(subor).zapis(sklad);
        Sklad nacitany = new CitacSuboru(subor).nacitaj();

        assertEquals("42", nacitany.getPrijemTovaru().getRegal().getTovar("42").getId());
        assertTrue(nacitany.getZoznamPracovnikov().containsKey("5"));
        assertTrue(nacitany.getSkladTovaru().vypisZoznamuPracovnikov().contains("ID: 5"));
    }

    @Test
    void chybajuciSuborVytvoriNovySkladASubor() {
        Path subor = this.tempDir.resolve("neexistuje.dat");

        Sklad sklad = new CitacSuboru(subor).nacitaj();

        assertNotNull(sklad);
        assertTrue(Files.exists(subor));
    }

    @Test
    void poskodenySuborSaZalohujeANacitaSaNovySklad() throws IOException {
        Path subor = this.tempDir.resolve("sklad.dat");
        Files.writeString(subor, "toto nie je serializovany sklad");

        Sklad sklad = new CitacSuboru(subor).nacitaj();

        assertNotNull(sklad);
        try (Stream<Path> files = Files.list(this.tempDir)) {
            assertTrue(files.anyMatch(cesta -> cesta.getFileName().toString().startsWith("sklad.dat.korumpovany-")));
        }
    }
}
