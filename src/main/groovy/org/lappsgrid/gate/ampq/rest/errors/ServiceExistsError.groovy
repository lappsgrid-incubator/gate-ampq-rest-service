package org.lappsgrid.gate.ampq.rest.errors

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

/**
 *
 */

class ServiceExistsError extends ResponseStatusException {
    ServiceExistsError(String message) {
        super(HttpStatus.BAD_REQUEST, message)
    }
}
