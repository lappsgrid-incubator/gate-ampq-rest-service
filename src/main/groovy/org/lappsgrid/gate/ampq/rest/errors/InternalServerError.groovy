package org.lappsgrid.gate.ampq.rest.errors

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.server.ResponseStatusException

/**
 *
 */

class InternalServerError extends ResponseStatusException {
    InternalServerError(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}
