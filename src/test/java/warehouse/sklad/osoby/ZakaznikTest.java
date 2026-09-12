package warehouse.sklad.osoby;

import org.junit.jupiter.api.Test;
import warehouse.sklad.TestData;
import warehouse.sklad.predmety.Tovar;

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
