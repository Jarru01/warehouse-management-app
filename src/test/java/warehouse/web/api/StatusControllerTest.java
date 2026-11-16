package warehouse.web.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import warehouse.security.SecurityConfig;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StatusController.class)
@Import(SecurityConfig.class)
class StatusControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JdbcTemplate jdbcTemplate;

    @Test
    void dostupnaDatabazaVratiOk() throws Exception {
        given(this.jdbcTemplate.queryForObject("select 1", Integer.class)).willReturn(1);

        this.mockMvc.perform(get("/api/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.database").value("ok"));
    }

    @Test
    void nedostupnaDatabazaVrati503() throws Exception {
        given(this.jdbcTemplate.queryForObject("select 1", Integer.class))
                .willThrow(new CannotGetJdbcConnectionException("databaza nebezi"));

        this.mockMvc.perform(get("/api/status"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.database").value("chyba"));
    }
}
