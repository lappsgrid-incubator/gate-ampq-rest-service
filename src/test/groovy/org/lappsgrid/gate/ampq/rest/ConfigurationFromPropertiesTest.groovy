package org.lappsgrid.gate.ampq.rest


import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.lappsgrid.gate.ampq.rest.util.Configuration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner

/**
 *
 */
@RunWith(SpringRunner)
@SpringBootTest(classes = [Configuration])
class ConfigurationFromPropertiesTest {

    @Autowired
    Configuration K

    @BeforeClass
    static void init() {
        System.setProperty("FILE_AGE", "42")
        System.setProperty("REAPER_DELAY", "42")
        System.setProperty("STORAGE_PATH", "/somewhere/special")
    }

//    @After
//    void clearSettings() {
//        System.clearProperty("FILE_AGE")
//        System.clearProperty("REAPER_DELAY")
//        System.clearProperty("STORAGE_PATH")
//    }

    @Test
    void smokeTest() {
        assert K != null
    }

    @Test
    void constructor() {
        new Configuration()
    }

    @Test
    void testValuesFromProperties() {
        assert 42 == K.fileAge
        assert 42 == K.reaperDelay
        assert "/somewhere/special" == K.storageDir
    }
}
