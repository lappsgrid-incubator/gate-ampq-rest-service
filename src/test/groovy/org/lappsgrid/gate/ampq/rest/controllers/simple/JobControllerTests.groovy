package org.lappsgrid.gate.ampq.rest.controllers.simple


import org.junit.Test
import org.lappsgrid.gate.ampq.rest.controllers.JobController
import org.lappsgrid.gate.ampq.rest.errors.GoneError
import org.lappsgrid.gate.ampq.rest.errors.NotFoundError
import org.lappsgrid.gate.ampq.rest.job.JobDescription
import org.lappsgrid.gate.ampq.rest.job.JobStatus
import org.lappsgrid.gate.ampq.rest.services.ManagerService
import org.lappsgrid.gate.ampq.rest.services.PostalService
import org.lappsgrid.gate.ampq.rest.services.StorageService
import org.lappsgrid.gate.ampq.rest.services.SubscriberService
import org.lappsgrid.test.Mock

import static org.springframework.test.util.AssertionErrors.fail

/**
 * Test the JobController without Spring
 */
class JobControllerTests {

    @Test
    void getJobById() {
//        Mock service = new Mock()
//        service.mock("get") { id ->
//            if (id.toString() == 'foo') return new JobDescription('foo')
//            return null
//        }
//        ManagerService manager = service.methods as ManagerService
        def get = { String id ->
            println "Getting JobDescription for $id"
            if (id == 'foo') return new JobDescription('foo')
            return null
        }
        ManagerService manager = Mock.Create(ManagerService, get:get)
        JobController controller = new JobController(manager)

        JobDescription job = controller.getJobStatus('foo')
        assert 'foo' == job.id
        assert JobStatus.IN_QUEUE == job.status
        assert 1 == Mock.called(ManagerService, "get")
    }

    @Test(expected = NotFoundError)
    void getUnknownJob() {
        Mock service = new Mock(get: { id -> null })
        ManagerService manager = service.methods as ManagerService
        JobController controller = new JobController(manager)
        controller.getJobStatus("no such id")
        fail "The call to controller.getJobStatus should have thrown an exception"
    }

    @Test(expected = NotFoundError)
    void unknownIdReturns404() {
        Mock service = new Mock([get: { null }])
        ManagerService manager = service.methods as ManagerService
        JobController controller = new JobController(manager)
        controller.getJobStatus('foo')
        fail "The call to controller.getJobStatus should have thrown an exception"
    }

    @Test
    void deleteExistingJob() {
        StorageService storage = Mock.Create(StorageService, save: { true }, remove: { true })
        PostalService po = Mock.Create(PostalService, send: { true })
        SubscriberService sub = Mock.Create(SubscriberService, start: {})

        ManagerService manager = new ManagerService(po, storage, sub)
        JobController controller = new JobController(manager)

        assert 0 == Mock.called(PostalService, "send")
        // Add a job so we can delete it.
        JobDescription job = manager.submit("some text", "abner")
        assert 1 == manager.jobs.size()
        assert 1 == Mock.called(PostalService, "send")
        assert 0 == Mock.called(StorageService, "remove")
        String response = controller.deleteJob(job.id)
        assert null != response
        assert 0 == manager.jobs.size()

        assert 1 == Mock.called(SubscriberService, "start")
        assert 1 == Mock.called(StorageService, "remove")
    }

    @Test(expected = GoneError)
    void deleteJobThatDoesntExistIs410Gone() {
        // Removing an item will fail (return false)
        Mock storageMock = new Mock(save: { true }, remove: { false })
        Mock poMock = new Mock(send: { true })
        Mock subscriberMock = new Mock(start: {}, stop: {}, close: {})
        SubscriberService subscriber = subscriberMock as SubscriberService
        StorageService storage = storageMock.methods as StorageService
        PostalService po = poMock.methods as PostalService
        ManagerService manager = new ManagerService(po, storage, subscriber)
        JobController controller = new JobController(manager)

        // Add a job. If the manager has the job recorded but it can't be
        // removed then we assume it is 410 GONE/
        JobDescription job = manager.submit("some text", "abner")
        assert 1 == manager.jobs.size()
        controller.deleteJob(job.id)
        fail "The call to controller.deleteJob should have thrown an exception"
    }

    @Test(expected = NotFoundError)
    void deleteNonExistentJob() {
        Mock storageMock = new Mock(save: { true }, remove: { true })
        Mock poMock = new Mock(send: { true })

        SubscriberService subscriber = Mock.Create(SubscriberService, start:{})
        StorageService storage = storageMock.methods as StorageService
        PostalService po = poMock.methods as PostalService
        ManagerService manager = new ManagerService(po, storage, subscriber)
        JobController controller = new JobController(manager)

        controller.deleteJob('no such it')
        fail "The call to controller.deleteJob() should have thrown an exception"
    }

}

