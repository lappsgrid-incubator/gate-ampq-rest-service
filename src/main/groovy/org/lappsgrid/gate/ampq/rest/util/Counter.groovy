package org.lappsgrid.gate.ampq.rest.util

/**
 * A simple counter used to generate unique ID values.
 */
class Counter {
    int count = 0

    Counter next() {
        ++count
        return this
    }

    String toString() {
        return count.toString()
    }
}
