package org.lappsgrid.gate.ampq.rest

import picocli.CommandLine.Command
import picocli.CommandLine.Option

/**
 * Used for command line parsing.
 */
@Command(name = "java -jar service.jar", description = "%nStart a REST API for a GATE/AMPQ service", sortOptions = false)
class Stub {
    @Option(names=["-x", "--exchange"], description = "RabbitMQ exchange to use", defaultValue = "services")
    String exchange
    @Option(names=["-m","--mailbox"], description = "the name of our mailbox", required = true)
    String returnAddress
    @Option(names=["-s","--services"], arity = "0..*", description = "AMPQ services being exposed via this REST service")
    Map<String,String> services

    @Option(names=["-r","--rabbit"], description = "address of the RabbitMQ server", defaultValue = "localhost")
    String server
    @Option(names=["-u","--username"], description = "username for the RabbitMQ server", defaultValue = "guest")
    String username
    @Option(names=["-p","--password"], description = "password for the user", defaultValue = "guest")
    String password

    @Option(names=["-l", "--logdir"], description = "directory used for log files", defaultValue = ".")
    String logdir
    @Option(names=["-f", "--logfile"], description = "log file name (without .log extension)", defaultValue = "ampq-rest-service")
    String logfile

    @Option(names=["-h", "--help"], description = "show this help and exit.", usageHelp = true)
    boolean showHelp
    @Option(names=["-v", "--version"], description = "show version information and exit", versionHelp = true)
    boolean showVersion


}
