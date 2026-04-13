package com.brandex.command;

// The command interface for the command pattern.
public interface Command {
    void execute();

    void undo();
}
