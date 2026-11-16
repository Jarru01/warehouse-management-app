package warehouse.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import warehouse.AbstractIntegrationTest;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PrihlasenieOchranaTest extends AbstractIntegrationTest {
    private static final Duration OKNO = Duration.ofMinutes(15);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final PosuvneHodiny hodiny = new PosuvneHodiny();
    private PrihlasenieOchrana ochrana;

    @BeforeEach
    void vytvorOchranu() {
        this.ochrana = new PrihlasenieOchrana(new NamedParameterJdbcTemplate(this.jdbcTemplate), 3, 5, OKNO,
                this.hodiny);
    }

    @Test
    void ucetSaZablokujePoLimiteNeuspechov() {
        for (int i = 0; i < 2; i++) {
            this.ochrana.zaznamenajNeuspech("RIADITEL:123", "10.0.0.1");
        }
        assertFalse(this.ochrana.jeZablokovane("RIADITEL:123", "10.0.0.1"));

        this.ochrana.zaznamenajNeuspech("RIADITEL:123", "10.0.0.1");

        assertTrue(this.ochrana.jeZablokovane("RIADITEL:123", "10.0.0.2"));
        assertFalse(this.ochrana.jeZablokovane("RIADITEL:999", "10.0.0.2"));
    }

    @Test
    void pocitadlaSuUlozeneVDatabaze() {
        this.ochrana.zaznamenajNeuspech("RIADITEL:123", "10.0.0.1");

        PrihlasenieOchrana inaInstancia = new PrihlasenieOchrana(new NamedParameterJdbcTemplate(this.jdbcTemplate),
                1, 5, OKNO, this.hodiny);
        assertTrue(inaInstancia.jeZablokovane("RIADITEL:123", null), "druha instancia vidi to iste pocitadlo");
    }

    @Test
    void blokovanieSkonciPoUplynutiOkna() {
        for (int i = 0; i < 3; i++) {
            this.ochrana.zaznamenajNeuspech("PRACOVNIK:1", null);
        }
        assertTrue(this.ochrana.jeZablokovane("PRACOVNIK:1", null));

        this.hodiny.posun(OKNO);

        assertFalse(this.ochrana.jeZablokovane("PRACOVNIK:1", null));
        this.ochrana.zaznamenajNeuspech("PRACOVNIK:1", null);
        assertFalse(this.ochrana.jeZablokovane("PRACOVNIK:1", null), "nove okno zacina od jedneho pokusu");
    }

    @Test
    void uspesnePrihlasenieVynulujePocitadloUctu() {
        this.ochrana.zaznamenajNeuspech("ZAKAZNIK:10", null);
        this.ochrana.zaznamenajNeuspech("ZAKAZNIK:10", null);

        this.ochrana.zaznamenajUspech("ZAKAZNIK:10");
        this.ochrana.zaznamenajNeuspech("ZAKAZNIK:10", null);
        this.ochrana.zaznamenajNeuspech("ZAKAZNIK:10", null);

        assertFalse(this.ochrana.jeZablokovane("ZAKAZNIK:10", null));
    }

    @Test
    void adresaSaZablokujePriHadaniRoznychUctov() {
        for (int i = 0; i < 5; i++) {
            this.ochrana.zaznamenajNeuspech("ZAKAZNIK:" + i, "10.0.0.9");
        }

        assertTrue(this.ochrana.jeZablokovane("ZAKAZNIK:77", "10.0.0.9"));
        assertFalse(this.ochrana.jeZablokovane("ZAKAZNIK:77", "10.0.0.8"));
    }

    @Test
    void udrzbaZmazeSkonceneOkna() {
        this.ochrana.zaznamenajNeuspech("ZAKAZNIK:1", "10.0.0.1");
        this.hodiny.posun(OKNO.plusSeconds(1));

        assertEquals(2, this.ochrana.zmazExpirovane());
        assertEquals(0, this.jdbcTemplate.queryForObject(
                "select count(*) from prihlasenie_pokusy where kluc in ('ucet:ZAKAZNIK:1', 'adresa:10.0.0.1')",
                Integer.class));
    }

    private static final class PosuvneHodiny extends Clock {
        private Instant teraz = Instant.parse("2026-01-01T10:00:00Z");

        void posun(Duration o) {
            this.teraz = this.teraz.plus(o);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return this.teraz;
        }
    }
}
