package warehouse.web.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import warehouse.AbstractIntegrationTest;
import warehouse.domain.AuditAkcia;
import warehouse.domain.Miestnost;
import warehouse.domain.NenajdenaEntitaException;
import warehouse.repository.TovarRepository;
import warehouse.security.Rola;
import warehouse.service.AuditService;
import warehouse.service.SkladService;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ViewFlowTest extends AbstractIntegrationTest {
    private static final String HESLO = "testovacie-heslo";
    private static final String NOVE_HESLO = "nove-vlastne-heslo";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SkladService skladService;

    @Autowired
    private TovarRepository tovarRepository;

    @Autowired
    private AuditService auditService;

    @MockitoBean
    private JavaMailSender mailSender;

    @Value("${warehouse.riaditel.heslo}")
    private String hesloRiaditela;

    @BeforeEach
    void zaregistrujZakaznikov() {
        this.skladService.registrujZakaznika("10", "Jan", "Novy", "jan@example.com", HESLO);
        this.skladService.registrujZakaznika("20", "Peter", "Novak", "peter@example.com", HESLO);
    }

    @Test
    void uvodnaStrankaZobrazujeRoloveTabyAIbaJednuFormu() throws Exception {
        this.mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Riaditeľ")))
                .andExpect(content().string(containsString("Pracovník")))
                .andExpect(content().string(containsString("Zákazník")))
                .andExpect(content().string(containsString("value=\"RIADITEL\"")))
                .andExpect(content().string(containsString("type=\"password\"")))
                .andExpect(content().string(containsString("name=\"_csrf\"")))
                .andExpect(content().string(not(containsString("/registracia"))));
    }

    @Test
    void vyberRolyZakaznikZobrazujePrihlasenieARegistraciu() throws Exception {
        this.mockMvc.perform(get("/").param("rola", "zakaznik"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("value=\"ZAKAZNIK\"")))
                .andExpect(content().string(containsString("/registracia")))
                .andExpect(content().string(not(containsString("value=\"RIADITEL\""))));
    }

    @Test
    void neprihlasenyPristupPresmerujeNaPrihlasenie() throws Exception {
        this.mockMvc.perform(get("/pracovnik"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void riaditelSaPrihlasiAVidiPanel() throws Exception {
        MockHttpSession session = this.prihlas(Rola.RIADITEL, "123", this.hesloRiaditela);

        this.mockMvc.perform(get("/riaditel").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Pracovníci")))
                .andExpect(content().string(containsString("Prepnúť používateľa")));
    }

    @Test
    void nespravneHesloZobraziChybu() throws Exception {
        this.mockMvc.perform(post("/prihlasenie").with(csrf())
                        .param("rola", "RIADITEL").param("id", "123").param("heslo", "zle-heslo-123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/?rola=riaditel&chyba"));

        this.mockMvc.perform(get("/").param("rola", "riaditel").param("chyba", ""))
                .andExpect(content().string(containsString("Nespravne ID alebo heslo.")));
    }

    @Test
    void samotneIdBezHeslaNestaci() throws Exception {
        this.mockMvc.perform(post("/prihlasenie").with(csrf()).param("rola", "RIADITEL").param("id", "123"))
                .andExpect(redirectedUrl("/?rola=riaditel&chyba"));
    }

    @Test
    void spravneHesloInejRolyNestaci() throws Exception {
        this.mockMvc.perform(post("/prihlasenie").with(csrf())
                        .param("rola", "PRACOVNIK").param("id", "123").param("heslo", this.hesloRiaditela))
                .andExpect(redirectedUrl("/?rola=pracovnik&chyba"));
    }

    @Test
    void prihlasenieBezCsrfTokenuSaOdmietne() throws Exception {
        MockHttpSession session = new MockHttpSession();

        this.mockMvc.perform(post("/prihlasenie").session(session)
                        .param("rola", "RIADITEL").param("id", "123").param("heslo", this.hesloRiaditela))
                .andExpect(redirectedUrl("/"));

        this.mockMvc.perform(get("/riaditel").session(session))
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void odhlasenieUkonciPrihlasenie() throws Exception {
        MockHttpSession session = this.prihlas(Rola.RIADITEL, "123", this.hesloRiaditela);

        this.mockMvc.perform(post("/odhlasenie").with(csrf()).session(session))
                .andExpect(redirectedUrl("/"));

        this.mockMvc.perform(get("/riaditel").session(session))
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void pracovnikSaPrihlasiAVidiSvojuMiestnost() throws Exception {
        this.skladService.prijmiPracovnika("1", "Peter", "Novak", HESLO);
        MockHttpSession session = this.prihlas(Rola.PRACOVNIK, "1", HESLO);

        this.mockMvc.perform(get("/pracovnik").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Peter")))
                .andExpect(content().string(containsString(Miestnost.SKLAD_TOVARU)));
    }

    @Test
    void pracovnikNemozeOtvoritPanelRiaditela() throws Exception {
        this.skladService.prijmiPracovnika("1", "Peter", "Novak", HESLO);
        MockHttpSession session = this.prihlas(Rola.PRACOVNIK, "1", HESLO);

        this.mockMvc.perform(get("/riaditel").session(session))
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void zakaznikSaZaregistrujeAPrihlasi() throws Exception {
        this.mockMvc.perform(post("/registracia").with(csrf())
                        .param("id", "30").param("meno", "Eva").param("priezvisko", "Mala")
                        .param("email", "eva@example.com").param("heslo", HESLO))
                .andExpect(redirectedUrl("/?rola=zakaznik"))
                .andExpect(flash().attributeExists("sprava"));

        MockHttpSession session = this.prihlas(Rola.ZAKAZNIK, "30", HESLO);
        this.mockMvc.perform(get("/zakaznik").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Eva")));
    }

    @Test
    void registraciaSKratkymHeslomZlyha() throws Exception {
        this.mockMvc.perform(post("/registracia").with(csrf())
                        .param("id", "30").param("meno", "Eva").param("priezvisko", "Mala")
                        .param("email", "eva@example.com").param("heslo", "kratke"))
                .andExpect(redirectedUrl("/?rola=zakaznik"))
                .andExpect(flash().attribute("chyba", containsString("Heslo musi mat 8 az 72 znakov.")));

        assertThrows(NenajdenaEntitaException.class, () -> this.skladService.zakaznik("30"));
    }

    @Test
    void zakaznikVloziTovarCezFormular() throws Exception {
        MockHttpSession session = this.prihlas(Rola.ZAKAZNIK, "10", HESLO);

        this.mockMvc.perform(post("/zakaznik/tovar").with(csrf())
                        .param("id", "100")
                        .param("nazov", "Tovar100")
                        .param("vaha", "500")
                        .param("prijemcaId", "20")
                        .param("prijemcaMeno", "Peter")
                        .param("prijemcaPriezvisko", "Novak")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/zakaznik"))
                .andExpect(flash().attributeExists("sprava"));

        assertEquals(Miestnost.PRIJEM_TOVARU, this.skladService.tovar("100").getRegal().getMiestnost().getKluc());
        assertEquals("10", this.skladService.tovar("100").getOdosielatel().getId());
    }

    @Test
    void kompletnyTokOdVlozeniaPoVyzdvihnutie() throws Exception {
        this.skladService.prijmiPracovnika("1", "Peter", "Novak", HESLO);
        MockHttpSession zakaznikSession = this.prihlas(Rola.ZAKAZNIK, "10", HESLO);
        MockHttpSession prijemcaSession = this.prihlas(Rola.ZAKAZNIK, "20", HESLO);
        MockHttpSession pracovnikSession = this.prihlas(Rola.PRACOVNIK, "1", HESLO);

        this.mockMvc.perform(post("/zakaznik/tovar").with(csrf())
                        .param("id", "100").param("nazov", "Tovar100").param("vaha", "500")
                        .param("prijemcaId", "20").param("prijemcaMeno", "Peter").param("prijemcaPriezvisko", "Novak")
                        .session(zakaznikSession))
                .andExpect(redirectedUrl("/zakaznik"));

        this.mockMvc.perform(post("/pracovnik/presun").with(csrf()).param("miestnost", Miestnost.PRIJEM_TOVARU)
                        .session(pracovnikSession))
                .andExpect(redirectedUrl("/pracovnik"));
        this.mockMvc.perform(post("/pracovnik/zober").with(csrf()).param("tovarId", "100").session(pracovnikSession))
                .andExpect(redirectedUrl("/pracovnik"));
        this.mockMvc.perform(post("/pracovnik/presun").with(csrf()).param("miestnost", Miestnost.VYDAJ_TOVARU)
                        .session(pracovnikSession))
                .andExpect(redirectedUrl("/pracovnik"));
        this.mockMvc.perform(post("/pracovnik/uloz").with(csrf()).session(pracovnikSession))
                .andExpect(redirectedUrl("/pracovnik"));

        this.mockMvc.perform(get("/zakaznik").session(prijemcaSession))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Tovar100")));

        this.mockMvc.perform(post("/zakaznik/vyzdvihnutie/100").with(csrf()).session(prijemcaSession))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/zakaznik"));

        assertTrue(this.tovarRepository.findById("100").isEmpty());
        assertThrows(NenajdenaEntitaException.class, () -> this.skladService.tovar("100"));
    }

    @Test
    void zakaznikSoZapornouVahouDostaneChybu() throws Exception {
        MockHttpSession session = this.prihlas(Rola.ZAKAZNIK, "10", HESLO);

        this.mockMvc.perform(post("/zakaznik/tovar").with(csrf())
                        .param("id", "100").param("nazov", "Tovar100").param("vaha", "-5")
                        .param("prijemcaId", "20").param("prijemcaMeno", "Peter").param("prijemcaPriezvisko", "Novak")
                        .session(session))
                .andExpect(redirectedUrl("/zakaznik"))
                .andExpect(flash().attribute("chyba", containsString("Vaha musi byt kladna.")));

        assertTrue(this.tovarRepository.findById("100").isEmpty());
    }

    @Test
    void riaditelNemozePrijatPracovnikaSNeplatnymMenom() throws Exception {
        MockHttpSession session = this.prihlas(Rola.RIADITEL, "123", this.hesloRiaditela);

        this.mockMvc.perform(post("/riaditel/pracovnici").with(csrf())
                        .param("id", "7").param("meno", "").param("priezvisko", "Novak").param("heslo", HESLO)
                        .session(session))
                .andExpect(redirectedUrl("/riaditel"))
                .andExpect(flash().attribute("chyba", containsString("Meno je povinne.")));

        assertTrue(this.skladService.pracovnici().stream().noneMatch(p -> p.getId().equals("7")));
    }

    @Test
    void riaditelPrijmePracovnikaKtorySaPrihlasi() throws Exception {
        MockHttpSession session = this.prihlas(Rola.RIADITEL, "123", this.hesloRiaditela);

        this.mockMvc.perform(post("/riaditel/pracovnici").with(csrf())
                        .param("id", "7").param("meno", "Ivan").param("priezvisko", "Novy").param("heslo", HESLO)
                        .session(session))
                .andExpect(redirectedUrl("/riaditel"))
                .andExpect(flash().attributeExists("sprava"));

        this.prihlas(Rola.PRACOVNIK, "7", HESLO);
    }

    @Test
    void riaditelNemozePridatRegalNadMaximalnuKapacitu() throws Exception {
        MockHttpSession session = this.prihlas(Rola.RIADITEL, "123", this.hesloRiaditela);
        int pocetRegalov = this.skladService.regale(Miestnost.SKLAD_TOVARU).size();

        this.mockMvc.perform(post("/riaditel/regale").with(csrf()).param("kapacita", "500").session(session))
                .andExpect(redirectedUrl("/riaditel"))
                .andExpect(flash().attributeExists("chyba"));

        assertEquals(pocetRegalov, this.skladService.regale(Miestnost.SKLAD_TOVARU).size());
    }

    @Test
    void riaditelNemozeOdstranitRegalMalehoSkladu() throws Exception {
        MockHttpSession session = this.prihlas(Rola.RIADITEL, "123", this.hesloRiaditela);
        Long regalId = this.skladService.regale(Miestnost.PRIJEM_TOVARU).get(0).getId();

        this.mockMvc.perform(post("/riaditel/regale/" + regalId + "/odstranenie").with(csrf()).session(session))
                .andExpect(redirectedUrl("/riaditel"))
                .andExpect(flash().attributeExists("chyba"));

        assertEquals(1, this.skladService.regale(Miestnost.PRIJEM_TOVARU).size());
        this.mockMvc.perform(get("/riaditel").session(session))
                .andExpect(content().string(not(containsString("/riaditel/regale/" + regalId + "/odstranenie"))));
    }

    @Test
    void apiPouzivaSessionZPrihlasenia() throws Exception {
        this.mockMvc.perform(get("/api/riaditel/pracovnici"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));

        this.mockMvc.perform(get("/api/riaditel/pracovnici")
                        .session(this.prihlas(Rola.RIADITEL, "123", this.hesloRiaditela)))
                .andExpect(status().isOk());
    }

    @Test
    void apiVratiCsrfToken() throws Exception {
        this.mockMvc.perform(get("/api/csrf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.headerName").value("X-CSRF-TOKEN"))
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void docasneHesloVynutiZmenuPredPokracovanim() throws Exception {
        MockHttpSession session = new MockHttpSession();
        this.mockMvc.perform(post("/prihlasenie").with(csrf()).session(session)
                        .param("rola", "RIADITEL").param("id", "123").param("heslo", this.hesloRiaditela))
                .andExpect(redirectedUrl("/heslo"));

        this.mockMvc.perform(get("/riaditel").session(session))
                .andExpect(redirectedUrl("/heslo"));
        this.mockMvc.perform(get("/api/riaditel/pracovnici").session(session))
                .andExpect(status().isForbidden());
        this.mockMvc.perform(get("/heslo").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("dočasným heslom")));

        this.zmenHeslo(session, this.hesloRiaditela, NOVE_HESLO)
                .andExpect(redirectedUrl("/riaditel"))
                .andExpect(flash().attribute("sprava", "Heslo bolo zmenene."));
        this.mockMvc.perform(get("/riaditel").session(session))
                .andExpect(status().isOk());
    }

    @Test
    void zmenaHeslaSoZlymSucasnymHeslomZlyha() throws Exception {
        MockHttpSession session = this.prihlas(Rola.ZAKAZNIK, "10", HESLO);

        this.zmenHeslo(session, "zle-sucasne-heslo", NOVE_HESLO)
                .andExpect(redirectedUrl("/heslo"))
                .andExpect(flash().attribute("chyba", "Sucasne heslo nie je spravne."));
    }

    @Test
    void zmenaHeslaVyzadujeZhodneNoveHesla() throws Exception {
        MockHttpSession session = this.prihlas(Rola.ZAKAZNIK, "10", HESLO);

        this.mockMvc.perform(post("/heslo").with(csrf()).session(session)
                        .param("sucasneHeslo", HESLO).param("noveHeslo", NOVE_HESLO)
                        .param("noveHesloZnova", "ine-nove-heslo"))
                .andExpect(redirectedUrl("/heslo"))
                .andExpect(flash().attribute("chyba", "Nove hesla sa nezhoduju."));
    }

    @Test
    void zmeneneHesloPlatiPriDalsomPrihlaseni() throws Exception {
        MockHttpSession session = this.prihlas(Rola.ZAKAZNIK, "10", HESLO);
        this.zmenHeslo(session, HESLO, NOVE_HESLO).andExpect(redirectedUrl("/zakaznik"));

        this.mockMvc.perform(post("/prihlasenie").with(csrf())
                        .param("rola", "ZAKAZNIK").param("id", "10").param("heslo", HESLO))
                .andExpect(redirectedUrl("/?rola=zakaznik&chyba"));
        this.prihlas(Rola.ZAKAZNIK, "10", NOVE_HESLO);
    }

    @Test
    void riaditelNastaviPracovnikoviNoveDocasneHeslo() throws Exception {
        this.skladService.prijmiPracovnika("1", "Peter", "Novak", HESLO);
        MockHttpSession riaditel = this.prihlas(Rola.RIADITEL, "123", this.hesloRiaditela);

        this.mockMvc.perform(post("/riaditel/pracovnici/1/heslo").with(csrf()).session(riaditel)
                        .param("heslo", "reset-heslo-123"))
                .andExpect(redirectedUrl("/riaditel"))
                .andExpect(flash().attributeExists("sprava"));

        MockHttpSession pracovnik = new MockHttpSession();
        this.mockMvc.perform(post("/prihlasenie").with(csrf()).session(pracovnik)
                        .param("rola", "PRACOVNIK").param("id", "1").param("heslo", "reset-heslo-123"))
                .andExpect(redirectedUrl("/heslo"));
    }

    @Test
    void apiZmeniHesloPrihlasenehoPouzivatela() throws Exception {
        MockHttpSession session = this.prihlas(Rola.ZAKAZNIK, "10", HESLO);

        this.mockMvc.perform(post("/api/heslo").with(csrf()).session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sucasneHeslo\":\"" + HESLO + "\",\"noveHeslo\":\"" + NOVE_HESLO + "\"}"))
                .andExpect(status().isNoContent());

        this.prihlas(Rola.ZAKAZNIK, "10", NOVE_HESLO);
    }

    @Test
    void statusHlasiDostupnuDatabazu() throws Exception {
        this.mockMvc.perform(get("/api/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.database").value("ok"));
    }

    @Test
    void zakaznikSiObnoviZabudnuteHesloEmailom() throws Exception {
        this.mockMvc.perform(post("/heslo/zabudnute").with(csrf()).param("email", " JAN@example.com "))
                .andExpect(redirectedUrl("/?rola=zakaznik"))
                .andExpect(flash().attribute("sprava", containsString("Ak k e-mailu existuje ucet")));

        ArgumentCaptor<SimpleMailMessage> email = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(this.mailSender).send(email.capture());
        assertEquals("jan@example.com", email.getValue().getTo()[0]);
        Matcher odkaz = Pattern.compile("http://localhost:8080/heslo/obnova\\?token=([A-Za-z0-9_-]+)")
                .matcher(email.getValue().getText());
        assertTrue(odkaz.find(), "e-mail obsahuje odkaz s tokenom");
        String token = odkaz.group(1);

        this.mockMvc.perform(get("/heslo/obnova").param("token", token))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Nové heslo")));
        this.mockMvc.perform(post("/heslo/obnova").with(csrf()).param("token", token)
                        .param("noveHeslo", NOVE_HESLO).param("noveHesloZnova", NOVE_HESLO))
                .andExpect(redirectedUrl("/?rola=zakaznik"))
                .andExpect(flash().attribute("sprava", "Heslo bolo zmenene. Teraz sa mozes prihlasit."));

        this.prihlas(Rola.ZAKAZNIK, "10", NOVE_HESLO);
        this.mockMvc.perform(get("/heslo/obnova").param("token", token))
                .andExpect(redirectedUrl("/?rola=zakaznik"))
                .andExpect(flash().attribute("chyba", "Odkaz na obnovu hesla je neplatny alebo expiroval."));
        assertEquals("ZAKAZNIK:10", this.auditService.najnovsie(AuditAkcia.OBNOVA_HESLA, 1).get(0).getPouzivatel());
    }

    @Test
    void neznamyEmailDostaneRovnakuOdpovedBezEmailu() throws Exception {
        this.mockMvc.perform(post("/heslo/zabudnute").with(csrf()).param("email", "nikto@example.com"))
                .andExpect(redirectedUrl("/?rola=zakaznik"))
                .andExpect(flash().attribute("sprava", containsString("Ak k e-mailu existuje ucet")));

        verify(this.mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void opakovanaZiadostHnedPoSebeNeposleDalsiEmail() throws Exception {
        this.mockMvc.perform(post("/heslo/zabudnute").with(csrf()).param("email", "peter@example.com"));
        this.mockMvc.perform(post("/heslo/zabudnute").with(csrf()).param("email", "peter@example.com"));

        verify(this.mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void vymyslenyTokenSaOdmietne() throws Exception {
        this.mockMvc.perform(get("/heslo/obnova").param("token", "vymysleny-token"))
                .andExpect(redirectedUrl("/?rola=zakaznik"))
                .andExpect(flash().attributeExists("chyba"));
        this.mockMvc.perform(post("/heslo/obnova").with(csrf()).param("token", "vymysleny-token")
                        .param("noveHeslo", NOVE_HESLO).param("noveHesloZnova", NOVE_HESLO))
                .andExpect(redirectedUrl("/?rola=zakaznik"))
                .andExpect(flash().attribute("chyba", "Odkaz na obnovu hesla je neplatny alebo expiroval."));
    }

    @Test
    void opakovaneZleHesloDocasneZablokujeUcet() throws Exception {
        this.skladService.registrujZakaznika("41", "Ivan", "Dlhy", "ivan@example.com", HESLO);
        for (int i = 0; i < 5; i++) {
            this.mockMvc.perform(post("/prihlasenie").with(csrf())
                            .param("rola", "ZAKAZNIK").param("id", "41").param("heslo", "zle-heslo-" + i))
                    .andExpect(redirectedUrl("/?rola=zakaznik&chyba"));
        }

        this.mockMvc.perform(post("/prihlasenie").with(csrf())
                        .param("rola", "ZAKAZNIK").param("id", "41").param("heslo", HESLO))
                .andExpect(redirectedUrl("/?rola=zakaznik&zablokovane"));
        this.mockMvc.perform(get("/").param("rola", "zakaznik").param("zablokovane", ""))
                .andExpect(content().string(containsString("Prilis vela neuspesnych pokusov")));

        assertEquals(5, this.auditService.najnovsie(AuditAkcia.NEUSPESNE_PRIHLASENIE, 50).stream()
                .filter(zaznam -> zaznam.getPouzivatel().equals("ZAKAZNIK:41")).count());
        assertEquals("ZAKAZNIK:41",
                this.auditService.najnovsie(AuditAkcia.ZABLOKOVANE_PRIHLASENIE, 1).get(0).getPouzivatel());
    }

    @Test
    void auditZaznamenaKtoCoUrobil() throws Exception {
        MockHttpSession riaditel = this.prihlas(Rola.RIADITEL, "123", this.hesloRiaditela);
        this.mockMvc.perform(post("/riaditel/pracovnici").with(csrf()).session(riaditel)
                        .param("id", "8").param("meno", "Ema").param("priezvisko", "Nova").param("heslo", HESLO))
                .andExpect(redirectedUrl("/riaditel"));

        this.mockMvc.perform(get("/api/riaditel/audit").session(riaditel).param("akcia", "PRIJATIE_PRACOVNIKA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].pouzivatel").value("RIADITEL:123"))
                .andExpect(jsonPath("$[0].detail").value("pracovnik 8 (Ema Nova)"));
        this.mockMvc.perform(get("/riaditel").session(riaditel))
                .andExpect(content().string(containsString("Posledné udalosti")))
                .andExpect(content().string(containsString("pracovnik 8 (Ema Nova)")));

        this.mockMvc.perform(post("/odhlasenie").with(csrf()).session(riaditel));
        assertEquals("RIADITEL:123", this.auditService.najnovsie(AuditAkcia.ODHLASENIE, 1).get(0).getPouzivatel());
    }

    @Test
    void healthCheckJeVerejnyAOstatneEndpointyNie() throws Exception {
        this.mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
        this.mockMvc.perform(get("/actuator/health/readiness"))
                .andExpect(status().isOk());
        this.mockMvc.perform(get("/actuator/env"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void cssAJsMajuVerziuVAdreseADlhuCache() throws Exception {
        String stranka = this.mockMvc.perform(get("/")).andReturn().getResponse().getContentAsString();
        Matcher css = Pattern.compile("/css/style-[0-9a-f]{32}\\.css").matcher(stranka);
        Matcher js = Pattern.compile("/js/app-[0-9a-f]{32}\\.js").matcher(stranka);
        assertTrue(css.find(), "stranka odkazuje na CSS s hashom obsahu");
        assertTrue(js.find(), "stranka odkazuje na JS s hashom obsahu");

        this.mockMvc.perform(get(css.group()))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", containsString("max-age=31536000")))
                .andExpect(content().string(containsString("--blue-700")));
        this.mockMvc.perform(get(js.group()))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", containsString("max-age=31536000")));
        this.mockMvc.perform(get("/"))
                .andExpect(header().string("Cache-Control", containsString("no-cache")));
    }

    @Test
    void popisApiVidiIbaRiaditel() throws Exception {
        this.mockMvc.perform(get("/v3/api-docs")).andExpect(redirectedUrl("/"));

        this.skladService.prijmiPracovnika("1", "Peter", "Novak", HESLO);
        this.mockMvc.perform(get("/v3/api-docs").session(this.prihlas(Rola.PRACOVNIK, "1", HESLO)))
                .andExpect(redirectedUrl("/"));

        MockHttpSession riaditel = this.prihlas(Rola.RIADITEL, "123", this.hesloRiaditela);
        this.mockMvc.perform(get("/v3/api-docs").session(riaditel))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Warehouse App API"))
                .andExpect(jsonPath("$.paths['/api/riaditel/pracovnici'].post.summary")
                        .value("Prijme pracovnika s docasnym heslom"))
                .andExpect(jsonPath("$.paths['/api/zakaznik/tovar']").exists())
                .andExpect(jsonPath("$.paths['/riaditel']").doesNotExist())
                .andExpect(jsonPath("$.components.securitySchemes.csrf.name").value("X-CSRF-TOKEN"));
        this.mockMvc.perform(get("/swagger-ui/index.html").session(riaditel))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Security-Policy",
                        containsString("style-src 'self' 'unsafe-inline'")))
                .andExpect(header().string("Content-Security-Policy", containsString("script-src 'self';")));
        this.mockMvc.perform(get("/riaditel").session(riaditel))
                .andExpect(header().string("Content-Security-Policy", not(containsString("unsafe-inline"))));
    }

    @Test
    void strankyPosielajuBezpecnostneHlavicky() throws Exception {
        this.mockMvc.perform(get("/"))
                .andExpect(header().string("Content-Security-Policy", containsString("default-src 'self'")))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"));
    }

    /**
     * Prihlasi pouzivatela. Ak ma docasne heslo, rovno si ho zmeni na {@link #NOVE_HESLO}.
     */
    private MockHttpSession prihlas(Rola rola, String id, String heslo) throws Exception {
        MockHttpSession session = new MockHttpSession();
        String ciel = this.mockMvc.perform(post("/prihlasenie").with(csrf()).session(session)
                        .param("rola", rola.name()).param("id", id).param("heslo", heslo))
                .andExpect(status().is3xxRedirection())
                .andReturn().getResponse().getRedirectedUrl();
        if ("/heslo".equals(ciel)) {
            this.zmenHeslo(session, heslo, NOVE_HESLO).andExpect(redirectedUrl(rola.getDomovskaStranka()));
        } else {
            assertEquals(rola.getDomovskaStranka(), ciel);
        }
        return session;
    }

    private ResultActions zmenHeslo(MockHttpSession session, String sucasne, String nove) throws Exception {
        return this.mockMvc.perform(post("/heslo").with(csrf()).session(session)
                .param("sucasneHeslo", sucasne).param("noveHeslo", nove).param("noveHesloZnova", nove));
    }
}
