package warehouse.web.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import warehouse.AbstractIntegrationTest;
import warehouse.domain.Miestnost;
import warehouse.repository.TovarRepository;
import warehouse.domain.NenajdenaEntitaException;
import warehouse.service.SkladService;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ViewFlowTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SkladService skladService;

    @Autowired
    private TovarRepository tovarRepository;

    @Test
    void uvodnaStrankaZobrazujeRoloveTabyAIbaJednuFormu() throws Exception {
        this.mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Riaditeľ")))
                .andExpect(content().string(containsString("Pracovník")))
                .andExpect(content().string(containsString("Zákazník")))
                .andExpect(content().string(containsString("/prihlasenie/riaditel")))
                .andExpect(content().string(not(containsString("/prihlasenie/zakaznik"))));
    }

    @Test
    void vyberRolyZakaznikZobrazujeIbaJehoFormu() throws Exception {
        this.mockMvc.perform(get("/").param("rola", "zakaznik"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/prihlasenie/zakaznik")))
                .andExpect(content().string(not(containsString("/prihlasenie/riaditel"))));
    }

    @Test
    void neopravnenyPristupPresmerujeNaUvod() throws Exception {
        this.mockMvc.perform(get("/pracovnik"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void riaditelSaPrihlasiAVidiPanel() throws Exception {
        MockHttpSession session = new MockHttpSession();

        this.mockMvc.perform(post("/prihlasenie/riaditel").param("id", "123").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/riaditel"));

        this.mockMvc.perform(get("/riaditel").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Pracovníci")))
                .andExpect(content().string(containsString("Prepnúť používateľa")));
    }

    @Test
    void nespravnePrihlasenieRiaditelaZobraziChybu() throws Exception {
        this.mockMvc.perform(post("/prihlasenie/riaditel").param("id", "999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attributeExists("chyba"));
    }

    @Test
    void pracovnikSaPrihlasiAVidiSvojuMiestnost() throws Exception {
        this.skladService.prijmiPracovnika("1", "Peter", "Novak");
        MockHttpSession session = new MockHttpSession();

        this.mockMvc.perform(post("/prihlasenie/pracovnik").param("id", "1").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/pracovnik"));

        this.mockMvc.perform(get("/pracovnik").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Peter")))
                .andExpect(content().string(containsString(Miestnost.SKLAD_TOVARU)));
    }

    @Test
    void zakaznikVloziTovarCezFormular() throws Exception {
        MockHttpSession session = this.prihlasZakaznika("10", "Jan", "Novy");

        this.mockMvc.perform(post("/zakaznik/tovar")
                        .param("id", "100")
                        .param("nazov", "Tovar100")
                        .param("vaha", "500")
                        .param("prijemcaId", "20")
                        .param("prijemcaMeno", "Peter")
                        .param("prijemcaPriezvisko", "Novak")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/zakaznik"));

        assertEquals(Miestnost.PRIJEM_TOVARU, this.skladService.tovar("100").getRegal().getMiestnost().getKluc());
    }

    @Test
    void kompletnyTokOdVlozeniaPoVyzdvihnutie() throws Exception {
        this.skladService.prijmiPracovnika("1", "Peter", "Novak");
        MockHttpSession zakaznikSession = this.prihlasZakaznika("10", "Jan", "Novy");
        MockHttpSession prijemcaSession = this.prihlasZakaznika("20", "Peter", "Novak");
        MockHttpSession pracovnikSession = this.prihlasPracovnika("1");

        this.mockMvc.perform(post("/zakaznik/tovar")
                        .param("id", "100").param("nazov", "Tovar100").param("vaha", "500")
                        .param("prijemcaId", "20").param("prijemcaMeno", "Peter").param("prijemcaPriezvisko", "Novak")
                        .session(zakaznikSession))
                .andExpect(redirectedUrl("/zakaznik"));

        this.mockMvc.perform(post("/pracovnik/presun").param("miestnost", Miestnost.PRIJEM_TOVARU)
                        .session(pracovnikSession))
                .andExpect(redirectedUrl("/pracovnik"));
        this.mockMvc.perform(post("/pracovnik/zober").param("tovarId", "100").session(pracovnikSession))
                .andExpect(redirectedUrl("/pracovnik"));
        this.mockMvc.perform(post("/pracovnik/presun").param("miestnost", Miestnost.VYDAJ_TOVARU)
                        .session(pracovnikSession))
                .andExpect(redirectedUrl("/pracovnik"));
        this.mockMvc.perform(post("/pracovnik/uloz").session(pracovnikSession))
                .andExpect(redirectedUrl("/pracovnik"));

        this.mockMvc.perform(get("/zakaznik").session(prijemcaSession))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Tovar100")));

        this.mockMvc.perform(post("/zakaznik/vyzdvihnutie/100").session(prijemcaSession))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/zakaznik"));

        assertTrue(this.tovarRepository.findById("100").isEmpty());
        assertThrows(NenajdenaEntitaException.class, () -> this.skladService.tovar("100"));
    }

    private MockHttpSession prihlasZakaznika(String id, String meno, String priezvisko) throws Exception {
        MockHttpSession session = new MockHttpSession();
        this.mockMvc.perform(post("/prihlasenie/zakaznik")
                        .param("id", id).param("meno", meno).param("priezvisko", priezvisko).session(session))
                .andExpect(redirectedUrl("/zakaznik"));
        return session;
    }

    private MockHttpSession prihlasPracovnika(String id) throws Exception {
        MockHttpSession session = new MockHttpSession();
        this.mockMvc.perform(post("/prihlasenie/pracovnik").param("id", id).session(session))
                .andExpect(redirectedUrl("/pracovnik"));
        return session;
    }
}
