package warehouse.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import warehouse.domain.Pracovnik;

import java.util.List;
import java.util.Optional;

/**
 * Repozitar pracovnikov.
 * @author Juraj
 */
public interface PracovnikRepository extends JpaRepository<Pracovnik, String> {

    /**
     * Vrati vsetkych pracovnikov s nacitanou miestnostou a drzanych tovarom.
     * @return zoznam pracovnikov
     */
    @Query("select p from Pracovnik p left join fetch p.miestnost left join fetch p.drzanyTovar order by p.id")
    List<Pracovnik> findAllWithDetail();

    /**
     * Vrati pracovnika s nacitanou miestnostou a drzanych tovarom.
     * @param id id pracovnika
     * @return pracovnik alebo prazdny vysledok
     */
    @Query("select p from Pracovnik p left join fetch p.miestnost left join fetch p.drzanyTovar where p.id = :id")
    Optional<Pracovnik> findWithDetailById(@Param("id") String id);

    /**
     * Vrati pracovnikov v miestnosti s danym klucom zoradenych podla id.
     * @param kluc kluc miestnosti
     * @return zoznam pracovnikov
     */
    List<Pracovnik> findByMiestnostKlucOrderByIdAsc(String kluc);
}
