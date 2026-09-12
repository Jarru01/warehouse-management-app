package warehouse.sklad;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidaciaTest {

    @Test
    void jeCisloAkceptujeIbaCislice() {
        assertTrue(Validacia.jeCislo("123"));
        assertFalse(Validacia.jeCislo("12a"));
        assertFalse(Validacia.jeCislo(""));
        assertFalse(Validacia.jeCislo(null));
    }

    @Test
    void jeTextAkceptujeIbaPismena() {
        assertTrue(Validacia.jeText("Juraj"));
        assertFalse(Validacia.jeText("Juraj1"));
        assertFalse(Validacia.jeText(""));
        assertFalse(Validacia.jeText(null));
    }

    @Test
    void jeNazovAkceptujePismenaACislice() {
        assertTrue(Validacia.jeNazov("Tovar123"));
        assertFalse(Validacia.jeNazov("Tovar 1"));
        assertFalse(Validacia.jeNazov(""));
    }

    @Test
    void jeKladneCisloOdmietaNuluZaporneAHodnotyMimoRozsahu() {
        assertTrue(Validacia.jeKladneCislo("5"));
        assertFalse(Validacia.jeKladneCislo("0"));
        assertFalse(Validacia.jeKladneCislo("-5"));
        assertFalse(Validacia.jeKladneCislo("99999999999999999999"));
    }
}
