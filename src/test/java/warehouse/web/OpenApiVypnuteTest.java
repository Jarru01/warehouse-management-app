package warehouse.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import warehouse.AbstractIntegrationTest;
import warehouse.security.PrihlasenyPouzivatel;
import warehouse.security.Rola;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * S nastavenim z prod profilu nie je popis API dostupny ani pre riaditela.
 */
@SpringBootTest(properties = {"springdoc.api-docs.enabled=false", "springdoc.swagger-ui.enabled=false"})
@AutoConfigureMockMvc
class OpenApiVypnuteTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void vProdukciiPopisApiNeexistuje() throws Exception {
        PrihlasenyPouzivatel riaditel = new PrihlasenyPouzivatel(Rola.RIADITEL, "123", "Test", "Riaditel", false);
        var prihlasenie = authentication(UsernamePasswordAuthenticationToken.authenticated(riaditel, null,
                List.of(new SimpleGrantedAuthority(Rola.RIADITEL.getOpravnenie()))));

        this.mockMvc.perform(get("/v3/api-docs").with(prihlasenie)).andExpect(status().isNotFound());
        this.mockMvc.perform(get("/swagger-ui/index.html").with(prihlasenie)).andExpect(status().isNotFound());
    }
}
