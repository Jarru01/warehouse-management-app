package warehouse.security;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import warehouse.domain.AuditAkcia;
import warehouse.service.AuditService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import warehouse.domain.Osoba;
import warehouse.service.HeslaService;

import java.util.List;
import java.util.Optional;

/**
 * Overi prihlasenie podla roly, id a hesla. Kazda rola ma vlastnu tabulku, preto sa rola posiela s formularom.
 * @author Juraj
 */
@Component
public class PrihlasenieAuthenticationProvider implements AuthenticationProvider {
    private static final String NESPRAVNE_UDAJE = "Nespravne ID alebo heslo.";
    private static final String PRILIS_VELA_POKUSOV = "Prilis vela neuspesnych pokusov o prihlasenie.";

    private final UcetService ucetService;
    private final HeslaService heslaService;
    private final PrihlasenieOchrana ochrana;
    private final AuditService auditService;
    private final String nahradnyHash;

    /**
     * Vytvori overovac so servisom uctov, hesiel, ochranou proti hadaniu hesiel a auditom.
     * @param ucetService servis uctov
     * @param heslaService servis hesiel
     * @param ochrana obmedzenie neuspesnych pokusov
     * @param auditService servis auditu
     * @param passwordEncoder kodovac hesiel pre nahradny hash
     */
    public PrihlasenieAuthenticationProvider(UcetService ucetService, HeslaService heslaService,
                                             PrihlasenieOchrana ochrana, AuditService auditService,
                                             PasswordEncoder passwordEncoder) {
        this.ucetService = ucetService;
        this.heslaService = heslaService;
        this.ochrana = ochrana;
        this.auditService = auditService;
        this.nahradnyHash = passwordEncoder.encode("nahradne-heslo-pre-neznameho-pouzivatela");
    }

    /**
     * Overi rolu, id a heslo. Pre neznameho pouzivatela tiez porovna heslo, aby cas odpovede neprezradil,
     * ktore ID existuje.
     * @param authentication poziadavka na prihlasenie
     * @return overene prihlasenie s rolou
     */
    @Override
    public Authentication authenticate(Authentication authentication) {
        String id = authentication.getName();
        String heslo = authentication.getCredentials() == null ? "" : authentication.getCredentials().toString();
        Optional<Rola> rola = authentication.getDetails() instanceof PrihlasenieDetails details
                ? Rola.podlaNazvu(details.getRola())
                : Optional.empty();
        String adresa = authentication.getDetails() instanceof WebAuthenticationDetails web
                ? web.getRemoteAddress()
                : null;
        String ucet = rola.map(Rola::name).orElse("NEZNAMA") + ":" + id;

        if (this.ochrana.jeZablokovane(ucet, adresa)) {
            this.auditService.zaznamenajPre(ucet, AuditAkcia.ZABLOKOVANE_PRIHLASENIE, "adresa " + adresa);
            throw new LockedException(PRILIS_VELA_POKUSOV);
        }

        Optional<Osoba> osoba = rola.flatMap(r -> this.ucetService.najdi(r, id));
        String hash = osoba.map(Osoba::getHesloHash).orElse(null);
        boolean zhoda = this.heslaService.zhoduje(heslo, hash == null ? this.nahradnyHash : hash);
        if (osoba.isEmpty() || hash == null || !zhoda) {
            this.ochrana.zaznamenajNeuspech(ucet, adresa);
            this.auditService.zaznamenajPre(ucet, AuditAkcia.NEUSPESNE_PRIHLASENIE, "adresa " + adresa);
            throw new BadCredentialsException(NESPRAVNE_UDAJE);
        }
        this.ochrana.zaznamenajUspech(ucet);
        this.auditService.zaznamenajPre(ucet, AuditAkcia.PRIHLASENIE, "adresa " + adresa);
        Osoba prihlasena = osoba.get();
        PrihlasenyPouzivatel pouzivatel = new PrihlasenyPouzivatel(rola.get(), id,
                prihlasena.getMeno(), prihlasena.getPriezvisko(), prihlasena.isZmenaHesla());
        return UsernamePasswordAuthenticationToken.authenticated(pouzivatel, null,
                List.of(new SimpleGrantedAuthority(rola.get().getOpravnenie())));
    }

    /**
     * Overovac spracuje prihlasenie menom a heslom.
     * @param authentication typ prihlasenia
     * @return true pre prihlasenie menom a heslom
     */
    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
