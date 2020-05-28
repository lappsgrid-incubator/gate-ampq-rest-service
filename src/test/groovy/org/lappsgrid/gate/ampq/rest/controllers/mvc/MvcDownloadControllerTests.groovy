package org.lappsgrid.gate.ampq.rest.controllers.mvc

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import org.junit.Test
import org.junit.runner.RunWith
import org.lappsgrid.gate.ampq.rest.controllers.DownloadController
import org.lappsgrid.gate.ampq.rest.job.JobDescription
import org.lappsgrid.gate.ampq.rest.job.JobStatus
import org.lappsgrid.gate.ampq.rest.json.ZonedDateTimeDeserializer
import org.lappsgrid.gate.ampq.rest.json.ZonedDateTimeSerializer
import org.lappsgrid.gate.ampq.rest.services.ManagerService
import org.lappsgrid.gate.ampq.rest.services.StorageService
import org.lappsgrid.test.Mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc

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
@WebMvcTest(controllers = [DownloadController])
class MvcDownloadControllerTests {

    static boolean EXISTS = false
    static String STORAGE_GET = null
    static JobDescription MANAGER_GET = null

    @Autowired
    MockMvc mvc

    @Autowired
    DownloadController controller

    @Test
    void smokeTest() {
        assert controller != null
    }

    @Test
    void fileExists() {
        EXISTS = true
        STORAGE_GET = "Hello world"
        mvc.perform(get("/download/anything"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().string(STORAGE_GET))
    }

    @Test
    void notInStorage_NotInManager_NotFound() {
        EXISTS = false
        MANAGER_GET = null
        mvc.perform(get("/download/anything")).andExpect(status().isNotFound())
    }

    @Test
    void notInStorage_StatusDone_Gone() {
        EXISTS = false
        MANAGER_GET = new JobDescription()
        MANAGER_GET.status = JobStatus.DONE

        mvc.perform(get("/download/anything")).andExpect(status().isGone())
    }

    @Test
    void notInStorage_StatusError_ISE() {
        EXISTS = false
        MANAGER_GET = new JobDescription()
        MANAGER_GET.status = JobStatus.ERROR

        mvc.perform(get("/download/anything")).andExpect(status().isInternalServerError())
    }

    @Test
    void notInStorage_StatusOther_Accepted() {
        EXISTS = false
        MANAGER_GET = new JobDescription()
        MANAGER_GET.status = JobStatus.IN_PROGRESS

        mvc.perform(get("/download/anything")).andExpect(status().isAccepted())
    }

    @Test
    void deleteExists() {
        EXISTS = true
        mvc.perform(delete("/download/anything")).andExpect(status().isOk())
    }

    @Test
    void deleteNotExists() {
        EXISTS = false
        mvc.perform(delete("/download/anything")).andExpect(status().isNotFound())
    }

    @TestConfiguration
    static class DownloadControllerTestConfiguration {
        @Bean
        StorageService storageService() {
//            Mock service = new Mock()
//            service.mock("exists") { EXISTS }
//            service.mock("get", { STORAGE_GET })
//            service.mock("remove") { true }
//            return service.methods as StorageService
            return Mock.Create(StorageService, exists: { EXISTS }, get: { STORAGE_GET }, remove: { true }, close: { true })
        }
        @Bean
        ManagerService managerService() {
            return new Mock(get: { MANAGER_GET }).methods as ManagerService
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
