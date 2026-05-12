package com.project.music_listenning.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public class PlaylistRequest {
    public record CreatePlaylistRequest(
            @NotBlank(message = "Tên playlist không được để trống")
            @Size(max = 100)
            String title,

            String description,
            boolean isPublic
    ) {}

    public record UpdatePlaylistRequest(
            @NotBlank @Size(max = 100)
            String title,
            String description,
            boolean isPublic
    ) {}

    public record AddSongRequest(
            UUID songId
    ) {}
}
