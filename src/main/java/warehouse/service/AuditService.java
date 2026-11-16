package warehouse.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import warehouse.domain.AuditAkcia;
import warehouse.domain.AuditZaznam;
import warehouse.repository.AuditZaznamRepository;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

/**
 * Zapisuje auditne zaznamy do databazy a do logu "AUDIT". Zaznam vytvoreny v ramci zmeny sa ulozi v tej istej
 * transakcii, takze neuspesna zmena po sebe nezanecha zaznam.
 * @author Juraj
 */
@Service
public class AuditService {
    /** Najvacsi pocet zaznamov vrateny naraz. */
    public static final int MAX_POCET = 200;
    /** Oznacenie pouzivatela, ktory nie je prihlaseny. */
    public static final String ANONYM = "anonym";

    private static final Logger AUDIT_LOG = LoggerFactory.getLogger("AUDIT");

    private final AuditZaznamRepository repository;
    private final Clock clock;

    /**
     * Vytvori servis auditu.
     * @param repository repozitar auditnych zaznamov
     */
    public AuditService(AuditZaznamRepository repository) {
        this.repository = repository;
        this.clock = Clock.systemUTC();
    }

    /**
     * Zapise udalost aktualne prihlaseneho pouzivatela.
     * @param akcia druh udalosti
     * @param detail doplnujuci opis
     */
    @Transactional
    public void zaznamenaj(AuditAkcia akcia, String detail) {
        this.zaznamenajPre(aktualnyPouzivatel(), akcia, detail);
    }

    /**
     * Zapise udalost za konkretneho pouzivatela, napriklad pri pokuse o prihlasenie.
     * @param pouzivatel kto udalost vykonal, napriklad RIADITEL:123
     * @param akcia druh udalosti
     * @param detail doplnujuci opis
     */
    @Transactional
    public void zaznamenajPre(String pouzivatel, AuditAkcia akcia, String detail) {
        String kto = pouzivatel == null || pouzivatel.isBlank() ? ANONYM : skrat(pouzivatel, 80);
        Instant cas = Instant.now(this.clock);
        this.repository.save(new AuditZaznam(cas, kto, akcia, detail));
        AUDIT_LOG.info("{} {} {}", kto, akcia, detail == null ? "" : detail);
    }

    /**
     * Vrati najnovsie zaznamy.
     * @param pocet pocet zaznamov, najviac {@link #MAX_POCET}
     * @return zaznamy od najnovsieho
     */
    @Transactional(readOnly = true)
    public List<AuditZaznam> najnovsie(int pocet) {
        return this.repository.findAllByOrderByCasDescIdDesc(PageRequest.of(0, obmedz(pocet)));
    }

    /**
     * Vrati najnovsie zaznamy danej akcie.
     * @param akcia druh udalosti
     * @param pocet pocet zaznamov, najviac {@link #MAX_POCET}
     * @return zaznamy od najnovsieho
     */
    @Transactional(readOnly = true)
    public List<AuditZaznam> najnovsie(AuditAkcia akcia, int pocet) {
        return this.repository.findByAkciaOrderByCasDescIdDesc(akcia, PageRequest.of(0, obmedz(pocet)));
    }

    private static String aktualnyPouzivatel() {
        Authentication prihlasenie = SecurityContextHolder.getContext().getAuthentication();
        if (prihlasenie == null || !prihlasenie.isAuthenticated()
                || prihlasenie instanceof AnonymousAuthenticationToken) {
            return ANONYM;
        }
        return prihlasenie.getName();
    }

    private static int obmedz(int pocet) {
        return Math.max(1, Math.min(pocet, MAX_POCET));
    }

    private static String skrat(String text, int dlzka) {
        return text.length() <= dlzka ? text : text.substring(0, dlzka);
    }
}
