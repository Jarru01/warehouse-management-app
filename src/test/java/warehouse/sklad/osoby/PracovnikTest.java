package warehouse.sklad.osoby;

import org.junit.jupiter.api.Test;
import warehouse.sklad.Sklad;
import warehouse.sklad.TestData;
import warehouse.sklad.miestnosti.MalySklad;
import warehouse.sklad.predmety.Tovar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PracovnikTest {

    private Sklad sklad;
    private Pracovnik pracovnik;

    private void pripravPracovnika() {
        this.sklad = new Sklad();
        this.pracovnik = new Pracovnik("Adam", "Testovaci", this.sklad, "1");
        this.sklad.getRiaditelSkladu().prijmiPracovnika(this.pracovnik);
    }

    @Test
    void novyPracovnikJeVHlavnomSklade() {
        this.pripravPracovnika();

        assertEquals(Sklad.MIESTNOST_SKLAD_TOVARU, this.pracovnik.getAktualnaMiestnost().getPopisMiestnosti());
        assertTrue(this.vypisPracovnikov(Sklad.MIESTNOST_SKLAD_TOVARU).contains("ID: 1"));
    }

    @Test
    void presunMedziMiestnostamiAktualizujeObidveMiestnosti() {
        this.pripravPracovnika();

        assertTrue(this.pracovnik.chodDoMiestnosti(Sklad.MIESTNOST_PRIJEM_TOVARU));

        assertEquals(Sklad.MIESTNOST_PRIJEM_TOVARU, this.pracovnik.getAktualnaMiestnost().getPopisMiestnosti());
        assertFalse(this.vypisPracovnikov(Sklad.MIESTNOST_SKLAD_TOVARU).contains("ID: 1"));
        assertTrue(this.vypisPracovnikov(Sklad.MIESTNOST_PRIJEM_TOVARU).contains("ID: 1"));
    }

    @Test
    void presunDoNeznamejMiestnostiZlyha() {
        this.pripravPracovnika();

        assertFalse(this.pracovnik.chodDoMiestnosti("neexistuje"));
    }

    @Test
    void zobranieTovaruZRegaluAVratenie() {
        this.pripravPracovnika();
        MalySklad prijemTovaru = this.sklad.getPrijemTovaru();
        Tovar tovar = TestData.tovar("7");
        assertTrue(prijemTovaru.getRegal().ulozTovar(tovar));
        this.pracovnik.chodDoMiestnosti(Sklad.MIESTNOST_PRIJEM_TOVARU);

        assertTrue(this.pracovnik.zoberTovar("7"));
        assertTrue(this.pracovnik.mamTovar());
        assertNull(prijemTovaru.getRegal().getTovar("7"));

        assertTrue(this.pracovnik.ulozTovar());

        assertFalse(this.pracovnik.mamTovar());
        assertSame(tovar, prijemTovaru.getRegal().getTovar("7"));
    }

    @Test
    void pracovnikSoPlnymiRukamiNezoberieDalsiTovar() {
        this.pripravPracovnika();
        MalySklad prijemTovaru = this.sklad.getPrijemTovaru();
        prijemTovaru.getRegal().ulozTovar(TestData.tovar("1"));
        prijemTovaru.getRegal().ulozTovar(TestData.tovar("2"));
        this.pracovnik.chodDoMiestnosti(Sklad.MIESTNOST_PRIJEM_TOVARU);

        assertTrue(this.pracovnik.zoberTovar("1"));
        assertFalse(this.pracovnik.zoberTovar("2"));

        assertTrue(this.pracovnik.mamTovar());
        assertNull(prijemTovaru.getRegal().getTovar("1"));
        assertNotNull(prijemTovaru.getRegal().getTovar("2"));
    }

    private String vypisPracovnikov(String kluc) {
        return this.sklad.getMiestnost(kluc).vypisZoznamuPracovnikov();
    }
}
