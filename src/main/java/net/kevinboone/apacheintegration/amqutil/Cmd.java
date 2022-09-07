/*==========================================================================
amqutil
Cmd.java
(c)2015 Kevin Boone
Distributed under the terms of the GPL v2.0
==========================================================================*/

package net.kevinboone.apacheintegration.amqutil;

import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import javax.jms.ConnectionFactory;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * Base class extended by all amqutil command-line commands
 */
public abstract class Cmd {

    private static final Logger log = LogManager.getLogger(Cmd.class);

    protected Options options = null;
    protected CommandLine cl = null;

    public static String DEFAULT_HOST = "localhost";
    public static int DEFAULT_PORT = 61616;
    public static String DEFAULT_USER = "admin";
    public static String DEFAULT_PASS = "admin";
    public static String DEFAULT_DESTINATION = "__test_destination";

    public Cmd() {
        options = new Options();
    }

    /**
     * Set the command-line options that all commands will support. Specific
     * commands will usually have to override this and add others.
     */
    public void setupOptions() {
        options.addOption(null, "time", false, "show time to complete operation in msec");
        options.addOption(null, "help", false, "show brief help");
        options.addOption(null, "loglevel", true, "set log level -- error, info, etc");
        options.addOption("q", "qpid", false, "use AMQP protocol");
        options.addOption("a", "artemis", false, "use Artemis protocol");
    }

    public abstract int run() throws Exception;

    /**
     * doRun wraps the (abstract) run() method in timing and logging options
     */
    public int doRun() throws Exception {
        if (cl.hasOption("help")) {
            briefHelp(System.out);
            return 0;
        }

        if (cl.hasOption("qpid")) {
            System.setProperty("amqutil.driver", "qpid");
        }

        if (cl.hasOption("artemis")) {
            System.setProperty("amqutil.driver", "artemis");
        }

        boolean time = false;
        long start = 0;
        String _logLevel = cl.getOptionValue("loglevel");
        if (_logLevel != null)
            System.setProperty("log.level", _logLevel);

        if (cl.hasOption("time"))
            time = true;
        if (time)
            start = System.currentTimeMillis();
        int ret = run();
        if (time) {
            System.out.println("Time elapsed: " + (System.currentTimeMillis() - start) + " msec");
        }
        return ret;
    }

    public void parseArgs(String[] args)
            throws ArgParseException {
        CommandLineParser clp = new GnuParser();
        try {
            cl = clp.parse(options, args);
        } catch (Exception e) {
            throw new ArgParseException(e);
        }
        if (cl.hasOption("qpid") && cl.hasOption("artemis")) {
            throw new ArgParseException("--qpid and --artemis cannot be used together");
        }
    }

    public abstract String getName();

    public abstract String getShortDescription();

    public abstract String getShortUsage();

    public void showOptions() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("amqutil " + getName(), options);
    }

    public void briefHelp(PrintStream p) {
        p.println(getShortUsage());
        p.println(getShortDescription());
        showOptions();
    }

    /**
     * Gets the Qpid Connection factory from either host/port or URL.
     * URL takes precedence.
     */
    org.apache.qpid.jms.JmsConnectionFactory getQpidFactory(String host, int port, String url) {
        org.apache.qpid.jms.JmsConnectionFactory factory = null;
        if (url != null && url.length() != 0) {
            factory = new org.apache.qpid.jms.JmsConnectionFactory(url);
            if (!host.equals("localhost") || port != 61616) {
                log.warn("Ignoring host/port arguments as a URL was specified");
            }
        } else {
            factory = new org.apache.qpid.jms.JmsConnectionFactory
                    ("amqp://" + host + ":" + port);
        }
        return factory;
    }

    /**
     * Gets the ActiveMQ Connection factory from either host/port or URL.
     * URL takes precedence.
     */
    org.apache.activemq.ActiveMQConnectionFactory getActiveMQFactory(String host, int port, String url) {
        org.apache.activemq.ActiveMQConnectionFactory factory = null;
        if (url != null && url.length() != 0) {
            factory = new org.apache.activemq.ActiveMQConnectionFactory(url);
            if (!host.equals("localhost") || port != 61616) {
                log.warn("Ignoring host/port arguments as a URL was specified");
            }
        } else {
            factory = new org.apache.activemq.ActiveMQConnectionFactory("tcp://" + host + ":" + port);
        }
        return factory;
    }

    /**
     * Gets the Artemis Connection factory from either host/port or URL.
     * URL takes precedence.
     */
    org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory getArtemisConnectionFactory(String host, int port, String url) {
        org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory factory = null;
        if (url != null && url.length() != 0) {
            factory =
                    new org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory(url);
            if (!host.equals("localhost") || port != 61616) {
                log.warn("Ignoring host/port arguments as a URL was specified");
            }
        } else {
            factory = new org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory("tcp://" + host + ":" + port);
        }
        return factory;
    }

    ConnectionFactory getFactory(String host, int port,
                                 String url) {
        if ("qpid".equals(System.getProperty("amqutil.driver")))
            return getQpidFactory(host, port, url);
        else if ("artemis".equals(System.getProperty("amqutil.driver")))
            return getArtemisConnectionFactory(host, port, url);
        else
            return getActiveMQFactory(host, port, url);
    }

    /**
     * Read a file into a string
     */
    static String readFile(String path) throws IOException {
        return FileUtils.readFileToString(new File(path), StandardCharsets.UTF_8);
    }


}

