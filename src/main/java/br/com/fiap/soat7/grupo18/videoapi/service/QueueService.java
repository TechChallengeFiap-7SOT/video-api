package br.com.fiap.soat7.grupo18.videoapi.service;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class QueueService {

    //read querue api url
    @Value("${queue.api.url}")
    private String queueApiUrl;

    @Autowired
    private RestTemplate restTemplate;

    private Logger log = Logger.getLogger(QueueService.class.getName());

    @Async
    public void sendToQueueAsync(String videoId) {
        try{
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");

            String bodyJson = generateBodyJson(videoId);

            HttpEntity<String> entity = new HttpEntity<>(bodyJson, headers);
            restTemplate.exchange(queueApiUrl, HttpMethod.POST, entity, String.class);
            log.info(String.format("POST request to %s -> %s", queueApiUrl, bodyJson));
        }catch(Exception e){
            log.log(Level.WARNING, String.format("Error when invoking '%s': %s", queueApiUrl, e.getMessage()));
        }
    }

    private String generateBodyJson(String videoID){
        return String.format("{\"message\": {\"id_video\": \"%s\"}}", videoID);
    }

}
