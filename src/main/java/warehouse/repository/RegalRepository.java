package warehouse.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import warehouse.domain.Regal;

import java.util.List;

/**
 * Repozitar regalov.
 * @author Juraj
 */
public interface RegalRepository extends JpaRepository<Regal, Long> {

    /**
     * Vrati regale v miestnosti s nacitanou miestnostou, zoradene podla id.
     * @param kluc kluc miestnosti
     * @return zoznam regalov
     */
    @Query("select r from Regal r join fetch r.miestnost where r.miestnost.kluc = :kluc order by r.id")
    List<Regal> findByMiestnostKlucWithDetail(@Param("kluc") String kluc);

    /**
     * Vrati regale v miestnosti s danym klucom zoradene podla id.
     * @param kluc kluc miestnosti
     * @return zoznam regalov
     */
    List<Regal> findByMiestnostKlucOrderByIdAsc(String kluc);
}
