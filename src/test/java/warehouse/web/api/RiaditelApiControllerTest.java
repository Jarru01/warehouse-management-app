package warehouse.web.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import warehouse.domain.Pracovnik;
import warehouse.domain.Regal;
import warehouse.domain.VelkySklad;
import warehouse.domain.NenajdenaEntitaException;
import warehouse.domain.NeplatnaOperaciaException;
import warehouse.security.Rola;
import warehouse.security.SecurityConfig;
import warehouse.service.SkladService;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static warehouse.web.api.ApiPrihlasenie.bezCsrf;
import static warehouse.web.api.ApiPrihlasenie.neprihlaseny;
import static warehouse.web.api.ApiPrihlasenie.pracovnik;
import static warehouse.web.api.ApiPrihlasenie.riaditel;
import static warehouse.web.api.ApiPrihlasenie.sDocasnymHeslom;

@WebMvcTest(RiaditelApiController.class)
@Import({DtoMapper.class, SecurityConfig.class})
class RiaditelApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SkladService skladService;

    @Test
    void prijatiePracovnikaVrati201() throws Exception {
        given(this.skladService.prijmiPracovnika("5", "Peter", "Novak", "tajneheslo1"))
                .willReturn(new Pracovnik("5", "Peter", "Novak"));

        this.mockMvc.perform(post("/api/riaditel/pracovnici").with(riaditel())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"5\",\"meno\":\"Peter\",\"priezvisko\":\"Novak\",\"heslo\":\"tajneheslo1\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("5"))
                .andExpect(jsonPath("$.miestnost").doesNotExist());
    }

    @Test
    void neplatneIdVrati400() throws Exception {
        this.mockMvc.perform(post("/api/riaditel/pracovnici").with(riaditel())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"x\",\"meno\":\"Peter\",\"priezvisko\":\"Novak\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.chyby.id").exists());
    }

    @Test
    void prilisDlheMenoVrati400() throws Exception {
        String dlheMeno = "A".repeat(101);

        this.mockMvc.perform(post("/api/riaditel/pracovnici").with(riaditel())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"5\",\"meno\":\"" + dlheMeno
                                + "\",\"priezvisko\":\"Novak\",\"heslo\":\"tajneheslo1\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.chyby.meno").exists());
    }

    @Test
    void konfliktVrati409() throws Exception {
        given(this.skladService.prijmiPracovnika(anyString(), anyString(), anyString(), anyString()))
                .willThrow(new NeplatnaOperaciaException("Pracovnik so zadanym ID uz existuje."));

        this.mockMvc.perform(post("/api/riaditel/pracovnici").with(riaditel())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"5\",\"meno\":\"Peter\",\"priezvisko\":\"Novak\",\"heslo\":\"tajneheslo1\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Neplatna operacia"));
    }

    @Test
    void neexistujuciPracovnikPriVyluceniVrati404() throws Exception {
        willThrow(new NenajdenaEntitaException("Pracovnik 99 neexistuje."))
                .given(this.skladService).vylucPracovnika("99");

        this.mockMvc.perform(delete("/api/riaditel/pracovnici/99").with(riaditel()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Entita neexistuje"));
    }

    @Test
    void zoznamPracovnikovVratiJson() throws Exception {
        given(this.skladService.pracovnici())
                .willReturn(List.of(new Pracovnik("5", "Peter", "Novak")));

        this.mockMvc.perform(get("/api/riaditel/pracovnici").with(riaditel()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("5"));
    }

    @Test
    void pridanieRegaluVrati201() throws Exception {
        given(this.skladService.pridajRegal(5))
                .willReturn(new Regal(new VelkySklad("skladTovaru"), 5));

        this.mockMvc.perform(post("/api/riaditel/regale").with(riaditel())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"kapacita\":5}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.kapacita").value(5));
    }

    @Test
    void neplatnaKapacitaRegaluVrati400() throws Exception {
        this.mockMvc.perform(post("/api/riaditel/regale").with(riaditel())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"kapacita\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.chyby.kapacita").exists());
    }

    @Test
    void kratkeHesloVrati400() throws Exception {
        this.mockMvc.perform(post("/api/riaditel/pracovnici").with(riaditel())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"5\",\"meno\":\"Peter\",\"priezvisko\":\"Novak\",\"heslo\":\"kratke\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.chyby.heslo").exists());
    }

    @Test
    void resetHeslaPracovnikaVrati204() throws Exception {
        this.mockMvc.perform(post("/api/riaditel/pracovnici/5/heslo").with(riaditel())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"heslo\":\"nove-docasne-1\"}"))
                .andExpect(status().isNoContent());

        then(this.skladService).should().resetujHesloPracovnika("5", "nove-docasne-1");
    }

    @Test
    void resetSKratkymHeslomVrati400() throws Exception {
        this.mockMvc.perform(post("/api/riaditel/pracovnici/5/heslo").with(riaditel())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"heslo\":\"kratke\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.chyby.heslo").exists());
    }

    @Test
    void sucasnaZmenaVrati409() throws Exception {
        given(this.skladService.pridajRegal(5)).willThrow(new DataIntegrityViolationException("duplicita"));

        this.mockMvc.perform(post("/api/riaditel/regale").with(riaditel())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"kapacita\":5}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Konflikt"));
    }

    @Test
    void zastaraneUdajeVrati409() throws Exception {
        willThrow(new ObjectOptimisticLockingFailureException(Regal.class, 1L))
                .given(this.skladService).odstranRegal(1L);

        this.mockMvc.perform(delete("/api/riaditel/regale/1").with(riaditel()))
                .andExpect(status().isConflict());
    }

    @Test
    void docasneHesloBlokujeApi() throws Exception {
        this.mockMvc.perform(get("/api/riaditel/pracovnici").with(sDocasnymHeslom(Rola.RIADITEL, "123")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value("Najprv si zmen docasne heslo."));

        then(this.skladService).should(never()).pracovnici();
    }

    @Test
    void bezCsrfTokenuVrati403() throws Exception {
        this.mockMvc.perform(delete("/api/riaditel/pracovnici/5").with(bezCsrf(Rola.RIADITEL, "123")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Zakazany pristup"));

        then(this.skladService).should(never()).vylucPracovnika(anyString());
    }

    @Test
    void bezPrihlaseniaVrati401() throws Exception {
        this.mockMvc.perform(delete("/api/riaditel/pracovnici/5").with(neprihlaseny()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Neprihlaseny pouzivatel"));

        then(this.skladService).should(never()).vylucPracovnika(anyString());
    }

    @Test
    void inaRolaVrati403() throws Exception {
        this.mockMvc.perform(delete("/api/riaditel/pracovnici/5").with(pracovnik("5")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Zakazany pristup"));

        then(this.skladService).should(never()).vylucPracovnika(anyString());
    }
}
