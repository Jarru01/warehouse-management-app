package sk.uniza.fri.sklad.predmety;

import sk.uniza.fri.sklad.IIdentifikovatelny;
import sk.uniza.fri.sklad.osoby.Zakaznik;

/**
 * Tovar reprezentuje predmet ktory vytvara zakaznik a uschovava sa v sklade.
 * @author Juraj
 */
public class Tovar implements IIdentifikovatelny {
    private final String id;            //id tovaru
    private final String nazovTovaru;   //nazov tovaru
    private final int vaha;             //vaha tovaru
    private final Zakaznik odosielatel; //odosielatel tovaru
    private final Zakaznik prijemca;    //prijemca tovaru

    /**
     * Inicializuje atributy tovaru z parametra, vytvara tovar.
     * @param id id tovaru
     * @param nazovTovaru nazov tovaru
     * @param vaha vaha tovaru
     * @param odosielatel odosielatel tovaru
     * @param prijemca prijemca ktoremu je tovar adresovany
     */
    public Tovar(String id, String nazovTovaru, int vaha, Zakaznik odosielatel, Zakaznik prijemca) {
        this.id = id;
        this.nazovTovaru = nazovTovaru;
        this.vaha = vaha;
        this.odosielatel = odosielatel;
        this.prijemca = prijemca;
    }

    /**
     * Vrati odosielatela tovaru.
     * @return odosielatel tovaru
     */
    public Zakaznik getOdosielatel() {
        return this.odosielatel;
    }

    /**
     * Vrati prijemcu tovaru.
     * @return prijemca tovaru
     */
    public Zakaznik getPrijemca() {
        return this.prijemca;
    }

    /**
     * Vrati nazov tovaru.
     * @return nazov tovaru
     */
    public String getNazovTovaru() {
        return this.nazovTovaru;
    }

    /**
     * Vrati vahu tovaru v gramoch.
     * @return vaha tovaru
     */
    public int getVaha() {
        return this.vaha;
    }

    /**
     * Vrati id tovaru.
     * @return id tovaru
     */
    @Override
    public String getId() {
        return this.id;
    }

    /**
     * Vrati string s informaciami o tovare.
     * @return string s informaciami o tovare
     */
    @Override
    public String toString() {
        return "[ID: " + this.id + ", " + "Nazov tovaru: " + this.nazovTovaru + ", " + "Vaha: " + this.vaha + "g, " +
                "Odosielatel: " + this.odosielatel + ", Prijemca: " + this.prijemca + "]";
    }
}
