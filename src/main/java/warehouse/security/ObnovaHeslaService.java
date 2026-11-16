package warehouse.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import warehouse.domain.AuditAkcia;
import warehouse.domain.NeplatnaOperaciaException;
import warehouse.domain.ObnovaHesla;
import warehouse.domain.Zakaznik;
import warehouse.repository.ObnovaHeslaRepository;
import warehouse.repository.ZakaznikRepository;
import warehouse.service.AuditService;
import warehouse.service.HeslaService;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

/**
 * Samoobsluzna obnova zabudnuteho hesla zakaznika e-mailom. Token je nahodny, jednorazovy a casovo obmedzeny;
 * v databaze je len jeho SHA-256 hash. Odpoved na ziadost je vzdy rovnaka, takze neprezradi, ci e-mail existuje.
 * @author Juraj
 */
@Service
public class ObnovaHeslaService {
    /** Najkratsi odstup medzi dvoma e-mailmi pre toho isteho zakaznika. */
    static final Duration MIN_ODSTUP = Duration.ofMinutes(2);
    private static final String NEPLATNY_ODKAZ = "Odkaz na obnovu hesla je neplatny alebo expiroval.";
    private static final Logger LOG = LoggerFactory.getLogger(ObnovaHeslaService.class);

    private final ZakaznikRepository zakaznikRepository;
    private final ObnovaHeslaRepository obnovaHeslaRepository;
    private final HeslaService heslaService;
    private final AuditService auditService;
    private final PrihlasenieOchrana ochrana;
    private final JavaMailSender mailSender;
    private final String verejnaAdresa;
    private final Duration platnost;
    private final String odosielatel;
    private final Clock clock;
    private final SecureRandom nahoda = new SecureRandom();

    /**
     * Vytvori servis obnovy hesla.
     * @param zakaznikRepository repozitar zakaznikov
     * @param obnovaHeslaRepository repozitar ziadosti o obnovu
     * @param heslaService servis hesiel
     * @param auditService servis auditu
     * @param ochrana obmedzenie neuspesnych prihlaseni, po obnove sa ucet odblokuje
     * @param mailSender odosielanie e-mailov
     * @param verejnaAdresa zakladna adresa aplikacie pre odkazy v e-mailoch
     * @param platnost platnost odkazu
     * @param odosielatel adresa odosielatela e-mailov
     */
    @Autowired
    public ObnovaHeslaService(ZakaznikRepository zakaznikRepository, ObnovaHeslaRepository obnovaHeslaRepository,
                              HeslaService heslaService, AuditService auditService, PrihlasenieOchrana ochrana,
                              JavaMailSender mailSender,
                              @Value("${warehouse.verejna-adresa}") String verejnaAdresa,
                              @Value("${warehouse.obnova-hesla.platnost:30m}") Duration platnost,
                              @Value("${warehouse.obnova-hesla.odosielatel}") String odosielatel) {
        this.zakaznikRepository = zakaznikRepository;
        this.obnovaHeslaRepository = obnovaHeslaRepository;
        this.heslaService = heslaService;
        this.auditService = auditService;
        this.ochrana = ochrana;
        this.mailSender = mailSender;
        this.verejnaAdresa = verejnaAdresa.endsWith("/")
                ? verejnaAdresa.substring(0, verejnaAdresa.length() - 1)
                : verejnaAdresa;
        this.platnost = platnost;
        this.odosielatel = odosielatel;
        this.clock = Clock.systemUTC();
    }

    /**
     * Ak e-mail patri zakaznikovi, posle mu odkaz na obnovu hesla. Inak neurobi nic.
     * @param email e-mail zadany vo formulari
     */
    @Transactional
    public void poziadaj(String email) {
        Optional<Zakaznik> najdeny = this.zakaznikRepository.findByEmail(Zakaznik.normalizujEmail(email));
        if (najdeny.isEmpty()) {
            return;
        }
        Zakaznik zakaznik = najdeny.get();
        Instant teraz = Instant.now(this.clock);
        if (this.obnovaHeslaRepository.existsByZakaznikIdAndVytvoreneAfter(zakaznik.getId(),
                teraz.minus(MIN_ODSTUP))) {
            return;
        }
        String token = this.novyToken();
        this.obnovaHeslaRepository.save(new ObnovaHesla(zakaznik.getId(), hash(token), teraz,
                teraz.plus(this.platnost)));
        this.auditService.zaznamenajPre("ZAKAZNIK:" + zakaznik.getId(), AuditAkcia.ZIADOST_O_OBNOVU_HESLA, null);
        this.posliEmail(zakaznik, token);
    }

    /**
     * Zisti, ci sa odkaz s tokenom este da pouzit.
     * @param token token z odkazu
     * @return true pre platny token
     */
    @Transactional(readOnly = true)
    public boolean jePlatny(String token) {
        return this.najdiPlatnu(token).isPresent();
    }

    /**
     * Nastavi nove heslo podla platneho tokenu, token spotrebuje a zrusi ostatne ziadosti zakaznika.
     * @param token token z odkazu
     * @param noveHeslo nove heslo
     */
    @Transactional
    public void obnov(String token, String noveHeslo) {
        ObnovaHesla obnova = this.najdiPlatnu(token)
                .orElseThrow(() -> new NeplatnaOperaciaException(NEPLATNY_ODKAZ));
        Zakaznik zakaznik = this.zakaznikRepository.findById(obnova.getZakaznikId())
                .orElseThrow(() -> new NeplatnaOperaciaException(NEPLATNY_ODKAZ));
        zakaznik.nastavHeslo(this.heslaService.zahesuj(noveHeslo), false);
        obnova.pouzi();
        this.obnovaHeslaRepository.zneplatniVsetky(zakaznik.getId());
        this.ochrana.zaznamenajUspech(Rola.ZAKAZNIK.name() + ":" + zakaznik.getId());
        this.auditService.zaznamenajPre("ZAKAZNIK:" + zakaznik.getId(), AuditAkcia.OBNOVA_HESLA, null);
    }

    private Optional<ObnovaHesla> najdiPlatnu(String token) {
        if (token == null || token.isBlank() || token.length() > 100) {
            return Optional.empty();
        }
        Instant teraz = Instant.now(this.clock);
        return this.obnovaHeslaRepository.findByTokenHash(hash(token)).filter(obnova -> obnova.jePlatna(teraz));
    }

    private void posliEmail(Zakaznik zakaznik, String token) {
        SimpleMailMessage sprava = new SimpleMailMessage();
        sprava.setFrom(this.odosielatel);
        sprava.setTo(zakaznik.getEmail());
        sprava.setSubject("Obnova hesla - Warehouse App");
        sprava.setText("Dobry den " + zakaznik.getMeno() + ",\n\n"
                + "niekto poziadal o obnovu hesla k vasmu uctu vo Warehouse App. Nove heslo si nastavite tu:\n\n"
                + this.verejnaAdresa + "/heslo/obnova?token=" + token + "\n\n"
                + "Odkaz plati " + this.platnost.toMinutes() + " minut a da sa pouzit iba raz.\n"
                + "Ak ste o obnovu nepoziadali, tento e-mail ignorujte; vase heslo sa nezmeni.\n");
        try {
            this.mailSender.send(sprava);
        } catch (MailException e) {
            LOG.error("E-mail na obnovu hesla pre zakaznika {} sa nepodarilo odoslat: {}", zakaznik.getId(),
                    e.getMessage());
        }
    }

    private String novyToken() {
        byte[] bajty = new byte[32];
        this.nahoda.nextBytes(bajty);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bajty);
    }

    private static String hash(String token) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(sha256.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 nie je dostupny.", e);
        }
    }
}
