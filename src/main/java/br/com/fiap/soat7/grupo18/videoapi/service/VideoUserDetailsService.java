package br.com.fiap.soat7.grupo18.videoapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import br.com.fiap.soat7.grupo18.videoapi.mongo.document.User;
import br.com.fiap.soat7.grupo18.videoapi.mongo.repository.UserMongoRepository;

@Service
public class VideoUserDetailsService implements UserDetailsService {

    @Autowired
    private UserMongoRepository userMongoRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var upperUserName = username.toUpperCase();
        User user = userMongoRepository.findByUserName(upperUserName)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return org.springframework.security.core.userdetails.User.builder()
                                        .username(user.getUserName())
                                        .password(user.getPassword()).build();
    }

}
