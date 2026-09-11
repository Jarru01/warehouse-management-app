package sk.uniza.fri.sklad.osoby;

import org.junit.jupiter.api.Test;
import sk.uniza.fri.sklad.TestData;
import sk.uniza.fri.sklad.predmety.Tovar;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ZakaznikTest {

    @Test
    void zakaznikMaId() {
        assertEquals("1", TestData.zakaznik().getId());
    }

    @Test
    void tovarPoznaIdSvojhoPrijemcu() {
        Tovar tovar = TestData.tovar("9");

        assertEquals("1", tovar.getPrijemca().getId());
    }
}
