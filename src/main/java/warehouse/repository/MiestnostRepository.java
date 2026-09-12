package warehouse.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import warehouse.domain.Miestnost;

/**
 * Repozitar miestnosti.
 * @author Juraj
 */
public interface MiestnostRepository extends JpaRepository<Miestnost, String> {
}
