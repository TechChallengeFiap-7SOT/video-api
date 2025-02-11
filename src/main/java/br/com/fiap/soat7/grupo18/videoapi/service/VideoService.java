package br.com.fiap.soat7.grupo18.videoapi.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import br.com.fiap.soat7.grupo18.videoapi.dto.VideoResponseDTO;
import br.com.fiap.soat7.grupo18.videoapi.dto.ZipResponseDTO;
import br.com.fiap.soat7.grupo18.videoapi.exception.NotFoundException;
import br.com.fiap.soat7.grupo18.videoapi.exception.RequiredDataException;
import br.com.fiap.soat7.grupo18.videoapi.mongo.document.Video;
import br.com.fiap.soat7.grupo18.videoapi.mongo.document.Zip;
import br.com.fiap.soat7.grupo18.videoapi.mongo.repository.VideoMongoRepository;
import br.com.fiap.soat7.grupo18.videoapi.type.VideoStatusEnum;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@Service
public class VideoService {

    @Autowired
    private VideoMongoRepository videoRepository;

    @Autowired
    private QueueService queueService;

    @Autowired
    private S3Service s3Service;

    /**
     * 
     * @param videoFile
     * @param notificationEmail
     * @param userName
     * @return
     * @throws Exception
     */
    @Transactional
    public VideoResponseDTO save(MultipartFile videoFile, String notificationEmail, String userName) throws Exception {
        
        if (videoFile == null){
            throw new RequiredDataException("Vídeo não enviado");
        }
        
        if (StringUtils.isBlank(userName)){
            throw new RequiredDataException("Usuário não informado");
        }

        if (StringUtils.isBlank(notificationEmail)){
            throw new RequiredDataException("E-mail de notificação não informado");
        }

        String userNameUpper = new String(userName).toUpperCase();
        String fileName = videoFile.getOriginalFilename();


        if (StringUtils.isBlank(fileName) || !Optional.ofNullable(fileName).map(String::toLowerCase).orElse("").endsWith(".mp4")){
            throw new RequiredDataException("Nome do arquivo não informado ou inválido. Envie um arquivo MP4");
        }

        final long fileSizeInBytes = videoFile.getSize();

        var video = Video.builder()
                .notificationEmail(notificationEmail)
                .user(userNameUpper)
                .sizeInBytes(fileSizeInBytes)
                .fileName(fileName)
                .status(VideoStatusEnum.EM_PROCESSAMENTO)
                .uploadDate(LocalDateTime.now())
                .build();
        video = videoRepository.save(video);

        String s3Filename = s3Service.uploadVideo(videoFile, videoFile.getOriginalFilename(), userName, video.getVideoId());
        video.setS3Filename(s3Filename);
        video = videoRepository.save(video); //atualiza o registro com o nome do arquivo no S3

        //chamar fila
        queueService.sendToQueueAsync(video.getVideoId());

        return VideoResponseDTO.builder()
                    .id(video.getVideoId())
                    .fileName(video.getFileName())
                    .status(video.getStatus())
                    .build();
    }

    //find video by id
    public Video getVideo(String videoId) {
        if (StringUtils.isBlank(videoId)){
            throw new RequiredDataException("ID do vídeo não informado");
        }

        var video = videoRepository.findById(videoId).orElseThrow(() -> new NotFoundException("Vídeo não encontrado"));
        return video;
    }


    public VideoResponseDTO getVideoS3Url(String videoId) {
        if (StringUtils.isBlank(videoId)){
            throw new RequiredDataException("ID do vídeo não informado");
        }

        var video = videoRepository.findById(videoId).orElseThrow(() -> new NotFoundException("Vídeo não encontrado"));
        return VideoResponseDTO.builder()
                .id(video.getVideoId())
                .s3Url(s3Service.genetarePresignedUrl(video.getS3Filename()))
                .build();
    }

    /**
     * 
     * @param zipFile
     * @param videoId
     * @param user
     */
    public void associateZip(MultipartFile zipFile, String videoId, String user) {
        if (zipFile == null){
            throw new RequiredDataException("ZIP não enviado");
        }

        String userName = new String(user).toUpperCase();
        String fileName = zipFile.getOriginalFilename();
        final long fileSizeInBytes = zipFile.getSize();

        if (StringUtils.isBlank(userName)){
            throw new RequiredDataException("Usuário não informado");
        }

        if (StringUtils.isBlank(fileName) || !Optional.ofNullable(fileName).map(String::toLowerCase).orElse("").endsWith(".zip")){
            throw new RequiredDataException("Nome do arquivo não informado ou inválido. Envie um arquivo ZIP");
        }
        
        var video = videoRepository.findByVideoIdAndUser(videoId, userName).orElseThrow(() -> new NotFoundException("Vídeo não encontrado"));

        String s3Filename = s3Service.uploadZip(zipFile, video.getVideoId() + ".zip", userName, videoId);
        video.setZipFile(Zip.builder()
                            .s3Filename(s3Filename)
                            .generationDate(LocalDateTime.now())
                            .sizeInBytes(fileSizeInBytes)
                            .build()
                        );
        video.setStatus(VideoStatusEnum.PROCESSADO);

        video = videoRepository.save(video); //atualiza o registro com o nome do arquivo ZIP no S3
    }

    public List<VideoResponseDTO> findAllByUser(String user) {
        if (StringUtils.isBlank(user)){
            throw new RequiredDataException("Usuário não informado");
        }

        String userName = new String(user).toUpperCase();

        List<Video> videos = videoRepository.findByUser(userName);
        return videos.stream()
                    .sorted(Comparator.comparing(Video::getUploadDate))
                    .map(v -> VideoResponseDTO.builder()
                                    .id(v.getVideoId())
                                    .fileName(v.getFileName())
                                    .status(v.getStatus())
                                    .uploadDate(v.getUploadDate())
                                    .build()
                    )
                    .toList();
    }

    public ZipResponseDTO downloadZip(String videoId, String user) {
        if (StringUtils.isBlank(videoId)){
            throw new RequiredDataException("ID do vídeo não informado");
        }

        if (StringUtils.isBlank(user)){
            throw new RequiredDataException("Usuário não informado");
        }

        String userName = new String(user).toUpperCase();
        var video = videoRepository.findByVideoIdAndUser(videoId, userName).orElseThrow(() -> new NotFoundException("Vídeo não encontrado"));

        if (VideoStatusEnum.PROCESSADO != video.getStatus()){
            throw new RequiredDataException("Vídeo ainda não processado");
        }

        String[] filenameParts = video.getFileName().split("\\.");
        ResponseBytes<GetObjectResponse> response = s3Service.downloadZip(video.getZipFile().getS3Filename());
        
        return ZipResponseDTO.builder()
                .fileName(String.format("%s.zip", filenameParts[0]))
                .fileByteArray(response.asByteArray())
                .build();
    }

}
