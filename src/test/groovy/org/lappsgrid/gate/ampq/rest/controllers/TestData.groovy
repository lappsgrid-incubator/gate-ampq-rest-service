package org.lappsgrid.gate.ampq.rest.controllers

/**
 *
 */
class TestData {

    static final String lif = '{"discriminator":"http://vocab.lappsgrid.org/ns/media/jsonld#lif","payload":{"@context":"http://vocab.lappsgrid.org/context-1.0.0.jsonld","metadata":{},"text":{"@value":"Hello world","@language":"en"},"views":[]},"parameters":{}}'
    static final String text = "Hello world."
    static final String pubann = '{"text":"Hello world."}'
    static final String gate = '''<GateDocument version="3">
<!-- The document's features-->

<GateDocumentFeatures>
<Feature>
  <Name className="java.lang.String">gate.SourceURL</Name>
  <Value className="java.lang.String">created from String</Value>
</Feature>
</GateDocumentFeatures>
<!-- The document content area with serialized nodes -->

<TextWithNodes>Hello world</TextWithNodes>
<!-- The default annotation set -->

<AnnotationSet>
</AnnotationSet>

</GateDocument>
'''
}
