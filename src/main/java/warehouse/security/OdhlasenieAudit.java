package warehouse.security;

import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.LogoutSuccessEvent;
import org.springframework.stereotype.Component;
import warehouse.domain.AuditAkcia;
import warehouse.service.AuditService;

/**
 * Zapise odhlasenie do auditu. Udalost publikuje Spring Security po uspesnom odhlaseni.
 * @author Juraj
 */
@Component
public class OdhlasenieAudit {
    private final AuditService auditService;

    /**
     * Vytvori posluchaca odhlaseni.
     * @param auditService servis auditu
     */
    public OdhlasenieAudit(AuditService auditService) {
        this.auditService = auditService;
    }

    /**
     * Zapise odhlasenie pouzivatela.
     * @param udalost udalost odhlasenia
     */
    @EventListener
    public void poOdhlaseni(LogoutSuccessEvent udalost) {
        this.auditService.zaznamenajPre(udalost.getAuthentication().getName(), AuditAkcia.ODHLASENIE, null);
    }
}
