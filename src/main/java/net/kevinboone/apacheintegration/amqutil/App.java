/*==========================================================================
amqutil
App.java
(c)2015 Kevin Boone
Distributed under the terms of the GPL v2.0
==========================================================================*/

package net.kevinboone.apacheintegration.amqutil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Main class for the amqutil application.
 */
public class App {

    private static final Logger log = LogManager.getLogger(App.class);

    /**
     * Start here!
     */
    public static void main(String[] args) {
        // If invoked with no arguments, dump a usage message and exit
        if (args.length < 1) {
            Usage.showBriefUsage(System.err);
            System.exit(-1);
        }



        int ret = 0; // OS return code

        // Check whether the first (command) argument matches something
        //  in the command list
        Cmd cmd = ListOfCommands.findCmd(args[0]);

        if (cmd != null) {
            try {
                String[] newArgs = new String[args.length - 1];
                System.arraycopy(args, 1, newArgs, 0, args.length - 1);
                // Give the command an opportunity to add its own command-line args
                cmd.setupOptions();
                cmd.parseArgs(newArgs);
                // Run the command, instrumented with logging and timing if specified
                ret = cmd.doRun();
            } catch (ArgParseException e) {
                log.error("Error parsing arguments for \"" + args[0] + "\" command: " + e.getMessage());
            } catch (BadTypeException e) {
                log.error("Unknown message type \"" + e.getMessage() + "\"");
            } catch (Exception e) {
                log.error(e.getMessage());
                System.exit(-1);
            }
        } else {
            log.error("Unknown command \"" + args[0] + "\"");
        }

        System.exit(ret);
    }
}



