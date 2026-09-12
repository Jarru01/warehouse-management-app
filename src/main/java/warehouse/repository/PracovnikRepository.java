package warehouse.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import warehouse.domain.Pracovnik;

import java.util.List;

/**
 * Repozitar pracovnikov.
 * @author Juraj
 */
public interface PracovnikRepository extends JpaRepository<Pracovnik, String> {

    /**
     * Vrati pracovnikov v miestnosti s danym klucom zoradenych podla id.
     * @param kluc kluc miestnosti
     * @return zoznam pracovnikov
     */
    List<Pracovnik> findByMiestnostKlucOrderByIdAsc(String kluc);
}
