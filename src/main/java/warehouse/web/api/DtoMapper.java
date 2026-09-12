package warehouse.web.api;

import org.springframework.stereotype.Component;
import warehouse.domain.Miestnost;
import warehouse.domain.Pracovnik;
import warehouse.domain.Regal;
import warehouse.domain.Riaditel;
import warehouse.domain.Tovar;
import warehouse.domain.ZakaznikInfo;
import warehouse.web.api.dto.MiestnostResponse;
import warehouse.web.api.dto.PracovnikResponse;
import warehouse.web.api.dto.RegalResponse;
import warehouse.web.api.dto.RiaditelResponse;
import warehouse.web.api.dto.TovarResponse;
import warehouse.web.api.dto.ZakaznikResponse;

import java.util.List;

/**
 * Prevod doménovych entit na DTO odpovede.
 * @author Juraj
 */
@Component
public class DtoMapper {

    /**
     * Prevedie riaditela na odpoved.
     * @param riaditel riaditel
     * @return odpoved
     */
    public RiaditelResponse naRiaditela(Riaditel riaditel) {
        return new RiaditelResponse(riaditel.getId(), riaditel.getMeno(), riaditel.getPriezvisko());
    }

    /**
     * Prevedie pracovnika na odpoved.
     * @param pracovnik pracovnik
     * @return odpoved
     */
    public PracovnikResponse naPracovnika(Pracovnik pracovnik) {
        return new PracovnikResponse(pracovnik.getId(), pracovnik.getMeno(), pracovnik.getPriezvisko(),
                pracovnik.getMiestnost() == null ? null : pracovnik.getMiestnost().getKluc(),
                pracovnik.getDrzanyTovar() == null ? null : pracovnik.getDrzanyTovar().getId());
    }

    /**
     * Prevedie tovar na odpoved.
     * @param tovar tovar
     * @return odpoved
     */
    public TovarResponse naTovar(Tovar tovar) {
        return new TovarResponse(tovar.getId(), tovar.getNazov(), tovar.getVaha(),
                this.naZakaznika(tovar.getOdosielatel()), this.naZakaznika(tovar.getPrijemca()),
                tovar.getRegal() == null ? null : tovar.getRegal().getMiestnost().getKluc(),
                tovar.getRegal() == null ? null : tovar.getRegal().getId(),
                tovar.getSlot());
    }

    /**
     * Prevedie udaje zakaznika na odpoved.
     * @param zakaznik udaje zakaznika
     * @return odpoved
     */
    public ZakaznikResponse naZakaznika(ZakaznikInfo zakaznik) {
        return new ZakaznikResponse(zakaznik.getId(), zakaznik.getMeno(), zakaznik.getPriezvisko());
    }

    /**
     * Prevedie miestnost na odpoved.
     * @param miestnost miestnost
     * @return odpoved
     */
    public MiestnostResponse naMiestnost(Miestnost miestnost) {
        return new MiestnostResponse(miestnost.getKluc(), miestnost.getTyp().name());
    }

    /**
     * Prevedie regal a jeho tovar na odpoved.
     * @param regal regal
     * @param tovar tovar v regali
     * @return odpoved
     */
    public RegalResponse naRegal(Regal regal, List<Tovar> tovar) {
        return new RegalResponse(regal.getId(), regal.getMiestnost().getKluc(), regal.getKapacita(),
                tovar.stream().map(this::naTovar).toList());
    }
}
