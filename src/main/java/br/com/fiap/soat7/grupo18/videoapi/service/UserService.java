package br.com.fiap.soat7.grupo18.videoapi.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.fiap.soat7.grupo18.videoapi.exception.RequiredDataException;
import br.com.fiap.soat7.grupo18.videoapi.mongo.document.User;
import br.com.fiap.soat7.grupo18.videoapi.mongo.repository.UserMongoRepository;

@Service
public class UserService {

    @Autowired
    private UserMongoRepository userMongoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public User saveUser(User user) {
        var optUser = Optional.ofNullable(user);

        if (optUser.map(User::getUserName).isEmpty()) {
            throw new RequiredDataException("Nome do usuário é obrigatório");
        }

        if (optUser.map(User::getPassword).isEmpty()) {
            throw new RequiredDataException("Senha do usuário é obrigatória");
        }

        user.setUserName(user.getUserName().toUpperCase().trim());
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userMongoRepository.save(user);
    }

    public User findUserByUsername(String username) {
        return userMongoRepository.findByUserName(username).orElse(null);
    }


}
