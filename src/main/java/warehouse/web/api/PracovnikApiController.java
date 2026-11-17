package warehouse.web.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import warehouse.service.SkladService;
import warehouse.web.api.dto.PracovnikResponse;
import warehouse.web.api.dto.PresunRequest;
import warehouse.web.api.dto.ZobratTovarRequest;

/**
 * REST rozhranie pre pracovnika.
 * @author Juraj
 */
@RestController
@RequestMapping("/api/pracovnik")
@Tag(name = "Pracovnik", description = "Operacie pracovnika nad vlastnym ID")
public class PracovnikApiController {
    private final SkladService skladService;
    private final DtoMapper mapper;

    /**
     * Vytvori kontroler so servisom a mapovacom.
     * @param skladService servis skladu
     * @param mapper prevodnik na DTO
     */
    public PracovnikApiController(SkladService skladService, DtoMapper mapper) {
        this.skladService = skladService;
        this.mapper = mapper;
    }

    /**
     * Vrati pracovnika s danym id.
     * @param id id pracovnika
     * @return pracovnik
     */
    @Operation(summary = "Stav pracovnika: miestnost a drzany tovar")
    @GetMapping("/{id}")
    public PracovnikResponse pracovnik(@PathVariable String id) {
        return this.mapper.naPracovnika(this.skladService.pracovnik(id));
    }

    /**
     * Presunie pracovnika do miestnosti.
     * @param id id pracovnika
     * @param request poziadavka na presun
     * @return presunuty pracovnik
     */
    @Operation(summary = "Presunie pracovnika do miestnosti")
    @PostMapping("/{id}/presun")
    public PracovnikResponse presun(@PathVariable String id, @Valid @RequestBody PresunRequest request) {
        return this.mapper.naPracovnika(this.skladService.presunPracovnika(id, request.miestnost()));
    }

    /**
     * Pracovnik zoberie tovar z regalu v aktualnej miestnosti.
     * @param id id pracovnika
     * @param request poziadavka na zobratie tovaru
     */
    @Operation(summary = "Pracovnik zoberie tovar z regalu v aktualnej miestnosti")
    @PostMapping("/{id}/zober")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void zoberTovar(@PathVariable String id, @Valid @RequestBody ZobratTovarRequest request) {
        this.skladService.zoberTovar(id, request.tovarId());
    }

    /**
     * Pracovnik ulozi drzany tovar v aktualnej miestnosti.
     * @param id id pracovnika
     */
    @Operation(summary = "Pracovnik ulozi drzany tovar do prveho volneho slotu")
    @PostMapping("/{id}/uloz")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void ulozTovar(@PathVariable String id) {
        this.skladService.ulozTovar(id);
    }
}
