package warehouse.sklad.terminaly;

import org.junit.jupiter.api.Test;
import warehouse.sklad.Sklad;
import warehouse.sklad.TestData;
import warehouse.sklad.TestPomocnik;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TerminalZakaznikaTest {

    @Test
    void zakaznikSaPrihlasiAUloziTovar() {
        Sklad sklad = new Sklad();
        TerminalZakaznika terminal = new TerminalZakaznika(sklad,
                TestPomocnik.vstup("10", "Jan", "Novy", "1", "100", "Tovar100", "500", "20", "Peter", "Novak", "x"));

        String vystup = TestPomocnik.zachytVystup(terminal::spustiTerminal);

        assertTrue(vystup.contains("Vitaj Jan"));
        assertTrue(vystup.contains("Tovar bol ulozeny."));
        assertNotNull(sklad.getPrijemTovaru().getRegal().getTovar("100"));
    }

    @Test
    void vyzdvihnutieCudziehoTovaruZlyha() {
        Sklad sklad = new Sklad();
        sklad.getVydajTovaru().getRegal().ulozTovar(TestData.tovar("100"));
        TerminalZakaznika terminal = new TerminalZakaznika(sklad,
                TestPomocnik.vstup("20", "Peter", "Novak", "2", "100", "x"));

        String vystup = TestPomocnik.zachytVystup(terminal::spustiTerminal);

        assertTrue(vystup.contains("ti nepatri"));
        assertNotNull(sklad.getVydajTovaru().getRegal().getTovar("100"));
    }
}
