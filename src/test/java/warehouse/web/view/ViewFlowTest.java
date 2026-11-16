package warehouse.web.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import warehouse.AbstractIntegrationTest;
import warehouse.domain.Miestnost;
import warehouse.domain.NenajdenaEntitaException;
import warehouse.repository.TovarRepository;
import warehouse.security.Rola;
import warehouse.service.SkladService;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
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

    @Value("${warehouse.riaditel.heslo}")
    private String hesloRiaditela;

    @BeforeEach
    void zaregistrujZakaznikov() {
        this.skladService.registrujZakaznika("10", "Jan", "Novy", HESLO);
        this.skladService.registrujZakaznika("20", "Peter", "Novak", HESLO);
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
                        .param("id", "30").param("meno", "Eva").param("priezvisko", "Mala").param("heslo", HESLO))
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
                        .param("id", "30").param("meno", "Eva").param("priezvisko", "Mala").param("heslo", "kratke"))
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
