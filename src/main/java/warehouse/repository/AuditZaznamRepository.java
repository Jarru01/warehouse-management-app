package warehouse.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import warehouse.domain.AuditAkcia;
import warehouse.domain.AuditZaznam;

import java.util.List;

/**
 * Repozitar auditnych zaznamov.
 * @author Juraj
 */
public interface AuditZaznamRepository extends JpaRepository<AuditZaznam, Long> {

    /**
     * Vrati najnovsie zaznamy.
     * @param strana pocet a poradie zaznamov
     * @return zaznamy od najnovsieho
     */
    List<AuditZaznam> findAllByOrderByCasDescIdDesc(Pageable strana);

    /**
     * Vrati najnovsie zaznamy danej akcie.
     * @param akcia druh udalosti
     * @param strana pocet a poradie zaznamov
     * @return zaznamy od najnovsieho
     */
    List<AuditZaznam> findByAkciaOrderByCasDescIdDesc(AuditAkcia akcia, Pageable strana);
}
