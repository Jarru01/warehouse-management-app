package warehouse.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import warehouse.domain.Regal;

import java.util.List;

/**
 * Repozitar regalov.
 * @author Juraj
 */
public interface RegalRepository extends JpaRepository<Regal, Long> {

    /**
     * Vrati regale v miestnosti s danym klucom zoradene podla id.
     * @param kluc kluc miestnosti
     * @return zoznam regalov
     */
    List<Regal> findByMiestnostKlucOrderByIdAsc(String kluc);
}
