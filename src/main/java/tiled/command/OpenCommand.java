package tiled.command;

import tiled.io.xml.XMLMapReader;

class OpenCommand extends Command {

    OpenCommand(CommandInterpreter outer) {
        super("open", ArgumentRequirement.REQUIRES_ONE, outer);
        this.interpreter = outer;
    }

    @Override
    int execute() {
        XMLMapReader t = new XMLMapReader();
        String filename = getArguments()[0];
        try {
            interpreter.setMap(t.readMap(filename), filename);
        } catch (Exception ex) {
            interpreter.raiseError("could not load file " + filename + "");
            return 1;
        }
        return 0;
    }
}
