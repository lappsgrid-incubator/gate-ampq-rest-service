package org.lappsgrid.gate.ampq.rest.services

import groovy.util.logging.Slf4j
import org.lappsgrid.gate.ampq.rest.Context
import org.springframework.stereotype.Service

/**
 * Manages the Context.SERVICES hash map.
 */
@Slf4j
@Service
class ServiceRegistry {

    ServiceRegistry() {
        log.info("Constructed the ServiceRegistry")
        Context.SERVICES = [:]
    }

//    ServiceRegistry() {
//        if (Context.SERVICES.size() == 0) {
//            return
//        }
//
//        Context.SERVICES.each { k,v ->
//            log.info("URL {} is mapped to mailbox {}", k, v)
//        }
//    }

    int size() { return Context.SERVICES.size() }

    boolean add(String name, String mailbox) {
        log.info("Mapping service URL {} to mailbox {}", name, mailbox)
        if (Context.SERVICES[name]) {
            log.warn("The URL {} has already been mapped to {}", name, Context.SERVICES[name])
            return false
        }
        Context.SERVICES[name] = mailbox
    }

    boolean replace(String name, String mailbox) {
        boolean replaced = Context.SERVICES[name] != null
        Context.SERVICES[name] = mailbox
        return replaced
    }

    String get(String name) {
        Context.SERVICES[name]
    }

    Map<String,String> list() {
        return Context.SERVICES.clone()
    }

    boolean remove(String name) {
        Context.SERVICES.remove(name) != null
    }
}
