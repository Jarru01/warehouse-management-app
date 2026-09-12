package warehouse.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MiestnostTest {

    @Test
    void miestnostiVracajuSvojTyp() {
        assertEquals(TypMiestnosti.VELKY, new VelkySklad("velky").getTyp());
        assertEquals(TypMiestnosti.MALY, new MalySklad("maly").getTyp());
    }
}
