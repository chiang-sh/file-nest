package io.github.chiang_sh.file_nest.file;

import io.github.chiang_sh.file_nest.file.dto.FileResponse;
import io.github.chiang_sh.file_nest.file_permission.FilePermissionEntity;
import io.github.chiang_sh.file_nest.file_permission.FilePermissionRepository;
import io.github.chiang_sh.file_nest.file_permission.FilePermissionType;
import io.github.chiang_sh.file_nest.folder.FolderEntity;
import io.github.chiang_sh.file_nest.folder.FolderService;
import io.github.chiang_sh.file_nest.minio.MinioProperties;
import io.github.chiang_sh.file_nest.user.UserEntity;
import io.github.chiang_sh.file_nest.user.UserRepository;
import io.minio.*;
import io.minio.errors.MinioException;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
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
    private final FolderService folderService;
    private final MinioClient minioClient;
    private final MinioProperties properties;

    @Autowired
    public FileService(
            UserRepository userRepository,
            FileRepository fileRepository,
            FilePermissionRepository filePermissionRepository,
            FolderService folderService,
            MinioClient minioClient,
            MinioProperties properties) {
        this.userRepository = userRepository;
        this.fileRepository = fileRepository;
        this.filePermissionRepository = filePermissionRepository;
        this.folderService = folderService;
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

        FileEntity file = new FileEntity();
        file.setUuid(uuid);
        file.setName(filename);
        if (parentUuid != null) {
            FileEntity parent =
                    fileRepository
                            .findByUuid(parentUuid)
                            .orElseThrow(
                                    () ->
                                            new NoSuchElementException(
                                                    "Parent folder not exist: " + parentUuid));
        }
        file.setStoragePath(objectKey);
        file.setStatus(StatusType.PENDING);
        fileRepository.save(file);

        FilePermissionEntity permission = new FilePermissionEntity();
        permission.setUser(user);
        permission.setFile(file);
        permission.setPermission(FilePermissionType.OWNER);
        filePermissionRepository.save(permission);

        return file;
    }

    public String presignedUrl(String objectKey, Http.Method method) throws MinioException {
        return presignedUrl(objectKey, method, 10);
    }

    public String presignedUrl(String objectKey, Http.Method method, int expiredMinutes)
            throws MinioException {
        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(method)
                        .bucket(properties.getBucketName())
                        .object(objectKey)
                        .expiry(expiredMinutes, TimeUnit.MINUTES)
                        .build());
    }

    public void confirmUpload(UUID uuid) throws MinioException {
        FileEntity entity =
                fileRepository
                        .findByUuid(uuid)
                        .orElseThrow(() -> new NoSuchElementException("File not exist: " + uuid));

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

    public FilePermissionEntity getAccessiblePermission(String username, UUID uuid) {
        UserEntity user =
                userRepository
                        .findByUsername(username)
                        .orElseThrow(
                                () -> new NoSuchElementException("User not exist: " + username));
        FileEntity file =
                fileRepository
                        .findByUuid(uuid)
                        .orElseThrow(() -> new NoSuchElementException("File not exist: " + uuid));
        if (!file.getStatus().equals(StatusType.COMPLETED)) {
            throw new IllegalStateException("File " + uuid + " upload is not completed.");
        }
        return filePermissionRepository
                .findByUserIdAndFileId(user.getId(), file.getId())
                .orElseThrow(() -> new AccessDeniedException("Access denied: " + uuid));
    }

    public FileResponse updateInfo(String username, UUID uuid, UUID folderUuid, String filename) {
        FilePermissionEntity permission = getAccessiblePermission(username, uuid);
        FileEntity file = permission.getFile();
        if (folderUuid != null) {
            FolderEntity folder = folderService.getAccessibleFolder(username, folderUuid);
            file.setFolder(folder);
        }
        if (filename != null && !filename.isEmpty()) {
            file.setName(filename);
        }
        fileRepository.save(file);
        return FileResponse.from(file, permission);
    }

    public void delete(String username, UUID uuid) {
        FilePermissionEntity permission = getAccessiblePermission(username, uuid);
        if (permission.getPermission().equals(FilePermissionType.READ)) {
            throw new AccessDeniedException("Insufficient permissions to delete this resource.");
        }
        FileEntity file = permission.getFile();
        fileRepository.delete(file);
    }
}
