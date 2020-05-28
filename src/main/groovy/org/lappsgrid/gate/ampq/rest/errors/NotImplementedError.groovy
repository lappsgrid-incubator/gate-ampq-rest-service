package org.lappsgrid.gate.ampq.rest.errors

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.server.ResponseStatusException

/**
 *
 */
class NotImplementedError extends ResponseStatusException {
    NotImplementedError(String message) {
        super(HttpStatus.NOT_IMPLEMENTED, message)
    }
}
