package br.com.fiap.soat7.grupo18.videoapi.service;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import br.com.fiap.soat7.grupo18.videoapi.mongo.document.User;
import br.com.fiap.soat7.grupo18.videoapi.mongo.document.Video;

@Service
public class MailNotificationService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${mailgun.url}")
    private String mailGunUrl;

    @Value("${mailgun.apikey}")
    private String mailGunApiKey;

    private Logger log = Logger.getLogger(MailNotificationService.class.getName());

    public void sendSuccessNotificationMail(Video video, User user){
        final String message = String.format("O arquivo %s foi processado com sucesso", video.getFileName());
        final String subject = "VIDEO-API - Arquivo processado com sucesso";
        sendNotificationMail(user, subject, message);
    }

    public void sendFailNotificationMail(Video video, User user){
        final String message = String.format("O arquivo %s falhou ao ser processado. Envie o arquivo novamente", video.getFileName());
        final String subject = "VIDEO-API - Falha ao processar arquivo";
        sendNotificationMail(user, subject, message);
    }

    private void sendNotificationMail(User user, String subject, String message) {
        try {
            String toUserEmail = String.format("%s <%s>", user.getUserName(), user.getEmail());

            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth("api", mailGunApiKey);
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("from", "Mailgun Sandbox <postmaster@sandboxd67754b1bb3a4b069b27f127562cde13.mailgun.org>");
            body.add("to", toUserEmail);
            body.add("subject", subject);
            body.add("text", message);

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

            restTemplate.exchange(mailGunUrl, HttpMethod.POST, requestEntity, String.class);
            log.info(String.format("Email sent: %s", mailGunUrl));

        } catch (Exception e) {
            log.log(Level.WARNING, String.format("Error when invoking '%s': %s", mailGunUrl, e.getMessage()));
        }
    }

}
