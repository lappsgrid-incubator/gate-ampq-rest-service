package org.lappsgrid.gate.ampq.rest.errors

/**
 *
 */
class ServiceError extends Exception {
    ServiceError() {
    }

    ServiceError(String message) {
        super(message)
    }

    ServiceError(String message, Throwable cause) {
        super(message, cause)
    }

    ServiceError(Throwable var1) {
        super(var1)
    }

    ServiceError(String message, Throwable cause, boolean supress, boolean trace) {
        super(message, cause, supress, trace)
    }
}
