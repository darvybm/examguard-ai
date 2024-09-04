package pucmm.eict.proyectofinal.examguard.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class RecordingNotFoundException extends RuntimeException {
    public RecordingNotFoundException(String message) {
        super(message);
    }
}
