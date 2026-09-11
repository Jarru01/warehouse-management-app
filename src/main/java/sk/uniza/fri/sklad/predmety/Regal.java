package sk.uniza.fri.sklad.predmety;

import java.io.Serializable;

/**
 * Trieda Regal vytvara regal so zadanou kapacitou, ktory uschovava tovar.
 * @author Juraj
 */
public class Regal implements Serializable {
    private static final long serialVersionUID = 2L;

    private final Tovar[] zoznamTovaru;   //zoznam tovaru v regali

    /**
     * Vytvori regal so zadanou kapacitou, bez tovaru.
     * @param kapacita kapacita regala
     */
    public Regal(int kapacita) {
        if (kapacita < 1) {
            throw new IllegalArgumentException("Kapacita regala musi byt kladna.");
        }
        this.zoznamTovaru = new Tovar[kapacita];
    }

    /**
     * Vracia kopiu zoznamu tovaru v regali.
     * @return zoznam tovaru
     */
    public Tovar[] getZoznamTovaru() {
        return this.zoznamTovaru.clone();
    }

    /**
     * Vracia konkretny tovar s danym ID alebo null, ak sa v regali nenachadza.
     * @param id id tovaru
     * @return tovar alebo null
     */
    public Tovar getTovar(String id) {
        for (Tovar tovar : this.zoznamTovaru) {
            if (tovar != null && tovar.getId().equals(id)) {
                return tovar;
            }
        }
        return null;
    }

    /**
     * Ulozi tovar z parametra do prveho volneho miesta v regali.
     * @param tovar tovar na ulozenie
     * @return true ak bol tovar ulozeny
     */
    public boolean ulozTovar(Tovar tovar) {
        if (tovar == null) {
            return false;
        }
        for (int i = 0; i < this.zoznamTovaru.length; i++) {
            if (this.zoznamTovaru[i] == null) {
                this.zoznamTovaru[i] = tovar;
                return true;
            }
        }
        return false;
    }

    /**
     * Odoberie tovar so zadanym ID z regalu.
     * @param id id tovaru
     * @return true ak bol tovar odobrany
     */
    public boolean odoberTovar(String id) {
        for (int i = 0; i < this.zoznamTovaru.length; i++) {
            if (this.zoznamTovaru[i] != null && this.zoznamTovaru[i].getId().equals(id)) {
                this.zoznamTovaru[i] = null;
                return true;
            }
        }
        return false;
    }

    /**
     * Zisti ci ma regal volne miesto.
     * @return boolean
     */
    public boolean maVolneMiesto() {
        for (Tovar tovar : this.zoznamTovaru) {
            if (tovar == null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Zisti ci je regal prazdny.
     * @return boolean
     */
    public boolean jePrazdny() {
        for (Tovar tovar : this.zoznamTovaru) {
            if (tovar != null) {
                return false;
            }
        }
        return true;
    }

    /**
     * Vrati string obsahujuci informacie o tovare v regali.
     * @return string zoznam tovaru v regali
     */
    @Override
    public String toString() {
        StringBuilder zoznam = new StringBuilder();
        for (int i = 0; i < this.zoznamTovaru.length; i++) {
            if (this.zoznamTovaru[i] != null) {
                zoznam.append(i + 1).append(". ").append(this.zoznamTovaru[i]).append('\n');
            } else {
                zoznam.append(i + 1).append(". ").append("[-]").append('\n');
            }
        }
        return "Regal(kapacita " + this.zoznamTovaru.length + "), zoznam tovaru:" + '\n'
                + zoznam.toString().indent(2);
    }
}
