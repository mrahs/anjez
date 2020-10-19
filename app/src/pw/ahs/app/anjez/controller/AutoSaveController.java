/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

  This file is part of Anjez.
  Anjez is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/

package pw.ahs.app.anjez.controller;

import javafx.application.Platform;
import pw.ahs.app.anjez.gui.helper.IOHelper;
import static pw.ahs.app.anjez.Globals.beforeClosingController;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class AutoSaveController {
    private ScheduledThreadPoolExecutor schedulerAutoSave = null;
    private IOHelper ioHelper = null;
    private static AutoSaveController instance = null;

    public static AutoSaveController getInstance() {
        if (instance == null) {
            instance = new AutoSaveController();
            beforeClosingController.addAction(instance::clean);
        }

        return instance;
    }

    private AutoSaveController() {
    }

    public void setIoHelper(IOHelper ioHelper) {
        this.ioHelper = ioHelper;
    }

    public void setInterval(int seconds) {
        if (ioHelper == null) return;

        if (seconds <= 0) {
            if (schedulerAutoSave != null) schedulerAutoSave.shutdownNow();
            schedulerAutoSave = null;
        } else {
            schedulerAutoSave = new ScheduledThreadPoolExecutor(1);
            schedulerAutoSave.scheduleWithFixedDelay(
                    () -> {
                        if (!ioHelper.isSaving()) {
                            Platform.runLater(() -> ioHelper.saveFile(false));
                        }
                    }
                    , seconds
                    , seconds
                    , TimeUnit.SECONDS
            );
        }
    }

    public void clean() {
        if (schedulerAutoSave != null) {
            schedulerAutoSave.shutdownNow();
            schedulerAutoSave = null;
        }
    }
}
