package br.com.fiap.soat7.grupo18.videoapi.handler;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import br.com.fiap.soat7.grupo18.videoapi.dto.VideoResponseDTO;
import br.com.fiap.soat7.grupo18.videoapi.service.VideoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/video")
@Tag(name = "Video API", description = "API de Vídeos")
public class VideoRestHandler {

    @Autowired
    private VideoService videoService;

    @PostMapping(consumes = "multipart/form-data", produces = "application/json")
    @Operation(description = "Realiza o upload de um vídeo")
    @ApiResponse(responseCode = "200", description = "Sucesso", content = @Content(mediaType = "application/json" , schema = @Schema(implementation = VideoResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    @ApiResponse(responseCode = "413", description = "Tamanho do arquivo excede o limite", content = @Content(mediaType = "text/plain"))
    public ResponseEntity<VideoResponseDTO> handleVideo(@RequestParam("video") MultipartFile videoFile,
                                                @RequestParam("notificationEmail") String notificationEmail,
                                                @RequestHeader(value = "USER", required = true) String user) throws Exception {
        var response = videoService.save(videoFile, notificationEmail, user);
        return ResponseEntity.ok(response);
    }

    @GetMapping(produces = "application/json", path = "/s3-url/{videoID}")
    @Operation(description = "Consulta a URL pública do S3 por ID do vídeo")
    @ApiResponse(responseCode = "200", description = "Sucesso", content = @Content(mediaType = "application/json" , schema = @Schema(implementation = VideoResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    @ApiResponse(responseCode = "404", description = "Vídeo não encontrado")
    public ResponseEntity<VideoResponseDTO> getVideoS3Url(@PathVariable("videoID") String videoId) {
        var response = videoService.getVideoS3Url(videoId);
        return ResponseEntity.ok(response);
    }

    @GetMapping(produces = "application/json")
    @Operation(description = "Lista todos os vídeos de um usuário")
    @ApiResponse(responseCode = "200", description = "Sucesso", content = @Content(mediaType = "application/json" , schema = @Schema(implementation = List.class)))
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    public ResponseEntity<List<VideoResponseDTO>> getVideosByUser(@RequestHeader(value = "USER", required = true) String user) {
        var response = videoService.findAllByUser(user);
        return ResponseEntity.ok(response);
    }
}
