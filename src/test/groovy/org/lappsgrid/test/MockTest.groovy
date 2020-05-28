package org.lappsgrid.test

import org.junit.Ignore
import org.junit.Test
import org.lappsgrid.gate.ampq.rest.job.JobDescription
import org.lappsgrid.gate.ampq.rest.services.ManagerService
import org.lappsgrid.gate.ampq.rest.services.SubscriberService

/**
 *
 */
@Ignore
class MockTest {
    @Test
    void test() {
        Mock mock = new Mock(get: { new JobDescription()} )
        ManagerService manager = mock.methods as ManagerService
        println manager.get("fpp").id
    }
    @Test
    void test2() {
        Mock mock = new Mock(get: { new JobDescription()} )
        ManagerService manager = mock as ManagerService
        println manager.get("fpp").id
        assert 1 == mock.called("get"  )
    }

    @Test
    void create() {
        SubscriberService subscriber = Mock.Create(SubscriberService, start: {}, stop: {}, close: {})
        subscriber.start()
        subscriber.stop()
        subscriber.start()
        subscriber.close()
        assert 2 == Mock.called(SubscriberService, "start")
        assert 1 == Mock.called(SubscriberService, "stop")
        assert 1 == Mock.called(SubscriberService, "close")
    }
}
