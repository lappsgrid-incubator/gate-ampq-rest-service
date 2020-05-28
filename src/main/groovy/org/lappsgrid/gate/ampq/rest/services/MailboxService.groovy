package org.lappsgrid.gate.ampq.rest.services

import org.lappsgrid.gate.ampq.rest.util.MailBox
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * The MailboxService is used to inject the ManagerService, StorageService, and
 * PostalService into the MailBox instance.  We don't have Spring inject them
 * directly since we want to manage the lifecycle of the MailBox (i.e. the connection
 * to the RabbitMQ server) separately from the service lifecycle.
 */
@Service
class MailboxService {

//    ManagerService manager
//    StorageService storage
//    PostalService po

    MailBox mailBox

    @Autowired
    MailboxService(ManagerService manager, StorageService storage, PostalService po) {
//        this.manager = manager
//        this.storage = storage
//        this.po = po
        mailBox = new MailBox(manager, storage, po)
    }
}
