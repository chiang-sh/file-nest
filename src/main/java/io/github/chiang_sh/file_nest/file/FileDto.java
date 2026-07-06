package io.github.chiang_sh.file_nest.file;

import io.github.chiang_sh.file_nest.file_permission.FilePermissionType;

import java.time.LocalDateTime;
import java.util.UUID;

public record FileDto(
        UUID uuid,
        String name,
        FileType type,
        Long size,
        LocalDateTime createdAt,
        FilePermissionType permission) {}
