package io.github.chiang_sh.file_nest.folder.dto;

import io.github.chiang_sh.file_nest.common.FileSystemDto;
import io.github.chiang_sh.file_nest.folder.FolderEntity;

import java.time.OffsetDateTime;
import java.util.UUID;

public record FolderResponse(UUID uuid, String name, OffsetDateTime createdAt) implements FileSystemDto {
    public static FolderResponse from(FolderEntity entity) {
        return new FolderResponse(entity.getUuid(), entity.getName(), entity.getCreatedAt());
    }
}
