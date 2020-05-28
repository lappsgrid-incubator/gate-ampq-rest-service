package org.lappsgrid.gate.ampq.rest.services

import groovy.util.logging.Slf4j
import org.lappsgrid.gate.ampq.rest.util.Configuration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileTime
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 *
 */
@Service
@Slf4j("logger")
class StorageService { //implements StatsProvider {

    final Configuration K

    private static String STATS_SIZE = "storage.files"
    private static String STATS_DELETED = "storage.deleted"

    private Map<String, Path> index
    private long deleted

    private Path directory

    private ScheduledExecutorService executor;

    // Spring does not autowire this for us, otherwise we end up with a circular
    // dependency.  The ManagerService will inject itself when it is ready.
    ManagerService manager

    StorageService() {
        K = new Configuration()
    }

    @Autowired
    StorageService(Configuration configuration) {
        K = configuration
        deleted = 0
        index = new HashMap<>()
        executor = (ScheduledExecutorService) Executors.newScheduledThreadPool(1)
        executor.scheduleAtFixedRate(new Reaper(), K.reaperDelay, K.reaperDelay, TimeUnit.MINUTES)
        executor.scheduleAtFixedRate(new DailyReaper(), 24, 24, TimeUnit.HOURS)
        directory = Paths.get(K.storageDir)
        if (!Files.exists(directory)) {
            try {
                Files.createDirectories(directory)
            }
            catch (IOException e) {
                logger.error("Unable to create storage directory.", e)
                directory = null
            }
        }
    }

    boolean add(String id, String json) throws IOException {
        if (directory == null) {
            return false
        }

        String filename = id + ".json"
        Path output = directory.resolve(filename)
        if (json.length() < 16384) {
            Files.write(output, json.bytes)
            index[id] = output
        }
        else {
            BufferedWriter writer = Files.newBufferedWriter(output)
            writer.write(json, 0, json.length())
            writer.flush()
            writer.close()
            index[id] = output
        }
        logger.info("Wrote {} to {}", id, output.toString())
        return true
    }

    String get(String id) {
        if (directory == null) {
            return null
        }
        Path path = index[id]
        if (path == null) {
            logger.warn("Document not found {}", id)
            return null
        }
        if (!Files.exists(path)) {
            logger.warn("Document was in index but is now gone: {}", id)
            index.remove(id)
            return null
        }
        return new String(Files.readAllBytes(path))

    }

    boolean remove(String id) {
        Path path = index[id]
        if (path == null) {
            logger.warn("Attempted to remove non-existent file {}", id)
            return false
        }
        index.remove(id)
        try {
            Files.delete(path)
            logger.info("Deleted file {}", path.toString())
        }
        catch (IOException e) {
            logger.error("Unable to delete {}", path.toString(), e)
        }
        return true
    }

    boolean exists(String id) {
        return index[id] != null
    }

//    @Override
    Map<String, Integer> stats(Map<String, Integer> stats) {
        stats.put(STATS_SIZE, index.size())
        stats.put(STATS_DELETED, deleted)
        return stats
    }

    void close() {
        executor.shutdown()
        executor.awaitTermination(2, TimeUnit.SECONDS)
        if (!executor.isTerminated()) {
            executor.shutdownNow()
        }
        logger.info "Storage service threads have terminated."
    }

    @Slf4j("logger")
    class Reaper implements Runnable {
        void run() {
            logger.debug("Running the storage system reaper.")
            Instant cutoff = Instant.now().minus(K.fileAge, ChronoUnit.MINUTES)
            List remove = []
            index.each { id, path ->
                BasicFileAttributes atts = Files.readAttributes(path, BasicFileAttributes)
                FileTime t = atts.creationTime()
                if (t.toInstant().isBefore(cutoff)) {
                    remove.add(id)
                    logger.info("Removing stale file for {}", id)
                    if (Files.exists(path)) try {
                        Files.delete(path)
                        ++deleted
                    }
                    catch (IOException e) {
                        logger.error("Error deleting file {}", path.toString(), e)
                    }
                }
            }
            remove.each {
                index.remove(it)
                manager.jobs.remove(it)
            }
        }
    }

    @Slf4j("logger")
    class DailyReaper implements Runnable {

        void run() {
            logger.info("Running the daily file reaper.")
            Instant cutoff = Instant.now().minus(24, ChronoUnit.HOURS)
            def beforeCutoff = { Path path ->
                Files.readAttributes(path, BasicFileAttributes).creationTime().toInstant().isBefore(cutoff)
            }

            try {
                Files.list(directory).filter(beforeCutoff).each { Files.delete(it) }
            }
            catch (Exception e) {
                logger.error("Error running the daily reaper.", e)
            }
        }
    }
}
