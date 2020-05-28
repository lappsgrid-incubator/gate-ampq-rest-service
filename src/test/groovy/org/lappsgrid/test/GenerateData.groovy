package org.lappsgrid.test

import gate.Gate
import org.junit.Ignore
import org.junit.Test
import org.lappsgrid.gate.ampq.rest.controllers.TestData
import org.lappsgrid.gate.ampq.rest.json.Serializer
import org.lappsgrid.pubannotation.model.Document
import org.lappsgrid.serialization.DataContainer
import org.lappsgrid.serialization.lif.Container

/**
 *
 */
@Ignore
class GenerateData {

    @Test
    void generateLif() {
        Container container = new Container()
        container.text = TestData.text
        container.language = "en"
        println new DataContainer(container).asJson()
    }

    @Test
    void generatePubAnn() {
        Document doc = new Document()
        doc.text = TestData.text
        println Serializer.toJson(doc)
    }

    @Test
    void generatGate() {
        Gate.init()
        gate.Document document = gate.Factory.newDocument("Hello world")
        println document.toXml()
    }

}
