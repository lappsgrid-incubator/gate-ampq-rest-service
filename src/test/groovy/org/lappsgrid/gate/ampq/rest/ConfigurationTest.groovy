package org.lappsgrid.gate.ampq.rest


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
class ConfigurationTest {

    @Autowired
    Configuration K

    @Test
    void smokeTest() {
        assert K != null
    }

    @Test
    void constructor() {
        new Configuration()
    }

    @Test
    void testDefaultValues() {
        assert 30 == K.fileAge
        assert 5 == K.reaperDelay
        assert "/tmp/timeml" == K.storageDir
    }
}
