package sk.uniza.fri.sklad.predmety;

import org.junit.jupiter.api.Test;
import sk.uniza.fri.sklad.TestData;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegalTest {

    @Test
    void novyRegalJePrazdnyAObsahujeKapacitu() {
        Regal regal = new Regal(3);

        assertEquals(3, regal.getZoznamTovaru().length);
        assertTrue(regal.jePrazdny());
        assertTrue(regal.maVolneMiesto());
    }

    @Test
    void ulozenyTovarJeNajditelnyPodlaId() {
        Regal regal = new Regal(2);
        Tovar tovar = TestData.tovar("1");

        regal.ulozTovar(tovar);

        assertSame(tovar, regal.getTovar("1"));
        assertFalse(regal.jePrazdny());
    }

    @Test
    void neznamyTovarVratiNull() {
        Regal regal = new Regal(2);

        assertNull(regal.getTovar("99"));
    }

    @Test
    void odobranieTovaruUvolniMiesto() {
        Regal regal = new Regal(1);
        regal.ulozTovar(TestData.tovar("1"));

        assertFalse(regal.maVolneMiesto());

        regal.odoberTovar("1");

        assertTrue(regal.jePrazdny());
        assertTrue(regal.maVolneMiesto());
    }

    @Test
    void ulozenieDoPlnehoRegaluTovarNepridava() {
        Regal regal = new Regal(1);
        Tovar prvy = TestData.tovar("1");
        Tovar druhy = TestData.tovar("2");

        regal.ulozTovar(prvy);
        regal.ulozTovar(druhy);

        assertSame(prvy, regal.getTovar("1"));
        assertNull(regal.getTovar("2"));
    }
}
