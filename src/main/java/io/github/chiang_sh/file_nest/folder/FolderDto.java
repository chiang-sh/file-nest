package io.github.chiang_sh.file_nest.folder;

import io.github.chiang_sh.file_nest.common.FileSystemDto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record FolderDto(
        UUID uuid,
        String name,
        OffsetDateTime createdAt) implements FileSystemDto {}
