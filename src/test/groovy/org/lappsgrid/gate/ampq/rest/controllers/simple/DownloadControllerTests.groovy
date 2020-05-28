package org.lappsgrid.gate.ampq.rest.controllers.simple

import org.junit.Test
import org.lappsgrid.gate.ampq.rest.controllers.DownloadController
import org.lappsgrid.gate.ampq.rest.job.JobDescription
import org.lappsgrid.gate.ampq.rest.job.JobStatus
import org.lappsgrid.gate.ampq.rest.services.ManagerService
import org.lappsgrid.gate.ampq.rest.services.StorageService
import org.lappsgrid.test.Mock
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

/**
 *
 */
class DownloadControllerTests {

    @Test
    void foundInStorage_Ok() {
        // storage.get(id) manager.get(id)
        Mock storageMock = new Mock(get:{ "Hello world" })
        StorageService storage = storageMock.methods as StorageService
        Mock managerMock = new Mock(get: { null })
        ManagerService manager = managerMock.methods as ManagerService
        DownloadController controller = new DownloadController(manager, storage)

        ResponseEntity response = controller.download("anything")
        assert HttpStatus.OK == response.statusCode
        assert "Hello world" == response.body.toString()
        assert storageMock.called("get", 1)
        assert managerMock.called("get", 0)
    }

    @Test
    void notInStorage_NotInManager_NotFound() {
        Mock storageMock = new Mock(get:{ null })
        StorageService storage = storageMock.methods as StorageService
        Mock managerMock = new Mock(get: { null })
        ManagerService manager = managerMock.methods as ManagerService
        DownloadController controller = new DownloadController(manager, storage)

        ResponseEntity response = controller.download("anything")
        assert HttpStatus.NOT_FOUND == response.statusCode
        assert storageMock.called("get", 1)
        assert managerMock.called("get", 1)
    }

    @Test
    void notInStorage_StatusDone_Gone() {
        Mock storageMock = new Mock(get:{ null })
        StorageService storage = storageMock.methods as StorageService
        JobDescription job = new JobDescription()
        job.status = JobStatus.DONE
        Mock managerMock = new Mock(get: { job })
        ManagerService manager = managerMock.methods as ManagerService
        DownloadController controller = new DownloadController(manager, storage)

        ResponseEntity response = controller.download("anything")
        assert HttpStatus.GONE == response.statusCode
        assert storageMock.called("get", 1)
        assert managerMock.called("get", 1)
    }

    @Test
    void notInStorage_StatusError_ISE() {
        Mock storageMock = new Mock(get:{ null })
        StorageService storage = storageMock.methods as StorageService
        JobDescription job = new JobDescription()
        job.status = JobStatus.ERROR
        Mock managerMock = new Mock(get: { job })
        ManagerService manager = managerMock.methods as ManagerService
        DownloadController controller = new DownloadController(manager, storage)

        ResponseEntity response = controller.download("anything")
        assert HttpStatus.INTERNAL_SERVER_ERROR == response.statusCode
        assert storageMock.called("get", 1)
        assert managerMock.called("get", 1)
    }

    @Test
    void notInStorage_StatusOther_Accepted() {
        Mock storageMock = new Mock(get:{ null })
        StorageService storage = storageMock.methods as StorageService
        JobDescription job = new JobDescription()
        job.status = JobStatus.IN_PROGRESS
        Mock managerMock = new Mock(get: { job })
        ManagerService manager = managerMock.methods as ManagerService
        DownloadController controller = new DownloadController(manager, storage)

        ResponseEntity response = controller.download("anything")
        assert HttpStatus.ACCEPTED == response.statusCode
        assert storageMock.called("get", 1)
        assert managerMock.called("get", 1)
    }

    @Test
    void deleteExists() {
        Mock storageMock = new Mock(exists:{ true }, remove: { true })
        StorageService storage = storageMock.methods as StorageService
        Mock managerMock = new Mock(get: { null })
        ManagerService manager = managerMock.methods as ManagerService
        DownloadController controller = new DownloadController(manager, storage)

        ResponseEntity response = controller.deleteDownload("anything")
        assert HttpStatus.OK == response.statusCode
        assert storageMock.called("exists", 1)
        assert storageMock.called("remove", 1)
    }

    @Test
    void deleteNotExists() {
        Mock storageMock = new Mock(exists:{ false }, remove: { true })
        StorageService storage = storageMock.methods as StorageService
        Mock managerMock = new Mock(get: { null })
        ManagerService manager = managerMock.methods as ManagerService
        DownloadController controller = new DownloadController(manager, storage)

        ResponseEntity response = controller.deleteDownload("anything")
        assert HttpStatus.NOT_FOUND == response.statusCode
        assert storageMock.called("exists", 1)
        assert storageMock.called("remove", 0)
    }
}
