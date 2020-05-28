package org.lappsgrid.gate.ampq.rest.services

import org.lappsgrid.pubannotation.model.Track
import org.lappsgrid.serialization.lif.Annotation
import org.lappsgrid.serialization.lif.View

/**
 * An AnnotationFactory converts one of the Pubannotation denotation, relation, or modification
 * objects into a LIF Annotation object.
 */
interface AnnotationFactory {
    Annotation create(View view, Track track, org.lappsgrid.pubannotation.model.Annotation a)
}

