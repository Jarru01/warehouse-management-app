package warehouse;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import warehouse.domain.AuditAkcia;
import warehouse.domain.AuditZaznam;
import warehouse.service.AuditService;

import java.net.CookieManager;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testy cez skutocny server Tomcat: sessions v databaze (Spring Session JDBC) a spracovanie hlaviciek
 * X-Forwarded-* od reverse proxy, ako v produkcii.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.autoconfigure.exclude=",
        "server.forward-headers-strategy=native"
})
class HttpIntegracnyTest extends AbstractIntegrationTest {
    private static final String ADRESA_KLIENTA = "203.0.113.7";

    @Value("${local.server.port}")
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private AuditService auditService;

    private final CookieManager cookies = new CookieManager();
    private final HttpClient klient = HttpClient.newBuilder().cookieHandler(this.cookies).build();

    @Test
    void sessionJeVDatabazeAProxyHlavickySaRespektuju() throws Exception {
        HttpResponse<String> csrf = this.klient.send(this.cezProxy("/api/csrf", false).GET().build(),
                HttpResponse.BodyHandlers.ofString());

        assertEquals(200, csrf.statusCode());
        assertTrue(csrf.headers().firstValue("Strict-Transport-Security").isEmpty(), "obycajne HTTP bez HSTS");
        assertTrue(this.cookies.getCookieStore().getCookies().stream()
                .anyMatch(cookie -> cookie.getName().equals("SESSION")), "session cookie zo Spring Session");
        Integer sessions = this.jdbcTemplate.queryForObject("select count(*) from spring_session", Integer.class);
        assertTrue(sessions != null && sessions > 0, "session je ulozena v PostgreSQL");

        String formular = "rola=RIADITEL&id=999&heslo=" + kod("zle-heslo-1") + "&_csrf=" + kod(token(csrf.body()));
        HttpResponse<String> prihlasenie = this.klient.send(this.cezProxy("/prihlasenie", false)
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(HttpRequest.BodyPublishers.ofString(formular)).build(),
                HttpResponse.BodyHandlers.ofString());

        assertEquals(302, prihlasenie.statusCode());
        assertTrue(prihlasenie.headers().firstValue("Location").orElseThrow().endsWith("/?rola=riaditel&chyba"));
        AuditZaznam zaznam = this.auditService.najnovsie(AuditAkcia.NEUSPESNE_PRIHLASENIE, 50).stream()
                .filter(z -> z.getPouzivatel().equals("RIADITEL:999"))
                .findFirst().orElseThrow();
        assertEquals("adresa " + ADRESA_KLIENTA, zaznam.getDetail(), "audit vidi skutocnu IP klienta za proxy");
    }

    @Test
    void poziadavkaZHttpsProxyDostaneHstsASecureCookie() throws Exception {
        HttpClient novyKlient = HttpClient.newHttpClient();
        HttpResponse<String> odpoved = novyKlient.send(this.cezProxy("/api/csrf", true).GET().build(),
                HttpResponse.BodyHandlers.ofString());

        assertEquals(200, odpoved.statusCode());
        assertTrue(odpoved.headers().firstValue("Strict-Transport-Security").isPresent(),
                "poziadavka z HTTPS proxy sa povazuje za zabezpecenu");
        String cookie = odpoved.headers().firstValue("Set-Cookie").orElseThrow();
        assertTrue(cookie.startsWith("SESSION=") && cookie.contains("Secure") && cookie.contains("HttpOnly"),
                "session cookie je Secure a HttpOnly: " + cookie);
    }

    @Test
    void healthCheckBeziCezSkutocnyServer() throws Exception {
        HttpResponse<String> odpoved = this.klient.send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + this.port + "/actuator/health/liveness"))
                        .GET().build(), HttpResponse.BodyHandlers.ofString());

        assertEquals(200, odpoved.statusCode());
        assertTrue(odpoved.body().contains("UP"));
    }

    private HttpRequest.Builder cezProxy(String cesta, boolean https) {
        HttpRequest.Builder poziadavka = HttpRequest.newBuilder(URI.create("http://localhost:" + this.port + cesta))
                .header("X-Forwarded-For", ADRESA_KLIENTA);
        return https ? poziadavka.header("X-Forwarded-Proto", "https") : poziadavka;
    }

    private static String token(String json) {
        Matcher zhoda = Pattern.compile("\"token\"\\s*:\\s*\"([^\"]+)\"").matcher(json);
        assertTrue(zhoda.find(), "odpoved obsahuje CSRF token");
        return zhoda.group(1);
    }

    private static String kod(String hodnota) {
        return URLEncoder.encode(hodnota, StandardCharsets.UTF_8);
    }
}
