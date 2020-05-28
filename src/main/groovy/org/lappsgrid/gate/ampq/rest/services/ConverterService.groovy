package org.lappsgrid.gate.ampq.rest.services

import org.lappsgrid.discriminator.Discriminators
import org.lappsgrid.pubannotation.model.*
import org.lappsgrid.serialization.lif.Container
import org.lappsgrid.serialization.lif.View

/**
 *
 */
class ConverterService {
    List types
    Container container

    Document convert(Container container) {
        Document doc = new Document()
        doc.text = container.text
    }

    Container convert(Document document) {
        AnnotationFactory denotation2Annotation = { View view, Track track, Annotation annotation ->
            Denotation denotation = (Denotation) annotation
            String type = track.expand(denotation.obj)
            long start = denotation.span.begin
            long end = denotation.span.end
            org.lappsgrid.serialization.lif.Annotation a = view.newAnnotation(denotation.id, type, start, end)
            a.label = denotation.obj
            return a
        }
        AnnotationFactory relation2Annotation = { View view, Track track, Annotation annotation ->
            Relation relation = (Relation) annotation
            org.lappsgrid.serialization.lif.Annotation a = view.newAnnotation(relation.id, Discriminators.Uri.RELATION)
            a.label = relation.pred
            a.features.subj = relation.subj
            a.features.obj = relation.obj
            a.features.pred = relation.pred
            return a
        }
        AnnotationFactory modification2Annotation = { View view, Track track, Annotation annotation ->
            Modification mod = (Modification) annotation
            org.lappsgrid.serialization.lif.Annotation a = view.newAnnotation(mod.id, 'http://vocab.lappsgrid.org/pubannotation/modification')
            a.label = mod.pred
            a.features.obj = mod.obj
            a.features.pred = mod.pred
            return a
        }
        container = new Container()
        container.text = document.text
        container.metadata.sourceid = document.id
        container.metadata.sourcedb = document.db

        document.tracks.each { track ->
            types = []
            addAnnotations('denotations', track, denotation2Annotation)
            addAnnotations('relations', track, relation2Annotation)
            addAnnotations('modifications', track, modification2Annotation)
            Map metadata = container.metadata
            if (metadata.tracks == null) {
                metadata.tracks = [:]
            }
            Map info = [:]
            info.types = types
            if (track.namespaces) {
                info.namespaces = track.namespaces
            }
            metadata.tracks[track.project] = info
//            metadata.tracks[track.project] = types
//            if (track.namespaces) {
//                if (metadata.namespaces == null) {
//                    metadata.namespaces = [:]
//                }
//                metadata.namespaces[track.project] = track.namespaces
//            }
        }
        return container
    }

    private void addAnnotations(String name, Track track, AnnotationFactory factory) {
        List list = (List) track.getProperty(name)
        if (list) {
            types.add(name)
            View view = container.newView(track.project + '.' + name)
            view.addContains(name, track.project, name)
            list.each { factory.create(view, track, it) }
        }

    }
}
