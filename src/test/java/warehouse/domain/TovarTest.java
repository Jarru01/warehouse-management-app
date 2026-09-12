package warehouse.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TovarTest {

    @Test
    void ulozenieDoRegaluNastaviSlot() {
        Regal regal = new Regal(new VelkySklad("test"), 3);
        Tovar tovar = tovar("100");

        tovar.ulozDoRegalu(regal, 2);

        assertSame(regal, tovar.getRegal());
        assertEquals(2, tovar.getSlot());
    }

    @Test
    void neplatnySlotSaOdmietne() {
        Regal regal = new Regal(new VelkySklad("test"), 3);
        Tovar tovar = tovar("100");

        assertThrows(NeplatnaOperaciaException.class, () -> tovar.ulozDoRegalu(regal, 0));
        assertThrows(NeplatnaOperaciaException.class, () -> tovar.ulozDoRegalu(regal, 4));
    }

    @Test
    void ulozenieBezRegaluSaOdmietne() {
        assertThrows(NeplatnaOperaciaException.class, () -> tovar("100").ulozDoRegalu(null, 1));
    }

    @Test
    void druheUlozenieSaOdmietne() {
        Regal regal = new Regal(new VelkySklad("test"), 3);
        Tovar tovar = tovar("100");
        tovar.ulozDoRegalu(regal, 1);

        assertThrows(NeplatnaOperaciaException.class, () -> tovar.ulozDoRegalu(regal, 2));
    }

    @Test
    void vyberZRegaluJeIdempotentny() {
        Tovar tovar = tovar("100");

        tovar.vyberZRegalu();

        assertNull(tovar.getRegal());
        assertNull(tovar.getSlot());
    }

    private static Tovar tovar(String id) {
        ZakaznikInfo zakaznik = new ZakaznikInfo("10", "Jan", "Novy");
        return new Tovar(id, "Tovar" + id, 100, zakaznik, zakaznik);
    }
}
