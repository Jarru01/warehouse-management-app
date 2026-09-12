package warehouse.web.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import warehouse.domain.Miestnost;
import warehouse.domain.Pracovnik;
import warehouse.domain.VelkySklad;
import warehouse.domain.NeplatnaOperaciaException;
import warehouse.service.SkladService;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PracovnikApiController.class)
@Import(DtoMapper.class)
class PracovnikApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SkladService skladService;

    @Test
    void prihlasenieVratiPracovnikaSMiestnostou() throws Exception {
        Pracovnik pracovnik = new Pracovnik("1", "Peter", "Novak");
        pracovnik.premiestniDo(new VelkySklad(Miestnost.SKLAD_TOVARU));
        given(this.skladService.pracovnik("1")).willReturn(pracovnik);

        this.mockMvc.perform(get("/api/pracovnik/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.miestnost").value(Miestnost.SKLAD_TOVARU));
    }

    @Test
    void presunVratiAktualizovanehoPracovnika() throws Exception {
        Pracovnik pracovnik = new Pracovnik("1", "Peter", "Novak");
        pracovnik.premiestniDo(new VelkySklad(Miestnost.PRIJEM_TOVARU));
        given(this.skladService.presunPracovnika("1", Miestnost.PRIJEM_TOVARU)).willReturn(pracovnik);

        this.mockMvc.perform(post("/api/pracovnik/1/presun")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"miestnost\":\"prijemTovaru\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.miestnost").value(Miestnost.PRIJEM_TOVARU));
    }

    @Test
    void zobratieTovaruVrati204() throws Exception {
        this.mockMvc.perform(post("/api/pracovnik/1/zober")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tovarId\":\"100\"}"))
                .andExpect(status().isNoContent());
    }

    @Test
    void neznameMiestoPresunuVrati400() throws Exception {
        given(this.skladService.presunPracovnika(anyString(), anyString()))
                .willThrow(new NeplatnaOperaciaException("Miestnost neexistuje."));

        this.mockMvc.perform(post("/api/pracovnik/1/presun")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"miestnost\":\"neexistuje\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void ulozenieBezTovaruVratiKonflikt() throws Exception {
        willThrow(new NeplatnaOperaciaException("Pracovnik nedrzi ziadny tovar."))
                .given(this.skladService).ulozTovar("1");

        this.mockMvc.perform(post("/api/pracovnik/1/uloz"))
                .andExpect(status().isConflict());
    }
}
