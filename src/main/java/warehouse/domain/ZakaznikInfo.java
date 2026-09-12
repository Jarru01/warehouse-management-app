package warehouse.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Udaje o zakaznikovi ulozene priamo pri tovare.
 * @author Juraj
 */
@Embeddable
public class ZakaznikInfo {

    @Column(nullable = false, length = 50)
    private String id;

    @Column(nullable = false, length = 100)
    private String meno;

    @Column(nullable = false, length = 100)
    private String priezvisko;

    protected ZakaznikInfo() {
    }

    /**
     * Vytvori udaje o zakaznikovi z parametrov.
     * @param id id zakaznika
     * @param meno meno zakaznika
     * @param priezvisko priezvisko zakaznika
     */
    public ZakaznikInfo(String id, String meno, String priezvisko) {
        this.id = id;
        this.meno = meno;
        this.priezvisko = priezvisko;
    }

    /**
     * Vrati id zakaznika.
     * @return id zakaznika
     */
    public String getId() {
        return this.id;
    }

    /**
     * Vrati meno zakaznika.
     * @return meno zakaznika
     */
    public String getMeno() {
        return this.meno;
    }

    /**
     * Vrati priezvisko zakaznika.
     * @return priezvisko zakaznika
     */
    public String getPriezvisko() {
        return this.priezvisko;
    }
}
