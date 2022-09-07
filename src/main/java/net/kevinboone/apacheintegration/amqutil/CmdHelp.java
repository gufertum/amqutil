/*==========================================================================
amqutil
CmdHelp.java
(c)2015 Kevin Boone
Distributed under the terms of the GPL v2.0
==========================================================================*/

package net.kevinboone.apacheintegration.amqutil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Implementation of the "help" command
 */
public class CmdHelp extends Cmd {

    private static final Logger log = LogManager.getLogger(CmdHelp.class);

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getShortUsage() {
        return "amqutil help {options} {command}";
    }

    @Override
    public String getShortDescription() {
        return "Displays the manual";
    }


    @Override
    public int run() throws Exception {
        String[] nonSwitchArgs = cl.getArgs();
        if (nonSwitchArgs.length == 0) {
            System.out.println("Show information for a command: amqutil help {command}");
            System.out.println("List all commands: amqutil commands");
            System.out.println("Display the manual: amqutil manual");
        } else {
            Cmd cmd = ListOfCommands.findCmd(nonSwitchArgs[0]);
            if (cmd != null) {
                // Call setupOptions() because otherwise the briefHelp() method
                //  won't have any options data to format for display
                cmd.setupOptions();
                cmd.briefHelp(System.out);
            } else
                log.error("Can't find command \"" + nonSwitchArgs[0] + "\"");
        }
        return 0;
    }

}



