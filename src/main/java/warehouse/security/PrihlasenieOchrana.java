package warehouse.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * Obmedzi pocet neuspesnych prihlaseni na ucet (rola a ID) a na IP adresu v casovom okne. Po prekroceni limitu
 * sa dalsie pokusy odmietaju, kym okno od prveho neuspechu neskonci. Blokuje sa aj neexistujuci ucet, takze
 * blokovanie neprezradi, ktore ID existuje. Pocitadla su v databaze: preziju restart a zdielaju ich vsetky
 * spustene instancie aplikacie.
 * @author Juraj
 */
@Component
public class PrihlasenieOchrana {
    private static final int MAX_DLZKA_KLUCA = 120;

    private static final String PRIPOCITAJ = """
            insert into prihlasenie_pokusy (kluc, pocet, zaciatok) values (:kluc, 1, :teraz)
            on conflict (kluc) do update set
                pocet = case when prihlasenie_pokusy.zaciatok <= :hranica then 1
                             else prihlasenie_pokusy.pocet + 1 end,
                zaciatok = case when prihlasenie_pokusy.zaciatok <= :hranica then :teraz
                                else prihlasenie_pokusy.zaciatok end
            """;

    private final NamedParameterJdbcTemplate jdbc;
    private final int maxNaUcet;
    private final int maxZAdresy;
    private final Duration okno;
    private final Clock clock;

    /**
     * Vytvori ochranu s limitmi z konfiguracie.
     * @param jdbc pristup k databaze
     * @param maxNaUcet povoleny pocet neuspechov na ucet v okne
     * @param maxZAdresy povoleny pocet neuspechov z jednej IP adresy v okne
     * @param okno dlzka okna
     */
    @Autowired
    public PrihlasenieOchrana(NamedParameterJdbcTemplate jdbc,
                              @Value("${warehouse.prihlasenie.max-pokusov-na-ucet:5}") int maxNaUcet,
                              @Value("${warehouse.prihlasenie.max-pokusov-z-adresy:20}") int maxZAdresy,
                              @Value("${warehouse.prihlasenie.okno:15m}") Duration okno) {
        this(jdbc, maxNaUcet, maxZAdresy, okno, Clock.systemUTC());
    }

    PrihlasenieOchrana(NamedParameterJdbcTemplate jdbc, int maxNaUcet, int maxZAdresy, Duration okno, Clock clock) {
        this.jdbc = jdbc;
        this.maxNaUcet = maxNaUcet;
        this.maxZAdresy = maxZAdresy;
        this.okno = okno;
        this.clock = clock;
    }

    /**
     * Zisti, ci su dalsie pokusy pre ucet alebo adresu docasne zablokovane.
     * @param ucet ucet v tvare ROLA:id
     * @param adresa IP adresa alebo null
     * @return true, ak sa ma pokus odmietnut bez overovania hesla
     */
    public boolean jeZablokovane(String ucet, String adresa) {
        return this.prekroceny(kluc("ucet:", ucet), this.maxNaUcet)
                || (adresa != null && this.prekroceny(kluc("adresa:", adresa), this.maxZAdresy));
    }

    /**
     * Zapocita neuspesne prihlasenie.
     * @param ucet ucet v tvare ROLA:id
     * @param adresa IP adresa alebo null
     */
    public void zaznamenajNeuspech(String ucet, String adresa) {
        this.pripocitaj(kluc("ucet:", ucet));
        if (adresa != null) {
            this.pripocitaj(kluc("adresa:", adresa));
        }
    }

    /**
     * Vynuluje pocitadlo uctu po uspesnom prihlaseni alebo obnove hesla. Pocitadlo adresy zostava.
     * @param ucet ucet v tvare ROLA:id
     */
    public void zaznamenajUspech(String ucet) {
        this.jdbc.update("delete from prihlasenie_pokusy where kluc = :kluc",
                new MapSqlParameterSource("kluc", kluc("ucet:", ucet)));
    }

    /**
     * Zmaze pocitadla, ktorych okno uz skoncilo. Vola ju pravidelna udrzba.
     * @return pocet zmazanych pocitadiel
     */
    public int zmazExpirovane() {
        return this.jdbc.update("delete from prihlasenie_pokusy where zaciatok <= :hranica",
                new MapSqlParameterSource("hranica", this.hranica()));
    }

    private boolean prekroceny(String kluc, int max) {
        List<Integer> pocet = this.jdbc.queryForList(
                "select pocet from prihlasenie_pokusy where kluc = :kluc and zaciatok > :hranica",
                new MapSqlParameterSource("kluc", kluc).addValue("hranica", this.hranica()), Integer.class);
        return !pocet.isEmpty() && pocet.get(0) >= max;
    }

    private void pripocitaj(String kluc) {
        this.jdbc.update(PRIPOCITAJ, new MapSqlParameterSource("kluc", kluc)
                .addValue("teraz", utc(Instant.now(this.clock)))
                .addValue("hranica", this.hranica()));
    }

    private OffsetDateTime hranica() {
        return utc(Instant.now(this.clock).minus(this.okno));
    }

    private static OffsetDateTime utc(Instant cas) {
        return cas.atOffset(ZoneOffset.UTC);
    }

    private static String kluc(String typ, String hodnota) {
        String kluc = typ + hodnota;
        return kluc.length() <= MAX_DLZKA_KLUCA ? kluc : kluc.substring(0, MAX_DLZKA_KLUCA);
    }
}
