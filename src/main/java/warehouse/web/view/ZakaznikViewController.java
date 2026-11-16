package warehouse.web.view;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import warehouse.domain.ZakaznikInfo;
import warehouse.domain.NenajdenaEntitaException;
import warehouse.domain.NeplatnaOperaciaException;
import warehouse.security.PrihlasenyPouzivatel;
import warehouse.service.SkladService;
import warehouse.web.api.DtoMapper;
import warehouse.web.api.dto.VlozitTovarRequest;
import warehouse.web.api.dto.ZakaznikRequest;

import java.util.Optional;

/**
 * Obrazovky zakaznika.
 * @author Juraj
 */
@Controller
@RequestMapping("/zakaznik")
public class ZakaznikViewController {
    private final SkladService skladService;
    private final DtoMapper mapper;
    private final FormularValidator validator;

    /**
     * Vytvori kontroler so servisom, mapovacom a validatorom formularov.
     * @param skladService servis skladu
     * @param mapper prevodnik na DTO
     * @param validator validator formularov
     */
    public ZakaznikViewController(SkladService skladService, DtoMapper mapper, FormularValidator validator) {
        this.skladService = skladService;
        this.mapper = mapper;
        this.validator = validator;
    }

    /**
     * Zobrazi panel zakaznika s jeho tovarom pripravenym na vydaj.
     * @param pouzivatel prihlaseny zakaznik
     * @param model model sablony
     * @return nazov sablony
     */
    @GetMapping
    public String panel(@AuthenticationPrincipal PrihlasenyPouzivatel pouzivatel, Model model) {
        model.addAttribute("tovar", this.skladService.tovarPreZakaznika(pouzivatel.id()).stream()
                .map(this.mapper::naTovar).toList());
        return "zakaznik";
    }

    /**
     * Vlozi novy tovar z formulara.
     * @param id id tovaru
     * @param nazov nazov tovaru
     * @param vaha vaha tovaru
     * @param prijemcaId id prijemcu
     * @param prijemcaMeno meno prijemcu
     * @param prijemcaPriezvisko priezvisko prijemcu
     * @param pouzivatel prihlaseny zakaznik, ktory je odosielatelom
     * @param attributes flash atributy
     * @return presmerovanie na panel
     */
    @PostMapping("/tovar")
    public String vlozTovar(@RequestParam String id, @RequestParam String nazov, @RequestParam int vaha,
                            @RequestParam String prijemcaId, @RequestParam String prijemcaMeno,
                            @RequestParam String prijemcaPriezvisko,
                            @AuthenticationPrincipal PrihlasenyPouzivatel pouzivatel,
                            RedirectAttributes attributes) {
        ZakaznikRequest odosielatel = new ZakaznikRequest(pouzivatel.id(), pouzivatel.meno(), pouzivatel.priezvisko());
        ZakaznikRequest prijemca = new ZakaznikRequest(prijemcaId, prijemcaMeno, prijemcaPriezvisko);
        Optional<String> chyby = this.validator.chyby(new VlozitTovarRequest(id, nazov, vaha, odosielatel, prijemca));
        if (chyby.isPresent()) {
            attributes.addFlashAttribute("chyba", chyby.get());
            return "redirect:/zakaznik";
        }
        try {
            this.skladService.vytvorTovar(id, nazov, vaha, naZakaznika(odosielatel), naZakaznika(prijemca));
            attributes.addFlashAttribute("sprava", "Tovar bol ulozeny.");
        } catch (NeplatnaOperaciaException e) {
            attributes.addFlashAttribute("chyba", e.getMessage());
        }
        return "redirect:/zakaznik";
    }

    /**
     * Vyzdvihne tovar pripraveny na vydaj.
     * @param tovarId id tovaru
     * @param pouzivatel prihlaseny zakaznik
     * @param attributes flash atributy
     * @return presmerovanie na panel
     */
    @PostMapping("/vyzdvihnutie/{tovarId}")
    public String vyzdvihniTovar(@PathVariable String tovarId,
                                 @AuthenticationPrincipal PrihlasenyPouzivatel pouzivatel,
                                 RedirectAttributes attributes) {
        try {
            this.skladService.vyzdvihniTovar(tovarId, pouzivatel.id());
            attributes.addFlashAttribute("sprava", "Tovar bol vyzdvihnuty.");
        } catch (NenajdenaEntitaException | NeplatnaOperaciaException e) {
            attributes.addFlashAttribute("chyba", e.getMessage());
        }
        return "redirect:/zakaznik";
    }

    private static ZakaznikInfo naZakaznika(ZakaznikRequest zakaznik) {
        return new ZakaznikInfo(zakaznik.id(), zakaznik.meno(), zakaznik.priezvisko());
    }
}
