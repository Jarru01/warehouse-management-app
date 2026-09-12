package warehouse.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import warehouse.domain.Pracovnik;

/**
 * Repozitar pracovnikov.
 * @author Juraj
 */
public interface PracovnikRepository extends JpaRepository<Pracovnik, String> {
}
