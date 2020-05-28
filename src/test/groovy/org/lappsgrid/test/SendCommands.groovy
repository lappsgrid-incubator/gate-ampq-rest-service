package org.lappsgrid.test

import org.junit.Test
import org.lappsgrid.rabbitmq.Message
import org.lappsgrid.rabbitmq.topic.MessageBox
import org.lappsgrid.rabbitmq.topic.PostOffice

import java.util.concurrent.CountDownLatch

/**
 *
 */
class SendCommands {

    @Test
    void pingAmpqService() {
        File file = new File("/var/corpora/covid/CORD-19_All_docs/txt/CORD-19-fffdca958d2869303da6e8e5e4b181e5de8f3854.txt")
        System.setProperty("RABBIT_HOST", "localhost")
        System.setProperty("RABBIT_USERNAME", "guest")
        System.setProperty("RABBIT_PASSWORD", "guest")
        PostOffice po = new PostOffice("services")
        Message ping = new Message()
                .command("submit")
                .body(file.text)
                .set("format", "text")
                .route("abner.tagger")
                .route("send.command")

        CountDownLatch startSignal = new CountDownLatch(1)
        CountDownLatch exitSignal = new CountDownLatch(1)
        MessageBox box = new MessageBox("services", "send.command") {
            void recv(Message message) {
                println message.command
                println message.body
                exitSignal.countDown()
            }
        }
        po.send(ping)
        exitSignal.await()
    }

    @Test
    void exitAmpqService() {
        File file = new File("/var/corpora/covid/CORD-19_All_docs/txt/CORD-19-fffdca958d2869303da6e8e5e4b181e5de8f3854.txt")
        System.setProperty("RABBIT_HOST", "localhost")
        System.setProperty("RABBIT_USERNAME", "guest")
        System.setProperty("RABBIT_PASSWORD", "guest")
        PostOffice po = new PostOffice("services")
        Message ping = new Message()
                .command("exit")
                .route("abner.tagger")
                .route("send.command")

        CountDownLatch startSignal = new CountDownLatch(1)
        CountDownLatch exitSignal = new CountDownLatch(1)
        MessageBox box = new MessageBox("services", "send.command") {
            void recv(Message message) {
                println message.command
                println message.body
                exitSignal.countDown()
            }
        }
        po.send(ping)
        exitSignal.await()
    }
}
