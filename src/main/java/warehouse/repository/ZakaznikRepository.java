package warehouse.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import warehouse.domain.Zakaznik;

/**
 * Repozitar registrovanych zakaznikov.
 * @author Juraj
 */
public interface ZakaznikRepository extends JpaRepository<Zakaznik, String> {
}
