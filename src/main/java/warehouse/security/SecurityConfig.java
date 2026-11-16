package warehouse.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Konfiguracia Spring Security: prihlasenie formularom s heslom, pristup podla roly, ochrana CSRF
 * a odpovede 401/403 vo formate ProblemDetail pre REST rozhranie.
 * @author Juraj
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private static final RequestMatcher API = request ->
            request.getRequestURI().startsWith(request.getContextPath() + "/api/");

    /**
     * Kodovac hesiel. Predvolene pouziva BCrypt a v hashi si pamata pouzity algoritmus.
     * @return kodovac hesiel
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /**
     * Retazec bezpecnostnych filtrov aplikacie.
     * @param http konfigurator HTTP bezpecnosti
     * @return retazec filtrov
     * @throws Exception chyba konfiguracie
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(pristup -> pristup
                        .requestMatchers("/", "/registracia", "/style.css", "/app.js", "/error",
                                "/api/status", "/api/csrf").permitAll()
                        .requestMatchers("/riaditel", "/riaditel/**", "/api/riaditel/**").hasRole(Rola.RIADITEL.name())
                        .requestMatchers("/pracovnik", "/pracovnik/**").hasRole(Rola.PRACOVNIK.name())
                        .requestMatchers("/api/pracovnik/{id}", "/api/pracovnik/{id}/**")
                        .access(vlastnaIdentita(Rola.PRACOVNIK, "id"))
                        .requestMatchers("/zakaznik", "/zakaznik/**", "/api/zakaznik/tovar")
                        .hasRole(Rola.ZAKAZNIK.name())
                        .requestMatchers("/api/zakaznik/{zakaznikId}/**")
                        .access(vlastnaIdentita(Rola.ZAKAZNIK, "zakaznikId"))
                        .requestMatchers("/api/miestnosti", "/api/miestnosti/**")
                        .hasAnyRole(Rola.RIADITEL.name(), Rola.PRACOVNIK.name())
                        .anyRequest().authenticated())
                .formLogin(prihlasenie -> prihlasenie
                        .loginPage("/")
                        .loginProcessingUrl("/prihlasenie")
                        .usernameParameter("id")
                        .passwordParameter("heslo")
                        .authenticationDetailsSource(PrihlasenieDetails::new)
                        .successHandler(SecurityConfig::poPrihlaseni)
                        .failureHandler(SecurityConfig::poNeuspesnomPrihlaseni)
                        .permitAll())
                .logout(odhlasenie -> odhlasenie
                        .logoutUrl("/odhlasenie")
                        .logoutSuccessUrl("/"))
                .exceptionHandling(chyby -> chyby
                        .defaultAuthenticationEntryPointFor((request, response, vynimka) ->
                                zapisProblem(response, HttpServletResponse.SC_UNAUTHORIZED,
                                        "Neprihlaseny pouzivatel", "Pristup vyzaduje prihlasenie."), API)
                        .defaultAuthenticationEntryPointFor(new LoginUrlAuthenticationEntryPoint("/"),
                                AnyRequestMatcher.INSTANCE)
                        .defaultAccessDeniedHandlerFor(SecurityConfig::zamietnutieApi, API)
                        .defaultAccessDeniedHandlerFor(SecurityConfig::zamietnutieStranky, AnyRequestMatcher.INSTANCE));
        return http.build();
    }

    /**
     * Povoli poziadavku iba danej roli a iba ak premenna v ceste zodpoveda id prihlaseneho pouzivatela.
     * @param rola pozadovana rola
     * @param premenna nazov premennej v ceste s id
     * @return rozhodovac o pristupe
     */
    static AuthorizationManager<RequestAuthorizationContext> vlastnaIdentita(Rola rola, String premenna) {
        return (autentifikacia, kontext) -> {
            Authentication prihlasenie = autentifikacia.get();
            boolean povolene = prihlasenie != null
                    && prihlasenie.getPrincipal() instanceof PrihlasenyPouzivatel pouzivatel
                    && pouzivatel.rola() == rola
                    && pouzivatel.id().equals(kontext.getVariables().get(premenna));
            return new AuthorizationDecision(povolene);
        };
    }

    private static void poPrihlaseni(HttpServletRequest request, HttpServletResponse response,
                                     Authentication prihlasenie) throws IOException {
        PrihlasenyPouzivatel pouzivatel = (PrihlasenyPouzivatel) prihlasenie.getPrincipal();
        response.sendRedirect(request.getContextPath() + pouzivatel.rola().getDomovskaStranka());
    }

    private static void poNeuspesnomPrihlaseni(HttpServletRequest request, HttpServletResponse response,
                                               AuthenticationException vynimka) throws IOException {
        String rola = Rola.podlaNazvu(request.getParameter("rola")).orElse(Rola.RIADITEL).getKluc();
        response.sendRedirect(request.getContextPath() + "/?rola=" + rola + "&chyba");
    }

    private static void zamietnutieApi(HttpServletRequest request, HttpServletResponse response,
                                       AccessDeniedException vynimka) throws IOException {
        String detail = vynimka instanceof CsrfException
                ? "Chyba alebo je neplatny CSRF token."
                : "Prihlaseny pouzivatel nema pristup k tejto operacii.";
        zapisProblem(response, HttpServletResponse.SC_FORBIDDEN, "Zakazany pristup", detail);
    }

    private static void zamietnutieStranky(HttpServletRequest request, HttpServletResponse response,
                                           AccessDeniedException vynimka) throws IOException {
        response.sendRedirect(request.getContextPath() + "/");
    }

    private static void zapisProblem(HttpServletResponse response, int status, String titul, String detail)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/problem+json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("{\"type\":\"about:blank\",\"title\":\"" + titul + "\",\"status\":" + status
                + ",\"detail\":\"" + detail + "\"}");
    }
}
