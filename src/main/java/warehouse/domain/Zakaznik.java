package warehouse.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Registrovany zakaznik, ktory posiela a vyzdvihuje tovar.
 * @author Juraj
 */
@Entity
@Table(name = "zakaznik")
public class Zakaznik extends Osoba {

    @Id
    @Column(nullable = false, length = 50)
    private String id;

    protected Zakaznik() {
    }

    /**
     * Vytvori zakaznika z parametrov.
     * @param id id zakaznika
     * @param meno meno zakaznika
     * @param priezvisko priezvisko zakaznika
     */
    public Zakaznik(String id, String meno, String priezvisko) {
        super(meno, priezvisko);
        this.id = id;
    }

    /**
     * Vrati id zakaznika.
     * @return id zakaznika
     */
    public String getId() {
        return this.id;
    }

    /**
     * Vrati udaje zakaznika v podobe, ktora sa uklada pri tovare.
     * @return udaje zakaznika
     */
    public ZakaznikInfo naInfo() {
        return new ZakaznikInfo(this.id, this.getMeno(), this.getPriezvisko());
    }
}
