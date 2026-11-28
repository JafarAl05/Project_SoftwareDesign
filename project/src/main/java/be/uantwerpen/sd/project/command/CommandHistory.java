package be.uantwerpen.sd.project.command;

import java.util.ArrayDeque;
import java.util.Deque;

/** COMMAND (invoker): runs commands and remembers them for undo/redo. */
public class CommandHistory {
    private final Deque<Command> undoStack = new ArrayDeque<>();
    private final Deque<Command> redoStack = new ArrayDeque<>();

    public void run(Command c) {
        c.execute();
        undoStack.push(c);
        redoStack.clear();            // new action -> the old "redo" no longer makes sense
    }

    public void undo() {
        if (undoStack.isEmpty()) return;
        Command c = undoStack.pop();
        c.undo();
        redoStack.push(c);
    }

    public void redo() {
        if (redoStack.isEmpty()) return;
        Command c = redoStack.pop();
        c.execute();
        undoStack.push(c);
    }

    public boolean canUndo() { return !undoStack.isEmpty(); }

    public boolean canRedo() { return !redoStack.isEmpty(); }

    public void clear() {
        undoStack.clear();
        redoStack.clear();
    }
}
