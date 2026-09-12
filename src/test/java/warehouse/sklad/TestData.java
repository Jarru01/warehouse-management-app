package warehouse.sklad;

import warehouse.sklad.miestnosti.MalySklad;
import warehouse.sklad.osoby.Zakaznik;
import warehouse.sklad.predmety.Tovar;

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
