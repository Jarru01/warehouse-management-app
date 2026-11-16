package warehouse.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Zapne planovane ulohy (pravidelna udrzba dat).
 * @author Juraj
 */
@Configuration
@EnableScheduling
public class PlanovanieConfig {
}
