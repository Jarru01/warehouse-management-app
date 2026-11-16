package warehouse.web.api;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.ProblemDetail;
import warehouse.domain.NenajdenaEntitaException;
import warehouse.domain.NeplatnaOperaciaException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Prevadza vynimky REST rozhrania na odpovede vo formate ProblemDetail. Obrazovky maju vlastne spracovanie.
 * @author Juraj
 */
@RestControllerAdvice(basePackageClasses = GlobalExceptionHandler.class)
public class GlobalExceptionHandler {
    /** Sprava pre sucasne zmeny tych istych udajov. */
    public static final String SPRAVA_KONFLIKTU = "Udaje sa medzicasom zmenili alebo uz existuju. Skus to znova.";

    /**
     * Prevedie konflikt pri ukladani (sucasna zmena, porusene obmedzenie databazy) na odpoved 409.
     * @param vynimka vynimka
     * @return problemovy detail
     */
    @ExceptionHandler({DataIntegrityViolationException.class, OptimisticLockingFailureException.class})
    public ProblemDetail konflikt(RuntimeException vynimka) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, SPRAVA_KONFLIKTU);
        problem.setTitle("Konflikt");
        return problem;
    }

    /**
     * Prevedie neopravneny pristup na odpoved 403.
     * @param vynimka vynimka
     * @return problemovy detail
     */
    @ExceptionHandler(ZakazanyPristupException.class)
    public ProblemDetail zakazanyPristup(ZakazanyPristupException vynimka) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, vynimka.getMessage());
        problem.setTitle("Zakazany pristup");
        return problem;
    }

    /**
     * Prevedie nenajdenu entitu na odpoved 404.
     * @param vynimka vynimka
     * @return problemovy detail
     */
    @ExceptionHandler(NenajdenaEntitaException.class)
    public ProblemDetail nenajdenaEntita(NenajdenaEntitaException vynimka) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, vynimka.getMessage());
        problem.setTitle("Entita neexistuje");
        return problem;
    }

    /**
     * Prevedie neplatnu operaciu na odpoved 409.
     * @param vynimka vynimka
     * @return problemovy detail
     */
    @ExceptionHandler(NeplatnaOperaciaException.class)
    public ProblemDetail neplatnaOperacia(NeplatnaOperaciaException vynimka) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, vynimka.getMessage());
        problem.setTitle("Neplatna operacia");
        return problem;
    }

    /**
     * Prevedie chyby validacie poziadavky na odpoved 400.
     * @param vynimka vynimka
     * @return problemovy detail s chybami jednotlivych poli
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail neplatnyVstup(MethodArgumentNotValidException vynimka) {
        Map<String, String> chyby = new LinkedHashMap<>();
        for (FieldError chyba : vynimka.getBindingResult().getFieldErrors()) {
            chyby.put(chyba.getField(), chyba.getDefaultMessage());
        }
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Neplatny vstup.");
        problem.setTitle("Neplatny vstup");
        problem.setProperty("chyby", chyby);
        return problem;
    }
}
