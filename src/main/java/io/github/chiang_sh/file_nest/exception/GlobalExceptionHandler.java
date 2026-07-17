package io.github.chiang_sh.file_nest.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> illegalArgumentException(Exception e) {
        logger.error(e.getMessage(), e);
        return ResponseEntity.badRequest().build();
    }

    @ExceptionHandler(exception = {NoSuchElementException.class, IllegalStateException.class})
    public ResponseEntity<String> noSuchElementException(Exception e) {
        logger.error(e.getMessage(), e);
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> generalException(Exception e) {
        logger.error("Unhandled exception", e);
        return ResponseEntity.internalServerError().body("Internal server error.");
    }
}
