package warehouse.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import warehouse.domain.ObnovaHesla;

import java.time.Instant;
import java.util.Optional;

/**
 * Repozitar ziadosti o obnovu hesla.
 * @author Juraj
 */
public interface ObnovaHeslaRepository extends JpaRepository<ObnovaHesla, Long> {

    /**
     * Najde ziadost podla hashu tokenu.
     * @param tokenHash hash tokenu
     * @return ziadost alebo prazdny vysledok
     */
    Optional<ObnovaHesla> findByTokenHash(String tokenHash);

    /**
     * Zisti, ci zakaznik poziadal o obnovu po danom case.
     * @param zakaznikId id zakaznika
     * @param od hranica casu
     * @return true, ak existuje novsia ziadost
     */
    boolean existsByZakaznikIdAndVytvoreneAfter(String zakaznikId, Instant od);

    /**
     * Zneplatni vsetky nepouzite ziadosti zakaznika.
     * @param zakaznikId id zakaznika
     * @return pocet zmenenych ziadosti
     */
    @Modifying
    @Query("update ObnovaHesla o set o.pouzite = true where o.zakaznikId = :zakaznikId and o.pouzite = false")
    int zneplatniVsetky(@Param("zakaznikId") String zakaznikId);

    /**
     * Zmaze ziadosti, ktore expirovali pred danym casom.
     * @param hranica hranica casu
     * @return pocet zmazanych ziadosti
     */
    @Modifying
    @Query("delete from ObnovaHesla o where o.platiDo < :hranica")
    int zmazExpirovane(@Param("hranica") Instant hranica);
}
