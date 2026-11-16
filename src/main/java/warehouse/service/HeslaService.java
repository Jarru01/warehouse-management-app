package warehouse.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import warehouse.domain.NeplatnaOperaciaException;

import java.nio.charset.StandardCharsets;

/**
 * Pravidla pre hesla a ich hashovanie na jednom mieste.
 * @author Juraj
 */
@Service
public class HeslaService {
    /** Najmensia dlzka hesla v znakoch. */
    public static final int MIN_DLZKA_HESLA = 8;
    /** Najvacsia dlzka hesla v bajtoch UTF-8, viac BCrypt nespracuje. */
    public static final int MAX_BAJTOV_HESLA = 72;

    private final PasswordEncoder passwordEncoder;

    /**
     * Vytvori servis s kodovacom hesiel.
     * @param passwordEncoder kodovac hesiel
     */
    public HeslaService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Overi pravidla hesla a vrati jeho hash.
     * @param heslo heslo v citatelnej podobe
     * @return hash hesla
     */
    public String zahesuj(String heslo) {
        if (heslo == null || heslo.length() < MIN_DLZKA_HESLA) {
            throw new NeplatnaOperaciaException("Heslo musi mat aspon " + MIN_DLZKA_HESLA + " znakov.");
        }
        if (heslo.getBytes(StandardCharsets.UTF_8).length > MAX_BAJTOV_HESLA) {
            throw new NeplatnaOperaciaException("Heslo je prilis dlhe.");
        }
        return this.passwordEncoder.encode(heslo);
    }

    /**
     * Porovna heslo s hashom. Neplatny vstup sa povazuje za nezhodu.
     * @param heslo heslo v citatelnej podobe
     * @param hash ulozeny hash alebo null
     * @return true, ak heslo zodpoveda hashu
     */
    public boolean zhoduje(String heslo, String hash) {
        if (heslo == null || hash == null) {
            return false;
        }
        try {
            return this.passwordEncoder.matches(heslo, hash);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
