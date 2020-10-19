/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

  This file is part of Anjez.
  Anjez is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/


package pw.ahs.app.anjez.gui.helper;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import pw.ahs.app.anjez.gui.Dialogs;
import pw.ahs.app.anjez.model.TDTask;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static pw.ahs.app.anjez.Globals.*;
import static pw.ahs.app.anjez.controller.PrefsController.pFile_Path;

public class IOHelper {

    private static IOHelper instance = null;
    private boolean saving = false;

    public static IOHelper getInstance() {
        if (instance == null)
            instance = new IOHelper();
        return instance;
    }

    private IOHelper() {
    }

    public boolean isSaving() {
        return saving;
    }

    public void newFile() {
        if (view.waitForTracking()) return;
        if (view.waitForSave()) return;

        prefsController.setPref(pFile_Path, "");
        TDTask.clearContexts();
        TDTask.clearProjects();
        view.getTasks().clear();
        undoRedoController.clearStacks();
        unsavedController.setSaved(true);
        view.refreshTasks();
    }

    public void openFile(String filePath) {
        if (view.waitForTracking()) return;

        if (view.waitForSave()) return;

        File f;
        if (filePath.isEmpty()) {
            FileChooser fc = new FileChooser();
            fc.setTitle(i18n.getString("m.open-dialog-title"));
            fc.setInitialDirectory(new File(System.getProperty("user.home")));
            fc.setInitialFileName(DEFAULT_FILE_NAME);
            fc.getExtensionFilters().add(TEXT_EXT_FILTER);
            fc.setSelectedExtensionFilter(TEXT_EXT_FILTER);
            f = fc.showOpenDialog(view.getStage());
            if (f == null) return; // not an error, the user just canceled
        } else {
            f = new File(filePath);
        }

        if (!f.exists() || !f.isFile()) {
            Dialogs.showMessageDialog(
                    view.getStage(),
                    i18n.getString("file.error.title"),
                    i18n.getString("file.error.can-not-open-task-file"),
                    i18n.getString("button.dismiss")
            );
            return;
        }


        TDTask.clearContexts();
        TDTask.clearProjects();
        final List<TDTask> readTasks = new ArrayList<>();
        final Stage waiting = Dialogs.createWaitingDialog(view.getStage(), i18n.getString("m.working"), "",i18n.getString("button.dismiss"));
        final BooleanProperty error = new SimpleBooleanProperty(false);
        final Path path = f.toPath();
        new Thread(() -> {
            try (BufferedReader br = Files.newBufferedReader(path, Charset.forName("UTF-8"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.isEmpty()) continue;
                    TDTask t = TDTask.parse(line);
                    if (t != null) readTasks.add(t);
                }
            } catch (IOException e) {
                error.set(true);
            }
            Platform.runLater(waiting::hide);
        }).start();

        waiting.showAndWait();

        if (error.get()) {
            Dialogs.showMessageDialog(
                    view.getStage(),
                    i18n.getString("file.error.title"),
                    i18n.getString("file.error.can-not-open-task-file"),
                    i18n.getString("button.dismiss")
            );
        } else if (!readTasks.isEmpty()) {
            prefsController.setPref(pFile_Path, f.getAbsolutePath());
            view.getTasks().clear();
            view.getTasks().addAll(readTasks);
            unsavedController.setSaved(true);
            undoRedoController.clearStacks();
            view.refreshTasks();
        }
    }

    public void saveFile(boolean newFile) {
        String filePath = prefsController.getPref(pFile_Path);
        if (filePath.isEmpty() && view.getTasks().isEmpty()) {
            // no file was opened and nothing to save
            return;
        }

        saving = true;
        File f;
        if (newFile || filePath.isEmpty()) {
            FileChooser fc = new FileChooser();
            fc.setTitle(i18n.getString("m.save-dialog-title"));
            fc.setInitialDirectory(new File(System.getProperty("user.home")));
            fc.setInitialFileName(DEFAULT_FILE_NAME);
            fc.getExtensionFilters().add(TEXT_EXT_FILTER);
            fc.setSelectedExtensionFilter(TEXT_EXT_FILTER);
            f = fc.showSaveDialog(view.getStage());
        } else {
            f = new File(filePath);
        }

        if (f == null) {
            // not an error, the user just canceled
        } else if (f.isDirectory()) {
            Dialogs.showMessageDialog(
                    view.getStage(),
                    i18n.getString("file.error.title"),
                    i18n.getString("file.error.can-not-save"),
                    i18n.getString("button.dismiss")
            );
        } else {
            final Stage waiting = Dialogs.createWaitingDialog(view.getStage(), i18n.getString("m.working"), "",i18n.getString("button.dismiss"));
            final BooleanProperty error = new SimpleBooleanProperty(false);
            final Path path = f.toPath();
            new Thread(() -> {
                try (BufferedWriter br = Files.newBufferedWriter(path, Charset.forName("UTF-8"))) {
                    for (TDTask t : view.getTasks()) {
                        br.write(t.toString());
                        br.write(LINE_SEP);
                    }
                } catch (IOException e) {
                    error.set(true);
                }
                Platform.runLater(waiting::hide);
            }).start();

            waiting.showAndWait();

            if (error.get()) {
                Dialogs.showMessageDialog(
                        view.getStage(),
                        i18n.getString("file.error.title"),
                        i18n.getString("file.error.can-not-save"),
                        i18n.getString("button.dismiss")
                );
            } else {
                prefsController.setPref(pFile_Path, f.getAbsolutePath());
                unsavedController.setSaved(true);
            }
        }

        saving = false;
    }
}
