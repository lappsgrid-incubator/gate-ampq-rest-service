package org.lappsgrid.gate.ampq.rest

import org.lappsgrid.rabbitmq.RabbitMQ
import org.springframework.boot.ExitCodeGenerator
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.ConfigurableApplicationContext
import picocli.CommandLine

/**
 *
 */
@SpringBootApplication
class EntryPoint {

    // Needed to close the application.
    static ConfigurableApplicationContext context
    static void main(String[] args) {

        Stub stub = new Stub()
        CommandLine cli = new CommandLine(stub)
        try {
            cli.parse(args)
        }
        catch (Exception e) {
            println e.message
            cli.usage(System.out)
            return
        }
        if (stub.showHelp) {
            cli.usage(System.out)
            return
        }
        if (stub.showVersion) {
            println()
            println "Lappsgrid AMPQ REST Frontend"
            println "Version v" + Version.version
            println()
            return
        }

//        System.setProperty("RABBIT_HOST", stub.server)
//        System.setProperty("RABBIT_USERNAME", stub.username)
//        System.setProperty("RABBIT_PASSWORD", stub.password)
        System.setProperty("log.dir", stub.logdir)
        System.setProperty("log.file", stub.logfile)
        stub.with {
            RabbitMQ.configure(server, username, password)
        }
        Context.EXCHANGE = stub.exchange
        Context.RETURN_ADDRESS = stub.returnAddress
        Context.SERVICES = stub.services

        context = SpringApplication.run(EntryPoint, [] as String[])
    }

    static void exit() {
        println "Terminating the application context."
        context.close()
        System.exit(0)
    }
}
