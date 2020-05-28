package org.lappsgrid.gate.ampq.rest.util

import gate.Gate
import groovy.util.logging.Slf4j
import org.anc.lapps.gate.serialization.GateSerializer
import org.lappsgrid.gate.ampq.rest.job.WorkOrder
import org.lappsgrid.gate.ampq.rest.services.ManagerService
import org.lappsgrid.gate.ampq.rest.services.PostalService
import org.lappsgrid.gate.ampq.rest.services.StorageService
import org.lappsgrid.pubannotation.model.Denotation
import org.lappsgrid.pubannotation.model.Document
import org.lappsgrid.pubannotation.model.Span
import org.lappsgrid.rabbitmq.Message
import org.lappsgrid.rabbitmq.topic.MessageBox
import org.lappsgrid.serialization.DataContainer
import org.lappsgrid.serialization.Serializer
import org.lappsgrid.serialization.lif.Container
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import static org.lappsgrid.gate.ampq.rest.Context.*
/**
 *
 */
class MailBox extends MessageBox {

//    public static final String RETURN_ADDRESS = "timeml.rest.service"

    Logger logger = LoggerFactory.getLogger(MailBox)

    StorageService storage
    ManagerService manager
    PostalService po

    MailBox(ManagerService manager, StorageService storage, PostalService po) {
        super('services', RETURN_ADDRESS)
        this.storage = storage
        this.manager = manager
        this.po = po
        if (this.po == null) {
            throw new NullPointerException("NULL PostalService was injected.")
        }
        if (!Gate.isInitialised()) {
            Gate.init()
        }
    }

    @Override
    void recv(Message message) {
        if ('exit' == message.command) {
            logger.info "Received an exit message."
            if (message.route.size() > 0) {
                // Only send the ACK if we have somewhere to send it.
                message.command = 'ok'
                po.send(message)
                sleep(500) { true }
            }
            manager.exit()
            return
        }
        if ('ping' == message.command) {
            logger.info("Was pinged by {}", message.route[0])
            message.command = 'pong'
            po.send(message)
            return
        }

        String orderId = message.get("order_id")
        if (orderId == null) {
            logger.warn("Received message {} with no order ID", message.id)
            return
        }
        WorkOrder order = manager.jobs[orderId]
        if (order == null) {
            logger.warn("Received a message for work order {} but the order was not found.", order.id)
            return
        }

        if (message.command == 'ok') {
            String xml = message.body.toString()
            gate.Document document = gate.Factory.newDocument(xml)
            if (order.format == 'gate') {
                storage.add(order.id, xml)
            }
            else {
                String json
                if (order.format == 'text') {
                    json = convertToLif(null, document)
                }
                else if (order.format == 'lif') {
                    DataContainer data = (DataContainer) order.original
                    json = convertToLif(data, document)
                }
                else if (order.format == 'pubann') {
                    json = convertToPubann((Document) order.original, xml)
                }
                else {
                    logger.error("Received a message with an invalid format.")
                    logger.error("Message id: {}", message.id)
                    logger.error("Message cmd: {}", message.command)
                    // Just write the Gate/XML and hope the caller can deal with it.
                    json = xml
                }
                if (json != null) {
                    logger.debug("Stored order {}", order.id)
                    storage.add(order.id, json)
                    manager.complete(order.id)
                }
            }
        }
        else if (message.command == 'error') {
            manager.failed(order.id, message.command)
        }
        else {
            logger.warn("Received an invalid message: {}", message.command)
            manager.failed(message.id, "Invalid message from the timeml-ampq service.")
//            logger.warn("command: {}", message.command)
//            logger.warn(Serializer.toPrettyJson(message))
        }
    }

    String convertToLif(DataContainer original, gate.Document document) {
        if (original == null) {
            Container container = new Container()
            container.text = document.content.getContent(0, document.content.size())
            original = new DataContainer(container)
        }
        Container container = original.payload
        GateSerializer.addToContainer(container, document)
        return original.asJson()
    }

    String convertToPubann(Document document, String xml) {
        List<Denotation> result = []
        def root = new XmlParser().parseText(xml)
        IDGenerator ids = new IDGenerator()

        logger.trace("Processing annotation sets")
        root.AnnotationSet.Annotation.each { a ->
            String type = a.@Type
            logger.trace("Processing annotation type {}", type)
            int start = a.@StartNode as int
            int end = a.@EndNode as int
            Span span = new Span(start, end)
            if (type == 'Token') {
                def cat = a.Feature.find { it.Name[0].value()[0] == 'category' }.Value[0].value()[0]
                result.add new Denotation(ids.get('tok'), span, cat)
            }
            else if (type == 'Lookup') {
                def majorType = a.Feature.find { it.Name[0].value()[0] == 'majorType' }.Value[0].value()[0]
                result.add new Denotation(ids.get('lookup'), span, majorType)
            }
            else if (type == 'EVENT') {
                def eventType = a.Feature.find { it.Name[0].value()[0] == 'class' }.Value[0].value()[0]
                result.add new Denotation(ids.get('event'), span, eventType)
            }
            else {
                result.add(new Denotation(ids.get('ann'), span, type))
            }
        }
        if (document.denotations == null) {
            document.denotations = []
        }
        document.denotations.addAll(result)
        return Serializer.toJson(document)
    }


}
