package warehouse.web.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import warehouse.domain.Pracovnik;
import warehouse.domain.Regal;
import warehouse.domain.VelkySklad;
import warehouse.service.NenajdenaEntitaException;
import warehouse.service.NeplatnaOperaciaException;
import warehouse.service.SkladService;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RiaditelApiController.class)
@Import(DtoMapper.class)
class RiaditelApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SkladService skladService;

    @Test
    void prijatiePracovnikaVrati201() throws Exception {
        given(this.skladService.prijmiPracovnika("5", "Peter", "Novak"))
                .willReturn(new Pracovnik("5", "Peter", "Novak"));

        this.mockMvc.perform(post("/api/riaditel/pracovnici")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"5\",\"meno\":\"Peter\",\"priezvisko\":\"Novak\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("5"))
                .andExpect(jsonPath("$.miestnost").doesNotExist());
    }

    @Test
    void neplatneIdVrati400() throws Exception {
        this.mockMvc.perform(post("/api/riaditel/pracovnici")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"x\",\"meno\":\"Peter\",\"priezvisko\":\"Novak\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.chyby.id").exists());
    }

    @Test
    void konfliktVrati409() throws Exception {
        given(this.skladService.prijmiPracovnika(anyString(), anyString(), anyString()))
                .willThrow(new NeplatnaOperaciaException("Pracovnik so zadanym ID uz existuje."));

        this.mockMvc.perform(post("/api/riaditel/pracovnici")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"5\",\"meno\":\"Peter\",\"priezvisko\":\"Novak\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Neplatna operacia"));
    }

    @Test
    void neexistujuciPracovnikPriVyluceniVrati404() throws Exception {
        willThrow(new NenajdenaEntitaException("Pracovnik 99 neexistuje."))
                .given(this.skladService).vylucPracovnika("99");

        this.mockMvc.perform(delete("/api/riaditel/pracovnici/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Entita neexistuje"));
    }

    @Test
    void zoznamPracovnikovVratiJson() throws Exception {
        given(this.skladService.pracovnici())
                .willReturn(List.of(new Pracovnik("5", "Peter", "Novak")));

        this.mockMvc.perform(get("/api/riaditel/pracovnici"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("5"));
    }

    @Test
    void pridanieRegaluVrati201() throws Exception {
        given(this.skladService.pridajRegal(5))
                .willReturn(new Regal(new VelkySklad("skladTovaru"), 5));

        this.mockMvc.perform(post("/api/riaditel/regale")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"kapacita\":5}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.kapacita").value(5));
    }

    @Test
    void neplatnaKapacitaRegaluVrati400() throws Exception {
        this.mockMvc.perform(post("/api/riaditel/regale")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"kapacita\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.chyby.kapacita").exists());
    }
}
