package org.lappsgrid.gate.ampq.rest.util

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

import javax.annotation.PostConstruct

/**
 * Common configuration settings obtained from (in order of preference):
 * 1. system environment variables
 * 2. Java system properties
 * 3. the application.properties file
 */
@Component
class Configuration {
    @Value('${file.age}')
    private final String DEFAULT_FILE_AGE //= 5
    @Value('${reaper.delay}')
    private final String DEFAULT_REAPER_DELAY //= 30
    @Value('${storage.directory}')
    private final String DEFAULT_STORAGE_DIR //= "/tmp/timeml"

    int fileAge
    int reaperDelay
    String storageDir

    Configuration() {
    }

    @PostConstruct
    private void init() {
        fileAge = get ("FILE_AGE", DEFAULT_FILE_AGE as int)
        reaperDelay = get("REAPER_DELAY", DEFAULT_REAPER_DELAY as int)
        storageDir = get("STORAGE_PATH", DEFAULT_STORAGE_DIR)
    }

    private int get(String name, int defaultValue) {
        String value = System.getenv(name)
        if (value) return Integer.parseInt(value)
        value = System.getProperty(name)
        if (value) return Integer.parseInt(value)
        return defaultValue
    }
    private String get(String name, String defaultValue) {
        String value = System.getenv(name)
        if (value) return value
        value = System.getProperty(name)
        if (value) return value
        return defaultValue
    }

}
