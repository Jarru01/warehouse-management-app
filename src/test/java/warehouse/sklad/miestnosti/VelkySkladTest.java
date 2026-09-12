package warehouse.sklad.miestnosti;

import org.junit.jupiter.api.Test;
import warehouse.sklad.Sklad;
import warehouse.sklad.TestData;
import warehouse.sklad.predmety.Regal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VelkySkladTest {

    @Test
    void novyVelkySkladNemaRegale() {
        VelkySklad sklad = new VelkySklad(Sklad.MIESTNOST_SKLAD_TOVARU);

        assertTrue(sklad.getZoznamRegalov().isEmpty());
        assertNull(sklad.getRegal());
    }

    @Test
    void pridanyRegalSaVratiAkoVolny() {
        VelkySklad sklad = new VelkySklad(Sklad.MIESTNOST_SKLAD_TOVARU);

        assertTrue(sklad.pridajRegal(3));

        assertEquals(1, sklad.getZoznamRegalov().size());
        assertNotNull(sklad.getRegal());
    }

    @Test
    void neplatnaKapacitaRegalNeprida() {
        VelkySklad sklad = new VelkySklad(Sklad.MIESTNOST_SKLAD_TOVARU);

        assertFalse(sklad.pridajRegal(0));
        assertFalse(sklad.pridajRegal(-5));

        assertTrue(sklad.getZoznamRegalov().isEmpty());
    }

    @Test
    void getRegalSPravnymIndexomVratiRegal() {
        VelkySklad sklad = new VelkySklad(Sklad.MIESTNOST_SKLAD_TOVARU);
        sklad.pridajRegal(3);

        assertNotNull(sklad.getRegal(1));
        assertNull(sklad.getRegal(2));
        assertNull(sklad.getRegal(0));
    }

    @Test
    void odstranitJeMozneLenPrazdnyRegal() {
        VelkySklad sklad = new VelkySklad(Sklad.MIESTNOST_SKLAD_TOVARU);
        sklad.pridajRegal(3);

        assertTrue(sklad.odstranRegal(1));

        assertTrue(sklad.getZoznamRegalov().isEmpty());
    }

    @Test
    void regalSTovaromSaNedaOdstranit() {
        VelkySklad sklad = new VelkySklad(Sklad.MIESTNOST_SKLAD_TOVARU);
        sklad.pridajRegal(1);
        Regal regal = sklad.getRegal();
        regal.ulozTovar(TestData.tovar("1"));

        assertFalse(sklad.odstranRegal(1));

        assertEquals(1, sklad.getZoznamRegalov().size());
    }

    @Test
    void plnyRegalSaNevratiAkoVolny() {
        VelkySklad sklad = new VelkySklad(Sklad.MIESTNOST_SKLAD_TOVARU);
        sklad.pridajRegal(1);
        Regal regal = sklad.getRegal();
        regal.ulozTovar(TestData.tovar("1"));

        assertNull(sklad.getRegal());
    }
}
