package warehouse.web.view;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Overi data z formularov rovnakymi pravidlami Bean Validation ako REST rozhranie.
 * @author Juraj
 */
@Component
public class FormularValidator {
    private final Validator validator;

    /**
     * Vytvori validator formularov.
     * @param validator validator Bean Validation
     */
    public FormularValidator(Validator validator) {
        this.validator = validator;
    }

    /**
     * Overi objekt poziadavky a vrati spravu s chybami, najviac jednou pre kazde pole.
     * @param poziadavka objekt poziadavky s anotaciami Bean Validation
     * @return sprava s chybami alebo prazdny vysledok, ak je vstup platny
     */
    public Optional<String> chyby(Object poziadavka) {
        Map<String, String> chybyPoli = new TreeMap<>();
        this.validator.validate(poziadavka).stream()
                .sorted(Comparator.comparing(ConstraintViolation::getMessage))
                .forEach(chyba -> chybyPoli.putIfAbsent(chyba.getPropertyPath().toString(), chyba.getMessage()));
        if (chybyPoli.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(String.join(" ", chybyPoli.values()));
    }
}
