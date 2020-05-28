package org.lappsgrid.gate.ampq.rest.controllers

import groovy.util.logging.Slf4j
import org.apache.commons.collections4.Put
import org.lappsgrid.gate.ampq.rest.Context
import org.lappsgrid.gate.ampq.rest.errors.BadRequest
import org.lappsgrid.gate.ampq.rest.errors.NoSuchServiceException
import org.lappsgrid.gate.ampq.rest.errors.NotFoundError
import org.lappsgrid.gate.ampq.rest.errors.ServiceExistsError
import org.lappsgrid.gate.ampq.rest.services.ServiceRegistry
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 *
 */
@Slf4j
@RequestMapping("/admin")
@RestController
class AdminController {

    ServiceRegistry services

    @Autowired
    AdminController(ServiceRegistry services) {
        this.services = services
    }

    @GetMapping(path="/services", produces = MediaType.APPLICATION_JSON_VALUE)
    Map<String,String> getServiceMapping() {
        return services.list()
    }

    @PostMapping(path = "/services", produces = MediaType.APPLICATION_JSON_VALUE)
    Map addServiceMapping(@RequestParam String url, @RequestParam(required = false) String mailbox) {
        String exists = services.get(url)
        if (exists) {
            throw new ServiceExistsError("There is already a mapping from $url to $exists")
        }
        if (mailbox == null) {
            mailbox = url
        }
        if (!services.add(url, mailbox)) {
            throw new ServiceExistsError("Unable to add mapping for URL $url")
        }
        return services.list()
    }

    @PutMapping(path="/services", produces = MediaType.APPLICATION_JSON_VALUE)
    Map replaceServiceMapping(@RequestParam String url, @RequestParam String mailbox) {
        String exists = services.get(url)
        if (exists == null) {
            throw new BadRequest("No service is registered for the URL $url")
        }
        if (services.replace(url, mailbox)) {
            log.info("Replaced mapping for {} with mail box {}", url, mailbox)
        }
        else {
            log.info("Added mapping for [] to mail box {}", url, mailbox)
        }
        return services.list()
    }

    @DeleteMapping(path="/services", produces = MediaType.APPLICATION_JSON_VALUE)
    Map deleteServiceMapping(@RequestParam String url) {
        if (services.remove(url)) {
            return services.list()
        }
        throw new NotFoundError("There is no mapping for URL " + url)
    }
}
