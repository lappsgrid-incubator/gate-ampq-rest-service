package org.lappsgrid.gate.ampq.rest.services

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.lappsgrid.gate.ampq.rest.errors.NotFoundError
import org.lappsgrid.gate.ampq.rest.job.JobDescription
import org.lappsgrid.gate.ampq.rest.job.JobStatus
import org.lappsgrid.gate.ampq.rest.job.WorkOrder
import org.lappsgrid.test.Mock

/**
 *
 */
class ManagerServiceTests {

    Mock poMock
    Mock storageMock

    PostalService po
    StorageService storage
    ManagerService manager
    SubscriberService subsriber

    @Before
    void setup() {
        poMock = new Mock(send: { it -> println it; true })
        po = poMock.methods as PostalService
        storageMock = new Mock(save: { true }, remove: { true })
        storage = storageMock.methods as StorageService
        subsriber = Mock.Create(SubscriberService, start:{})
        manager = new ManagerService(po, storage, subsriber)
    }

    @After
    void teardown() {
        po = null
        storage = null
        subsriber = null
        poMock = storageMock = null
        manager = null
    }

    @Test
    void submitText() {
        // Override the default constructor so it doesn't attempt to connect with RabbitMQ.
//        PostalService.metaClass.constructor = { println "Mock PostalService constructor"; new PostalService(false) }
//        StorageService.metaClass.constructor = { println "Mock StorageService constructor"; new StorageService()  }

        JobDescription job = manager.submit("Hello world", "abner")
        assert job != null
        assert JobStatus.IN_PROGRESS == job.status
        assert null != job.startedAt
        assert poMock.called("send", 1)
        assert storageMock.called("save", 0)
        assert 1 == manager.jobs.size()
    }

    @Test
    void save() {
        WorkOrder order = new WorkOrder()
        JobDescription job = manager.save(order)
        assert order.id == job.id
        assert poMock.called("send", 1)
        assert manager.jobs.size() == 1
    }

    @Test
    void getFound() {
        JobDescription job = manager.submit("Hello world", "target")
        assert JobStatus.IN_PROGRESS == job.status
        assert poMock.called("send", 1)
        assert 1 == manager.jobs.size()
        JobDescription j2 = manager.get(job.id)
        assert j2 != null
        assert job.id == j2.id
    }

    @Test
    void getNotFound() {
        JobDescription job = manager.get("no such id")
        assert null == job
    }

    @Test
    void remove() {
        JobDescription j1 = manager.submit("some text", "target")
        JobDescription j2 = manager.submit("some more text", "target")
        assert j1 != null
        assert j2 != null
        assert j1.id != j2.id
        assert poMock.called("send", 2)
        assert 2 == manager.jobs.size()
        assert manager.remove(j1.id)
        assert 1 == manager.jobs.size()
        assert null == manager.get(j1.id)
        assert manager.remove(j2.id)
        assert 0 == manager.jobs.size()
        assert storageMock.called("remove", 2)
    }

    @Test(expected = NotFoundError)
    void removeInvalidId() {
        manager.remove("no such id")
    }

    @Test
    void failed() {
        String message = "something bad"
        JobDescription job = manager.submit("some text", "target")
        assert manager.failed(job.id, message)

        JobDescription j = manager.get(job.id)
        assert JobStatus.ERROR == j.status
        assert message == j.message
    }

    @Test
    void failedWithThrowable() {
        String message = "something bad"
        JobDescription job = manager.submit("some text", "target")
        assert manager.failed(job.id, new NullPointerException(message))

        JobDescription j = manager.get(job.id)
        assert JobStatus.ERROR == j.status
        assert message == j.message
    }

    @Test
    void failedInvalidIdReturnsFalse() {
        assert !manager.failed("no_such_id", "message")
    }

    @Test
    void aborted() {
        JobDescription job = manager.submit("some text", "target")
        manager.aborted(job.id)

        JobDescription j = manager.get(job.id)
        assert JobStatus.STOPPED == j.status
        assert 'Processing was interrupted. Try submitting the job again.' == j.message
    }

    @Test
    void completed() {
        JobDescription job = manager.submit("some text", "target")
        manager.complete(job.id)

        JobDescription j = manager.get(job.id)
        assert JobStatus.DONE == j.status
        assert null != j.finishedAt
        assert "/download/${job.id}" == j.resultUrl
    }
}


