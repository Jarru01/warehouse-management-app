package warehouse.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Obmedzi pocet neuspesnych prihlaseni na ucet (rola a ID) a na IP adresu v casovom okne. Po prekroceni limitu
 * sa dalsie pokusy odmietaju, kym okno od prveho neuspechu neskonci. Blokuje sa aj neexistujuci ucet, takze
 * blokovanie neprezradi, ktore ID existuje. Pocitadla su v pamati a po restarte sa vynuluju.
 * @author Juraj
 */
@Component
public class PrihlasenieOchrana {
    private static final int MAX_DLZKA_KLUCA = 120;
    private static final int UPRATAT_NAD = 10_000;

    private record Pokusy(int pocet, Instant zaciatok) {
    }

    private final Map<String, Pokusy> zlyhania = new ConcurrentHashMap<>();
    private final int maxNaUcet;
    private final int maxZAdresy;
    private final Duration okno;
    private final Clock clock;

    /**
     * Vytvori ochranu s limitmi z konfiguracie.
     * @param maxNaUcet povoleny pocet neuspechov na ucet v okne
     * @param maxZAdresy povoleny pocet neuspechov z jednej IP adresy v okne
     * @param okno dlzka okna
     */
    @Autowired
    public PrihlasenieOchrana(@Value("${warehouse.prihlasenie.max-pokusov-na-ucet:5}") int maxNaUcet,
                              @Value("${warehouse.prihlasenie.max-pokusov-z-adresy:20}") int maxZAdresy,
                              @Value("${warehouse.prihlasenie.okno:15m}") Duration okno) {
        this(maxNaUcet, maxZAdresy, okno, Clock.systemUTC());
    }

    PrihlasenieOchrana(int maxNaUcet, int maxZAdresy, Duration okno, Clock clock) {
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
        if (this.zlyhania.size() > UPRATAT_NAD) {
            this.zlyhania.entrySet().removeIf(zaznam -> this.vyprsal(zaznam.getValue()));
        }
    }

    /**
     * Vynuluje pocitadlo uctu po uspesnom prihlaseni. Pocitadlo adresy zostava.
     * @param ucet ucet v tvare ROLA:id
     */
    public void zaznamenajUspech(String ucet) {
        this.zlyhania.remove(kluc("ucet:", ucet));
    }

    private boolean prekroceny(String kluc, int max) {
        Pokusy pokusy = this.zlyhania.get(kluc);
        if (pokusy == null) {
            return false;
        }
        if (this.vyprsal(pokusy)) {
            this.zlyhania.remove(kluc, pokusy);
            return false;
        }
        return pokusy.pocet() >= max;
    }

    private void pripocitaj(String kluc) {
        Instant teraz = Instant.now(this.clock);
        this.zlyhania.compute(kluc, (k, pokusy) -> pokusy == null || this.vyprsal(pokusy)
                ? new Pokusy(1, teraz)
                : new Pokusy(pokusy.pocet() + 1, pokusy.zaciatok()));
    }

    private boolean vyprsal(Pokusy pokusy) {
        return !Instant.now(this.clock).isBefore(pokusy.zaciatok().plus(this.okno));
    }

    private static String kluc(String typ, String hodnota) {
        String kluc = typ + hodnota;
        return kluc.length() <= MAX_DLZKA_KLUCA ? kluc : kluc.substring(0, MAX_DLZKA_KLUCA);
    }
}
