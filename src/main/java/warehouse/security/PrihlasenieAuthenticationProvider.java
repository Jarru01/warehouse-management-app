package warehouse.security;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import warehouse.domain.Osoba;
import warehouse.repository.PracovnikRepository;
import warehouse.repository.RiaditelRepository;
import warehouse.repository.ZakaznikRepository;

import java.util.List;
import java.util.Optional;

/**
 * Overi prihlasenie podla roly, id a hesla. Kazda rola ma vlastnu tabulku, preto sa rola posiela s formularom.
 * @author Juraj
 */
@Component
public class PrihlasenieAuthenticationProvider implements AuthenticationProvider {
    private static final String NESPRAVNE_UDAJE = "Nespravne ID alebo heslo.";

    private final RiaditelRepository riaditelRepository;
    private final PracovnikRepository pracovnikRepository;
    private final ZakaznikRepository zakaznikRepository;
    private final PasswordEncoder passwordEncoder;
    private final String nahradnyHash;

    /**
     * Vytvori overovac s repozitarmi pouzivatelov a kodovacom hesiel.
     * @param riaditelRepository repozitar riaditelov
     * @param pracovnikRepository repozitar pracovnikov
     * @param zakaznikRepository repozitar zakaznikov
     * @param passwordEncoder kodovac hesiel
     */
    public PrihlasenieAuthenticationProvider(RiaditelRepository riaditelRepository,
                                             PracovnikRepository pracovnikRepository,
                                             ZakaznikRepository zakaznikRepository,
                                             PasswordEncoder passwordEncoder) {
        this.riaditelRepository = riaditelRepository;
        this.pracovnikRepository = pracovnikRepository;
        this.zakaznikRepository = zakaznikRepository;
        this.passwordEncoder = passwordEncoder;
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
        Optional<Osoba> osoba = rola.flatMap(r -> this.najdi(r, id));
        String hash = osoba.map(Osoba::getHesloHash).orElse(null);

        boolean zhoda = this.porovnaj(heslo, hash == null ? this.nahradnyHash : hash);
        if (osoba.isEmpty() || hash == null || !zhoda) {
            throw new BadCredentialsException(NESPRAVNE_UDAJE);
        }
        PrihlasenyPouzivatel pouzivatel = new PrihlasenyPouzivatel(rola.get(), id,
                osoba.get().getMeno(), osoba.get().getPriezvisko());
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

    private Optional<Osoba> najdi(Rola rola, String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        return switch (rola) {
            case RIADITEL -> this.riaditelRepository.findById(id).map(Osoba.class::cast);
            case PRACOVNIK -> this.pracovnikRepository.findById(id).map(Osoba.class::cast);
            case ZAKAZNIK -> this.zakaznikRepository.findById(id).map(Osoba.class::cast);
        };
    }

    private boolean porovnaj(String heslo, String hash) {
        try {
            return this.passwordEncoder.matches(heslo, hash);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
