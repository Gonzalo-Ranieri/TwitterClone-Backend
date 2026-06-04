package com.twitterclone.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    @NotBlank(message = "El nombre de usuario es requerido")
    @Size(min = 3, max = 30, message = "El nombre de usuario debe tener entre 3 y 30 caracteres")
    private String username;

    @NotBlank(message = "El email es requerido")
    @Email(message = "El formato del email no es válido")
    private String email;

    @Size(max = 280, message = "La biografía no puede superar los 280 caracteres")
    private String bio;

    @NotBlank(message = "La foto de perfil es requerida")
    private String avatarPlaceholder;

    @NotBlank(message = "La foto de portada es requerida")
    private String bannerPlaceholder;
    private boolean showEmail;
}
