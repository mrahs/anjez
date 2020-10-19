/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

  This file is part of Anjez.
  Anjez is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/

package pw.ahs.app.anjez.command;

import pw.ahs.app.anjez.gui.App;
import pw.ahs.app.anjez.model.TDTask;

import java.util.Objects;

public class EditTaskCommand implements Command {
    private final TDTask tdTask;
    private final App invoker;

    private final TDTask.TDTaskField tdTaskField;
    private final Object newValue;
    private final Object oldValue;

    public EditTaskCommand(App invoker, TDTask tdTask, TDTask.TDTaskField tdTaskField, Object oldValue, Object newValue) {
        this.invoker = Objects.requireNonNull(invoker);
        this.tdTask = Objects.requireNonNull(tdTask);
        this.tdTaskField = Objects.requireNonNull(tdTaskField);
        this.newValue = newValue;
        this.oldValue = tdTaskField == TDTask.TDTaskField.Done ? !(boolean) newValue : oldValue;
    }

    @Override
    public boolean execute() {
        return invoker.editTask(tdTask, tdTaskField, newValue);
    }

    @Override
    public Command undoCommand() {
        return new EditTaskCommand(this.invoker, this.tdTask, this.tdTaskField, this.newValue, this.oldValue);
    }

    @Override
    public String getDesc() {
        return "edit";
    }
}
