package br.com.fiap.soat7.grupo18.videoapi.dto;

import java.io.Serializable;

import org.springframework.data.annotation.Id;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UserResponseDTO implements Serializable{

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Id
    @EqualsAndHashCode.Include
    private String userId;

    private String userName;

    private String email;

}
