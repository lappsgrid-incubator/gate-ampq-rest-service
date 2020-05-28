package org.lappsgrid.gate.ampq.rest.job

import org.junit.Test
import org.lappsgrid.gate.ampq.rest.json.Serializer
import org.lappsgrid.gate.ampq.rest.util.Time

import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalUnit

/**
 *
 */
class JobDescriptionTest {

    @Test
    void elapsedTests() {
        assert 1 == time(1, ChronoUnit.MILLIS)
        assert 100 == time(100, ChronoUnit.MILLIS)
        assert 1000 == time(1, ChronoUnit.SECONDS)
        assert 60000 == time(60, ChronoUnit.SECONDS)
        assert 60000 == time(1, ChronoUnit.MINUTES)
        assert 3600000 == time(3600, ChronoUnit.SECONDS)
        assert 3600000 == time(60, ChronoUnit.MINUTES)
        assert 3600000 == time(1, ChronoUnit.HOURS)
    }

    @Test
    void zeroElapsedIfNotStarted() {
        JobDescription job = new JobDescription()
        assert 0 == job.elapsed
        assert null == job._elapsed
    }

    @Test
    void serialize() {
        JobDescription j = new JobDescription()
        Serializer.toJson(j)
    }

    Long time(long amount, TemporalUnit unit) {
        JobDescription j = new JobDescription()
        ZonedDateTime now = Time.now()
        j.startedAt = now
        j.finishedAt = now.toInstant()
                .plus(amount, unit)
                .atZone(ZoneId.ofOffset("", ZoneOffset.UTC))
        return j.elapsed
    }
}
