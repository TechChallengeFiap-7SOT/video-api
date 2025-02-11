package br.com.fiap.soat7.grupo18.videoapi.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiap.soat7.grupo18.videoapi.dto.UserRequestDTO;
import br.com.fiap.soat7.grupo18.videoapi.dto.UserResponseDTO;
import br.com.fiap.soat7.grupo18.videoapi.mongo.document.User;
import br.com.fiap.soat7.grupo18.videoapi.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("public/user")
@Tag(name = "Usuários API", description = "API de Usuários")
public class UserRestHandler {


    @Autowired
    private UserService userService;

    @PostMapping(consumes = "application/json", produces = "application/json")
    @Operation(description = "Realiza o cadastro de um usuário")
    @ApiResponse(responseCode = "200", description = "Sucesso", content = @Content(mediaType = "application/json" , schema = @Schema(implementation = UserResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    public ResponseEntity<UserResponseDTO> handleUser(@RequestBody UserRequestDTO user) throws Exception {
        
        User savedUser = userService.saveUser(User.builder()
                            .userName(user.getUserName())
                            .password(user.getPassword())
                            .email(user.getEmail())
                            .build());

        var response = UserResponseDTO.builder()
                            .userId(savedUser.getUserId())
                            .userName(savedUser.getUserName())
                            .email(savedUser.getEmail())
                            .build();
        
        return ResponseEntity.ok(response);
        
    }
}
