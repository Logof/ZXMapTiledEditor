/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tiled.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author upachler
 */
public class HelpCommand extends Command {
    private Logger log = LoggerFactory.getLogger(HelpCommand.class);

    HelpCommand(CommandInterpreter interp) {
        super("help", ArgumentRequirement.REQUIRES_NONE, interp);
    }

    @Override
    int execute() {
        // FIXME: this should be more detailed
        log.info("Supported commands are: ");

        for (Command c : interpreter.getCommandPrototypes()) {
            log.info("\t'" + c.getName() + '\'');
        }
        log.info("");
        return 0;
    }

}
