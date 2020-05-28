package org.lappsgrid.gate.ampq.rest.job

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import org.lappsgrid.gate.ampq.rest.util.Time

import java.time.ZonedDateTime

/**
 * Data model for the JSON format returned by the API for /submit and /job
 * end points.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class JobDescription {
    String id
    @JsonProperty('submitted_at')
    ZonedDateTime submittedAt
    @JsonProperty('started_at')
    ZonedDateTime startedAt
    @JsonProperty('stopped_at')
    ZonedDateTime stoppedAt
    @JsonProperty('finished_at')
    ZonedDateTime finishedAt
    @JsonProperty('ETA')
    ZonedDateTime eta
    @JsonProperty("elapsed")
    Long _elapsed
    @JsonProperty('result_URL')
    String resultUrl
    JobStatus status
    String message

    JobDescription() {
        this(UUID.randomUUID().toString())
    }

    JobDescription(String id) {
        this.id = id
        submittedAt = Time.now()
        status = JobStatus.IN_QUEUE
    }

    @JsonIgnore
    Long getElapsed() {
        if (startedAt == null) {
            return 0L
        }
        if (_elapsed == null) {
            calculateElapsed()
        }
        return _elapsed
    }

    void calculateElapsed() {
        if (startedAt == null) {
            return
        }
        if (stoppedAt) {
            _elapsed = Time.between(startedAt, stoppedAt)
        }
        else if (finishedAt) {
            _elapsed = Time.between(startedAt, finishedAt)
        }
        else {
            _elapsed = Time.since(startedAt)
        }

    }

}
