package io.github.chiang_sh.file_nest.folder.dto;

import java.util.UUID;

public record CreateFolderRequest(UUID parentUuid, String name) {}
