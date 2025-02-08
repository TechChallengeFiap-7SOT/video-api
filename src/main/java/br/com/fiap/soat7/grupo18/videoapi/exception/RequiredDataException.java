package br.com.fiap.soat7.grupo18.videoapi.exception;

public class RequiredDataException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public RequiredDataException(String message){
        super(message);
    }

}
