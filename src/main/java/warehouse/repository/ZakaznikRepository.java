package warehouse.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import warehouse.domain.Zakaznik;

import java.util.Optional;

/**
 * Repozitar registrovanych zakaznikov.
 * @author Juraj
 */
public interface ZakaznikRepository extends JpaRepository<Zakaznik, String> {

    /**
     * Najde zakaznika podla e-mailu ulozeneho malymi pismenami.
     * @param email normalizovany e-mail
     * @return zakaznik alebo prazdny vysledok
     */
    Optional<Zakaznik> findByEmail(String email);

    /**
     * Zisti, ci e-mail uz pouziva iny zakaznik.
     * @param email normalizovany e-mail
     * @return true, ak e-mail existuje
     */
    boolean existsByEmail(String email);
}
