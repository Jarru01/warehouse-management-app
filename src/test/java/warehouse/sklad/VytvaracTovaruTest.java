package warehouse.sklad;

import org.junit.jupiter.api.Test;
import warehouse.sklad.osoby.Zakaznik;
import warehouse.sklad.predmety.Tovar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VytvaracTovaruTest {

    @Test
    void vytvoriTovarZKonzolovychVstupov() {
        Sklad sklad = new Sklad();
        Zakaznik odosielatel = TestData.zakaznik();
        VytvaracTovaru vytvarac = new VytvaracTovaru(sklad,
                TestPomocnik.vstup("100", "Tovar100", "500", "20", "Peter", "Novak"));

        Tovar tovar = vytvarac.vytvorTovar(odosielatel);

        assertNotNull(tovar);
        assertEquals("100", tovar.getId());
        assertEquals("20", tovar.getPrijemca().getId());
        assertEquals("Peter", tovar.getPrijemca().getMeno());
        assertEquals("Novak", tovar.getPrijemca().getPriezvisko());
    }

    @Test
    void ukonceniePrerusiTvorbu() {
        Sklad sklad = new Sklad();
        VytvaracTovaru vytvarac = new VytvaracTovaru(sklad, TestPomocnik.vstup("ukonci"));

        assertNull(vytvarac.vytvorTovar(TestData.zakaznik()));
    }

    @Test
    void duplicitneIdTovarNevytvori() {
        Sklad sklad = new Sklad();
        sklad.getPrijemTovaru().getRegal().ulozTovar(TestData.tovar("100"));
        VytvaracTovaru vytvarac = new VytvaracTovaru(sklad, TestPomocnik.vstup("100"));
        Tovar[] vysledok = new Tovar[1];

        String vystup = TestPomocnik.zachytVystup(() -> vysledok[0] = vytvarac.vytvorTovar(TestData.zakaznik()));

        assertNull(vysledok[0]);
        assertTrue(vystup.contains("uz existuje"));
    }

    @Test
    void plnyPrijemTovarNevytvori() {
        Sklad sklad = new Sklad();
        for (int i = 1; i <= 5; i++) {
            assertTrue(sklad.getPrijemTovaru().getRegal().ulozTovar(TestData.tovar(String.valueOf(i))));
        }
        VytvaracTovaru vytvarac = new VytvaracTovaru(sklad, TestPomocnik.vstup("100"));
        Tovar[] vysledok = new Tovar[1];

        String vystup = TestPomocnik.zachytVystup(() -> vysledok[0] = vytvarac.vytvorTovar(TestData.zakaznik()));

        assertNull(vysledok[0]);
        assertTrue(vystup.contains("plny"));
    }
}
