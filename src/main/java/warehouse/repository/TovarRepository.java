package warehouse.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import warehouse.domain.Tovar;

import java.util.List;

/**
 * Repozitar tovaru.
 * @author Juraj
 */
public interface TovarRepository extends JpaRepository<Tovar, String> {

    /**
     * Vrati tovar v regali zoradany podla slotu.
     * @param regalId id regalu
     * @return zoznam tovaru
     */
    List<Tovar> findByRegalIdOrderBySlotAsc(Long regalId);

    /**
     * Spocita tovar v regali.
     * @param regalId id regalu
     * @return pocet tovaru v regali
     */
    long countByRegalId(Long regalId);
}
