package warehouse.sklad.terminaly;

import org.junit.jupiter.api.Test;
import warehouse.sklad.Sklad;
import warehouse.sklad.TestPomocnik;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TerminalRiaditelaTest {

    @Test
    void riaditelSaPrihlasiAPrijmePracovnika() {
        Sklad sklad = new Sklad();
        TerminalRiaditela terminal = new TerminalRiaditela(sklad,
                TestPomocnik.vstup("123", "5", "5", "Peter", "Novak", "x"));

        String vystup = TestPomocnik.zachytVystup(terminal::spustiTerminal);

        assertTrue(vystup.contains("Vitaj Juraj"));
        assertTrue(vystup.contains("Pracovnik bol uspesne prijaty."));
        assertTrue(sklad.getZoznamPracovnikov().containsKey("5"));
    }

    @Test
    void nespravneIdSaNeprijme() {
        Sklad sklad = new Sklad();
        TerminalRiaditela terminal = new TerminalRiaditela(sklad,
                TestPomocnik.vstup("000", "000", "000"));

        String vystup = TestPomocnik.zachytVystup(terminal::spustiTerminal);

        assertTrue(vystup.contains("prilis vela neplatnych pokusov"));
        assertFalse(sklad.getZoznamPracovnikov().containsKey("000"));
    }
}
