package warehouse.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Jeden zaznam auditu: kto, kedy a co urobil. Zaznamy sa nikdy nemenia.
 * @author Juraj
 */
@Entity
@Table(name = "audit_zaznam")
public class AuditZaznam {
    /** Najvacsia dlzka detailu v znakoch. */
    public static final int MAX_DLZKA_DETAILU = 500;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Instant cas;

    @Column(nullable = false, length = 80)
    private String pouzivatel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AuditAkcia akcia;

    @Column(length = MAX_DLZKA_DETAILU)
    private String detail;

    protected AuditZaznam() {
    }

    /**
     * Vytvori zaznam auditu.
     * @param cas cas udalosti
     * @param pouzivatel kto udalost vykonal, napriklad RIADITEL:123
     * @param akcia druh udalosti
     * @param detail doplnujuci opis alebo null
     */
    public AuditZaznam(Instant cas, String pouzivatel, AuditAkcia akcia, String detail) {
        this.cas = cas;
        this.pouzivatel = pouzivatel;
        this.akcia = akcia;
        this.detail = detail == null || detail.length() <= MAX_DLZKA_DETAILU
                ? detail
                : detail.substring(0, MAX_DLZKA_DETAILU);
    }

    /**
     * Vrati id zaznamu.
     * @return id zaznamu
     */
    public Long getId() {
        return this.id;
    }

    /**
     * Vrati cas udalosti.
     * @return cas udalosti
     */
    public Instant getCas() {
        return this.cas;
    }

    /**
     * Vrati, kto udalost vykonal.
     * @return rola a id alebo "anonym"
     */
    public String getPouzivatel() {
        return this.pouzivatel;
    }

    /**
     * Vrati druh udalosti.
     * @return druh udalosti
     */
    public AuditAkcia getAkcia() {
        return this.akcia;
    }

    /**
     * Vrati doplnujuci opis udalosti.
     * @return opis alebo null
     */
    public String getDetail() {
        return this.detail;
    }
}
