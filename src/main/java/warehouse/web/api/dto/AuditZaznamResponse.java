package warehouse.web.api.dto;

import java.time.Instant;

/**
 * Zaznam auditu v odpovedi.
 * @param id id zaznamu
 * @param cas cas udalosti v UTC
 * @param pouzivatel kto udalost vykonal, napriklad RIADITEL:123
 * @param akcia druh udalosti
 * @param detail doplnujuci opis alebo null
 * @author Juraj
 */
public record AuditZaznamResponse(Long id, Instant cas, String pouzivatel, String akcia, String detail) {
}
