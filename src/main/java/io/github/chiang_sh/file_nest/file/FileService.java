package io.github.chiang_sh.file_nest.file;

import io.github.chiang_sh.file_nest.minio.MinioProperties;
import io.github.chiang_sh.file_nest.user.UserEntity;
import io.github.chiang_sh.file_nest.user.UserRepository;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.MinioException;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(rollbackFor = Exception.class)
public class FileService {

    private UserRepository userRepository;
    private FileRepository fileRepository;
    private MinioClient minioClient;
    private MinioProperties properties;

    @Autowired
    public FileService(
            UserRepository userRepository,
            FileRepository fileRepository,
            MinioClient minioClient,
            MinioProperties properties) {
        this.userRepository = userRepository;
        this.fileRepository = fileRepository;
        this.minioClient = minioClient;
        this.properties = properties;
    }

    public List<FileDto> getChildren(String username, UUID parentUuid) {
        UserEntity user = userRepository.findByUsername(username).orElseThrow();
        return parentUuid == null
                ? fileRepository.findRootChildren(user.getId())
                : fileRepository.findChildren(user.getId(), parentUuid);
    }

    public void uploadFile(String username, UUID parentUuid, MultipartFile file)
            throws MinioException, IOException {
        UserEntity user = userRepository.findByUsername(username).orElseThrow();
        UUID uuid = UUID.randomUUID();
        String originalFilename = file.getOriginalFilename();
        String filename = FilenameUtils.getName(originalFilename);
        String extension = FilenameUtils.getExtension(originalFilename);
        String minioFilename = uuid + "." + extension;
        String objectPath = String.join("/", "users", Long.toString(user.getId()), minioFilename);

        FileEntity fileEntity = new FileEntity();
        fileEntity.setUuid(uuid);
        fileEntity.setName(filename);
        fileEntity.setType(FileType.FILE);
        if (parentUuid != null) {
            FileEntity parent = fileRepository.findByUuid(parentUuid).orElseThrow();
            fileEntity.setParent(parent);
        }
        fileEntity.setStoragePath(objectPath);
        fileEntity.setSize(file.getSize());
        fileRepository.save(fileEntity);

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(properties.getBucketName())
                        .object(objectPath)
                        .stream(file.getInputStream(), file.getSize(), -1L)
                        .contentType(file.getContentType())
                        .build());
    }
}
