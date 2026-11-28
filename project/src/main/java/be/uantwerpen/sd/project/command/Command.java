package be.uantwerpen.sd.project.command;

/** COMMAND: an action as an object, which you can also undo. */
public interface Command {
    void execute();

    void undo();
}
