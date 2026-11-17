package warehouse.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import warehouse.domain.Tovar;

import java.util.List;
import java.util.Optional;

/**
 * Repozitar tovaru.
 * @author Juraj
 */
public interface TovarRepository extends JpaRepository<Tovar, String> {

    /**
     * Vrati tovar s nacitanym regalom a jeho miestnostou.
     * @param id id tovaru
     * @return tovar alebo prazdny vysledok
     */
    @Query("select t from Tovar t left join fetch t.regal r left join fetch r.miestnost where t.id = :id")
    Optional<Tovar> findWithDetailById(@Param("id") String id);

    /**
     * Vrati tovar v regali s nacitanym regalom a jeho miestnostou, zoradeny podla slotu.
     * @param regalId id regalu
     * @return zoznam tovaru
     */
    @Query("select t from Tovar t left join fetch t.regal r left join fetch r.miestnost where r.id = :regalId order by t.slot")
    List<Tovar> findByRegalIdWithDetail(@Param("regalId") Long regalId);

    /**
     * Vrati tovar v regali zoradany podla slotu.
     * @param regalId id regalu
     * @return zoznam tovaru
     */
    List<Tovar> findByRegalIdOrderBySlotAsc(Long regalId);

    /**
     * Vrati tovar v miestnosti s danym klucom, ktoreho prijemcom je zadany zakaznik.
     * @param kluc kluc miestnosti
     * @param zakaznikId id zakaznika
     * @return zoznam tovaru
     */
    @Query("select t from Tovar t join fetch t.regal r join fetch r.miestnost "
            + "where r.miestnost.kluc = :kluc and t.prijemca.id = :zakaznikId order by t.id")
    List<Tovar> findByMiestnostKlucAndPrijemcaId(@Param("kluc") String kluc, @Param("zakaznikId") String zakaznikId);

    /**
     * Vrati vsetok tovar ulozeny v regaloch s nacitanym regalom a miestnostou jednym dotazom.
     * @return tovar zoradeny podla regalu a slotu
     */
    @Query("select t from Tovar t join fetch t.regal r join fetch r.miestnost order by r.id, t.slot")
    List<Tovar> findAllUlozeneWithDetail();

    /**
     * Vrati tovar ulozeny v regaloch jednej miestnosti jednym dotazom.
     * @param kluc kluc miestnosti
     * @return tovar zoradeny podla regalu a slotu
     */
    @Query("select t from Tovar t join fetch t.regal r join fetch r.miestnost m where m.kluc = :kluc "
            + "order by r.id, t.slot")
    List<Tovar> findByMiestnostKlucWithDetail(@Param("kluc") String kluc);

    /**
     * Spocita tovar v regali.
     * @param regalId id regalu
     * @return pocet tovaru v regali
     */
    long countByRegalId(Long regalId);
}
