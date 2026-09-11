package sk.uniza.fri.sklad;

import org.junit.jupiter.api.Test;
import sk.uniza.fri.sklad.miestnosti.MalySklad;
import sk.uniza.fri.sklad.miestnosti.VelkySklad;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkladTest {

    @Test
    void novySkladMaTriMiestnosti() {
        Sklad sklad = new Sklad();

        assertEquals(3, sklad.getZoznamMiestnosti().size());
        assertTrue(sklad.getMiestnost(Sklad.MIESTNOST_SKLAD_TOVARU) instanceof VelkySklad);
        assertTrue(sklad.getMiestnost(Sklad.MIESTNOST_PRIJEM_TOVARU) instanceof MalySklad);
        assertTrue(sklad.getMiestnost(Sklad.MIESTNOST_VYDAJ_TOVARU) instanceof MalySklad);
        assertNull(sklad.getMiestnost("neexistuje"));
    }

    @Test
    void typovanePristupyVracajuSpravneMiestnosti() {
        Sklad sklad = new Sklad();

        assertNotNull(sklad.getSkladTovaru());
        assertNotNull(sklad.getPrijemTovaru());
        assertNotNull(sklad.getVydajTovaru());
    }

    @Test
    void novySkladNemaPracovnikovAMaRiaditela() {
        Sklad sklad = new Sklad();

        assertTrue(sklad.getZoznamPracovnikov().isEmpty());
        assertNotNull(sklad.getRiaditelSkladu());
        assertEquals(Sklad.PREDVOLENE_ID_RIADITELA, sklad.getRiaditelSkladu().getId());
    }
}
