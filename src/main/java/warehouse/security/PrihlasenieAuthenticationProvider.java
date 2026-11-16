package warehouse.security;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
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

    private final UcetService ucetService;
    private final HeslaService heslaService;
    private final String nahradnyHash;

    /**
     * Vytvori overovac so servisom uctov a hesiel.
     * @param ucetService servis uctov
     * @param heslaService servis hesiel
     * @param passwordEncoder kodovac hesiel pre nahradny hash
     */
    public PrihlasenieAuthenticationProvider(UcetService ucetService, HeslaService heslaService,
                                             PasswordEncoder passwordEncoder) {
        this.ucetService = ucetService;
        this.heslaService = heslaService;
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
        Optional<Osoba> osoba = rola.flatMap(r -> this.ucetService.najdi(r, id));
        String hash = osoba.map(Osoba::getHesloHash).orElse(null);

        boolean zhoda = this.heslaService.zhoduje(heslo, hash == null ? this.nahradnyHash : hash);
        if (osoba.isEmpty() || hash == null || !zhoda) {
            throw new BadCredentialsException(NESPRAVNE_UDAJE);
        }
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
