package io.github.chiang_sh.file_nest.folder;

import io.github.chiang_sh.file_nest.common.FileSystemDto;
import io.github.chiang_sh.file_nest.security.SecurityUser;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/folders")
@Tag(name = "File manipulation")
public class FolderController {

    private final FolderService folderService;

    @Autowired
    public FolderController(FolderService folderService) {
        this.folderService = folderService;
    }

    @GetMapping("/root/children")
    public List<FileSystemDto> getRootChildren(@AuthenticationPrincipal SecurityUser securityUser) {
        return folderService.getChildren(securityUser.getUsername());
    }

    @GetMapping("/{folderUuid}/children")
    public List<FileSystemDto> getChildren(
            @AuthenticationPrincipal SecurityUser securityUser, @PathVariable UUID folderUuid) {
        return folderService.getChildren(securityUser.getUsername(), folderUuid);
    }
}
