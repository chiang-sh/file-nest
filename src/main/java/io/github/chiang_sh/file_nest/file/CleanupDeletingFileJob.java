package io.github.chiang_sh.file_nest.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class CleanupDeletingFileJob {

    private static final Logger logger = LoggerFactory.getLogger(CleanupDeletingFileJob.class);
    private final FileService fileService;

    public CleanupDeletingFileJob(FileService fileService) {
        this.fileService = fileService;
    }

    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.MINUTES)
    public void cleanup() {
        logger.info("Cleanup deleting files job started.");
        Long lastId = 0L;
        int deleteCount = 0;
        while (true) {
            List<FileEntity> files =
                    fileService.findCleanupFiles(StatusType.DELETING, lastId, null);
            if (files.isEmpty()) {
                break;
            }
            for (FileEntity file : files) {
                try {
                    fileService.deleteObject(file);
                    fileService.deleteRecord(file);
                    deleteCount++;
                } catch (Exception e) {
                    logger.error("Failed to clean up deleting file {}", file.getUuid(), e);
                }
            }
            lastId = files.getLast().getId();
        }
        logger.info("Cleanup deleting files job completed. Deleted {} files.", deleteCount);
    }
}
