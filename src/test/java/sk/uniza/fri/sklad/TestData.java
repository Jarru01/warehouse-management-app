package sk.uniza.fri.sklad;

import sk.uniza.fri.sklad.miestnosti.MalySklad;
import sk.uniza.fri.sklad.osoby.Zakaznik;
import sk.uniza.fri.sklad.predmety.Tovar;

public final class TestData {
    private TestData() {
    }

    public static Zakaznik zakaznik() {
        return new Zakaznik("1", "Jan", "Testovaci",
                new MalySklad(Sklad.MIESTNOST_PRIJEM_TOVARU), new MalySklad(Sklad.MIESTNOST_VYDAJ_TOVARU));
    }

    public static Tovar tovar(String id) {
        Zakaznik zakaznik = zakaznik();
        return new Tovar(id, "Tovar" + id, 100, zakaznik, zakaznik);
    }
}
