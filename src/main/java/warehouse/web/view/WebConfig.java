package warehouse.web.view;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import warehouse.web.api.ApiRolaInterceptor;

import java.util.Set;

import static warehouse.web.view.SessionKluce.ROLA_PRACOVNIK;
import static warehouse.web.view.SessionKluce.ROLA_RIADITEL;
import static warehouse.web.view.SessionKluce.ROLA_ZAKAZNIK;

/**
 * Registracia interceptorov pre role, pre obrazovky aj pre REST rozhranie.
 * @author Juraj
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Zaregistruje interceptor rol pre jednotlive casti aplikacie. Obrazovky pri neopravnenom pristupe
     * presmeruju na uvodnu stranku, REST rozhranie vrati 401 alebo 403.
     * @param registry registracia interceptorov
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RolaInterceptor(ROLA_RIADITEL)).addPathPatterns("/riaditel", "/riaditel/**");
        registry.addInterceptor(new RolaInterceptor(ROLA_PRACOVNIK)).addPathPatterns("/pracovnik", "/pracovnik/**");
        registry.addInterceptor(new RolaInterceptor(ROLA_ZAKAZNIK)).addPathPatterns("/zakaznik", "/zakaznik/**");

        registry.addInterceptor(new ApiRolaInterceptor(Set.of(ROLA_RIADITEL), null))
                .addPathPatterns("/api/riaditel/**");
        registry.addInterceptor(new ApiRolaInterceptor(Set.of(ROLA_PRACOVNIK), "id"))
                .addPathPatterns("/api/pracovnik/**");
        registry.addInterceptor(new ApiRolaInterceptor(Set.of(ROLA_ZAKAZNIK), "zakaznikId"))
                .addPathPatterns("/api/zakaznik/**");
        registry.addInterceptor(new ApiRolaInterceptor(Set.of(ROLA_RIADITEL, ROLA_PRACOVNIK), null))
                .addPathPatterns("/api/miestnosti/**");
    }
}
