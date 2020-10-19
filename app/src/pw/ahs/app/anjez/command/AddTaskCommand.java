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

import java.util.ArrayList;
import java.util.Objects;

public class AddTaskCommand implements Command {
    private final TDTask tdTask;
    private final App invoker;

    public AddTaskCommand(TDTask tdTask, App app) {
        this.tdTask = Objects.requireNonNull(tdTask);
        this.invoker = Objects.requireNonNull(app);
    }

    @Override
    public boolean execute() {
        return invoker.addTask(tdTask);
    }

    @Override
    public Command undoCommand() {
        ArrayList<TDTask> tdTasks = new ArrayList<>(1);
        tdTasks.add(tdTask);
        return new DelTasksCommand(invoker, tdTasks);
    }

    @Override
    public String getDesc() {
        return "add";
    }
}
