/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

  This file is part of Anjez.
  Anjez is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/


package pw.ahs.app.anjez.controller;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import pw.ahs.app.anjez.Globals;
import pw.ahs.app.anjez.command.Command;

public class UndoRedoController {
    private final ObservableList<Command> undoStack;
    private final ObservableList<Command> redoStack;
    private Command cmdSaveOffset;

    private static UndoRedoController instance = null;

    public static UndoRedoController getInstance() {
        if (instance == null)
            instance = new UndoRedoController();
        return instance;
    }

    private UndoRedoController() {
        undoStack = FXCollections.observableArrayList();
        redoStack = FXCollections.observableArrayList();
        cmdSaveOffset = null;
    }

    public void clearStacks() {
        undoStack.clear();
        redoStack.clear();
    }

    public void pushCommand(Command cmd) {
        undoStack.add(cmd);
    }

    public void executeThenPushCommand(Command cmd) {
        boolean wasSaved = Globals.unsavedController.isSaved();
        if (cmd.execute()) {
            undoStack.add(cmd);
            if (wasSaved)
                cmdSaveOffset = cmd;
        }

    }

    public void addUndoListener(ListChangeListener<Command> listener) {
        undoStack.addListener(listener);
    }

    public void addRedoListener(ListChangeListener<Command> listener) {
        redoStack.addListener(listener);
    }

    public Command peekUndo() {
        return undoStack.get(undoStack.size() - 1);
    }

    public Command peekRedo() {
        return redoStack.get(redoStack.size() - 1);
    }

    public void undo() {
        if (undoStack.isEmpty()) return;
        Command lastCommand = undoStack.get(undoStack.size() - 1);
        if (lastCommand.undoCommand().execute()) {
            undoStack.remove(undoStack.size() - 1);
            redoStack.add(lastCommand);
            if (lastCommand == cmdSaveOffset) {
                Globals.unsavedController.setSaved(true);
                cmdSaveOffset = null;
            }
        }
    }

    public void redo() {
        if (redoStack.isEmpty()) return;
        Command lastCommand = redoStack.get(redoStack.size() - 1);
        boolean wasSaved = Globals.unsavedController.isSaved();
        if (lastCommand.execute()) {
            redoStack.remove(redoStack.size() - 1);
            undoStack.add(lastCommand);
            if (wasSaved)
                cmdSaveOffset = lastCommand;
        }
    }
}
