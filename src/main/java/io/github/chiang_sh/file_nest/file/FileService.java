package io.github.chiang_sh.file_nest.file;

import io.github.chiang_sh.file_nest.file_permission.FilePermissionEntity;
import io.github.chiang_sh.file_nest.file_permission.FilePermissionRepository;
import io.github.chiang_sh.file_nest.file_permission.FilePermissionType;
import io.github.chiang_sh.file_nest.minio.MinioProperties;
import io.github.chiang_sh.file_nest.user.UserEntity;
import io.github.chiang_sh.file_nest.user.UserRepository;
import io.minio.*;
import io.minio.errors.MinioException;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Transactional(rollbackFor = Exception.class)
public class FileService {

    private final UserRepository userRepository;
    private final FileRepository fileRepository;
    private final FilePermissionRepository filePermissionRepository;
    private final MinioClient minioClient;
    private final MinioProperties properties;

    @Autowired
    public FileService(
            UserRepository userRepository,
            FileRepository fileRepository,
            FilePermissionRepository filePermissionRepository,
            MinioClient minioClient,
            MinioProperties properties) {
        this.userRepository = userRepository;
        this.fileRepository = fileRepository;
        this.filePermissionRepository = filePermissionRepository;
        this.minioClient = minioClient;
        this.properties = properties;
    }

    public FileEntity createFile(String username, String originalFilename, UUID parentUuid) {
        UserEntity user =
                userRepository
                        .findByUsername(username)
                        .orElseThrow(
                                () -> new NoSuchElementException("User not exist: " + username));
        UUID uuid = UUID.randomUUID();
        String filename = FilenameUtils.getName(originalFilename);
        String extension = FilenameUtils.getExtension(originalFilename);
        String minioFilename = uuid + "." + extension;
        String objectKey = String.join("/", "users", Long.toString(user.getId()), minioFilename);

        FileEntity fileEntity = new FileEntity();
        fileEntity.setUuid(uuid);
        fileEntity.setName(filename);
        if (parentUuid != null) {
            FileEntity parent =
                    fileRepository
                            .findByUuid(parentUuid)
                            .orElseThrow(
                                    () ->
                                            new NoSuchElementException(
                                                    "Parent folder not exist: " + parentUuid));
        }
        fileEntity.setStoragePath(objectKey);
        fileEntity.setStatus(StatusType.PENDING);
        fileRepository.save(fileEntity);

        FilePermissionEntity permissionEntity = new FilePermissionEntity();
        permissionEntity.setUser(user);
        permissionEntity.setFile(fileEntity);
        permissionEntity.setPermission(FilePermissionType.OWNER);
        filePermissionRepository.save(permissionEntity);

        return fileEntity;
    }

    public String uploadUrl(String objectKey) throws MinioException {
        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Http.Method.PUT)
                        .bucket(properties.getBucketName())
                        .object(objectKey)
                        .expiry(5, TimeUnit.MINUTES)
                        .build());
    }

    public void confirmUpload(UUID uuid) throws MinioException {
        FileEntity entity =
                fileRepository
                        .findByUuid(uuid)
                        .orElseThrow(() -> new NoSuchElementException("Folder not exist: " + uuid));

        StatObjectResponse stat =
                minioClient.statObject(
                        StatObjectArgs.builder()
                                .bucket(properties.getBucketName())
                                .object(entity.getStoragePath())
                                .build());
        entity.setContentType(stat.contentType());
        entity.setSize(stat.size());
        entity.setStatus(StatusType.COMPLETED);
        fileRepository.save(entity);
    }
}
