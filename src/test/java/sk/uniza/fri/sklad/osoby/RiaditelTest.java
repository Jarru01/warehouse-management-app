package sk.uniza.fri.sklad.osoby;

import org.junit.jupiter.api.Test;
import sk.uniza.fri.sklad.Sklad;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RiaditelTest {

    @Test
    void prijatyPracovnikJeVZoznameAjVHlavnomSklade() {
        Sklad sklad = new Sklad();
        Pracovnik pracovnik = new Pracovnik("Peter", "Novy", sklad, "5");

        assertTrue(sklad.getRiaditelSkladu().prijmiPracovnika(pracovnik));

        assertTrue(sklad.getZoznamPracovnikov().containsKey("5"));
        assertTrue(sklad.getSkladTovaru().vypisZoznamuPracovnikov().contains("ID: 5"));
    }

    @Test
    void prijatieExistujucehoIdZlyha() {
        Sklad sklad = new Sklad();
        sklad.getRiaditelSkladu().prijmiPracovnika(new Pracovnik("Peter", "Novy", sklad, "5"));

        boolean prijaty = sklad.getRiaditelSkladu().prijmiPracovnika(new Pracovnik("Iny", "Pracovnik", sklad, "5"));

        assertFalse(prijaty);
    }

    @Test
    void vylucenyPracovnikZmizneZZoznamuAjZMiestnosti() {
        Sklad sklad = new Sklad();
        Pracovnik pracovnik = new Pracovnik("Peter", "Novy", sklad, "5");
        sklad.getRiaditelSkladu().prijmiPracovnika(pracovnik);

        assertTrue(sklad.getRiaditelSkladu().vylucPracovnika("5"));

        assertFalse(sklad.getZoznamPracovnikov().containsKey("5"));
        assertFalse(sklad.getSkladTovaru().vypisZoznamuPracovnikov().contains("ID: 5"));
    }

    @Test
    void vylucenieNeznamehoIdZlyha() {
        Sklad sklad = new Sklad();

        assertFalse(sklad.getRiaditelSkladu().vylucPracovnika("99"));
    }
}
