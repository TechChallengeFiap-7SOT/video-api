package br.com.fiap.soat7.grupo18.videoapi.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "br.com.fiap.soat7.grupo18.videoapi.mongo.repository")
@EntityScan(basePackages = "br.com.fiap.soat7.grupo18.videoapi.mongo.document")
public class MongoDBConfig {
    //classe para configurar os repositorios mongoDB
}
