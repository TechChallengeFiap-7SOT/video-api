package br.com.fiap.soat7.grupo18.videoapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import br.com.fiap.soat7.grupo18.videoapi.exception.NotFoundException;
import br.com.fiap.soat7.grupo18.videoapi.exception.RequiredDataException;

@ControllerAdvice
public class GlobalExceptionHandlerConfig {

    @Value("${spring.servlet.multipart.max-file-size}")
    private String maxFileSizeParameter;

    @ExceptionHandler({RequiredDataException.class})
    @ResponseBody
    public ResponseEntity<String> handleDomainException(RequiredDataException re){
        return ResponseEntity.badRequest().body(re.getMessage());
    }

    @ExceptionHandler({MaxUploadSizeExceededException.class})
    @ResponseBody
    public ResponseEntity<String> handleMaxUploadSizeException(MaxUploadSizeExceededException re){
        re.printStackTrace();
        final String errorMessage = "O tamanho do arquivo excede " + maxFileSizeParameter;
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(errorMessage);
    }

    @ExceptionHandler({NotFoundException.class})
    @ResponseBody
    public ResponseEntity<String> handleNotFoundException(NotFoundException nfe){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(nfe.getMessage());
    }

    @ExceptionHandler({MissingRequestHeaderException.class})
    @ResponseBody
    public ResponseEntity<String> handleMissingRequestHeaderException(MissingRequestHeaderException mrhe){
        return ResponseEntity.badRequest().body(mrhe.getMessage());
    }


    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<String> handleNotFoundException(Exception e){
        e.printStackTrace();
        return ResponseEntity.internalServerError().body(e.getMessage());
    }
}
