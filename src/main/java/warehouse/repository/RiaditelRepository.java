package warehouse.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import warehouse.domain.Riaditel;

/**
 * Repozitar riaditelov.
 * @author Juraj
 */
public interface RiaditelRepository extends JpaRepository<Riaditel, String> {
}
