package sk.uniza.fri.sklad.miestnosti;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MalySkladTest {

    @Test
    void malySkladMaFixnuKapacituRegala() {
        MalySklad sklad = new MalySklad("prijemTovaru");

        assertNotNull(sklad.getRegal());
        assertEquals(5, sklad.getRegal().getZoznamTovaru().length);
    }

    @Test
    void popisMiestnostiSaVrati() {
        MalySklad sklad = new MalySklad("prijemTovaru");

        assertEquals("prijemTovaru", sklad.getPopisMiestnosti());
    }

    @Test
    void vypisZoznamuRegalovObsahujePopisMiestnosti() {
        MalySklad sklad = new MalySklad("prijemTovaru");

        assertTrue(sklad.vypisZoznamuRegalov().contains("prijemTovaru"));
    }
}
