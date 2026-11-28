package be.uantwerpen.sd.project.command;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CommandHistoryUTest {

    /** Small fake command: counts how often it is "on". */
    static class Counter implements Command {
        int value;
        public void execute() { value++; }
        public void undo() { value--; }
    }

    @Test
    void runUndoRedo() {
        CommandHistory h = new CommandHistory();
        Counter c = new Counter();
        h.run(c);
        assertEquals(1, c.value);
        h.undo();
        assertEquals(0, c.value);
        assertTrue(h.canRedo());
        h.redo();
        assertEquals(1, c.value);
        assertTrue(h.canUndo());
    }

    @Test
    void emptyHistoryDoesNothing() {
        CommandHistory h = new CommandHistory();
        h.undo();
        h.redo();
        assertFalse(h.canUndo());
        assertFalse(h.canRedo());
    }

    @Test
    void newCommandClearsRedo() {
        CommandHistory h = new CommandHistory();
        h.run(new Counter());
        h.undo();
        h.run(new Counter());
        assertFalse(h.canRedo());
        h.clear();
        assertFalse(h.canUndo());
    }
}
