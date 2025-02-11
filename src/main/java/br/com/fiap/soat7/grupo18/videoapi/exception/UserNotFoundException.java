package br.com.fiap.soat7.grupo18.videoapi.exception;

public class UserNotFoundException extends RuntimeException{


    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public UserNotFoundException(String message){
        super(message);
    }

}
