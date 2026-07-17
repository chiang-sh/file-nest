package io.github.chiang_sh.file_nest.folder;

import io.github.chiang_sh.file_nest.common.FileSystemDto;
import io.github.chiang_sh.file_nest.folder.dto.CreateFolderRequest;
import io.github.chiang_sh.file_nest.folder.dto.DeleteFolderRequest;
import io.github.chiang_sh.file_nest.folder.dto.FolderResponse;
import io.github.chiang_sh.file_nest.folder.dto.UpdateFolderRequest;
import io.github.chiang_sh.file_nest.security.SecurityUser;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<FolderResponse> createFolder(
            @AuthenticationPrincipal SecurityUser securityUser,
            @RequestBody CreateFolderRequest body) {
        if (body.name() == null || body.name().isEmpty()) {
            throw new IllegalArgumentException("The argument must not be null.");
        }
        FolderResponse response =
                folderService.create(securityUser.getUsername(), body.name(), body.parentUuid());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping
    public FolderResponse updateFolder(
            @AuthenticationPrincipal SecurityUser securityUser,
            @RequestBody UpdateFolderRequest body) {
        if (body.uuid() == null || body.name() == null || body.name().isEmpty()) {
            throw new IllegalArgumentException("The argument must not be null.");
        }
        return folderService.update(securityUser.getUsername(), body.uuid(), body.name());
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteFolder(
            @AuthenticationPrincipal SecurityUser securityUser,
            @RequestBody DeleteFolderRequest body) {
        if (body.uuid() == null) {
            throw new IllegalArgumentException("The argument must not be null.");
        }
        folderService.delete(securityUser.getUsername(), body.uuid());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
