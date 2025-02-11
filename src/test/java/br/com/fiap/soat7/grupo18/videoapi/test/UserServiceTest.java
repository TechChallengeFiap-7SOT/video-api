package br.com.fiap.soat7.grupo18.videoapi.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.com.fiap.soat7.grupo18.videoapi.exception.RequiredDataException;
import br.com.fiap.soat7.grupo18.videoapi.mongo.document.User;
import br.com.fiap.soat7.grupo18.videoapi.mongo.repository.UserMongoRepository;
import br.com.fiap.soat7.grupo18.videoapi.service.UserService;

public class UserServiceTest {

     @Mock
    private UserMongoRepository userMongoRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSaveUserUserNameNull() {
        User user = new User();
        user.setPassword("password");

        Exception exception = assertThrows(RequiredDataException.class, () -> {
            userService.saveUser(user);
        });

        assertEquals("Nome do usuário é obrigatório", exception.getMessage());
    }

    @Test
    void testSaveUserPasswordNull() {
        User user = new User();
        user.setUserName("username");

        Exception exception = assertThrows(RequiredDataException.class, () -> {
            userService.saveUser(user);
        });

        assertEquals("Senha do usuário é obrigatória", exception.getMessage());
    }

    @Test
    void testSaveUserSuccess() {
        User user = new User();
        user.setUserName("username");
        user.setPassword("password");

        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userMongoRepository.save(any(User.class))).thenReturn(user);

        User savedUser = userService.saveUser(user);

        assertNotNull(savedUser);
        assertEquals("USERNAME", savedUser.getUserName());
        assertEquals("encodedPassword", savedUser.getPassword());
    }

    @Test
    void testFindUserByUsernameNotFound() {
        String username = "username";

        when(userMongoRepository.findByUserName(username)).thenReturn(Optional.empty());

        User user = userService.findUserByUsername(username);

        assertNull(user);
    }

    @Test
    void testFindUserByUsernameSuccess() {
        String username = "username";
        User user = new User();
        user.setUserName(username);

        when(userMongoRepository.findByUserName(username)).thenReturn(Optional.of(user));

        User foundUser = userService.findUserByUsername(username);

        assertNotNull(foundUser);
        assertEquals(username, foundUser.getUserName());
    }
}
