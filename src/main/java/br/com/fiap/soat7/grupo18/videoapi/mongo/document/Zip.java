package br.com.fiap.soat7.grupo18.videoapi.mongo.document;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Document(collection = "zip")
public class Zip implements Serializable {
    
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Id
    @EqualsAndHashCode.Include
    private String zipId;

    @Field("size_in_bytes")
    private Long sizeInBytes;

    @Field("s3_filename")
    private String s3Filename;

    @Field("generation_date")
    private LocalDateTime generationDate;



}
