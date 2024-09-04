package pucmm.eict.proyectofinal.examguard.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class FolderAccessDeniedException extends RuntimeException {
    public FolderAccessDeniedException(String message) {
        super(message);
    }
}