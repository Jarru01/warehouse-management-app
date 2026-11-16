package warehouse.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Registracia interceptora, ktory vynuti zmenu docasneho hesla.
 * @author Juraj
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Zaregistruje interceptor pre vsetky cesty okrem zmeny hesla, uvodnej stranky a statickych suborov.
     * @param registry registracia interceptorov
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new ZmenaHeslaInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/", "/error", "/style.css", "/app.js", "/api/status", "/api/csrf",
                        SecurityConfig.ZMENA_HESLA, "/api" + SecurityConfig.ZMENA_HESLA);
    }
}
