package warehouse.security;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PrihlasenieOchranaTest {
    private static final Duration OKNO = Duration.ofMinutes(15);

    private final PosuvneHodiny hodiny = new PosuvneHodiny();
    private final PrihlasenieOchrana ochrana = new PrihlasenieOchrana(3, 5, OKNO, this.hodiny);

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
    void blokovanieSkonciPoUplynutiOkna() {
        for (int i = 0; i < 3; i++) {
            this.ochrana.zaznamenajNeuspech("PRACOVNIK:1", null);
        }
        assertTrue(this.ochrana.jeZablokovane("PRACOVNIK:1", null));

        this.hodiny.posun(OKNO);

        assertFalse(this.ochrana.jeZablokovane("PRACOVNIK:1", null));
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
