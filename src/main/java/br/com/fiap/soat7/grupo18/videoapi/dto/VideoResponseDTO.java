package br.com.fiap.soat7.grupo18.videoapi.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

import br.com.fiap.soat7.grupo18.videoapi.type.VideoStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class VideoResponseDTO implements Serializable {
    
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @EqualsAndHashCode.Include
    private String id;

    private String fileName;

    private String s3Url;

    private LocalDateTime uploadDate;

    private VideoStatusEnum status;

    private ZipResponseDTO zipFile;

}
