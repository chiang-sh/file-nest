package io.github.chiang_sh.file_nest.file;

import io.github.chiang_sh.file_nest.common.FileSystemDto;
import io.github.chiang_sh.file_nest.file_permission.FilePermissionType;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

public record FileDto (
        UUID uuid,
        String name,
        String contentType,
        Long size,
        OffsetDateTime createdAt,
        FilePermissionType permission) implements FileSystemDto {}
