package warehouse.web.api;

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
 * Prevadza vynimky na odpovede vo formate ProblemDetail.
 * @author Juraj
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

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
