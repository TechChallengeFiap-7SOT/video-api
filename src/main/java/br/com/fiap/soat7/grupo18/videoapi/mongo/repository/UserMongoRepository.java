package br.com.fiap.soat7.grupo18.videoapi.mongo.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import br.com.fiap.soat7.grupo18.videoapi.mongo.document.User;

public interface UserMongoRepository extends MongoRepository<User, String> {

    //custom query to search by uppercase userName
    @Query("{ 'userName': { $regex: ?0, $options: 'i' } }")
    Optional<User> findByUserName(String userName);

}
