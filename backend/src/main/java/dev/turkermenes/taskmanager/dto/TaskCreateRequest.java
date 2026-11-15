package dev.turkermenes.taskmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TaskCreateRequest(

        @NotBlank(message = "Başlık boş olamaz")
        @Size(max = 100, message = "Başlık en fazla 100 karakter olabilir")
        String title,

        @Size(max = 500, message = "Açıklama en fazla 500 karakter olabilir")
        String description

) {}
