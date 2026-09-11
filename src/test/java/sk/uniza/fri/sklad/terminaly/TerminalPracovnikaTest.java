package sk.uniza.fri.sklad.terminaly;

import org.junit.jupiter.api.Test;
import sk.uniza.fri.sklad.Sklad;
import sk.uniza.fri.sklad.TestPomocnik;
import sk.uniza.fri.sklad.osoby.Pracovnik;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TerminalPracovnikaTest {

    @Test
    void pracovnikSaPrihlasiAPresunieSaDoMiestnosti() {
        Sklad sklad = new Sklad();
        sklad.getRiaditelSkladu().prijmiPracovnika(new Pracovnik("Peter", "Novak", sklad, "5"));
        TerminalPracovnika terminal = new TerminalPracovnika(sklad,
                TestPomocnik.vstup("5", "1", "2", "x"));

        String vystup = TestPomocnik.zachytVystup(terminal::spustiTerminal);

        assertTrue(vystup.contains("Vitaj Peter"));
        assertTrue(vystup.contains("Presiel si do miestnosti prijemTovaru."));
        assertEquals(Sklad.MIESTNOST_PRIJEM_TOVARU,
                sklad.getZoznamPracovnikov().get("5").getAktualnaMiestnost().getPopisMiestnosti());
    }
}
