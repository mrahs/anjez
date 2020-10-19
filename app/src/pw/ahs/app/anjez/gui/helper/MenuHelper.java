/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

  This file is part of Anjez.
  Anjez is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/


package pw.ahs.app.anjez.gui.helper;

import javafx.beans.binding.BooleanBinding;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import javafx.scene.input.KeyCombination;
import javafx.scene.shape.SVGPath;
import pw.ahs.app.anjez.command.AddTasksCommand;
import pw.ahs.app.anjez.command.Command;
import pw.ahs.app.anjez.gui.Dialogs;
import pw.ahs.app.anjez.gui.Splash;
import pw.ahs.app.anjez.gui.UIHelper;
import pw.ahs.app.anjez.model.TDTask;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;

import static pw.ahs.app.anjez.Globals.*;
import static pw.ahs.app.anjez.controller.PrefsController.pFile_Path;

public class MenuHelper {

    private static MenuHelper instance = null;

    public static MenuHelper getInstance() {
        if (instance == null)
            instance = new MenuHelper();
        return instance;
    }

    private MenuHelper() {
    }

    @SuppressWarnings("unchecked") // generics! (find it at // generics!)
    public void init(
            MenuButton menuBtn,
            TextField filterBox,
            TableView<TDTask> table,
            Tooltip[] tooltips
    ) {
        MenuItem mnuNew = new MenuItem();
        mnuNew.setAccelerator(KeyCombination.valueOf("shortcut+n"));
        mnuNew.setOnAction(event -> UIHelper.io.newFile());

        MenuItem mnuSaveAs = new MenuItem();
        mnuSaveAs.setAccelerator(KeyCombination.valueOf("shortcut+shift+s"));
        mnuSaveAs.setOnAction(event -> UIHelper.io.saveFile(true));

        MenuItem mnuSave = new MenuItem();
        mnuSave.setAccelerator(KeyCombination.valueOf("shortcut+s"));
        mnuSave.setOnAction(event -> UIHelper.io.saveFile(false));

        MenuItem mnuOpen = new MenuItem();
        mnuOpen.setAccelerator(KeyCombination.valueOf("shortcut+o"));
        mnuOpen.setOnAction(event -> UIHelper.io.openFile(""));

        MenuItem mnuReload = new MenuItem();
        mnuReload.setAccelerator(KeyCombination.valueOf("shortcut+r"));
        mnuReload.setOnAction(event -> {
            String filePath;
            if (!(filePath = prefsController.getPref(pFile_Path)).isEmpty()) {
                UIHelper.io.openFile(filePath);
            }
        });

        MenuItem mnuClose = new MenuItem();
        mnuClose.setAccelerator(KeyCombination.valueOf("shortcut+w"));
        mnuClose.setOnAction(event -> view.exit());

        MenuItem mnuFilter = new MenuItem();
        mnuFilter.setAccelerator(KeyCombination.valueOf("shortcut+f"));
        mnuFilter.setOnAction(event -> {
            filterBox.selectAll();
            filterBox.requestFocus();
        });

        MenuItem mnuOptions = new MenuItem();
        mnuOptions.setAccelerator(KeyCombination.valueOf("shortcut+p"));
        mnuOptions.setOnAction(event -> UIHelper.prefs.show());

        MenuItem mnuAbout = new MenuItem();
        mnuAbout.setAccelerator(KeyCombination.valueOf("shortcut+F1"));
        mnuAbout.setOnAction(actionEvent -> UIHelper.about.showAbout(view));

        MenuItem mnuHelpFile = new MenuItem();
        mnuHelpFile.setAccelerator(KeyCombination.valueOf("F1"));
        mnuHelpFile.setOnAction(actionEvent -> {
            if (!controller.showHelpFile(view))
                Dialogs.showMessageDialog(
                        view.getStage(),
                        i18n.getString("file.error.title"),
                        i18n.getString("file.error.help-file-missing"),
                        i18n.getString("button.dismiss")
                );
        });

        MenuItem mnuCheatSheet = new MenuItem();
        mnuCheatSheet.setAccelerator(KeyCombination.valueOf("alt+?"));
        mnuCheatSheet.setOnAction(actionEvent -> UIHelper.shortcutSheet.showShortcutSheet());

        MenuItem mnuRefresh = new MenuItem();
        mnuRefresh.setAccelerator(KeyCombination.valueOf("F5"));
        mnuRefresh.setOnAction(actionEvent -> view.refreshTasks());

        MenuItem mnuUndo = new MenuItem();
        mnuUndo.setAccelerator(KeyCombination.valueOf("shortcut+z"));
        mnuUndo.setOnAction(actionEvent -> undoRedoController.undo());
        mnuUndo.setDisable(true);

        MenuItem mnuRedo = new MenuItem();
        mnuRedo.setAccelerator(KeyCombination.valueOf("shortcut+y"));
        mnuRedo.setOnAction(actionEvent -> undoRedoController.redo());
        mnuRedo.setDisable(true);

        MenuItem mnuCopy = new MenuItem();
        mnuCopy.setAccelerator(KeyCombination.valueOf("shortcut+c"));
        mnuCopy.setOnAction(actionEvent -> controller.copy(table.getSelectionModel().getSelectedItems()));
        mnuCopy.disableProperty().bind(new BooleanBinding() {
            {
                bind(table.getSelectionModel().selectedIndexProperty());
            }

            @Override
            protected boolean computeValue() {
                return table.getSelectionModel().getSelectedIndex() < 0;
            }
        });

        MenuItem mnuEdit = new MenuItem();
        mnuEdit.setAccelerator(KeyCombination.valueOf("f2"));
        mnuEdit.setOnAction(evt -> {
            TableColumn selectedColumn = table.getSelectionModel().getSelectedCells().get(0).getTableColumn();
            // generics!
            table.edit(table.getSelectionModel().getSelectedIndex(), selectedColumn);
        });
        mnuEdit.disableProperty().bind(new BooleanBinding() {
            {
                bind(table.getSelectionModel().selectedIndexProperty());
            }

            @Override
            protected boolean computeValue() {
                return table.getSelectionModel().getSelectedIndex() < 0
                        || table.getSelectionModel().getSelectedIndices().size() != 1;
            }
        });

        MenuItem mnuDel = new MenuItem();
        mnuDel.setAccelerator(KeyCombination.valueOf("delete"));
        mnuDel.setOnAction(evt -> view.deleteSelectedItems());
        mnuDel.disableProperty().bind(new BooleanBinding() {
            {
                bind(table.getSelectionModel().selectedIndexProperty());
            }

            @Override
            protected boolean computeValue() {
                return table.getSelectionModel().getSelectedIndex() < 0;
            }
        });

        MenuItem mnuPaste = new MenuItem();
        mnuPaste.setAccelerator(KeyCombination.valueOf("shortcut+v"));
        mnuPaste.setOnAction(evt -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            if (clipboard.hasContent(DataFormat.PLAIN_TEXT)) {
                String[] lines = clipboard.getString().split("\\n");
                if (lines.length == 0) return;

                Collection<TDTask> items = new ArrayList<>(lines.length);
                StringBuilder itemsText = new StringBuilder();
                for (String line : lines) {
                    line = line.trim();
                    if (line.isEmpty()) continue;
                    TDTask tdTask = controller.parseTask(line);
                    if (tdTask == null) continue;
                    items.add(tdTask);
                    itemsText.append(tdTask).append("\n");
                }

                TextArea textArea = new TextArea(itemsText.toString());
                textArea.setEditable(false);
                textArea.setPrefColumnCount(15);
                textArea.setPrefRowCount(10);
                if (Dialogs.showYesNoDialog(
                        view.getStage(),
                        i18n.getString("m.paste.title"),
                        i18n.getString("m.paste.msg"),
                        i18n.getString("button.add"),
                        i18n.getString("button.cancel"),
                        "",
                        1,
                        textArea
                ) == Dialogs.Result.YES) {
                    Command cmd = new AddTasksCommand(view, items);
                    undoRedoController.executeThenPushCommand(cmd);
                }
            }
        });

        MenuItem mnuTrackingDetails = new MenuItem();
        mnuTrackingDetails.setAccelerator(KeyCombination.valueOf("alt+enter"));
        mnuTrackingDetails.setOnAction(evt -> UIHelper.tracking.showTrackingDetails(view.getStage(), table.getSelectionModel().getSelectedItem()));
        mnuTrackingDetails.disableProperty().bind(new BooleanBinding() {
            {
                bind(table.getSelectionModel().selectedIndexProperty());
            }

            @Override
            protected boolean computeValue() {
                return table.getSelectionModel().getSelectedIndex() < 0
                        || table.getSelectionModel().getSelectedIndices().size() != 1;
            }
        });

        MenuItem mnuDbxOpen = new MenuItem();
        mnuDbxOpen.setAccelerator(KeyCombination.valueOf("shortcut+alt+o"));
        mnuDbxOpen.setOnAction(actionEvent -> UIHelper.dropBox.dbxOpen());

        MenuItem mnuDbxSave = new MenuItem();
        mnuDbxSave.setAccelerator(KeyCombination.valueOf("shortcut+alt+s"));
        mnuDbxSave.setOnAction(actionEvent -> UIHelper.dropBox.dbxSave());

        MenuItem mnuDbxReload = new MenuItem();
        mnuDbxReload.setAccelerator(KeyCombination.valueOf("shortcut+alt+r"));
        mnuDbxReload.setOnAction(actionEvent -> UIHelper.dropBox.dbxReload());

        MenuItem mnuTour = new MenuItem();
        mnuTour.setAccelerator(KeyCombination.valueOf("shift+F1"));
        mnuTour.setOnAction(actionEvent -> UIHelper.tour.showGuiTour());

        MenuItem mnuSplash = new MenuItem();
        mnuSplash.setAccelerator(KeyCombination.valueOf("shortcut+shift+F1"));
        mnuSplash.setOnAction(evt -> Splash.show());

        MenuItem mnuUpdate = new MenuItem();
        mnuUpdate.setOnAction(evt -> view.checkForUpdate(true));

        Menu menuFile = new Menu();
        menuFile.getItems().addAll(mnuNew, mnuOpen, mnuReload, mnuSave, mnuSaveAs);

        Menu menuDropBox = new Menu();
        menuDropBox.getItems().addAll(mnuDbxOpen, mnuDbxReload, mnuDbxSave);

        Menu menuTask = new Menu();
        menuTask.getItems().addAll(mnuCopy, mnuPaste, mnuEdit, mnuDel, mnuTrackingDetails, mnuUndo, mnuRedo);

        Menu mnuHelp = new Menu();
        mnuHelp.getItems().addAll(mnuHelpFile, mnuTour, mnuCheatSheet, mnuUpdate, mnuSplash, mnuAbout);

        SVGPath svgPath = new SVGPath();
        svgPath.setContent("M 0 0 H 16 V 3.2 H 0 Z M 0 6.4 H 16 V 9.6 H 0 Z M 0 12.8 H 16 V 16 H 0 Z");
        svgPath.setId("menu-icon");
//        svgPath.setContent("M1,2h14c0.553,0,1-0.448,1-1s-0.447-1-1-1H1C0.448,0,0,0.448,0,1S0.448,2,1,2zM15,5.875H1c-0.552,0-1,0.448-1,1c0,0.553,0.448,1,1,1h14c0.553,0,1-0.447,1-1C16,6.323,15.553,5.875,15,5.875z M15,11.75H1c-0.552,0-1,0.447-1,1s0.448,1,1,1h14c0.553,0,1-0.447,1-1S15.553,11.75,15,11.75z");
        menuBtn.setGraphic(svgPath);
        menuBtn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        menuBtn.getStyleClass().clear();
        menuBtn.getStyleClass().add("button");
        menuBtn.setId("menu");
        menuBtn.setTooltip(tooltips[0]);

        menuBtn.getItems().addAll(
                menuFile,
                menuTask,
                new SeparatorMenuItem(),
                mnuRefresh, mnuFilter,
                new SeparatorMenuItem(),
                mnuOptions, mnuHelp, mnuClose);
        if (UIHelper.dropBox.isAvailable()) {
            menuBtn.getItems().add(1, menuDropBox);
        }

        undoRedoController.addUndoListener(change -> {
            while (change.next())
                if (change.wasAdded() || change.wasRemoved()) {
                    if (change.getList().isEmpty()) {
                        mnuUndo.setText(i18n.getString("menu.undo"));
                        mnuUndo.setDisable(true);
                    } else {
                        mnuUndo.setText(i18n.getString("menu.undo") + " " + i18n.getString("undo-redo." + undoRedoController.peekUndo().getDesc()));
                        mnuUndo.setDisable(false);
                    }
                }
        });

        undoRedoController.addRedoListener(change -> {
            while (change.next())
                if (change.wasAdded() || change.wasRemoved()) {
                    if (change.getList().isEmpty()) {
                        mnuRedo.setText(i18n.getString("menu.redo"));
                        mnuRedo.setDisable(true);
                    } else {
                        mnuRedo.setText(i18n.getString("menu.redo") + " " + i18n.getString("undo-redo." + undoRedoController.peekRedo().getDesc()));
                        mnuRedo.setDisable(false);
                    }
                }
        });

        Function<String, Void> menuLangListener = (newVal) -> {
            mnuNew.setText(i18n.getString("menu.new"));
            mnuSaveAs.setText(i18n.getString("menu.save-as"));
            mnuSave.setText(i18n.getString("menu.save"));
            mnuOpen.setText(i18n.getString("menu.open"));
            mnuReload.setText(i18n.getString("menu.reload"));
            mnuClose.setText(i18n.getString("menu.close"));
            mnuFilter.setText(i18n.getString("menu.filter"));
            mnuOptions.setText(i18n.getString("menu.prefs"));
            mnuAbout.setText(i18n.getString("menu.about"));
            mnuHelpFile.setText(i18n.getString("menu.help-file"));
            mnuHelp.setText(i18n.getString("menu.help"));
            mnuTour.setText(i18n.getString("menu.tour"));
            mnuSplash.setText(i18n.getString("menu.splash"));
            mnuCheatSheet.setText(i18n.getString("menu.shortcuts-sheet"));
            mnuRefresh.setText(i18n.getString("menu.refresh"));
            mnuUndo.setText(i18n.getString("menu.undo"));
            mnuRedo.setText(i18n.getString("menu.redo"));
            mnuCopy.setText(i18n.getString("menu.copy"));
            mnuPaste.setText(i18n.getString("menu.paste"));
            mnuDbxOpen.setText(i18n.getString("menu.open"));
            mnuDbxSave.setText(i18n.getString("menu.save"));
            mnuDbxReload.setText(i18n.getString("menu.reload"));
            menuFile.setText(i18n.getString("menu.file"));
            menuDropBox.setText(i18n.getString("menu.dropbox"));
            menuTask.setText(i18n.getString("menu.task"));
            mnuEdit.setText(i18n.getString("menu.edit"));
            mnuDel.setText(i18n.getString("menu.del"));
            mnuTrackingDetails.setText(i18n.getString("menu.track-details"));
            mnuUpdate.setText(i18n.getString("menu.update"));
            menuBtn.getTooltip().setText(i18n.getString("menu.menu"));
            return null;
        };

        menuLangListener.apply(null);

        // add language listener
        currentLangController.addListener(menuLangListener);
    }
}
