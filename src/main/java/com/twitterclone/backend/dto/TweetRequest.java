package com.twitterclone.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TweetRequest {

    @NotBlank(message = "El contenido del tweet no puede estar vacío")
    @Size(max = 280, message = "El tweet no puede superar los 280 caracteres")
    private String content;
}
