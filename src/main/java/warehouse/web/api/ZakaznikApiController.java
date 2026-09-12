package warehouse.web.api;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import warehouse.domain.Tovar;
import warehouse.domain.ZakaznikInfo;
import warehouse.service.SkladService;
import warehouse.web.api.dto.TovarResponse;
import warehouse.web.api.dto.VlozitTovarRequest;
import warehouse.web.api.dto.ZakaznikRequest;

/**
 * REST rozhranie pre zakaznika.
 * @author Juraj
 */
@RestController
@RequestMapping("/api/zakaznik")
public class ZakaznikApiController {
    private final SkladService skladService;
    private final DtoMapper mapper;

    /**
     * Vytvori kontroler so servisom a mapovacom.
     * @param skladService servis skladu
     * @param mapper prevodnik na DTO
     */
    public ZakaznikApiController(SkladService skladService, DtoMapper mapper) {
        this.skladService = skladService;
        this.mapper = mapper;
    }

    /**
     * Vytvori tovar a ulozi ho do miestnosti na prijem tovaru.
     * @param request poziadavka na vytvorenie tovaru
     * @return vytvoreny tovar
     */
    @PostMapping("/tovar")
    @ResponseStatus(HttpStatus.CREATED)
    public TovarResponse vytvorTovar(@Valid @RequestBody VlozitTovarRequest request) {
        Tovar tovar = this.skladService.vytvorTovar(request.id(), request.nazov(), request.vaha(),
                this.naZakaznika(request.odosielatel()), this.naZakaznika(request.prijemca()));
        return this.mapper.naTovar(tovar);
    }

    /**
     * Zakaznik vyzdvihne svoj tovar z miestnosti na vydaj tovaru.
     * @param zakaznikId id zakaznika
     * @param tovarId id tovaru
     */
    @PostMapping("/{zakaznikId}/vyzdvihnutie/{tovarId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void vyzdvihniTovar(@PathVariable String zakaznikId, @PathVariable String tovarId) {
        this.skladService.vyzdvihniTovar(tovarId, zakaznikId);
    }

    private ZakaznikInfo naZakaznika(ZakaznikRequest request) {
        return new ZakaznikInfo(request.id(), request.meno(), request.priezvisko());
    }
}
