package org.lappsgrid.gate.ampq.rest.controllers.mvc

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import org.junit.Test
import org.junit.runner.RunWith
import org.lappsgrid.gate.ampq.rest.controllers.JobController
import org.lappsgrid.gate.ampq.rest.job.JobDescription
import org.lappsgrid.gate.ampq.rest.job.JobStatus
import org.lappsgrid.gate.ampq.rest.json.Serializer
import org.lappsgrid.gate.ampq.rest.json.ZonedDateTimeDeserializer
import org.lappsgrid.gate.ampq.rest.json.ZonedDateTimeSerializer
import org.lappsgrid.gate.ampq.rest.services.ManagerService
import org.lappsgrid.test.Mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.http.MediaType
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult

import java.time.ZonedDateTime

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 *
 */
@RunWith(SpringRunner)
@AutoConfigureMockMvc
@WebMvcTest(controllers = [JobController])
class MvcJobControllerTests {
    @Autowired
    ManagerService manager

    @Autowired
    MockMvc mvc

    @Test
    void ensureTheMockBehavesAsExpected() {
        assert manager.remove("foo")
        assert !manager.remove("anything_else")
    }

    @Test
    void invalidId_NotFound() {
        mvc.perform(get("/job/invalid_id"))
            .andExpect(status().isNotFound())
            .andExpect(status().reason("There is no job with ID invalid_id"))
    }

    @Test
    void validId_returnJobDescription() {
        MvcResult result = mvc.perform(get("/job/foo").accept(MediaType.ALL))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andReturn()
        String json = result.response.contentAsString
        JobDescription description = Serializer.parse(json, JobDescription)
        assert 'foo' == description.id
        assert JobStatus.IN_QUEUE == description.status
    }

    @Test
    void deleteValidIdIsOk() {
        mvc.perform(delete("/job/foo")).andExpect(status().isOk())
    }

    @Test
    void "delete unknown id is 410 GONE"() {
        mvc.perform(delete("/job/invalid")).andExpect(status().isGone())
    }

    @TestConfiguration
    static class JobControllerTestConfiguration {
        @Bean
        @Primary
        ManagerService managerService() {
            println "Creating a mock manager service."
//            Mock service = new Mock()
//            service.mock("get") { id ->
//                if (id == 'foo') return new JobDescription('foo')
//                return null
//            }
//            service.mock("remove") { id -> id == "foo" }
//            return service.methods as ManagerService
            def methods = [
                get: { id ->
                    if (id == 'foo') return new JobDescription('foo')
                    return null
                },
                remove: { it == 'foo' }
            ]
            return Mock.Create(methods, ManagerService)
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
