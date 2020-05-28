package org.lappsgrid.gate.ampq.rest.controllers

import groovy.util.logging.Slf4j
import org.lappsgrid.gate.ampq.rest.errors.BadRequest
import org.lappsgrid.gate.ampq.rest.errors.NoSuchServiceException
import org.lappsgrid.gate.ampq.rest.job.JobDescription
import org.lappsgrid.gate.ampq.rest.services.ManagerService
import org.lappsgrid.gate.ampq.rest.services.ServiceRegistry
import org.lappsgrid.pubannotation.model.Document as PubDocument
import org.lappsgrid.serialization.Data
import org.lappsgrid.serialization.DataContainer
import org.lappsgrid.serialization.Serializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import javax.servlet.http.HttpServletResponse

//import gate.Document as GateDocument

/**
 *
 */
@Slf4j("logger")
@RestController
@RequestMapping("/submit")
class SubmitController {

    ManagerService manager
    ServiceRegistry services

    @Autowired
    SubmitController(ManagerService manager, ServiceRegistry services) {
        this.manager = manager
        this.services = services
    }

    @PostMapping(path="/{service}", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    JobDescription postText(@PathVariable String service, @RequestBody String text, HttpServletResponse response) {
        logger.debug('POST /submit/{}/text',service)
        JobDescription job =  manager.submit(text, getMailbox(service))
        response.setHeader("Location", "/job/${job.id}")
        return job
    }

    @PostMapping(path="/{service}/pubann", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    JobDescription postPubann(@PathVariable String service, @RequestBody String json, HttpServletResponse response) {
        logger.debug('POST /submit/{}/pubann', service)
        PubDocument document
        try {
            document = Serializer.parse(json, PubDocument)
        }
        catch (Exception e) {
            throw new BadRequest("Unable to parse a PubAnnotation document from the given input.")
        }
        JobDescription job = manager.submit(document, getMailbox(service))
        response.setHeader("Location", "/job/${job.id}")
        return job
    }

    @PostMapping(path="/{service}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    JobDescription postLif(@PathVariable String service, @RequestBody String json, HttpServletResponse response) {
        logger.debug('POST /submit/{}/lif', service)
        Data data
        try {
            data = Serializer.parse(json, DataContainer)
        }
        catch (Exception e) {
            throw new BadRequest("Unable to parse a LIF document from the given input.")
        }
        JobDescription job = manager.submit(data, getMailbox(service))
        response.setHeader("Location", "/job/${job.id}")
        return job
    }

    @PostMapping(path="/{service}", consumes = MediaType.TEXT_XML_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    JobDescription postGateXml(@PathVariable String service, @RequestBody String xml, HttpServletResponse response) {
        logger.debug('POST /submit/{}/gate', service)
        JobDescription job = manager.submitXml(xml, getMailbox(service))
        response.setHeader("Location", "/job/${job.id}")
        return job
    }

    private String getMailbox(String name) throws NoSuchServiceException {
        String mailbox = services.get(name)
        if (mailbox == null) {
            throw new NoSuchServiceException("There is no service named $name")
        }
        return mailbox
    }
}
