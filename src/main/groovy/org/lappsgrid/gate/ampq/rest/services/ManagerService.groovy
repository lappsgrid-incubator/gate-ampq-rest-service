package org.lappsgrid.gate.ampq.rest.services

import groovy.util.logging.Slf4j
import org.lappsgrid.gate.ampq.rest.EntryPoint
import org.lappsgrid.gate.ampq.rest.errors.NotFoundError
import org.lappsgrid.gate.ampq.rest.job.JobDescription
import org.lappsgrid.gate.ampq.rest.job.JobStatus
import org.lappsgrid.gate.ampq.rest.job.WorkOrder
import org.lappsgrid.gate.ampq.rest.util.Time
import org.lappsgrid.pubannotation.model.Document
import org.lappsgrid.serialization.Data
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 *
 */
@Service
@Slf4j("logger")
class ManagerService {

    final PostalService po
    final StorageService storage
    final Map<String, WorkOrder> jobs = [:]
    final SubscriberService subscriber

    // Only used in Mocking
    protected ManagerService() {
        jobs = [:]
    }

    @Autowired
    ManagerService(final PostalService po, final StorageService storage, final SubscriberService subscriber) {
        if (po == null) {
            throw new NullPointerException("Null PostalService injected.")
        }
        if (storage == null) {
            throw new NullPointerException("Null StorageSerivce injected.")
        }
        this.po = po
        this.storage = storage
        this.storage.manager = this
        this.subscriber = subscriber
        subscriber.start()
        jobs = new HashMap<>()

        logger.info("Started manager service.")
    }

    JobDescription get(final String id) {
        if (jobs[id]) {
            return jobs[id].job
        }
        return null
    }

    JobDescription submit(final Document document, final String target) {
        logger.debug("Document {} submitted", document.id)
        save(new WorkOrder(document, target))
    }

    JobDescription submit(Data data, String target) {
        save(new WorkOrder(data, target))
    }

    JobDescription submit(String text, String target) {
        save(new WorkOrder(text, 'text', target))
    }

    JobDescription submitXml(String xml, String target) {
        save(new WorkOrder(xml, 'gate', target))
    }

    JobDescription save(WorkOrder order) {
        jobs[order.job.id] = order
        po.send(order)
        order.job.startedAt = Time.now()
        order.job.status = JobStatus.IN_PROGRESS
        return order.job
    }

    boolean remove(String id) {
        if (jobs[id] == null) {
            logger.warn("Attempted to remove {} but there is no work order.", id)
            throw new NotFoundError("There is no job with that id.")
        }
        JobDescription job = jobs[id].job
        jobs.remove(id)
        return storage.remove(job.id)
    }

    boolean aborted(String id) {
        if (jobs[id] == null) {
            logger.warn("Order {} was cancelled but there is no work order.", id)
            return false
        }
        logger.warn("Order {} was aborted.", id)
        JobDescription job = jobs[id].job
        job.stoppedAt = job.finishedAt = Time.now()
        job.message = 'Processing was interrupted. Try submitting the job again.'
        job.status = JobStatus.STOPPED
        return true
    }

    boolean failed(String id, Throwable t) {
        failed(id, t.message)
    }

    boolean failed(String id, String message) {
        if (jobs[id] == null) {
            logger.warn("Order {} failed but there is no job description.", id)
            return false
        }
        logger.error("Failed to process order {}", id)
        JobDescription job = jobs[id].job
        job.finishedAt = Time.now()
        job.message = message
        job.status = JobStatus.ERROR
        return true
    }

    boolean complete(String id) {
        if (jobs[id] == null) {
            logger.error("Order {} was completed but there is work order.", id)
            return false
        }
        JobDescription job = jobs[id].job
        if (job == null) {
            logger.error("Malformed work order (). No job description found.")
            return false
        }
        job.message = 'ok'
        job.finishedAt = Time.now()
        job.resultUrl = "/download/${id}"
        job.status = JobStatus.DONE
        logger.info("Work order complete: {}", id)
        return true
    }

    void exit() {
        po.close()
        storage.close()
        subscriber.close()
        logger.info "Manager service is exiting"
        EntryPoint.exit()
    }
}
