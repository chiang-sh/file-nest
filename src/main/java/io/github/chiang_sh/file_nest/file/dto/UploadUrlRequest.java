package io.github.chiang_sh.file_nest.file.dto;

import java.util.UUID;

public record UploadUrlRequest(UUID parentUuid, String filename) {}
