package warehouse.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Tovar ulozeny v regali alebo drzany pracovnikom.
 * @author Juraj
 */
@Entity
@Table(name = "tovar", uniqueConstraints = @UniqueConstraint(columnNames = {"regal_id", "slot"}))
public class Tovar {

    @Id
    @Column(nullable = false, length = 50)
    private String id;

    @Column(nullable = false, length = 100)
    private String nazov;

    @Column(nullable = false)
    private int vaha;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "id", column = @Column(name = "odosielatel_id", nullable = false, length = 50)),
            @AttributeOverride(name = "meno", column = @Column(name = "odosielatel_meno", nullable = false, length = 100)),
            @AttributeOverride(name = "priezvisko", column = @Column(name = "odosielatel_priezvisko", nullable = false, length = 100))
    })
    private ZakaznikInfo odosielatel;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "id", column = @Column(name = "prijemca_id", nullable = false, length = 50)),
            @AttributeOverride(name = "meno", column = @Column(name = "prijemca_meno", nullable = false, length = 100)),
            @AttributeOverride(name = "priezvisko", column = @Column(name = "prijemca_priezvisko", nullable = false, length = 100))
    })
    private ZakaznikInfo prijemca;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "regal_id")
    private Regal regal;

    @Column(name = "slot")
    private Integer slot;

    protected Tovar() {
    }

    /**
     * Vytvori tovar z parametrov. Tovar nie je umiestneny v ziadnom regali.
     * @param id id tovaru
     * @param nazov nazov tovaru
     * @param vaha vaha tovaru v gramoch
     * @param odosielatel odosielatel tovaru
     * @param prijemca prijemca tovaru
     */
    public Tovar(String id, String nazov, int vaha, ZakaznikInfo odosielatel, ZakaznikInfo prijemca) {
        this.id = id;
        this.nazov = nazov;
        this.vaha = vaha;
        this.odosielatel = odosielatel;
        this.prijemca = prijemca;
    }

    /**
     * Ulozi tovar do regalu na dany slot.
     * @param regal regal, do ktoreho sa tovar uklada
     * @param slot poradove cislo slotu v regali
     */
    public void ulozDoRegalu(Regal regal, int slot) {
        this.regal = regal;
        this.slot = slot;
    }

    /**
     * Vyberie tovar z regalu.
     */
    public void vyberZRegalu() {
        this.regal = null;
        this.slot = null;
    }

    /**
     * Vrati id tovaru.
     * @return id tovaru
     */
    public String getId() {
        return this.id;
    }

    /**
     * Vrati nazov tovaru.
     * @return nazov tovaru
     */
    public String getNazov() {
        return this.nazov;
    }

    /**
     * Vrati vahu tovaru v gramoch.
     * @return vaha tovaru
     */
    public int getVaha() {
        return this.vaha;
    }

    /**
     * Vrati udaje o odosielatelovi.
     * @return odosielatel
     */
    public ZakaznikInfo getOdosielatel() {
        return this.odosielatel;
    }

    /**
     * Vrati udaje o prijemcovi.
     * @return prijemca
     */
    public ZakaznikInfo getPrijemca() {
        return this.prijemca;
    }

    /**
     * Vrati regal, v ktorom je tovar ulozeny, alebo null.
     * @return regal alebo null
     */
    public Regal getRegal() {
        return this.regal;
    }

    /**
     * Vrati slot v regali alebo null.
     * @return slot alebo null
     */
    public Integer getSlot() {
        return this.slot;
    }
}
