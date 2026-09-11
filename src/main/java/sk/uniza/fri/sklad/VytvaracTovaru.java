package sk.uniza.fri.sklad;

import sk.uniza.fri.sklad.miestnosti.ISkladovaMiestnost;
import sk.uniza.fri.sklad.miestnosti.Miestnost;
import sk.uniza.fri.sklad.miestnosti.VelkySklad;
import sk.uniza.fri.sklad.osoby.Zakaznik;
import sk.uniza.fri.sklad.predmety.Regal;
import sk.uniza.fri.sklad.predmety.Tovar;
import sk.uniza.fri.sklad.terminaly.Vstup;

import java.util.function.Predicate;

/**
 * Trieda sluzi na vytvorenie tovaru pomocou konzolovych vyziev, do ktorych zakaznik zadava udaje o tovare.
 * @author Juraj
 */
public class VytvaracTovaru {
    private final Sklad sklad;   //sklad do ktoreho sa tovar uklada
    private final Vstup vstup;   //zdielany vstup konzoly

    /**
     * Naplni atributy z parametrov.
     * @param sklad sklad do ktoreho sa tovar uklada
     * @param vstup zdielany vstup konzoly
     */
    public VytvaracTovaru(Sklad sklad, Vstup vstup) {
        this.sklad = sklad;
        this.vstup = vstup;
    }

    /**
     * Vytvori novy tovar podla udajov zadanych pouzivatelom, ak to je mozne. Vstup "ukonci" tvorbu prerusi.
     * @param odosielatel prihlaseny zakaznik, ktory tovar posiela
     * @return vytvoreny tovar alebo null
     */
    public Tovar vytvorTovar(Zakaznik odosielatel) {
        if (!this.sklad.getPrijemTovaru().getRegal().maVolneMiesto()) {
            System.out.println("Regal v miestnosti na prijem tovaru je plny.");
            return null;
        }

        String id = this.nacitaj("Zadaj ID tovaru:", "Zadal si neplatne ID.", Validacia::jeCislo);
        if (id == null) {
            return null;
        }
        if (this.tovarExistuje(id)) {
            System.out.println("Tovar so zadanym ID uz existuje.");
            return null;
        }
        String nazov = this.nacitaj("Zadaj nazov tovaru:", "Zadal si neplatny nazov.", Validacia::jeNazov);
        if (nazov == null) {
            return null;
        }
        String vaha = this.nacitaj("Zadaj vahu tovaru (g):", "Zadal si neplatnu vahu.", Validacia::jeKladneCislo);
        if (vaha == null) {
            return null;
        }
        String idPrijemcu = this.nacitaj("Zadaj ID prijemcu:", "Zadal si neplatne ID.", Validacia::jeCislo);
        if (idPrijemcu == null) {
            return null;
        }
        String meno = this.nacitaj("Zadaj meno prijemcu:", "Zadal si neplatne meno.", Validacia::jeText);
        if (meno == null) {
            return null;
        }
        String priezvisko = this.nacitaj("Zadaj priezvisko prijemcu:", "Zadal si neplatne priezvisko.", Validacia::jeText);
        if (priezvisko == null) {
            return null;
        }

        Zakaznik prijemca = new Zakaznik(idPrijemcu, meno, priezvisko,
                this.sklad.getPrijemTovaru(), this.sklad.getVydajTovaru());
        return new Tovar(id, nazov, Integer.parseInt(vaha), odosielatel, prijemca);
    }

    /**
     * Nacita vstup a opakuje vyzvu, kym nie je zadany platny text. Vstup "ukonci" nacitavanie prerusi.
     * @param vyzva vyzva zobrazena pouzivatelovi
     * @param chybovaSprava sprava zobrazena pri neplatnom vstupe
     * @param validacia pravidlo platnosti vstupu
     * @return platny text alebo null pri ukonceni
     */
    private String nacitaj(String vyzva, String chybovaSprava, Predicate<String> validacia) {
        while (true) {
            System.out.println(vyzva);
            String vstup = this.vstup.nacitaj();
            if (vstup.equals("ukonci")) {
                return null;
            }
            if (validacia.test(vstup)) {
                return vstup;
            }
            System.out.println(chybovaSprava);
        }
    }

    /**
     * Overi ci sa tovar so zadanym id uz nachadza v sklade.
     * @param id id tovaru
     * @return true ak tovar so zadanym id existuje
     */
    private boolean tovarExistuje(String id) {
        for (Miestnost miestnost : this.sklad.getZoznamMiestnosti().values()) {
            if (miestnost instanceof VelkySklad) {
                for (Regal regal : ((VelkySklad)miestnost).getZoznamRegalov()) {
                    if (regal.getTovar(id) != null) {
                        return true;
                    }
                }
            } else if (((ISkladovaMiestnost)miestnost).getRegal().getTovar(id) != null) {
                return true;
            }
        }
        return false;
    }
}
