package warehouse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Vstupny bod webovej aplikacie skladu.
 * @author Juraj
 */
@SpringBootApplication
public class WarehouseApplication {

    /**
     * Spusti Spring Boot aplikaciu.
     * @param args argumenty prikazoveho riadku
     */
    public static void main(String[] args) {
        SpringApplication.run(WarehouseApplication.class, args);
    }
}
