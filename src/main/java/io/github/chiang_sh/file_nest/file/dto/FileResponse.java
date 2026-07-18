package io.github.chiang_sh.file_nest.file.dto;

import io.github.chiang_sh.file_nest.common.FileSystemDto;
import io.github.chiang_sh.file_nest.file.FileEntity;
import io.github.chiang_sh.file_nest.file_permission.FilePermissionEntity;
import io.github.chiang_sh.file_nest.file_permission.FilePermissionType;

import java.time.OffsetDateTime;
import java.util.UUID;

public record FileResponse(
        UUID uuid,
        String name,
        String contentType,
        Long size,
        OffsetDateTime createdAt,
        FilePermissionType permission)
        implements FileSystemDto {
    public static FileResponse from(FileEntity file, FilePermissionEntity permission) {
        return new FileResponse(
                file.getUuid(),
                file.getName(),
                file.getContentType(),
                file.getSize(),
                file.getCreatedAt(),
                permission.getPermission());
    }
}
