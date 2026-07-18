package io.github.chiang_sh.file_nest.file.dto;

import java.util.UUID;

public record UpdateFileRequest(UUID folderUuid, String filename) {}
