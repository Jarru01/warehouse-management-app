package warehouse.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import warehouse.repository.ObnovaHeslaRepository;
import warehouse.security.PrihlasenieOchrana;
import warehouse.service.AuditService;

import java.time.Duration;
import java.time.Instant;

/**
 * Pravidelna udrzba dat: maze stare auditne zaznamy, expirovane odkazy na obnovu hesla a skoncene pocitadla
 * neuspesnych prihlaseni. Spusta sa podla {@code warehouse.udrzba.cron}, predvolene kazdu noc.
 * @author Juraj
 */
@Component
public class Udrzba {
    private static final Logger LOG = LoggerFactory.getLogger(Udrzba.class);

    private final AuditService auditService;
    private final ObnovaHeslaRepository obnovaHeslaRepository;
    private final PrihlasenieOchrana ochrana;
    private final Duration uchovavatAudit;

    /**
     * Vytvori udrzbu.
     * @param auditService servis auditu
     * @param obnovaHeslaRepository repozitar ziadosti o obnovu hesla
     * @param ochrana obmedzenie neuspesnych prihlaseni
     * @param uchovavatAudit ako dlho sa uchovavaju auditne zaznamy
     */
    public Udrzba(AuditService auditService, ObnovaHeslaRepository obnovaHeslaRepository,
                  PrihlasenieOchrana ochrana, @Value("${warehouse.audit.uchovavat:365d}") Duration uchovavatAudit) {
        this.auditService = auditService;
        this.obnovaHeslaRepository = obnovaHeslaRepository;
        this.ochrana = ochrana;
        this.uchovavatAudit = uchovavatAudit;
    }

    /**
     * Vykona udrzbu a zapise do logu, kolko zaznamov zmazala.
     */
    @Scheduled(cron = "${warehouse.udrzba.cron:0 30 3 * * *}")
    @Transactional
    public void vykonaj() {
        int audit = this.auditService.zmazStarsieAko(this.uchovavatAudit);
        int odkazy = this.obnovaHeslaRepository.zmazExpirovane(Instant.now());
        int pocitadla = this.ochrana.zmazExpirovane();
        LOG.info("Udrzba: zmazanych {} auditnych zaznamov, {} odkazov na obnovu hesla, {} pocitadiel prihlaseni.",
                audit, odkazy, pocitadla);
    }
}
