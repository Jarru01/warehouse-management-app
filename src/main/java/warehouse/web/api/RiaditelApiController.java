package warehouse.web.api;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import warehouse.domain.Regal;
import warehouse.service.SkladService;
import warehouse.web.api.dto.PracovnikResponse;
import warehouse.web.api.dto.PridatRegalRequest;
import warehouse.web.api.dto.PrijmoutPracovnikaRequest;
import warehouse.web.api.dto.RegalResponse;
import warehouse.web.api.dto.RiaditelResponse;

import java.util.List;

/**
 * REST rozhranie pre riaditela.
 * @author Juraj
 */
@RestController
@RequestMapping("/api/riaditel")
public class RiaditelApiController {
    private final SkladService skladService;
    private final DtoMapper mapper;

    /**
     * Vytvori kontroler so servisom a mapovacom.
     * @param skladService servis skladu
     * @param mapper prevodnik na DTO
     */
    public RiaditelApiController(SkladService skladService, DtoMapper mapper) {
        this.skladService = skladService;
        this.mapper = mapper;
    }

    /**
     * Vrati riaditela s danym id.
     * @param id id riaditela
     * @return riaditel
     */
    @GetMapping("/{id}")
    public RiaditelResponse riaditel(@PathVariable String id) {
        return this.mapper.naRiaditela(this.skladService.riaditel(id));
    }

    /**
     * Vrati vsetkych pracovnikov.
     * @return zoznam pracovnikov
     */
    @GetMapping("/pracovnici")
    public List<PracovnikResponse> pracovnici() {
        return this.skladService.pracovnici().stream().map(this.mapper::naPracovnika).toList();
    }

    /**
     * Prijme noveho pracovnika.
     * @param request poziadavka na prijatie
     * @return prijaty pracovnik
     */
    @PostMapping("/pracovnici")
    @ResponseStatus(HttpStatus.CREATED)
    public PracovnikResponse prijmiPracovnika(@Valid @RequestBody PrijmoutPracovnikaRequest request) {
        return this.mapper.naPracovnika(
                this.skladService.prijmiPracovnika(request.id(), request.meno(), request.priezvisko()));
    }

    /**
     * Vyluci pracovnika s danym id.
     * @param id id pracovnika
     */
    @DeleteMapping("/pracovnici/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void vylucPracovnika(@PathVariable String id) {
        this.skladService.vylucPracovnika(id);
    }

    /**
     * Prida novy regal do hlavneho skladu.
     * @param request poziadavka na pridanie regalu
     * @return pridany regal
     */
    @PostMapping("/regale")
    @ResponseStatus(HttpStatus.CREATED)
    public RegalResponse pridajRegal(@Valid @RequestBody PridatRegalRequest request) {
        Regal regal = this.skladService.pridajRegal(request.kapacita());
        return this.mapper.naRegal(regal, List.of());
    }

    /**
     * Odstrani prazdny regal s danym id.
     * @param id id regalu
     */
    @DeleteMapping("/regale/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void odstranRegal(@PathVariable Long id) {
        this.skladService.odstranRegal(id);
    }
}
