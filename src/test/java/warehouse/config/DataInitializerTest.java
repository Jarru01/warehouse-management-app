package warehouse.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import warehouse.AbstractIntegrationTest;
import warehouse.domain.MalySklad;
import warehouse.domain.Miestnost;
import warehouse.domain.Riaditel;
import warehouse.domain.VelkySklad;
import warehouse.repository.MiestnostRepository;
import warehouse.repository.RiaditelRepository;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class DataInitializerTest extends AbstractIntegrationTest {

    @Autowired
    private RiaditelRepository riaditelRepository;

    @Autowired
    private MiestnostRepository miestnostRepository;

    @Test
    void inicializaciaVytvoriRiaditelaAPredvoleneMiestnosti() {
        assertTrue(this.riaditelRepository.findById(Riaditel.PREDVOLENE_ID).isPresent());
        assertInstanceOf(VelkySklad.class, this.miestnostRepository.findById(Miestnost.SKLAD_TOVARU).orElseThrow());
        assertInstanceOf(MalySklad.class, this.miestnostRepository.findById(Miestnost.PRIJEM_TOVARU).orElseThrow());
        assertInstanceOf(MalySklad.class, this.miestnostRepository.findById(Miestnost.VYDAJ_TOVARU).orElseThrow());
    }
}
