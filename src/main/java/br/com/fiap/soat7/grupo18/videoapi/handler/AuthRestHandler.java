package br.com.fiap.soat7.grupo18.videoapi.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import br.com.fiap.soat7.grupo18.videoapi.exception.UserNotFoundException;
import br.com.fiap.soat7.grupo18.videoapi.mongo.document.User;
import br.com.fiap.soat7.grupo18.videoapi.service.UserService;

public abstract class AuthRestHandler {

    @Autowired
    protected UserService userService;

    protected User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof UserDetails userDetails) {
            String userName = userDetails.getUsername();
            return userService.findUserByUsername(userName);
        }

        throw new UserNotFoundException("Usuário não encontrado");
    }

}
