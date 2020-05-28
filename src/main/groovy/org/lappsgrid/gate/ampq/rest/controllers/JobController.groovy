package org.lappsgrid.gate.ampq.rest.controllers

import groovy.util.logging.Slf4j
import org.lappsgrid.gate.ampq.rest.errors.BadRequest
import org.lappsgrid.gate.ampq.rest.errors.GoneError
import org.lappsgrid.gate.ampq.rest.errors.NotFoundError
import org.lappsgrid.gate.ampq.rest.job.JobDescription
import org.lappsgrid.gate.ampq.rest.json.Serializer
import org.lappsgrid.gate.ampq.rest.services.ManagerService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
/**
 *
 */
@Slf4j("logger")
@RestController
class JobController {

    ManagerService manager

    @Autowired
    JobController(ManagerService manager) {
        if (manager == null) throw new NullPointerException("null object injected...");
        this.manager = manager
    }

    @RequestMapping(path = "/job/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    JobDescription getJobStatus(@PathVariable String id) {
        if (id == null) {
            throw new BadRequest("No ID provided. Use /job?id=<job id>")
        }
        JobDescription job = manager.get(id)
        if (job == null) {
            throw new NotFoundError("There is no job with ID $id".toString())
        }
        return job
    }

    @DeleteMapping(path="/job/{id}")
    String deleteJob(@PathVariable String id) {
        if (manager.remove(id)) {
            return "File has been removed."
        }
        throw new GoneError("That file has already been removed.")
    }
}
