package io.github.chiang_sh.file_nest.folder.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface FileSystemDto {
    UUID uuid();
    String name();
    OffsetDateTime createdAt();
}
