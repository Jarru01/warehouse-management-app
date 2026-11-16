package warehouse.security;

import org.springframework.security.core.AuthenticatedPrincipal;

import java.io.Serializable;

/**
 * Prihlaseny pouzivatel ulozeny v bezpecnostnom kontexte.
 * @param rola rola pouzivatela
 * @param id id pouzivatela v ramci roly
 * @param meno meno pouzivatela
 * @param priezvisko priezvisko pouzivatela
 * @param zmenaHesla true, ak sa prihlasil docasnym heslom a musi si ho zmenit
 * @author Juraj
 */
public record PrihlasenyPouzivatel(Rola rola, String id, String meno, String priezvisko, boolean zmenaHesla)
        implements AuthenticatedPrincipal, Serializable {

    /**
     * Vrati jednoznacne meno prihlasenia v tvare ROLA:id, pouziva sa napriklad v audite.
     * @return rola a id
     */
    @Override
    public String getName() {
        return this.rola.name() + ":" + this.id;
    }

    /**
     * Vrati toho isteho pouzivatela po zmene hesla.
     * @return pouzivatel bez povinnosti zmenit heslo
     */
    public PrihlasenyPouzivatel poZmeneHesla() {
        return new PrihlasenyPouzivatel(this.rola, this.id, this.meno, this.priezvisko, false);
    }
}
