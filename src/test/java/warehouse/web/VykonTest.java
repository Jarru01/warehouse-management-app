package warehouse.web;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;
import warehouse.AbstractIntegrationTest;
import warehouse.domain.Miestnost;
import warehouse.domain.Regal;
import warehouse.domain.Tovar;
import warehouse.domain.ZakaznikInfo;
import warehouse.repository.TovarRepository;
import warehouse.security.PrihlasenyPouzivatel;
import warehouse.security.Rola;
import warehouse.service.SkladService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pocet SQL dotazov pri nacitani obrazoviek nesmie rast s poctom regalov a tovaru (ziadne N+1 dotazy).
 */
@SpringBootTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@AutoConfigureMockMvc
@Transactional
class VykonTest extends AbstractIntegrationTest {
    private static final int NAJVIAC_DOTAZOV = 8;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SkladService skladService;

    @Autowired
    private TovarRepository tovarRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    private Statistics statistiky;
    private int pocitadloTovaru;

    @BeforeEach
    void pripravStatistiky() {
        this.statistiky = this.entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        this.skladService.prijmiPracovnika("1", "Peter", "Novak", "testovacie-heslo");
        this.pridajRegaleSTovarom(2);
    }

    @Test
    void panelRiaditelaMaStalyPocetDotazov() throws Exception {
        long malySklad = this.dotazyPri("/riaditel", RIADITEL);
        this.pridajRegaleSTovarom(20);
        long velkySklad = this.dotazyPri("/riaditel", RIADITEL);

        assertEquals(malySklad, velkySklad, "pocet dotazov nezavisi od poctu regalov");
        assertTrue(velkySklad <= NAJVIAC_DOTAZOV, "panel riaditela: " + velkySklad + " dotazov");
    }

    @Test
    void panelPracovnikaMaStalyPocetDotazov() throws Exception {
        long malySklad = this.dotazyPri("/pracovnik", PRACOVNIK);
        this.pridajRegaleSTovarom(20);
        long velkySklad = this.dotazyPri("/pracovnik", PRACOVNIK);

        assertEquals(malySklad, velkySklad, "pocet dotazov nezavisi od poctu regalov");
        assertTrue(velkySklad <= NAJVIAC_DOTAZOV, "panel pracovnika: " + velkySklad + " dotazov");
    }

    @Test
    void apiRegalovMiestnostiMaStalyPocetDotazov() throws Exception {
        long malySklad = this.dotazyPri("/api/miestnosti/" + Miestnost.SKLAD_TOVARU + "/regale", PRACOVNIK);
        this.pridajRegaleSTovarom(20);
        long velkySklad = this.dotazyPri("/api/miestnosti/" + Miestnost.SKLAD_TOVARU + "/regale", PRACOVNIK);

        assertEquals(malySklad, velkySklad, "pocet dotazov nezavisi od poctu regalov");
        assertTrue(velkySklad <= 3, "API regalov: " + velkySklad + " dotazov");
        this.mockMvc.perform(get("/api/miestnosti/" + Miestnost.SKLAD_TOVARU + "/regale").with(PRACOVNIK))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(22))
                .andExpect(jsonPath("$[0].tovar.length()").value(2))
                .andExpect(jsonPath("$[0].tovar[0].slot").value(1));
    }

    private long dotazyPri(String cesta, RequestPostProcessor prihlasenie) throws Exception {
        this.entityManager.flush();
        this.entityManager.clear();
        this.statistiky.clear();
        this.mockMvc.perform(get(cesta).with(prihlasenie)).andExpect(status().isOk());
        return this.statistiky.getPrepareStatementCount();
    }

    private void pridajRegaleSTovarom(int pocet) {
        ZakaznikInfo zakaznik = new ZakaznikInfo("10", "Jan", "Novy");
        for (int i = 0; i < pocet; i++) {
            Regal regal = this.skladService.pridajRegal(3);
            for (int slot = 1; slot <= 2; slot++) {
                String id = "V" + (++this.pocitadloTovaru);
                Tovar tovar = new Tovar(id, "Tovar" + id, 100, zakaznik, zakaznik);
                tovar.ulozDoRegalu(regal, slot);
                this.tovarRepository.save(tovar);
            }
        }
    }

    private static final RequestPostProcessor RIADITEL = prihlaseny(Rola.RIADITEL, "123");
    private static final RequestPostProcessor PRACOVNIK = prihlaseny(Rola.PRACOVNIK, "1");

    private static RequestPostProcessor prihlaseny(Rola rola, String id) {
        PrihlasenyPouzivatel pouzivatel = new PrihlasenyPouzivatel(rola, id, "Test", "Pouzivatel", false);
        return authentication(UsernamePasswordAuthenticationToken.authenticated(pouzivatel, null,
                List.of(new SimpleGrantedAuthority(rola.getOpravnenie()))));
    }
}
