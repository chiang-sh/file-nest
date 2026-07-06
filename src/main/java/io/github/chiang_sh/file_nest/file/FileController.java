package io.github.chiang_sh.file_nest.file;

import io.github.chiang_sh.file_nest.security.SecurityUser;
import io.github.chiang_sh.file_nest.user.UserEntity;
import io.github.chiang_sh.file_nest.user.UserRepository;
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

    private UserRepository userRepository;
    private FileRepository fileRepository;

    @Autowired
    public FileController(UserRepository userRepository, FileRepository fileRepository) {
        this.userRepository = userRepository;
        this.fileRepository = fileRepository;
    }

    @GetMapping("/children")
    public List<FileDto> getChildren(
            @AuthenticationPrincipal SecurityUser securityUser,
            @RequestParam(required = false) UUID parentUuid) {
        UserEntity user = userRepository.findByUsername(securityUser.getUsername()).orElseThrow();
        return parentUuid == null
                ? fileRepository.findRootChildren(user.getId())
                : fileRepository.findChildren(user.getId(), parentUuid);
    }
}
