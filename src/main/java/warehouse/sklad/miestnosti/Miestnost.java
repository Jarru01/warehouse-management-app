package warehouse.sklad.miestnosti;

import warehouse.sklad.osoby.Pracovnik;

import java.util.HashMap;
import java.util.Map;

/**
 * Zakladna miestnost ktora ma svoj nazov a obsahuje zoznam pracovnikov ktori sa v nej nachadzaju, s ktorymi miestnost
 * manipuluje.
 * @author Juraj
 */
public abstract class Miestnost {
    private final Map<String, Pracovnik> zoznamPracovnikov; //zoznam pracovnikov v miestnosti
    private final String popisMiestnosti;                    //popis miestnosti

    /**
     * Vytvori novu miestnost bez pracovnikov s nazvom z parametra.
     * @param popisMiestnosti nazov miestnosti
     */
    public Miestnost(String popisMiestnosti) {
        this.zoznamPracovnikov = new HashMap<>();
        this.popisMiestnosti = popisMiestnosti;
    }

    /**
     * Vrati nazov/popis miestnosti.
     * @return nazov/popis miestnosti
     */
    public String getPopisMiestnosti() {
        return this.popisMiestnosti;
    }

    /**
     * Vrati string so zoznamom pracovnikov v miestnosti.
     * @return string so zoznamom pracovnikov v miestnosti
     */
    public String vypisZoznamuPracovnikov() {
        StringBuilder zoznam = new StringBuilder();
        int i = 1;
        for (Pracovnik pracovnik : this.zoznamPracovnikov.values()) {
            zoznam.append(i).append(". ").append(pracovnik).append('\n');
            i++;
        }
        return "Vypis pracovnikov v miestnosti " + this.getPopisMiestnosti() + ":" + '\n'
                + zoznam.toString().indent(2);
    }

    /**
     * Pokusi sa pridat pracovnika z parametra do miestnosti.
     * @param pracovnik pracovnik na pridatie
     * @return true ak bol pracovnik pridany
     */
    public boolean pridajPracovnika(Pracovnik pracovnik) {
        if (pracovnik == null || this.zoznamPracovnikov.containsKey(pracovnik.getId())) {
            return false;
        }
        this.zoznamPracovnikov.put(pracovnik.getId(), pracovnik);
        return true;
    }

    /**
     * Pokusi sa odobrat pracovnika z parametra z miestnosti.
     * @param pracovnik pracovnik na odobratie
     * @return true ak bol pracovnik odobrany
     */
    public boolean odoberPracovnika(Pracovnik pracovnik) {
        if (pracovnik == null || !this.zoznamPracovnikov.containsKey(pracovnik.getId())) {
            return false;
        }
        this.zoznamPracovnikov.remove(pracovnik.getId());
        return true;
    }
}
