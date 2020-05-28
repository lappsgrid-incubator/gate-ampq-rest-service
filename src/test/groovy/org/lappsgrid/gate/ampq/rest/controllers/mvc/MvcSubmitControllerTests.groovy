package org.lappsgrid.gate.ampq.rest.controllers.mvc

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.lappsgrid.gate.ampq.rest.controllers.SubmitController
import org.lappsgrid.gate.ampq.rest.job.JobDescription
import org.lappsgrid.gate.ampq.rest.json.Serializer
import org.lappsgrid.gate.ampq.rest.json.ZonedDateTimeDeserializer
import org.lappsgrid.gate.ampq.rest.json.ZonedDateTimeSerializer
import org.lappsgrid.gate.ampq.rest.services.ManagerService
import org.lappsgrid.gate.ampq.rest.services.ServiceRegistry
import org.lappsgrid.pubannotation.model.Document
import org.lappsgrid.serialization.Data
import org.lappsgrid.test.Mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult

import java.time.ZonedDateTime

import static org.hamcrest.Matchers.startsWith
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.sta

/**
 *
 */
@RunWith(SpringRunner)
@AutoConfigureMockMvc
@WebMvcTest(controllers = [SubmitController])
class MvcSubmitControllerTests {

    // Value to be returned by the mock ManagerService when a document of type TYPE is submitted.
    static JobDescription JOB = new JobDescription()
    static Class TYPE = String.class

    @Autowired
    MockMvc mvc

    @Test
    void submitText() {
        def req = post("/submit/abner")
                    .contentType(MediaType.TEXT_PLAIN)
                    .content(org.lappsgrid.gate.ampq.rest.controllers.TestData.text)
        TYPE = String
        MvcResult result = mvc.perform(req).andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(header().string("Location", startsWith("/job/")))
            .andReturn()

        // Make sure we can parse the json returned by the controller.
        Serializer.parse(result.response.contentAsString, JobDescription)
    }

    // Since the endpoints have changed this situation is not as easy to detect.
    @Ignore
    void submitTextToLifEndpoint() {
        def req = post("/submit/abner")
                .contentType(MediaType.TEXT_PLAIN)
                .content(org.lappsgrid.gate.ampq.rest.controllers.TestData.text)
        TYPE = String
        mvc.perform(req).andExpect(status().isUnsupportedMediaType())
                .andExpect(header().string("Accept", MediaType.APPLICATION_JSON_VALUE))
    }

    @Test
    void submitLif() {
        def req = post("/submit/abner")
                .contentType(MediaType.APPLICATION_JSON)
                .content(org.lappsgrid.gate.ampq.rest.controllers.TestData.lif)
        TYPE = Data
        mvc.perform(req).andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(header().string("Location", startsWith("/job/")))
    }

    @Test
    void submitPubAnn() {
        def req = post("/submit/abner/pubann")
                .contentType(MediaType.APPLICATION_JSON)
                .content(org.lappsgrid.gate.ampq.rest.controllers.TestData.pubann)
        TYPE = Document
        mvc.perform(req).andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(header().string("Location", startsWith("/job/")))
    }

    @Test
    void submitGate() {
        def req = post("/submit/abner")
                .contentType(MediaType.TEXT_XML)
                .content(org.lappsgrid.gate.ampq.rest.controllers.TestData.gate)
        TYPE = String
        mvc.perform(req).andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(header().string("Location", startsWith("/job/")))
    }

    @Test
    void submitUnknownServiceIsBadRequest() {
        def req = post("/submit/timeml")
                .contentType(MediaType.TEXT_PLAIN)
                .content(org.lappsgrid.gate.ampq.rest.controllers.TestData.text)
        TYPE = String
        mvc.perform(req).andExpect(status().isBadRequest())
                .andExpect(status().reason("There is no service named timeml"))
    }

    @TestConfiguration
    static class SubmitControllerTestConfiguration {
        @Bean
        ManagerService managerService() {
            Mock service = new Mock()
            service.mock("submit") { object, ignored ->
                if (TYPE.isInstance(object)) {
                    return JOB
                }
                throw new IllegalArgumentException("Invalid type was submitted")
            }
            service.mock("submitXml") { object, ignored ->
                if (TYPE.isInstance(object)) {
                    return JOB
                }
                throw new IllegalArgumentException("Invalid type was submitted")
            }
            return service.methods as ManagerService
        }
        @Bean
        ServiceRegistry services() {
            Mock service = new Mock()
            service.mock("get") { it == 'abner' ? 'abner' : null }
            service.mock("add") { true }
            return service.methods as ServiceRegistry
        }
        @Bean
        ObjectMapper jsonObjectMapper() {
            ObjectMapper mapper = new ObjectMapper()
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
            SimpleModule mod = new SimpleModule()
            mod.addSerializer(ZonedDateTime, new ZonedDateTimeSerializer())
            mod.addDeserializer(ZonedDateTime, new ZonedDateTimeDeserializer())
            mapper.registerModule(mod)
//        Jackson2ObjectMapperBuilder.json().modules(mod).serializationInclusion()
            return mapper
        }
    }
}
