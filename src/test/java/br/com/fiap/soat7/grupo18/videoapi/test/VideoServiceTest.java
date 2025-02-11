package br.com.fiap.soat7.grupo18.videoapi.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;

import br.com.fiap.soat7.grupo18.videoapi.dto.VideoResponseDTO;
import br.com.fiap.soat7.grupo18.videoapi.dto.ZipResponseDTO;
import br.com.fiap.soat7.grupo18.videoapi.exception.NotFoundException;
import br.com.fiap.soat7.grupo18.videoapi.exception.RequiredDataException;
import br.com.fiap.soat7.grupo18.videoapi.mongo.document.Video;
import br.com.fiap.soat7.grupo18.videoapi.mongo.document.Zip;
import br.com.fiap.soat7.grupo18.videoapi.mongo.repository.VideoMongoRepository;
import br.com.fiap.soat7.grupo18.videoapi.service.QueueService;
import br.com.fiap.soat7.grupo18.videoapi.service.S3Service;
import br.com.fiap.soat7.grupo18.videoapi.service.VideoService;
import br.com.fiap.soat7.grupo18.videoapi.type.VideoStatusEnum;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

public class VideoServiceTest {

    @Mock
    private VideoMongoRepository videoRepository;

    @Mock
    private QueueService queueService;

    @Mock
    private S3Service s3Service;

    @InjectMocks
    private VideoService videoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSaveVideoFileNull() {
        MultipartFile videoFile = null;
        String notificationEmail = "test@example.com";
        String userName = "testUser";

        Exception exception = assertThrows(RequiredDataException.class, () -> {
            videoService.save(videoFile, notificationEmail, userName);
        });

        assertEquals("Vídeo não enviado", exception.getMessage());
    }

    @Test
    void testSaveUserNameBlank() {
        MultipartFile videoFile = mock(MultipartFile.class);
        String notificationEmail = "test@example.com";
        String userName = "";

        Exception exception = assertThrows(RequiredDataException.class, () -> {
            videoService.save(videoFile, notificationEmail, userName);
        });

        assertEquals("Usuário não informado", exception.getMessage());
    }

    @Test
    void testSaveNotificationEmailBlank() {
        MultipartFile videoFile = mock(MultipartFile.class);
        String notificationEmail = "";
        String userName = "testUser";

        Exception exception = assertThrows(RequiredDataException.class, () -> {
            videoService.save(videoFile, notificationEmail, userName);
        });

        assertEquals("E-mail de notificação não informado", exception.getMessage());
    }

    @Test
    void testSaveFileNameInvalid() {
        MultipartFile videoFile = mock(MultipartFile.class);
        when(videoFile.getOriginalFilename()).thenReturn("invalidfile.txt");
        String notificationEmail = "test@example.com";
        String userName = "testUser";

        Exception exception = assertThrows(RequiredDataException.class, () -> {
            videoService.save(videoFile, notificationEmail, userName);
        });

        assertEquals("Nome do arquivo não informado ou inválido. Envie um arquivo MP4", exception.getMessage());
    }

    @Test
    void testSaveSuccess() throws Exception {
        MultipartFile videoFile = mock(MultipartFile.class);
        when(videoFile.getOriginalFilename()).thenReturn("video.mp4");
        when(videoFile.getSize()).thenReturn(1024L);
        String notificationEmail = "test@example.com";
        String userName = "testUser";

        Video video = Video.builder()
                .notificationEmail(notificationEmail)
                .user(userName.toUpperCase())
                .sizeInBytes(1024L)
                .fileName("video.mp4")
                .status(VideoStatusEnum.EM_PROCESSAMENTO)
                .uploadDate(LocalDateTime.now())
                .build();

        when(videoRepository.save(any(Video.class))).thenReturn(video);
        when(s3Service.uploadVideo(any(MultipartFile.class), anyString(), anyString(), anyString())).thenReturn("s3filename");

        VideoResponseDTO response = videoService.save(videoFile, notificationEmail, userName);

        assertNotNull(response);
        assertEquals("video.mp4", response.getFileName());
        assertEquals(VideoStatusEnum.EM_PROCESSAMENTO, response.getStatus());
    }

    @Test
    void testGetVideoNotFound() {
        String videoId = "invalidId";

        when(videoRepository.findById(videoId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(NotFoundException.class, () -> {
            videoService.getVideo(videoId);
        });

        assertEquals("Vídeo não encontrado", exception.getMessage());
    }

    @Test
    void testGetVideoSuccess() {
        String videoId = "validId";
        Video video = new Video();
        video.setVideoId(videoId);

        when(videoRepository.findById(videoId)).thenReturn(Optional.of(video));

        Video result = videoService.getVideo(videoId);

        assertNotNull(result);
        assertEquals(videoId, result.getVideoId());
    }

    @Test
    void testGetVideoS3UrlNotFound() {
        String videoId = "invalidId";

        when(videoRepository.findById(videoId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(NotFoundException.class, () -> {
            videoService.getVideoS3Url(videoId);
        });

        assertEquals("Vídeo não encontrado", exception.getMessage());
    }

    @Test
    void testGetVideoS3UrlSuccess() {
        String videoId = "validId";
        Video video = new Video();
        video.setVideoId(videoId);
        video.setS3Filename("s3filename");

        when(videoRepository.findById(videoId)).thenReturn(Optional.of(video));
        when(s3Service.genetarePresignedUrl("s3filename")).thenReturn("s3url");

        VideoResponseDTO response = videoService.getVideoS3Url(videoId);

        assertNotNull(response);
        assertEquals(videoId, response.getId());
        assertEquals("s3url", response.getS3Url());
    }

    @Test
    void testAssociateZipVideoNotFound() {
        MultipartFile zipFile = mock(MultipartFile.class);
        String videoId = "invalidId";
        String user = "testUser";

        when(videoRepository.findByVideoIdAndUser(videoId, user.toUpperCase())).thenReturn(Optional.empty());

        when(zipFile.getOriginalFilename()).thenReturn("mockedFile.zip");

        Exception exception = assertThrows(NotFoundException.class, () -> {
            videoService.associateZip(zipFile, videoId, user);
        });

        assertEquals("Vídeo não encontrado", exception.getMessage());
    }

    @Test
    void testAssociateZipSuccess() {
        MultipartFile zipFile = mock(MultipartFile.class);
        when(zipFile.getOriginalFilename()).thenReturn("file.zip");
        when(zipFile.getSize()).thenReturn(1024L);
        String videoId = "validId";
        String user = "testUser";

        Video video = new Video();
        video.setVideoId(videoId);
        video.setUser(user.toUpperCase());

        when(videoRepository.findByVideoIdAndUser(videoId, user.toUpperCase())).thenReturn(Optional.of(video));
        when(s3Service.uploadZip(any(MultipartFile.class), anyString(), anyString(), anyString())).thenReturn("s3filename");

        videoService.associateZip(zipFile, videoId, user);

        verify(videoRepository, times(1)).save(any(Video.class));
    }

    @Test
    void testFindAllByUserSuccess() {
        String user = "testUser";
        Video video = new Video();
        video.setUser(user.toUpperCase());

        when(videoRepository.findByUser(user.toUpperCase())).thenReturn(List.of(video));

        List<VideoResponseDTO> response = videoService.findAllByUser(user);

        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    void testDownloadZipVideoNotFound() {
        String videoId = "invalidId";
        String user = "testUser";

        when(videoRepository.findByVideoIdAndUser(videoId, user.toUpperCase())).thenReturn(Optional.empty());

        Exception exception = assertThrows(NotFoundException.class, () -> {
            videoService.downloadZip(videoId, user);
        });

        assertEquals("Vídeo não encontrado", exception.getMessage());
    }

    @Test
    void testDownloadZipSuccess() {
        String videoId = "validId";
        String user = "testUser";
        Video video = new Video();
        video.setVideoId(videoId);
        video.setUser(user.toUpperCase());
        video.setStatus(VideoStatusEnum.PROCESSADO);
        video.setFileName("video.mp4");
        Zip zip = new Zip();
        zip.setS3Filename("s3filename");
        video.setZipFile(zip);

        when(videoRepository.findByVideoIdAndUser(videoId, user.toUpperCase())).thenReturn(Optional.of(video));
        ResponseBytes<GetObjectResponse> responseBytes = mock(ResponseBytes.class);
        when(responseBytes.asByteArray()).thenReturn(new byte[0]);
        when(s3Service.downloadZip("s3filename")).thenReturn(responseBytes);

        ZipResponseDTO response = videoService.downloadZip(videoId, user);

        assertNotNull(response);
        assertEquals("video.zip", response.getFileName());
    }
}