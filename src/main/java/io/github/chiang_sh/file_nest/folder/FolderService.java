package io.github.chiang_sh.file_nest.folder;

import io.github.chiang_sh.file_nest.common.FileSystemDto;
import io.github.chiang_sh.file_nest.file.FileEntity;
import io.github.chiang_sh.file_nest.file.FileRepository;
import io.github.chiang_sh.file_nest.file.FileService;
import io.github.chiang_sh.file_nest.file.dto.FileResponse;
import io.github.chiang_sh.file_nest.folder.dto.FolderResponse;
import io.github.chiang_sh.file_nest.user.UserRepository;
import io.minio.errors.MinioException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional(rollbackFor = Exception.class)
public class FolderService {

    private final UserRepository userRepository;
    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;
    private final FileService fileService;

    @Autowired
    public FolderService(
            UserRepository userRepository,
            FileRepository fileRepository,
            FolderRepository folderRepository,
            FileService fileService) {
        this.userRepository = userRepository;
        this.fileRepository = fileRepository;
        this.folderRepository = folderRepository;
        this.fileService = fileService;
    }

    public List<FileSystemDto> getChildren(Long userId, int pageNumber, int pageSize) {
        return getChildren(userId, null, pageNumber, pageSize);
    }

    public List<FileSystemDto> getChildren(
            Long userId, UUID folderUuid, int pageNumber, int pageSize) {
        int folderCount = folderRepository.countByOwnerId(userId);
        int totalFolderPageNumber = Math.ceilDiv(folderCount, pageSize);
        List<FolderResponse> folders = List.of();
        List<FileResponse> files = List.of();

        // Paginate folders first, then files.
        if (pageNumber < totalFolderPageNumber) {
            int offset = (pageNumber - 1) * pageSize;
            folders =
                    folderUuid == null
                            ? folderRepository.findRootFolders(userId, pageSize, offset)
                            : folderRepository.findChildrenFolders(
                                    userId, folderUuid, pageSize, offset);
            ;
        } else if (pageNumber == totalFolderPageNumber) {
            int folderOffset = (pageNumber - 1) * pageSize;
            folders =
                    folderUuid == null
                            ? folderRepository.findRootFolders(userId, pageSize, folderOffset)
                            : folderRepository.findChildrenFolders(
                                    userId, folderUuid, pageSize, folderOffset);
            int filePageSize = pageSize - folders.size();
            files =
                    folderUuid == null
                            ? fileRepository.findRootFiles(userId, filePageSize, 0)
                            : fileRepository.findChildrenFiles(userId, folderUuid, filePageSize, 0);
        } else {
            int offset = (pageNumber - 1) * pageSize - folderCount;
            files =
                    folderUuid == null
                            ? fileRepository.findRootFiles(userId, pageSize, offset)
                            : fileRepository.findChildrenFiles(
                                    userId, folderUuid, pageSize, offset);
        }

        List<FileSystemDto> children = new ArrayList<>(folders.size() + files.size());
        children.addAll(folders);
        children.addAll(files);
        return children;
    }

    public FolderResponse create(Long userId, String name, UUID parentUuid) {
        FolderEntity newFolder = new FolderEntity();
        newFolder.setName(name);
        newFolder.setOwner(userRepository.getReferenceById(userId));

        if (parentUuid != null) {
            FolderEntity parent =
                    folderRepository
                            .findByUuidAndOwnerId(parentUuid, userId)
                            .orElseThrow(
                                    () ->
                                            new NoSuchElementException(
                                                    "Parent folder not exist: " + parentUuid));
            newFolder.setParentFolder(parent);
        }

        newFolder = folderRepository.save(newFolder);
        return FolderResponse.from(newFolder);
    }

    public FolderResponse update(Long userId, UUID uuid, UUID parentUuid, String name) {
        FolderEntity folder =
                folderRepository
                        .findByUuidAndOwnerId(uuid, userId)
                        .orElseThrow(() -> new NoSuchElementException("Folder not exist: " + uuid));
        if (parentUuid != null) {
            if (parentUuid.equals(uuid)) {
                throw new IllegalArgumentException(
                        "The parent UUID must not be the same as the resource UUID.");
            }
            FolderEntity parent =
                    folderRepository
                            .findByUuidAndOwnerId(parentUuid, userId)
                            .orElseThrow(
                                    () ->
                                            new NoSuchElementException(
                                                    "Folder not exist: " + parentUuid));
            folder.setParentFolder(parent);
        }
        if (name != null && !name.isEmpty()) {
            folder.setName(name);
        }
        folderRepository.save(folder);
        return FolderResponse.from(folder);
    }

    public void delete(Long userId, UUID uuid) throws MinioException {
        FolderEntity folder =
                folderRepository
                        .findByUuidAndOwnerId(uuid, userId)
                        .orElseThrow(() -> new NoSuchElementException("Folder not exist: " + uuid));

        Queue<FolderEntity> subFolders =
                new ArrayDeque<>(
                        folderRepository.findByParentFolderIdAndOwnerId(
                                folder.getId(), folder.getOwner().getId()));
        List<FileEntity> subFiles =
                new ArrayList<>(fileRepository.findByUserIdAndFolderId(userId, folder.getId()));
        while (!subFolders.isEmpty()) {
            FolderEntity subFolder = subFolders.poll();
            subFolders.addAll(
                    folderRepository.findByParentFolderIdAndOwnerId(
                            subFolder.getId(), folder.getOwner().getId()));
            subFiles.addAll(fileRepository.findByUserIdAndFolderId(userId, subFolder.getId()));
        }
        fileService.delete(userId, subFiles);
        folderRepository.delete(folder);
    }
}
