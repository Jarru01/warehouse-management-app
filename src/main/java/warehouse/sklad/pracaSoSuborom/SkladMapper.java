package warehouse.sklad.pracaSoSuborom;

import warehouse.sklad.Sklad;
import warehouse.sklad.miestnosti.MalySklad;
import warehouse.sklad.miestnosti.Miestnost;
import warehouse.sklad.miestnosti.VelkySklad;
import warehouse.sklad.osoby.Pracovnik;
import warehouse.sklad.osoby.Riaditel;
import warehouse.sklad.osoby.Zakaznik;
import warehouse.sklad.predmety.Regal;
import warehouse.sklad.predmety.Tovar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Trieda SkladMapper prevadza sklad medzi doménovym modelom a datovym modelom ukladanym do JSON suboru.
 * @author Juraj
 */
public final class SkladMapper {
    public static final int VERZIA = 1;
    public static final String TYP_VELKY = "velky";
    public static final String TYP_MALY = "maly";

    private SkladMapper() {
    }

    /**
     * Prevedie sklad na datovy model. Zoznamy su zotriedene, aby bol obsah suboru deterministicky.
     * @param sklad sklad na prevod
     * @return datovy model skladu
     */
    public static SkladData naData(Sklad sklad) {
        List<MiestnostData> miestnosti = new ArrayList<>();
        Map<String, TovarData> tovary = new TreeMap<>();

        for (Miestnost miestnost : new TreeMap<>(sklad.getZoznamMiestnosti()).values()) {
            List<RegalData> regale = new ArrayList<>();
            for (Regal regal : zoznamRegalov(miestnost)) {
                List<String> tovarIds = new ArrayList<>();
                for (Tovar tovar : regal.getZoznamTovaru()) {
                    if (tovar != null) {
                        tovarIds.add(tovar.getId());
                        tovary.putIfAbsent(tovar.getId(), naTovarData(tovar));
                    }
                }
                regale.add(new RegalData(regal.getKapacita(), tovarIds));
            }
            miestnosti.add(new MiestnostData(miestnost.getPopisMiestnosti(), typMiestnosti(miestnost), regale));
        }

        List<PracovnikData> pracovnici = new ArrayList<>();
        for (Pracovnik pracovnik : new TreeMap<>(sklad.getZoznamPracovnikov()).values()) {
            String miestnost = pracovnik.getAktualnaMiestnost() == null
                    ? null : pracovnik.getAktualnaMiestnost().getPopisMiestnosti();
            String drzanyTovar = pracovnik.getAktualnyTovar() == null
                    ? null : pracovnik.getAktualnyTovar().getId();
            if (pracovnik.getAktualnyTovar() != null) {
                tovary.putIfAbsent(pracovnik.getAktualnyTovar().getId(),
                        naTovarData(pracovnik.getAktualnyTovar()));
            }
            pracovnici.add(new PracovnikData(pracovnik.getId(), pracovnik.getMeno(), pracovnik.getPriezvisko(),
                    miestnost, drzanyTovar));
        }

        return new SkladData(VERZIA, naOsobaData(sklad.getRiaditelSkladu()), miestnosti,
                new ArrayList<>(tovary.values()), pracovnici);
    }

    /**
     * Zostroji sklad z datoveho modelu. Neplatne alebo nekonzistentne data sposobia vynimku.
     * @param data datovy model skladu
     * @return zostrojeny sklad
     */
    public static Sklad naSklad(SkladData data) {
        if (data == null) {
            throw new PerzistenciaException("Subor neobsahuje data skladu.");
        }
        if (data.verzia() != VERZIA) {
            throw new PerzistenciaException("Nepodporovana verzia suboru: " + data.verzia());
        }
        if (data.riaditel() == null) {
            throw new PerzistenciaException("V subore chyba riaditel skladu.");
        }

        Sklad sklad = Sklad.prazdny(data.riaditel().meno(), data.riaditel().priezvisko(), data.riaditel().id());

        Map<Regal, RegalData> regale = new HashMap<>();
        for (MiestnostData miestnostData : prazdnyZoznam(data.miestnosti())) {
            vytvorMiestnost(sklad, miestnostData, regale);
        }
        overPovinneMiestnosti(sklad);

        Map<String, Tovar> tovary = new HashMap<>();
        for (TovarData tovarData : prazdnyZoznam(data.tovar())) {
            if (tovary.containsKey(tovarData.id())) {
                throw new PerzistenciaException("Duplicitne ID tovaru: " + tovarData.id());
            }
            tovary.put(tovarData.id(), vytvorTovar(tovarData, sklad));
        }

        for (Map.Entry<Regal, RegalData> vstup : regale.entrySet()) {
            for (String idTovaru : prazdnyZoznam(vstup.getValue().tovar())) {
                Tovar tovar = tovary.get(idTovaru);
                if (tovar == null) {
                    throw new PerzistenciaException("V regali je neznamy tovar: " + idTovaru);
                }
                if (!vstup.getKey().ulozTovar(tovar)) {
                    throw new PerzistenciaException("Tovar " + idTovaru + " sa nezmestil do regala.");
                }
            }
        }

        for (PracovnikData pracovnikData : prazdnyZoznam(data.pracovnici())) {
            vytvorPracovnika(pracovnikData, sklad, tovary);
        }
        return sklad;
    }

    private static void vytvorMiestnost(Sklad sklad, MiestnostData data, Map<Regal, RegalData> regale) {
        if (data.kluc() == null || data.kluc().isEmpty()) {
            throw new PerzistenciaException("Miestnost bez kluca.");
        }
        Miestnost miestnost;
        if (TYP_VELKY.equals(data.typ())) {
            VelkySklad velkySklad = new VelkySklad(data.kluc());
            for (RegalData regalData : prazdnyZoznam(data.regale())) {
                if (!velkySklad.pridajRegal(regalData.kapacita())) {
                    throw new PerzistenciaException("Neplatna kapacita regala v miestnosti " + data.kluc() + ".");
                }
                regale.put(velkySklad.getRegal(velkySklad.getZoznamRegalov().size()), regalData);
            }
            miestnost = velkySklad;
        } else if (TYP_MALY.equals(data.typ())) {
            MalySklad malySklad = new MalySklad(data.kluc());
            if (prazdnyZoznam(data.regale()).size() != 1) {
                throw new PerzistenciaException("Mala miestnost " + data.kluc() + " musi mat prave jeden regal.");
            }
            RegalData regalData = data.regale().get(0);
            if (regalData.kapacita() != malySklad.getRegal().getKapacita()) {
                throw new PerzistenciaException("Neplatna kapacita regala v miestnosti " + data.kluc() + ".");
            }
            regale.put(malySklad.getRegal(), regalData);
            miestnost = malySklad;
        } else {
            throw new PerzistenciaException("Neznamy typ miestnosti: " + data.typ());
        }
        if (!sklad.pridajMiestnost(miestnost)) {
            throw new PerzistenciaException("Duplicitna miestnost: " + data.kluc());
        }
    }

    private static void overPovinneMiestnosti(Sklad sklad) {
        if (!(sklad.getMiestnost(Sklad.MIESTNOST_SKLAD_TOVARU) instanceof VelkySklad)
                || !(sklad.getMiestnost(Sklad.MIESTNOST_PRIJEM_TOVARU) instanceof MalySklad)
                || !(sklad.getMiestnost(Sklad.MIESTNOST_VYDAJ_TOVARU) instanceof MalySklad)) {
            throw new PerzistenciaException("V subore chybaju povinne miestnosti skladu.");
        }
    }

    private static Tovar vytvorTovar(TovarData data, Sklad sklad) {
        if (data.id() == null || data.odosielatel() == null || data.prijemca() == null) {
            throw new PerzistenciaException("Tovar s neuplnymi udajmi.");
        }
        return new Tovar(data.id(), data.nazov(), data.vaha(),
                naZakaznik(data.odosielatel(), sklad), naZakaznik(data.prijemca(), sklad));
    }

    private static void vytvorPracovnika(PracovnikData data, Sklad sklad, Map<String, Tovar> tovary) {
        Miestnost miestnost = sklad.getMiestnost(data.miestnost());
        if (miestnost == null) {
            throw new PerzistenciaException("Pracovnik " + data.id() + " je v neexistujucej miestnosti.");
        }
        Tovar drzanyTovar = null;
        if (data.drzanyTovar() != null) {
            drzanyTovar = tovary.get(data.drzanyTovar());
            if (drzanyTovar == null) {
                throw new PerzistenciaException("Pracovnik " + data.id() + " drzi neznamy tovar.");
            }
        }
        Pracovnik pracovnik = new Pracovnik(data.meno(), data.priezvisko(), sklad, data.id());
        if (!sklad.registrujPracovnika(pracovnik)) {
            throw new PerzistenciaException("Duplicitny pracovnik: " + data.id());
        }
        pracovnik.obnovStav(miestnost, drzanyTovar);
    }

    private static TovarData naTovarData(Tovar tovar) {
        return new TovarData(tovar.getId(), tovar.getNazovTovaru(), tovar.getVaha(),
                naOsobaData(tovar.getOdosielatel()), naOsobaData(tovar.getPrijemca()));
    }

    private static OsobaData naOsobaData(Zakaznik zakaznik) {
        return new OsobaData(zakaznik.getId(), zakaznik.getMeno(), zakaznik.getPriezvisko());
    }

    private static OsobaData naOsobaData(Riaditel riaditel) {
        return new OsobaData(riaditel.getId(), riaditel.getMeno(), riaditel.getPriezvisko());
    }

    private static Zakaznik naZakaznik(OsobaData data, Sklad sklad) {
        return new Zakaznik(data.id(), data.meno(), data.priezvisko(),
                sklad.getPrijemTovaru(), sklad.getVydajTovaru());
    }

    private static List<Regal> zoznamRegalov(Miestnost miestnost) {
        if (miestnost instanceof VelkySklad velkySklad) {
            return velkySklad.getZoznamRegalov();
        }
        if (miestnost instanceof MalySklad malySklad) {
            return List.of(malySklad.getRegal());
        }
        throw new PerzistenciaException("Neznamy typ miestnosti: " + miestnost.getPopisMiestnosti());
    }

    private static String typMiestnosti(Miestnost miestnost) {
        if (miestnost instanceof VelkySklad) {
            return TYP_VELKY;
        }
        if (miestnost instanceof MalySklad) {
            return TYP_MALY;
        }
        throw new PerzistenciaException("Neznamy typ miestnosti: " + miestnost.getPopisMiestnosti());
    }

    private static <T> List<T> prazdnyZoznam(List<T> zoznam) {
        return zoznam == null ? List.of() : zoznam;
    }
}
