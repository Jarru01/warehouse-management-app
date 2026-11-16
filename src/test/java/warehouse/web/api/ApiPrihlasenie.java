package warehouse.web.api;

import org.springframework.test.web.servlet.request.RequestPostProcessor;
import warehouse.web.view.SessionKluce;

/**
 * Pomocka pre testy REST rozhrania, ktora do poziadavky vlozi session prihlaseneho pouzivatela.
 * @author Juraj
 */
final class ApiPrihlasenie {

    private ApiPrihlasenie() {
    }

    static RequestPostProcessor ako(String rola, String identita) {
        return request -> {
            request.getSession().setAttribute(SessionKluce.ROLA, rola);
            request.getSession().setAttribute(SessionKluce.IDENTITA, identita);
            return request;
        };
    }

    static RequestPostProcessor riaditel() {
        return ako(SessionKluce.ROLA_RIADITEL, "123");
    }

    static RequestPostProcessor pracovnik(String id) {
        return ako(SessionKluce.ROLA_PRACOVNIK, id);
    }

    static RequestPostProcessor zakaznik(String id) {
        return ako(SessionKluce.ROLA_ZAKAZNIK, id);
    }
}
