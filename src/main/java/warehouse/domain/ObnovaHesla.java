package warehouse.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Jednorazova ziadost o obnovu hesla zakaznika. Uklada sa iba hash tokenu, samotny token je len v e-maile.
 * @author Juraj
 */
@Entity
@Table(name = "obnova_hesla")
public class ObnovaHesla {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "zakaznik_id", nullable = false, length = 50)
    private String zakaznikId;

    @Column(name = "token_hash", nullable = false, length = 64, unique = true)
    private String tokenHash;

    @Column(nullable = false)
    private Instant vytvorene;

    @Column(name = "plati_do", nullable = false)
    private Instant platiDo;

    @Column(nullable = false)
    private boolean pouzite;

    protected ObnovaHesla() {
    }

    /**
     * Vytvori ziadost o obnovu hesla.
     * @param zakaznikId id zakaznika
     * @param tokenHash SHA-256 hash tokenu v hexadecimalnom tvare
     * @param vytvorene cas vytvorenia
     * @param platiDo cas, do ktoreho je token platny
     */
    public ObnovaHesla(String zakaznikId, String tokenHash, Instant vytvorene, Instant platiDo) {
        this.zakaznikId = zakaznikId;
        this.tokenHash = tokenHash;
        this.vytvorene = vytvorene;
        this.platiDo = platiDo;
    }

    /**
     * Zisti, ci sa token este da pouzit.
     * @param teraz aktualny cas
     * @return true, ak token nie je pouzity ani expirovany
     */
    public boolean jePlatna(Instant teraz) {
        return !this.pouzite && teraz.isBefore(this.platiDo);
    }

    /**
     * Oznaci token ako pouzity.
     */
    public void pouzi() {
        this.pouzite = true;
    }

    /**
     * Vrati id zakaznika.
     * @return id zakaznika
     */
    public String getZakaznikId() {
        return this.zakaznikId;
    }

    /**
     * Vrati cas vytvorenia ziadosti.
     * @return cas vytvorenia
     */
    public Instant getVytvorene() {
        return this.vytvorene;
    }
}
