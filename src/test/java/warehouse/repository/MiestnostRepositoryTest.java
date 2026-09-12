package warehouse.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import warehouse.AbstractIntegrationTest;
import warehouse.domain.MalySklad;
import warehouse.domain.VelkySklad;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MiestnostRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private MiestnostRepository miestnostRepository;

    @Test
    void uloziARozlisiTypyMiestnosti() {
        this.miestnostRepository.save(new VelkySklad("test-velky"));
        this.miestnostRepository.save(new MalySklad("test-maly"));

        assertInstanceOf(VelkySklad.class, this.miestnostRepository.findById("test-velky").orElseThrow());
        assertInstanceOf(MalySklad.class, this.miestnostRepository.findById("test-maly").orElseThrow());
    }
}
