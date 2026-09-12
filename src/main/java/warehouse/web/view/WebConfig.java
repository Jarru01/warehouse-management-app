package warehouse.web.view;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Registracia interceptorov pre role.
 * @author Juraj
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Zaregistruje interceptor rol pre jednotlive casti aplikacie.
     * @param registry registracia interceptorov
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RolaInterceptor("RIADITEL")).addPathPatterns("/riaditel", "/riaditel/**");
        registry.addInterceptor(new RolaInterceptor("PRACOVNIK")).addPathPatterns("/pracovnik", "/pracovnik/**");
        registry.addInterceptor(new RolaInterceptor("ZAKAZNIK")).addPathPatterns("/zakaznik", "/zakaznik/**");
    }
}
