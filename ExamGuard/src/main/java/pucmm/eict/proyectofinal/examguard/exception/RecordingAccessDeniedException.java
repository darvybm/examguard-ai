package pucmm.eict.proyectofinal.examguard.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class RecordingAccessDeniedException extends RuntimeException {
    public RecordingAccessDeniedException(String message) {
        super(message);
    }
}
