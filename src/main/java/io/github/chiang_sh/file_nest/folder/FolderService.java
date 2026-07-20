package io.github.chiang_sh.file_nest.folder;

import io.github.chiang_sh.file_nest.common.FileSystemDto;
import io.github.chiang_sh.file_nest.file.FileRepository;
import io.github.chiang_sh.file_nest.file.dto.FileResponse;
import io.github.chiang_sh.file_nest.folder.dto.FolderResponse;
import io.github.chiang_sh.file_nest.user.UserEntity;
import io.github.chiang_sh.file_nest.user.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@Transactional(rollbackFor = Exception.class)
public class FolderService {

    private final UserRepository userRepository;
    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;

    @Autowired
    public FolderService(
            UserRepository userRepository,
            FileRepository fileRepository,
            FolderRepository folderRepository) {
        this.userRepository = userRepository;
        this.fileRepository = fileRepository;
        this.folderRepository = folderRepository;
    }

    public List<FileSystemDto> getChildren(Long userId) {
        List<FolderResponse> folders = folderRepository.findRootFolders(userId);
        List<FileResponse> files = fileRepository.findRootFiles(userId);
        List<FileSystemDto> children = new ArrayList<>(folders.size() + files.size());
        children.addAll(folders);
        children.addAll(files);
        return children;
    }

    public List<FileSystemDto> getChildren(Long userId, UUID folderUuid) {
        List<FolderResponse> folders = folderRepository.findChildrenFolders(userId, folderUuid);
        List<FileResponse> files = fileRepository.findChildrenFiles(userId, folderUuid);
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

    public void delete(String username, UUID uuid) {
        FolderEntity folder = getAccessibleFolder(username, uuid);
        folderRepository.delete(folder);
    }
}
