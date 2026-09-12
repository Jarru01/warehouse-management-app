package warehouse.web.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import warehouse.domain.Tovar;
import warehouse.domain.ZakaznikInfo;
import warehouse.service.NeplatnaOperaciaException;
import warehouse.service.SkladService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ZakaznikApiController.class)
@Import(DtoMapper.class)
class ZakaznikApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SkladService skladService;

    @Test
    void vlozenieTovaruVrati201() throws Exception {
        Tovar tovar = new Tovar("100", "Tovar100", 500,
                new ZakaznikInfo("10", "Jan", "Novy"), new ZakaznikInfo("20", "Peter", "Novak"));
        given(this.skladService.vytvorTovar(eq("100"), eq("Tovar100"), eq(500),
                any(ZakaznikInfo.class), any(ZakaznikInfo.class))).willReturn(tovar);

        this.mockMvc.perform(post("/api/zakaznik/tovar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": "100",
                                  "nazov": "Tovar100",
                                  "vaha": 500,
                                  "odosielatel": {"id": "10", "meno": "Jan", "priezvisko": "Novy"},
                                  "prijemca": {"id": "20", "meno": "Peter", "priezvisko": "Novak"}
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("100"))
                .andExpect(jsonPath("$.prijemca.id").value("20"));
    }

    @Test
    void neplatnaVahaVrati400() throws Exception {
        this.mockMvc.perform(post("/api/zakaznik/tovar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": "100",
                                  "nazov": "Tovar100",
                                  "vaha": 0,
                                  "odosielatel": {"id": "10", "meno": "Jan", "priezvisko": "Novy"},
                                  "prijemca": {"id": "20", "meno": "Peter", "priezvisko": "Novak"}
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.chyby.vaha").exists());
    }

    @Test
    void duplicitnyTovarVrati409() throws Exception {
        given(this.skladService.vytvorTovar(any(), any(), any(Integer.class), any(), any()))
                .willThrow(new NeplatnaOperaciaException("Tovar so zadanym ID uz existuje."));

        this.mockMvc.perform(post("/api/zakaznik/tovar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": "100",
                                  "nazov": "Tovar100",
                                  "vaha": 500,
                                  "odosielatel": {"id": "10", "meno": "Jan", "priezvisko": "Novy"},
                                  "prijemca": {"id": "20", "meno": "Peter", "priezvisko": "Novak"}
                                }
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void vyzdvihnutieVrati204() throws Exception {
        this.mockMvc.perform(post("/api/zakaznik/20/vyzdvihnutie/100"))
                .andExpect(status().isNoContent());
    }

    @Test
    void vyzdvihnutieCudziehoTovaruVrati409() throws Exception {
        willThrow(new NeplatnaOperaciaException("Tovar nepatri zadanemu zakaznikovi."))
                .given(this.skladService).vyzdvihniTovar("100", "10");

        this.mockMvc.perform(post("/api/zakaznik/10/vyzdvihnutie/100"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Neplatna operacia"));
    }
}
