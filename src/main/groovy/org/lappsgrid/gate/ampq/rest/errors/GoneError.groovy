package org.lappsgrid.gate.ampq.rest.errors

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.server.ResponseStatusException

/**
 *
 */
class GoneError extends ResponseStatusException {
    GoneError(String message) {
        super(HttpStatus.GONE, message)
    }
}
