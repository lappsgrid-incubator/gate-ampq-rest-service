package org.lappsgrid.gate.ampq.rest.controllers.simple

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.lappsgrid.gate.ampq.rest.controllers.SubmitController
import org.lappsgrid.gate.ampq.rest.controllers.TestData
import org.lappsgrid.gate.ampq.rest.job.JobDescription
import org.lappsgrid.gate.ampq.rest.job.JobStatus
import org.lappsgrid.gate.ampq.rest.services.ManagerService
import org.lappsgrid.gate.ampq.rest.services.PostalService
import org.lappsgrid.gate.ampq.rest.services.ServiceRegistry
import org.lappsgrid.gate.ampq.rest.services.StorageService
import org.lappsgrid.gate.ampq.rest.services.SubscriberService
import org.lappsgrid.test.Mock

import javax.servlet.http.HttpServletResponse

/**
 *
 */
class SubmitControllerTests {

    Mock sm // mock of the StorageService
    Mock pm // mock of the PostalService
    Mock rm // mock of the HttpServletResponse
    Mock ss

    // Header to be set
    String headerName = null
    String headerValue = null

    ManagerService manager
    SubmitController controller
    SubscriberService subscriber

    HttpServletResponse response

    @Before
    void setup() {
        sm = new Mock(save: { true })  // StorageManager
        pm = new Mock(send: { true })  // PostalService
        ss = new Mock(add: { true }, get: {'abner' == it ? it : null}) // ServicesService
        rm = new Mock(setHeader: { String k, String v -> headerName=k; headerValue=v})
        StorageService storage = sm.methods as StorageService
        PostalService po = pm.methods as PostalService
        ServiceRegistry services = ss.methods as ServiceRegistry
        response = rm.methods as HttpServletResponse

        subscriber = Mock.Create(SubscriberService, start:{})
        manager = new ManagerService(po, storage, subscriber)
        controller = new SubmitController(manager, services)
    }

    @After
    void teardown() {
        subscriber = null
        manager = null
        controller = null
    }

    @Test
    void submitText() {
        JobDescription job = controller.postText('abner', TestData.text, response)
        assert null != job
        assert JobStatus.IN_PROGRESS == job.status
        assert rm.called("setHeader", 1)
        assert pm.called("send", 1)
        assert "Location" == headerName
        assert "/job/${job.id}" == headerValue
    }

    @Test
    void submitLif() {
        JobDescription job = controller.postLif('abner', TestData.lif, response)
        assert null != job
        assert JobStatus.IN_PROGRESS == job.status
        assert rm.called("setHeader", 1)
        assert pm.called("send", 1)
        assert "Location" == headerName
        assert "/job/${job.id}" == headerValue
    }

    @Test
    void submitPubAnn() {
        JobDescription job = controller.postPubann('abner', TestData.pubann, response)
        assert null != job
        assert JobStatus.IN_PROGRESS == job.status
        assert rm.called("setHeader", 1)
        assert pm.called("send", 1)
        assert "Location" == headerName
        assert "/job/${job.id}" == headerValue
    }

    @Test
    void submitGate() {
        JobDescription job = controller.postGateXml('abner', TestData.gate, response)
        assert null != job
        assert JobStatus.IN_PROGRESS == job.status
        assert rm.called("setHeader", 1)
        assert pm.called("send", 1)
        assert "Location" == headerName
        assert "/job/${job.id}" == headerValue
    }
}
