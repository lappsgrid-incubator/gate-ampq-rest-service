package org.lappsgrid.gate.ampq.rest.errors

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.server.ResponseStatusException

/**
 *
 */
class BadRequest extends ResponseStatusException {
    BadRequest(String message) {
        super(HttpStatus.BAD_REQUEST, message)
    }
}
