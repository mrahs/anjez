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
import java.util.Collection;

public class DelTasksCommand implements Command {
    private final Collection<TDTask> tdTasks;
    private final App invoker;

    public DelTasksCommand(App invoker, Collection<TDTask> tdTasks) {
        this.invoker = invoker;
        this.tdTasks = new ArrayList<>(tdTasks);
    }

    @Override
    public boolean execute() {
        return invoker.deleteTasks(tdTasks);
    }

    @Override
    public Command undoCommand() {
        return new AddTasksCommand(invoker, tdTasks);
    }

    @Override
    public String getDesc() {
        return "del";
    }
}
