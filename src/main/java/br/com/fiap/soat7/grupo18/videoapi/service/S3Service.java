package br.com.fiap.soat7.grupo18.videoapi.service;

import java.io.IOException;
import java.time.Duration;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import br.com.fiap.soat7.grupo18.videoapi.exception.ExternalServiceException;
import jakarta.annotation.PostConstruct;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Service
public class S3Service {

    private static String VIDEO_S3_FOLDER = "VIDEO";

    private static String ZIP_S3_FOLDER = "ZIP";

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.default-region}")
    private String defaultRegion;

    @Value("${aws.s3.presigned-url-expiration-in-seconds:120}")
    private int expirationPresignedUrl;

    private S3Client s3Client;

    @PostConstruct
    public void init(){
        s3Client = S3Client.builder()
                .region(Region.of(defaultRegion))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    /**
     * 
     * @param file
     * @param fileName
     * @param userName
     * @param videoId
     * @return
     */
    public String uploadVideo(MultipartFile file, String fileName, String userName, String videoId) {
        final String[] filenameParts = Optional.ofNullable(fileName).orElse("*.*").split("\\.");
        final String s3Key = String.format("%s/%s/%s.%s",
                                            userName,
                                            VIDEO_S3_FOLDER,
                                            videoId,
                                            filenameParts[1]);
        return upload(file, s3Key);
    }

    /**
     * 
     * @param file
     * @param fileName
     * @param userName
     * @param videoId
     * @return
     */
    public String uploadZip(MultipartFile file, String fileName, String userName, String videoId) {
        final String[] filenameParts = Optional.ofNullable(fileName).orElse("*.*").split("\\.");
        final String s3Key = String.format("%s/%s/%s.%s",
                                            userName,
                                            ZIP_S3_FOLDER,
                                            videoId,
                                            filenameParts[1]);
        return upload(file, s3Key);
    }

    
    private String upload(MultipartFile file, String s3Key) {
        
        try{
            s3Client.putObject(
                                PutObjectRequest.builder()
                                        .bucket(bucketName)
                                        .key(s3Key)
                                        .contentType(file.getContentType())
                                        .build(),    
                                RequestBody.fromInputStream(file.getInputStream(), file.getSize())
                        );
            return s3Key;
        }catch(IOException e){
            throw new ExternalServiceException("Problemas ao enviar arquivo para o S3: " + e.getMessage());
        }
    }

    public String genetarePresignedUrl(String s3Key){
        try(S3Presigner presigner = S3Presigner.create()){
            GetObjectRequest objectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofSeconds(expirationPresignedUrl))
                    .getObjectRequest(objectRequest)
                    .build();

            return presigner.presignGetObject(presignRequest).url().toString();
        }
    }

    public ResponseBytes<GetObjectResponse> downloadZip(String s3Filename) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Filename)
                .build();

        return s3Client.getObjectAsBytes(request);
    }
}
