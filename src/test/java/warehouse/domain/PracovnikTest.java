package warehouse.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PracovnikTest {

    @Test
    void pracovnikJeOsobaSDedicnymiUdajmi() {
        Pracovnik pracovnik = new Pracovnik("1", "Peter", "Novak");

        assertInstanceOf(Osoba.class, pracovnik);
        assertEquals("Peter", pracovnik.getMeno());
        assertEquals("Novak", pracovnik.getPriezvisko());
    }

    @Test
    void uchopenieAPolozenieTovaru() {
        Pracovnik pracovnik = new Pracovnik("1", "Peter", "Novak");

        pracovnik.uchop(tovar("100"));
        assertTrue(pracovnik.drziTovar());

        pracovnik.poloz();
        assertFalse(pracovnik.drziTovar());
    }

    @Test
    void pracovnikNemozeDrzatDvaTovary() {
        Pracovnik pracovnik = new Pracovnik("1", "Peter", "Novak");
        pracovnik.uchop(tovar("100"));

        assertThrows(NeplatnaOperaciaException.class, () -> pracovnik.uchop(tovar("200")));
    }

    @Test
    void uchopenieNulovehoTovaruZlyha() {
        Pracovnik pracovnik = new Pracovnik("1", "Peter", "Novak");

        assertThrows(NeplatnaOperaciaException.class, () -> pracovnik.uchop(null));
    }

    private static Tovar tovar(String id) {
        ZakaznikInfo zakaznik = new ZakaznikInfo("10", "Jan", "Novy");
        return new Tovar(id, "Tovar" + id, 100, zakaznik, zakaznik);
    }
}
