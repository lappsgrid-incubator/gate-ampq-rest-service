package org.lappsgrid.gate.ampq.rest.controllers.mvc

import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.lappsgrid.gate.ampq.rest.Context
import org.lappsgrid.gate.ampq.rest.controllers.AdminController
import org.lappsgrid.gate.ampq.rest.services.ServiceRegistry
import org.lappsgrid.serialization.Serializer
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

import static junit.framework.Assert.assertNotNull
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 *
 */
@RunWith(SpringRunner)
@AutoConfigureMockMvc
@WebMvcTest(controllers = [AdminController])
class MvcAdminControllerTests {

    @Autowired
    MockMvc mvc

    @After
    void teardown() {
        Context.SERVICES = null
    }

    @Test
    void testList() {
        Context.SERVICES = [ abner: "abner", timeml:"timeml", heidel:"heideltime"]
        MvcResult result = mvc.perform(get("/admin/services"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
            .andReturn()
        String json = result.response.contentAsString
        Map map = Serializer.parse(json, HashMap)
        assert Context.SERVICES == map
    }

    @Test
    void testAdd() {
        Context.SERVICES = [:]
        MvcResult result = mvc.perform(post("/admin/services", ).param("url", "foo").param("mailbox", "bar"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
            .andReturn()

        assert 1 == Context.SERVICES.size()
        assert "bar" == Context.SERVICES.foo
    }

    @Test
    void testAddOneParameter() {
        Context.SERVICES = [:]
        MvcResult result = mvc.perform(post("/admin/services", ).param("url", "foo"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andReturn()

        assert 1 == Context.SERVICES.size()
        assert "foo" == Context.SERVICES.foo
    }

    @Test
    void addingDupicateisBadRequest() {
        Context.SERVICES = [foo:"bar"]
        mvc.perform(post("/admin/services").param("url","foo").param("mailbox", "foobar"))
            .andExpect(status().isBadRequest())
            .andExpect(status().reason("There is already a mapping from foo to bar"))
    }

    @Test
    void replaceExisting() {
        Context.SERVICES = [foo:"bar"]
        mvc.perform(put("/admin/services").param("url","foo").param("mailbox", "foobar"))
                .andExpect(status().isOk())
        assert "foobar" == Context.SERVICES.foo
    }

    @Test
    void deleteExistingIsOk() {
        Context.SERVICES = [foo:"bar"]
        mvc.perform(delete("/admin/services").param("url", "foo"))
                .andExpect(status().isOk())
        assert 0 == Context.SERVICES.size()
    }

    @Test
    void deleteNonExistingIsNotFound() {
        Context.SERVICES = [:]
        mvc.perform(delete("/admin/services").param("url", "foo"))
                .andExpect(status().isNotFound())
    }

    @TestConfiguration
    static class AdminControllerTestConfiguration {

        @Bean
        ServiceRegistry serviceRegistry() {
//            return Mock.Create(ServiceRegistry, get: { it })
            return new ServiceRegistry()
        }
    }
}
