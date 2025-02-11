package br.com.fiap.soat7.grupo18.videoapi.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRequestDTO implements Serializable {
    
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private String userName;

    private String password;

    private String email;

}
