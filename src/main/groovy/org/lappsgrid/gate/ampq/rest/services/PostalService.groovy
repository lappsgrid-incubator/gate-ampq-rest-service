package org.lappsgrid.gate.ampq.rest.services

import groovy.util.logging.Slf4j
import org.lappsgrid.gate.ampq.rest.Context
import org.lappsgrid.gate.ampq.rest.job.WorkOrder
import org.lappsgrid.rabbitmq.Message
import org.lappsgrid.rabbitmq.topic.PostOffice
import org.springframework.stereotype.Service

import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * The PostalService manages the connection to the RabbitMQ server and
 * is responsible for sending all messages.  Connections and channels are
 * costly to open and channels are not thread safe so a singleton service is use
 * to manage access.
 */
@Slf4j
@Service
class PostalService  {

//    public static final String EVENTS_ADDRESS = Context.RETURN_ADDRESS

    /** Sends message to the RabbitMQ server. */
    PostOffice po
    /** Manages the idle timer thread. */
    ExecutorService executor
    /** The time the postal service was last used. */
    Instant timestamp
    /**
     * Connects to the RabbitMQ server.  The initial closure does a lazy initialization of the
     * ExecutorService and replaces itself with a closure that simply checks and reopens the
     * PostOffice if needed.
     */
    Closure connect

    PostalService() {
        timestamp = Instant.now()
        connect = {
            log.info("Initializing to send first message.")
            po = new PostOffice(Context.EXCHANGE)
            executor = Executors.newScheduledThreadPool(1)
            executor.scheduleAtFixedRate(new IdleTimer(), 5, 5, TimeUnit.MINUTES)
            connect = {
                if (po == null) {
                    log.info("Reconnecting to the PostOffice")
                    po = new PostOffice(Context.EXCHANGE)
                }
            }
        }
    }

    void send(WorkOrder order) {
        log.debug("Sending work order {} to {}", order.id, order.target)
        connect()
        String format = order.format == 'gate' ? 'gate' : 'text'
        Message message = new Message()
                .command('submit')
                .body(order.text)
                .set("order_id", order.id)
                .set('format', format)
                .route(order.target)
                .route(Context.RETURN_ADDRESS)
        po.send(message)
        timestamp = Instant.now()
    }

    void send(Message message) {
        connect()
        po.send(message)
    }

    void close() {
        executor.shutdown()
        executor.awaitTermination(2, TimeUnit.SECONDS)
        if (!executor.isShutdown()) {
            executor.shutdownNow()
        }
        po.close()
        log.info "Postal service terminated."
    }

    class IdleTimer implements Runnable {
        void run() {
            if (po == null) {
                return
            }
            Instant cutoff = Instant.now().minus(5, ChronoUnit.MINUTES)
            if (timestamp.isBefore(cutoff)) {
                log.info("Closing the post office due to idle timeout.")
                po.close()
                po = null
            }
        }
    }
}
