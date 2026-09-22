package io.github.chiang_sh.file_nest.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;

@Component
public class CleanupPendingFileJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanupPendingFileJob.class);
    private final FileService fileService;

    public CleanupPendingFileJob(FileService fileService) {
        this.fileService = fileService;
    }

    @Scheduled(cron = "0 0 3 * * *") // Run the job at 3:00 AM every day.
    public void cleanup() {
        LOGGER.info("Cleanup pending files job started.");
        Long lastId = 0L;
        int deleteCount = 0;

        // Upload presigned URL expires after 5 minutes.
        // Use a 10-minutes buffer since the file record is created before the presigned URL.
        OffsetDateTime expired = OffsetDateTime.now().minusMinutes(10);

        while (true) {
            List<FileEntity> files =
                    fileService.findCleanupFiles(StatusType.PENDING, lastId, expired);
            if (files.isEmpty()) {
                break;
            }
            for (FileEntity file : files) {
                try {
                    fileService.deleteObject(file);
                    fileService.deleteRecord(file);
                    deleteCount++;
                } catch (Exception e) {
                    LOGGER.error("Failed to clean up pending file {}", file.getUuid(), e);
                }
            }
            lastId = files.getLast().getId();
        }
        LOGGER.info("Cleanup pending files job completed. Deleted {} files.", deleteCount);
    }
}
