package warehouse.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import warehouse.AbstractIntegrationTest;
import warehouse.domain.Pracovnik;
import warehouse.domain.Regal;
import warehouse.domain.Tovar;
import warehouse.domain.VelkySklad;
import warehouse.domain.ZakaznikInfo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TovarRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private MiestnostRepository miestnostRepository;

    @Autowired
    private RegalRepository regalRepository;

    @Autowired
    private TovarRepository tovarRepository;

    @Autowired
    private PracovnikRepository pracovnikRepository;

    @Test
    void tovarUlozenyVRegaliSaNajdePodlaSlotu() {
        VelkySklad sklad = this.miestnostRepository.save(new VelkySklad("test-velky"));
        Regal regal = this.regalRepository.save(new Regal(sklad, 3));
        Tovar tovar = new Tovar("100", "Tovar100", 500, zakaznik("10"), zakaznik("20"));
        tovar.ulozDoRegalu(regal, 2);
        this.tovarRepository.save(tovar);

        Tovar nacitany = this.tovarRepository.findById("100").orElseThrow();

        assertEquals("Tovar100", nacitany.getNazov());
        assertEquals(500, nacitany.getVaha());
        assertEquals(2, nacitany.getSlot());
        assertEquals("test-velky", nacitany.getRegal().getMiestnost().getKluc());
        assertEquals(1, this.tovarRepository.findByRegalIdOrderBySlotAsc(regal.getId()).size());
        assertEquals(1, this.tovarRepository.countByRegalId(regal.getId()));
    }

    @Test
    void pracovnikMozeDrzatTovarMimoRegalu() {
        Pracovnik pracovnik = this.pracovnikRepository.save(new Pracovnik("5", "Peter", "Novak"));
        Tovar tovar = this.tovarRepository.save(new Tovar("200", "Tovar200", 300, zakaznik("10"), zakaznik("20")));
        pracovnik.uchop(tovar);
        this.pracovnikRepository.saveAndFlush(pracovnik);

        Pracovnik nacitany = this.pracovnikRepository.findById("5").orElseThrow();

        assertTrue(nacitany.drziTovar());
        assertEquals("200", nacitany.getDrzanyTovar().getId());
        assertNull(nacitany.getDrzanyTovar().getRegal());
        assertEquals("Jan", nacitany.getDrzanyTovar().getOdosielatel().getMeno());
    }

    private static ZakaznikInfo zakaznik(String id) {
        return new ZakaznikInfo(id, "Jan", "Testovaci");
    }
}
