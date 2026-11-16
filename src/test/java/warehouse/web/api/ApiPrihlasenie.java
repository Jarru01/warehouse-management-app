package warehouse.web.api;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import warehouse.security.PrihlasenyPouzivatel;
import warehouse.security.Rola;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

/**
 * Pomocka pre testy REST rozhrania: prihlaseny pouzivatel s danou rolou a platny CSRF token.
 * @author Juraj
 */
final class ApiPrihlasenie {

    private ApiPrihlasenie() {
    }

    static RequestPostProcessor bezCsrf(Rola rola, String id) {
        return prihlasenie(rola, id, false);
    }

    static RequestPostProcessor sDocasnymHeslom(Rola rola, String id) {
        RequestPostProcessor prihlasenie = prihlasenie(rola, id, true);
        RequestPostProcessor token = csrf();
        return request -> token.postProcessRequest(prihlasenie.postProcessRequest(request));
    }

    private static RequestPostProcessor prihlasenie(Rola rola, String id, boolean zmenaHesla) {
        PrihlasenyPouzivatel pouzivatel = new PrihlasenyPouzivatel(rola, id, "Test", "Pouzivatel", zmenaHesla);
        return authentication(UsernamePasswordAuthenticationToken.authenticated(pouzivatel, null,
                List.of(new SimpleGrantedAuthority(rola.getOpravnenie()))));
    }

    static RequestPostProcessor ako(Rola rola, String id) {
        RequestPostProcessor prihlasenie = bezCsrf(rola, id);
        RequestPostProcessor token = csrf();
        return request -> token.postProcessRequest(prihlasenie.postProcessRequest(request));
    }

    static RequestPostProcessor riaditel() {
        return ako(Rola.RIADITEL, "123");
    }

    static RequestPostProcessor pracovnik(String id) {
        return ako(Rola.PRACOVNIK, id);
    }

    static RequestPostProcessor zakaznik(String id) {
        return ako(Rola.ZAKAZNIK, id);
    }

    static RequestPostProcessor neprihlaseny() {
        return csrf();
    }
}
