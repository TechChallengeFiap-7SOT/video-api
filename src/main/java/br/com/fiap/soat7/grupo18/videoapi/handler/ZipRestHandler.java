package br.com.fiap.soat7.grupo18.videoapi.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import br.com.fiap.soat7.grupo18.videoapi.dto.VideoResponseDTO;
import br.com.fiap.soat7.grupo18.videoapi.dto.ZipResponseDTO;
import br.com.fiap.soat7.grupo18.videoapi.mongo.document.User;
import br.com.fiap.soat7.grupo18.videoapi.service.MailNotificationService;
import br.com.fiap.soat7.grupo18.videoapi.service.VideoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/zip")
@Tag(name = "ZIP API", description = "API para envio do ZIP")
public class ZipRestHandler extends AuthRestHandler {

    @Autowired
    private VideoService videoService;

    @Autowired
    private MailNotificationService mailNotificationService;

    @PostMapping(consumes = "multipart/form-data", produces = "application/json", path = "/{videoID}")
    @Operation(description = "Realiza o upload do zip das imagens do vídeo")
    @ApiResponse(responseCode = "202", description = "Sucesso", content = @Content(mediaType = "application/json" , schema = @Schema(implementation = VideoResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    @ApiResponse(responseCode = "404", description = "Vídeo não encontrado")
    @ApiResponse(responseCode = "413", description = "Tamanho do arquivo excede o limite", content = @Content(mediaType = "text/plain"))
    public ResponseEntity<String> handleZip(@RequestParam("zip") MultipartFile zipFile,
                                                        @PathVariable("videoID") String videoId) throws Exception {
        
        User user = getAuthenticatedUser();
        videoService.associateZip(zipFile, videoId, user.getUserName());
        var video = videoService.getVideo(videoId);
        try{
            mailNotificationService.sendSuccessNotificationMail(video, user);
        }
        catch(Exception e){
            System.out.println("Erro ao enviar e-mail de notificação");
        }
        return ResponseEntity.accepted().build();
    }

    @GetMapping(path = "/download/{videoID}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @Operation(description = "Realiza download do ZIP dos vídeos processados")
    @ApiResponse(responseCode = "200", description = "Sucesso", content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE , schema = @Schema(implementation = byte[].class)))
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    @ApiResponse(responseCode = "404", description = "Vídeo não encontrado")
    public ResponseEntity<byte[]> downloadZip(@PathVariable("videoID") String videoId) throws Exception {
        User user = getAuthenticatedUser();
        ZipResponseDTO zipResponse = videoService.downloadZip(videoId, user.getUserName());
        return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(zipResponse.getFileByteArray().length)
                    .header("Content-Disposition", "attachment; filename=" + zipResponse.getFileName())
                    .body(zipResponse.getFileByteArray());
    }

}
