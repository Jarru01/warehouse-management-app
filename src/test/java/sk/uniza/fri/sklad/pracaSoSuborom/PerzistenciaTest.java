package sk.uniza.fri.sklad.pracaSoSuborom;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sk.uniza.fri.sklad.Sklad;
import sk.uniza.fri.sklad.TestData;
import sk.uniza.fri.sklad.osoby.Pracovnik;
import sk.uniza.fri.sklad.predmety.Tovar;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

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
        assertTrue(sklad.getPrijemTovaru().getRegal().ulozTovar(TestData.tovar("42")));
        Pracovnik pracovnik = new Pracovnik("Peter", "Novy", sklad, "5");
        assertTrue(sklad.getRiaditelSkladu().prijmiPracovnika(pracovnik));
        assertTrue(pracovnik.chodDoMiestnosti(Sklad.MIESTNOST_PRIJEM_TOVARU));
        assertTrue(pracovnik.zoberTovar("42"));

        new ZapisovacSuboru(subor).zapis(sklad);
        Sklad nacitany = new CitacSuboru(subor).nacitaj();

        assertEquals("Juraj", nacitany.getRiaditelSkladu().getMeno());
        assertEquals(Sklad.PREDVOLENE_ID_RIADITELA, nacitany.getRiaditelSkladu().getId());
        assertEquals(3, nacitany.getZoznamMiestnosti().size());
        Pracovnik nacitanyPracovnik = nacitany.getZoznamPracovnikov().get("5");
        assertNotNull(nacitanyPracovnik);
        assertEquals("Peter", nacitanyPracovnik.getMeno());
        assertEquals(Sklad.MIESTNOST_PRIJEM_TOVARU,
                nacitanyPracovnik.getAktualnaMiestnost().getPopisMiestnosti());
        assertNotNull(nacitanyPracovnik.getAktualnyTovar());
        assertEquals("42", nacitanyPracovnik.getAktualnyTovar().getId());
        assertEquals("Tovar42", nacitanyPracovnik.getAktualnyTovar().getNazovTovaru());
        assertEquals(100, nacitanyPracovnik.getAktualnyTovar().getVaha());
    }

    @Test
    void regaleATovarSaObnovia() {
        Path subor = this.tempDir.resolve("sklad.dat");
        Sklad sklad = new Sklad();
        assertTrue(sklad.getSkladTovaru().pridajRegal(10));
        assertTrue(sklad.getSkladTovaru().getRegal(1).ulozTovar(TestData.tovar("7")));

        new ZapisovacSuboru(subor).zapis(sklad);
        Sklad nacitany = new CitacSuboru(subor).nacitaj();

        assertEquals(1, nacitany.getSkladTovaru().getZoznamRegalov().size());
        assertEquals(10, nacitany.getSkladTovaru().getRegal(1).getKapacita());
        assertNotNull(nacitany.getSkladTovaru().getRegal(1).getTovar("7"));
    }

    @Test
    void ulozenySuborJeCitatelnyJson() throws IOException {
        Path subor = this.tempDir.resolve("sklad.dat");

        new ZapisovacSuboru(subor).zapis(new Sklad());
        String obsah = Files.readString(subor, StandardCharsets.UTF_8);

        assertTrue(obsah.contains("\"verzia\""), obsah);
        assertTrue(obsah.contains("\"riaditel\""), obsah);
        assertTrue(obsah.contains(Sklad.MIESTNOST_SKLAD_TOVARU), obsah);
    }

    @Test
    void chybajuciSuborVytvoriNovySkladASubor() {
        Path subor = this.tempDir.resolve("neexistuje.dat");

        Sklad sklad = new CitacSuboru(subor).nacitaj();

        assertNotNull(sklad);
        assertTrue(Files.exists(subor));
    }

    @Test
    void poskodenySuborSaOdstraniANacitaSaNovySklad() throws IOException {
        Path subor = this.tempDir.resolve("sklad.dat");
        Files.writeString(subor, "toto nie je JSON");

        Sklad sklad = new CitacSuboru(subor).nacitaj();

        assertNotNull(sklad);
        assertTrue(sklad.getZoznamPracovnikov().isEmpty());
        assertTrue(Files.readString(subor, StandardCharsets.UTF_8).contains("\"verzia\""));
    }

    @Test
    void staryBinarnySuborSaOdstraniANacitaSaNovySklad() throws IOException {
        Path subor = this.tempDir.resolve("sklad.dat");
        Files.write(subor, new byte[]{(byte) 0xAC, (byte) 0xED, 0x00, 0x05, 0x74, 0x00});

        Sklad sklad = new CitacSuboru(subor).nacitaj();

        assertNotNull(sklad);
        assertTrue(Files.readString(subor, StandardCharsets.UTF_8).contains("\"verzia\""));
    }

    @Test
    void nepodporovanaVerziaSaOdstraniANacitaSaNovySklad() throws IOException {
        Path subor = this.tempDir.resolve("sklad.dat");
        Files.writeString(subor, "{\"verzia\": 99}");

        Sklad sklad = new CitacSuboru(subor).nacitaj();

        assertNotNull(sklad);
        assertTrue(Files.readString(subor, StandardCharsets.UTF_8).contains("\"verzia\""));
    }
}
