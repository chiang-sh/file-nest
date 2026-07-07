package io.github.chiang_sh.file_nest.file;

import io.github.chiang_sh.file_nest.security.SecurityUser;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
@Tag(name = "File manipulation")
public class FileController {

    private FileService fileService;

    @Autowired
    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping("/children")
    public List<FileDto> getChildren(
            @AuthenticationPrincipal SecurityUser securityUser,
            @RequestParam(required = false) UUID parentUuid) {
        return fileService.getChildren(securityUser.getUsername(), parentUuid);
    }
}
