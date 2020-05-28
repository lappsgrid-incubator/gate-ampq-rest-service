package org.lappsgrid.gate.ampq.rest.services

import org.lappsgrid.gate.ampq.rest.Context
import org.lappsgrid.rabbitmq.Message
import org.lappsgrid.rabbitmq.pubsub.Subscriber
import org.lappsgrid.rabbitmq.topic.PostOffice
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * Responds to broadcast messages.
 */
@Service
class SubscriberService {
    Logger log = LoggerFactory.getLogger(SubscriberService)
    Subscriber subscriber
    PostalService po

    SubscriberService() {

    }

    @Autowired
    SubscriberService(PostalService po) {
        this.po = po
        log.info("SubscriberService initialized")
    }

    void start() {
        log.info("Starting SubscriberService")
        subscriber = new Subscriber(Context.EXCHANGE + ".broadcast")
        subscriber.register { String message ->
            log.info("Received broadcast message: {}", message)
            String[] tokens = message.split(" ")
            String command = tokens[0]
            if (command == "ping") {
                if (tokens.length == 2) {
                    Message response = new Message().route(tokens[1])
                    response.command("pong")
                    response.body(Context.RETURN_ADDRESS)
                    po.send(response)
                }
                else {
                    log.warn("Message was not understood: {}", tokens[0])
                    log.warn("Message dropped. ")
                    //response.command("error")
                    //response.body(Context.RETURN_ADDRESS + ": I did not understand " + tokens[0])
                }
            }
            else if (command == "exit") {
                log.debug("Sending ourselves an exit message")
                Message exit = new Message().route(Context.RETURN_ADDRESS).command("exit")
                po.send(exit)
            }
            else {
                log.warn("Received an invalid broadcast message: {}", message)
            }
        }
    }

    void stop() {
        close()
    }
    void close() {
        log.info("Closing subscriber")
        subscriber.close()
    }
}
