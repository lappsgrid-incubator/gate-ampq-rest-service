package org.lappsgrid.gate.ampq.rest.controllers

import groovy.util.logging.Slf4j
import org.lappsgrid.gate.ampq.rest.job.JobDescription
import org.lappsgrid.gate.ampq.rest.job.JobStatus
import org.lappsgrid.gate.ampq.rest.services.ManagerService
import org.lappsgrid.gate.ampq.rest.services.StorageService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

/**
 *
 */
@Controller
@Slf4j("logger")
@RequestMapping(path="/download")
class DownloadController {

    private StorageService storage
    private ManagerService manager

    @Autowired
    DownloadController(ManagerService manager, StorageService storage) {
        this.manager = manager
        this.storage = storage
    }

    @GetMapping(path="/{id}", produces = 'application/json')
    ResponseEntity download(@PathVariable String id) {

        String data = storage.get(id)
        if (data != null) {
            return ResponseEntity.ok().body(data)
        }
        // There is nothing in storage. See if the manager knows anything about the job.
        JobDescription description = manager.get(id)
        if (description == null) {
            return ResponseEntity.status(404).body("No job or download available for id " + id + "\n")
        }
        if (JobStatus.DONE == description.status) {
            // A job was completed but the result no longer exists.
            return ResponseEntity.status(HttpStatus.GONE).body(description)
        }
        if (JobStatus.ERROR == description.status) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error was encountered while processing this document.")
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED).location(new URI('/job/'+id)).body(description)
    }

    @DeleteMapping(path='/{id}')
    ResponseEntity deleteDownload(@PathVariable String id) {
        if (storage.exists(id)) {
            storage.remove(id)
            return ResponseEntity.ok("Download deleted.\n")
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No such download found.\n")
    }

}
