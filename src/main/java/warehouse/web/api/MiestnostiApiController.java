package warehouse.web.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import warehouse.domain.Regal;
import warehouse.service.SkladService;
import warehouse.web.api.dto.MiestnostResponse;
import warehouse.web.api.dto.RegalResponse;

import java.util.List;

/**
 * REST rozhranie pre prehlad miestnosti a regalov.
 * @author Juraj
 */
@RestController
@RequestMapping("/api/miestnosti")
public class MiestnostiApiController {
    private final SkladService skladService;
    private final DtoMapper mapper;

    /**
     * Vytvori kontroler so servisom a mapovacom.
     * @param skladService servis skladu
     * @param mapper prevodnik na DTO
     */
    public MiestnostiApiController(SkladService skladService, DtoMapper mapper) {
        this.skladService = skladService;
        this.mapper = mapper;
    }

    /**
     * Vrati vsetky miestnosti.
     * @return zoznam miestnosti
     */
    @GetMapping
    public List<MiestnostResponse> miestnosti() {
        return this.skladService.miestnosti().stream().map(this.mapper::naMiestnost).toList();
    }

    /**
     * Vrati regale v miestnosti s ich obsahom.
     * @param kluc kluc miestnosti
     * @return zoznam regalov
     */
    @GetMapping("/{kluc}/regale")
    public List<RegalResponse> regale(@PathVariable String kluc) {
        return this.skladService.regale(kluc).stream()
                .map(regal -> this.naRegal(regal))
                .toList();
    }

    private RegalResponse naRegal(Regal regal) {
        return this.mapper.naRegal(regal, this.skladService.tovarVRegali(regal.getId()));
    }
}
