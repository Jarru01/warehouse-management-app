package warehouse.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import warehouse.domain.Regal;

import java.util.List;
import java.util.Optional;

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

    /**
     * Vrati vsetky regale s nacitanou miestnostou jednym dotazom, zoradene podla miestnosti a id.
     * @return zoznam regalov
     */
    @Query("select r from Regal r join fetch r.miestnost m order by m.kluc, r.id")
    List<Regal> findAllWithDetail();

    /**
     * Najde prvy volny slot v miestnosti: regal s najnizsim id, ktory ma volne miesto, a v nom najnizsie
     * cislo volneho slotu. Jeden dotaz namiesto pocitania a nacitania tovaru regal po regali.
     * @param kluc kluc miestnosti
     * @return regal a slot alebo prazdny vysledok, ak je miestnost plna
     */
    @Query(value = """
            select r.id as "regalId", s.slot as "slot"
            from regal r
            cross join lateral generate_series(1, r.kapacita) as s(slot)
            where r.miestnost_kluc = :kluc
              and not exists (select 1 from tovar t where t.regal_id = r.id and t.slot = s.slot)
            order by r.id, s.slot
            limit 1
            """, nativeQuery = true)
    Optional<VolnySlot> findPrvyVolnySlot(@Param("kluc") String kluc);

    /**
     * Volny slot v regali.
     */
    interface VolnySlot {
        /**
         * Vrati id regalu.
         * @return id regalu
         */
        Long getRegalId();

        /**
         * Vrati cislo slotu.
         * @return cislo slotu od 1
         */
        Integer getSlot();
    }
}
