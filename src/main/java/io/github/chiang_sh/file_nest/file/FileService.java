package io.github.chiang_sh.file_nest.file;

import io.github.chiang_sh.file_nest.user.UserEntity;
import io.github.chiang_sh.file_nest.user.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class FileService {

    private UserRepository userRepository;
    private FileRepository fileRepository;

    @Autowired
    public FileService(UserRepository userRepository, FileRepository fileRepository) {
        this.userRepository = userRepository;
        this.fileRepository = fileRepository;
    }

    public List<FileDto> getChildren(String username, UUID parentUuid) {
        UserEntity user = userRepository.findByUsername(username).orElseThrow();
        return parentUuid == null
                ? fileRepository.findRootChildren(user.getId())
                : fileRepository.findChildren(user.getId(), parentUuid);
    }
}
