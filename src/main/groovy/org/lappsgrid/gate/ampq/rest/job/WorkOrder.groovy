package org.lappsgrid.gate.ampq.rest.job


import org.lappsgrid.pubannotation.model.Document
import org.lappsgrid.serialization.Data

/**
 * Information about the documents that have been submitted for processing. Since
 * we want the API to return documents in the original format as they were submitted
 * we need to store a copy of the original.
 */
class WorkOrder {
    /** The WorkOrder ID is the same as the job ID. */
    String id
    /** The name of the RMQ mailbox the order will be sent to. */
    String target
    /** The text to be annotated by GATE. */
    String text
    /** The format of the input document. Used so we can return the same format. */
    String format
    /** A copy of the original input document. */
    Object original
    /** Job state information. */
    JobDescription job

    WorkOrder() {
        this.job = new JobDescription()
        this.id = this.job.id
    }
    WorkOrder(Data data, String target) {
        this()
        this.original = data
        this.text = data.payload.text.toString()
        this.format = 'lif'
        this.target = target
    }
    WorkOrder(Document data, String target) {
        this()
        this.original = data
        this.text = data.text
        this.format = 'pubann'
        this.target = target
    }
    WorkOrder(String data, String format, String target) {
        this()
        this.original = data
        this.text = data
        this.format = format
        this.target = target
    }
}
