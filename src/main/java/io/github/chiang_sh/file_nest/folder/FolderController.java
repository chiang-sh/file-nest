package io.github.chiang_sh.file_nest.folder;

import io.github.chiang_sh.file_nest.common.FileSystemDto;
import io.github.chiang_sh.file_nest.folder.dto.CreateFolderRequest;
import io.github.chiang_sh.file_nest.folder.dto.FolderResponse;
import io.github.chiang_sh.file_nest.folder.dto.UpdateFolderRequest;
import io.github.chiang_sh.file_nest.security.SecurityUser;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/folders")
@Tag(name = "File manipulation")
public class FolderController {

    private static final String ROOT = "root";
    private final FolderService folderService;

    @Autowired
    public FolderController(FolderService folderService) {
        this.folderService = folderService;
    }

    @GetMapping("/{folderUuid}/children")
    public List<FileSystemDto> getChildren(
            @AuthenticationPrincipal SecurityUser securityUser, @PathVariable String folderUuid) {
        if (folderUuid == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        if (folderUuid.equals(ROOT)) {
            return folderService.getChildren(securityUser.getUsername());
        }
        return folderService.getChildren(securityUser.getUsername(), UUID.fromString(folderUuid));
    }

    @PostMapping
    public FolderResponse createFolder(
            @AuthenticationPrincipal SecurityUser securityUser,
            @RequestBody CreateFolderRequest body) {
        if (body.parentUuid() == null || body.name() == null || body.name().isEmpty()) {
            throw new IllegalArgumentException("The argument must not be null.");
        }
        if (body.parentUuid().equals(ROOT)) {
            return folderService.create(securityUser.getUsername(), body.name());
        }
        return folderService.create(
                securityUser.getUsername(), body.name(), UUID.fromString(body.parentUuid()));

    }
}
