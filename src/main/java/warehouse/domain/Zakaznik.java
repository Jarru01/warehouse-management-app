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

    @Column(length = 254, unique = true)
    private String email;

    protected Zakaznik() {
    }

    /**
     * Vytvori zakaznika z parametrov.
     * @param id id zakaznika
     * @param meno meno zakaznika
     * @param priezvisko priezvisko zakaznika
     * @param email e-mail na obnovu hesla, ulozi sa malymi pismenami
     */
    public Zakaznik(String id, String meno, String priezvisko, String email) {
        super(meno, priezvisko);
        this.id = id;
        this.email = normalizujEmail(email);
    }

    /**
     * Upravi e-mail do jednotneho tvaru na ukladanie a vyhladavanie.
     * @param email e-mail alebo null
     * @return e-mail bez okrajovych medzier a malymi pismenami, alebo null
     */
    public static String normalizujEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(java.util.Locale.ROOT);
    }

    /**
     * Vrati e-mail zakaznika.
     * @return e-mail alebo null pri ucte z obdobia pred zavedenim e-mailov
     */
    public String getEmail() {
        return this.email;
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
