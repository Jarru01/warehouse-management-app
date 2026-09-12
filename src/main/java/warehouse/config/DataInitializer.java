package warehouse.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import warehouse.domain.MalySklad;
import warehouse.domain.Miestnost;
import warehouse.domain.Regal;
import warehouse.domain.Riaditel;
import warehouse.domain.VelkySklad;
import warehouse.repository.MiestnostRepository;
import warehouse.repository.RegalRepository;
import warehouse.repository.RiaditelRepository;

/**
 * Naplni databazu pociatocnymi datami, ak este neexistuju.
 * @author Juraj
 */
@Component
public class DataInitializer implements ApplicationRunner {
    private final RiaditelRepository riaditelRepository;
    private final MiestnostRepository miestnostRepository;
    private final RegalRepository regalRepository;

    /**
     * Vytvori inicializator s potrebnymi repozitarmi.
     * @param riaditelRepository repozitar riaditelov
     * @param miestnostRepository repozitar miestnosti
     * @param regalRepository repozitar regalov
     */
    public DataInitializer(RiaditelRepository riaditelRepository, MiestnostRepository miestnostRepository,
                           RegalRepository regalRepository) {
        this.riaditelRepository = riaditelRepository;
        this.miestnostRepository = miestnostRepository;
        this.regalRepository = regalRepository;
    }

    /**
     * Vytvori riaditela a predvolene miestnosti, ak v databaze chybaju.
     * @param args argumenty spustenia
     */
    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (this.riaditelRepository.count() == 0) {
            this.riaditelRepository.save(new Riaditel(Riaditel.PREDVOLENE_ID, Riaditel.PREDVOLENE_MENO,
                    Riaditel.PREDVOLENE_PRIEZVISKO));
        }
        if (this.miestnostRepository.count() == 0) {
            this.miestnostRepository.save(new VelkySklad(Miestnost.SKLAD_TOVARU));
            MalySklad prijemTovaru = new MalySklad(Miestnost.PRIJEM_TOVARU);
            MalySklad vydajTovaru = new MalySklad(Miestnost.VYDAJ_TOVARU);
            this.miestnostRepository.save(prijemTovaru);
            this.miestnostRepository.save(vydajTovaru);
            this.regalRepository.save(new Regal(prijemTovaru, Miestnost.KAPACITA_MALEHO_SKLADU));
            this.regalRepository.save(new Regal(vydajTovaru, Miestnost.KAPACITA_MALEHO_SKLADU));
        }
    }
}
