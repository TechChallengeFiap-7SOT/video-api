package br.com.fiap.soat7.grupo18.videoapi.mongo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import br.com.fiap.soat7.grupo18.videoapi.mongo.document.Video;

public interface VideoMongoRepository extends MongoRepository<Video, String> {

    @Query("{ 'videoId': :#{#videoId}, 'user': :#{#user} }")
    Optional<Video> findByVideoIdAndUser(String videoId, String user);

    List<Video> findByUser(String user);

}
