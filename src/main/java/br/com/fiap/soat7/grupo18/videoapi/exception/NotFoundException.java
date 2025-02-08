package br.com.fiap.soat7.grupo18.videoapi.exception;

public class NotFoundException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public NotFoundException(String message){
        super(message);
    }

}
