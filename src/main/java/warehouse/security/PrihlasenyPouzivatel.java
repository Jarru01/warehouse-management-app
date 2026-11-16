package warehouse.security;

import java.io.Serializable;

/**
 * Prihlaseny pouzivatel ulozeny v bezpecnostnom kontexte.
 * @param rola rola pouzivatela
 * @param id id pouzivatela v ramci roly
 * @param meno meno pouzivatela
 * @param priezvisko priezvisko pouzivatela
 * @author Juraj
 */
public record PrihlasenyPouzivatel(Rola rola, String id, String meno, String priezvisko) implements Serializable {
}
