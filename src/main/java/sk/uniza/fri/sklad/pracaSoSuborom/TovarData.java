package sk.uniza.fri.sklad.pracaSoSuborom;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Datovy model tovaru v subore skladu.
 * @param id id tovaru
 * @param nazov nazov tovaru
 * @param vaha vaha tovaru v gramoch
 * @param odosielatel odosielatel tovaru
 * @param prijemca prijemca tovaru
 * @author Juraj
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TovarData(String id, String nazov, int vaha, OsobaData odosielatel, OsobaData prijemca) {
}
