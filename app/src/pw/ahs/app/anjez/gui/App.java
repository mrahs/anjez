/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

  This file is part of Anjez.
  Anjez is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/

package pw.ahs.app.anjez.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.SetChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import pw.ahs.app.anjez.Globals;
import pw.ahs.app.anjez.command.AddTaskCommand;
import pw.ahs.app.anjez.command.Command;
import pw.ahs.app.anjez.command.DelTasksCommand;
import pw.ahs.app.anjez.model.TDTask;

import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;

import static pw.ahs.app.anjez.Globals.*;
import static pw.ahs.app.anjez.controller.PrefsController.*;

public final class App extends Application {

    /*
    ************************************************************
    *
    * UI Fields
    *
    ************************************************************/

    private Stage theStage;
    private final VBox root = new VBox();
    private final TextField filterBox = new TextField();
    private final MenuButton menuBtn = new MenuButton();
    private final TableView<TDTask> table = new TableView<>();
    private final GridPane statusBar = new GridPane();
    private final Button btnPlayStop = new Button();
    private final ContextMenu[] cMenus = new ContextMenu[8];
    private final Tooltip[] tooltips = new Tooltip[18];

    private final Collection<Image> icons = new ArrayList<>(6);

    private final ObservableList<TDTask> tasks = FXCollections.observableArrayList();
    private final FilteredList<TDTask> filteredTasks = tasks.filtered(p -> true);
    private final SortedList<TDTask> sortedTasks = filteredTasks.sorted();

    /**
     * this must be initialized to true, to avoid filtering the tasks twice on startup
     * if it's initialized to false, and the the option showDoneTasks is initially false,
     * the value will NOT be invalidated, hence, no call to filter the tasks.
     * if, on the other hand, it's initialized to true, the option showDoneTasks can be
     * initially true or false, which may or may not trigger filtering, but things are good
     */
    private final SimpleBooleanProperty showDoneTasks = new SimpleBooleanProperty(true);

    /*
    ************************************************************
    *
    * Methods
    *
    ************************************************************/

    public static void main(String[] args) {
        Application.launch(args);
    }

    private void initialize() {
//        Instant start = Instant.now();

        // icons
        int j = 8;
        for (int i = 0; i < 6; i++) {
            j *= 2;
            InputStream is = getClass().getResourceAsStream("/pw/ahs/app/anjez/res/anjez-icon-" + j + ".png");
            if (is == null) continue;
            icons.add(new Image(is));
        }
        Platform.runLater(() -> Splash.updateSplashIcons(icons));
        theStage.setTitle(APP_TITLE);
        theStage.getIcons().setAll(icons);

        // load globals & helpers
        try {
            Class.forName("Globals");
            Class.forName("UIHelper");
        } catch (ClassNotFoundException ignored) {
        }

        // complete autoSaveController initialization
        Globals.autoSaveController.setIoHelper(UIHelper.io);

        // filter box
        filterBox.setId("filter-box");

        // other
        showDoneTasks.addListener((observableValue, oldValue, newValue) -> {
            prefsController.setPref(pShow_Done_Task, "" + newValue);
            filterTasks();
        });

        unsavedController.addListener((saved) -> {
            if (saved) {
                statusBar.getStyleClass().removeAll("unsaved-state");
                theStage.setTitle(APP_TITLE);
            } else {
                statusBar.getStyleClass().addAll("unsaved-state");
                theStage.setTitle(APP_TITLE + "*");
            }
            return null;
        });

        // this listener must be registered first
        Function<String, Void> mainLangListener = (newLang) -> {
            if (newLang.equals("en")) {
                theCollator = COLLATOR_EN;
                i18n = ResourceBundle.getBundle("pw.ahs.app.anjez.i18n.word", Locale.US);
                theStage.getScene().setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
            } else if (newLang.equals("ar")) {
                theCollator = COLLATOR_AR;
                i18n = ResourceBundle.getBundle("pw.ahs.app.anjez.i18n.word", new Locale("ar", "SA"));
                theStage.getScene().setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            }
            return null;
        };
        Function<String, Void> langListener = (newLang) -> {
            PRIORITY_CHARS_I18N.clear();
            for (String p : TDTask.PRIORITY_CHARS) {
                if (p.isEmpty()) continue;
                PRIORITY_CHARS_I18N.put(i18n.getString("todo-txt." + p), p);
            }

            prefsController.updateDefault(pFilter_Query, FILTER_PREFIX + i18n.getString("filter.all"));
            filterBox.setPromptText(i18n.getString("filter.prompt"));
            dropBoxController.setUserLocal(i18n.getLocale().toString());
            return null;
        };
        langListener.apply(null);
        currentLangController.addListener(mainLangListener);
        currentLangController.addListener(langListener);

        tasks.addListener((ListChangeListener<TDTask>) change -> {
            while (change.next()) {
                if (change.wasAdded() || change.wasRemoved())
                    unsavedController.setSaved(false);
            }
        });


        prefsController.init();
        UIHelper.prefs.init(table, showDoneTasks, statusBar);
        UIHelper.tour.init(root, filterBox, menuBtn, table, statusBar, btnPlayStop);
        UIHelper.shortcutSheet.init(tooltips);
        UIHelper.table.init(table, sortedTasks, cMenus, btnPlayStop);
        UIHelper.menu.init(menuBtn, filterBox, table, tooltips);
        UIHelper.statusBar.init(statusBar, tooltips, tasks, filterBox, table, showDoneTasks, btnPlayStop);
        initActions();

        // layout
        HBox layoutFilterMenu = new HBox(filterBox, menuBtn);
        HBox.setHgrow(filterBox, Priority.ALWAYS);
        layoutFilterMenu.setAlignment(Pos.CENTER);

        root.getChildren().addAll(layoutFilterMenu, table, statusBar);
        VBox.setVgrow(table, Priority.ALWAYS);
        root.setSpacing(1);
        root.setPrefSize(640, 480);

        // Options
        if (!prefsController.loadPrefs(PREFS_FILE_NAME))
            Dialogs.showMessageDialog(
                    theStage,
                    i18n.getString("file.error.title"),
                    i18n.getString("prefs.error"),
                    i18n.getString("button.dismiss")
            );

        // stage
        theStage.setOnCloseRequest(event -> {
            event.consume();
            exit();
        });

        // default exception handler
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            StringWriter stringWriter = new StringWriter();
            e.printStackTrace(new PrintWriter(stringWriter));
            String msg = getUserAgentString() + "\n" + stringWriter.toString();

            VBox vBox = new VBox();
            TextArea textArea = new TextArea(msg);
            Hyperlink hyperlink = new Hyperlink(i18n.getString("m.unknown-error-link"));
            hyperlink.setOnAction(evt -> {
                hyperlink.setOnAction(null);
                vBox.getChildren().addAll(textArea);
                hyperlink.getScene().getWindow().sizeToScene();
            });
            vBox.getChildren().addAll(hyperlink);
            if (Dialogs.showYesNoDialog(
                    theStage,
                    i18n.getString("m.unknown-error-title"),
                    i18n.getString("m.unknown-error-msg"),
                    i18n.getString("button.send"),
                    i18n.getString("button.discard"),
                    "",
                    1,
                    vBox
            ) == Dialogs.Result.YES) {
                feedback(msg);
            }
        });

//        Instant end = Instant.now();
//        long d = java.time.Duration.between(start, end).toMillis();
//        if (d < 2000) {
//            try {
//                Thread.sleep(2000 - d);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
        finishInit();
    }

    private void finishInit() {
        Platform.runLater(() -> {

            // scene
            Scene scene = new Scene(root);
            scene.getAccelerators().put(KeyCombination.valueOf("f3"), () -> {
                filterBox.selectAll();
                filterBox.requestFocus();
            });
            scene.getAccelerators().put(KeyCombination.valueOf("f10"), () -> {
                menuBtn.requestFocus();
                menuBtn.show();
            });
//            scene.getAccelerators().put(KeyCombination.valueOf("shortcut+v"), () -> );

            scene.getStylesheets().add(getClass().getResource("/pw/ahs/app/anjez/gui/css/style.bss").toExternalForm());
            scene.setOnDragOver(evt -> {
                if (evt.getDragboard().hasFiles()) {
                    List<File> files = evt.getDragboard().getFiles();
                    if (files.size() == 1 && files.get(0).getName().endsWith(".txt"))
                        evt.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                }
            });
            scene.setOnDragEntered(evt -> root.getStyleClass().addAll("drag"));
            scene.setOnDragExited(evt -> root.getStyleClass().removeAll("drag"));
            scene.setOnDragDropped(evt -> UIHelper.io.openFile(evt.getDragboard().getFiles().get(0).getAbsolutePath()));
            theStage.setScene(scene);

            // show
            theStage.show();
            Splash.hide();

            // prefs
            UIHelper.prefs.apply();

            // update
            if (prefsController.getPrefBoolean(pCheck_For_Update_On_Startup)) {
                checkForUpdate(false);
            }

            // focus filter box
            filterBox.requestFocus();
        });

        /*
         * The following must be executed in a separate call to fix encountered in Kubuntu 14.10
         * where the window becomes un-resizable. 
         */
        Platform.runLater(()->{
            // command line argument handler & last file
            List<String> params = getParameters().getUnnamed();
            if (params.isEmpty()) {
                if (prefsController.getPrefBoolean(pRemember_File_Path) && !prefsController.getPref(pFile_Path).isEmpty()) {
                    UIHelper.io.openFile(prefsController.getPref(pFile_Path));
                }
            } else {
                UIHelper.io.openFile(params.get(0));
            }
        });
    }

    @Override
    public void start(Stage stage) throws Exception {
        Splash.show(APP_TITLE, new Image("/pw/ahs/app/anjez/res/anjez-icon-128.png"), icons);

        theStage = stage;
        view = this;

        // tooltips must be initialized in FXThread
        for (int i = 0; i < tooltips.length; i++) {
            tooltips[i] = new Tooltip("");
        }

        // context menus must be initialized in FXThread
        for (int i = 0; i < cMenus.length; i++) {
            cMenus[i] = new ContextMenu();
        }

        new Thread(this::initialize).start();
    }

    private void initActions() {
        // delete task
        table.setOnKeyReleased(event -> {
            // the second condition is to avoid invoking when delete key is meant to delete text inside cell
            if (event.getCode() == KeyCode.DELETE && table.getEditingCell() == null) {
                event.consume();
                deleteSelectedItems();
            }
        });

        // filter tasks
        filterBox.textProperty().addListener((observableValue, oldString, newValue) -> filterTasks());

        // auto complete
        AutoCompletePopupBinding autoCompletePopupBinding = new AutoCompletePopupBinding();
        autoCompletePopupBinding.setTarget(filterBox);
        SetChangeListener<String> stringSetChangeListener = change -> {
            if (change.wasAdded()) {
                autoCompletePopupBinding.addSuggestion(change.getElementAdded());
            } else if (change.wasRemoved()) {
                autoCompletePopupBinding.removeSuggestion(change.getElementRemoved());
            }
        };
        TDTask.PROJECTS.addListener(stringSetChangeListener);
        TDTask.CONTEXTS.addListener(stringSetChangeListener);
        filterBox.addEventFilter(KeyEvent.KEY_PRESSED, evt -> {
            if (evt.getCode() == KeyCode.SPACE && evt.isControlDown()) {
                int i = filterBox.getText().lastIndexOf(' ') + 1;
                filterBox.selectRange(i, filterBox.getCaretPosition());
                autoCompletePopupBinding.setCurrentText(filterBox.getSelectedText());
            }
        });
        autoCompletePopupBinding.setOnItemChosen(s -> {
            filterBox.replaceSelection(s);
            return null;
        });
        Function<String, Void> autoCompleteLangListener = newLang -> {
            autoCompletePopupBinding.addSuggestion(FILTER_PREFIX + i18n.getString("filter.all"));
            autoCompletePopupBinding.addSuggestion(FILTER_PREFIX + i18n.getString("filter.today"));
            autoCompletePopupBinding.addSuggestion(FILTER_PREFIX + i18n.getString("filter.next"));
            autoCompletePopupBinding.addSuggestion(FILTER_PREFIX + i18n.getString("filter.week"));
            autoCompletePopupBinding.addSuggestion(FILTER_PREFIX + i18n.getString("filter.some-day"));
            autoCompletePopupBinding.addSuggestion(FILTER_PREFIX + i18n.getString("filter.overdue"));
            autoCompletePopupBinding.addSuggestion(SHORTCUT_PREFIX + i18n.getString("filter.today"));
            autoCompletePopupBinding.addSuggestion(SHORTCUT_PREFIX + i18n.getString("filter.next"));
            return null;
        };
        Function<String, Void> autoCompleteBeforeLangListener = newLang -> {
            autoCompletePopupBinding.removeSuggestion(FILTER_PREFIX + i18n.getString("filter.all"));
            autoCompletePopupBinding.removeSuggestion(FILTER_PREFIX + i18n.getString("filter.today"));
            autoCompletePopupBinding.removeSuggestion(FILTER_PREFIX + i18n.getString("filter.next"));
            autoCompletePopupBinding.removeSuggestion(FILTER_PREFIX + i18n.getString("filter.week"));
            autoCompletePopupBinding.removeSuggestion(FILTER_PREFIX + i18n.getString("filter.some-day"));
            autoCompletePopupBinding.removeSuggestion(FILTER_PREFIX + i18n.getString("filter.overdue"));
            autoCompletePopupBinding.removeSuggestion(SHORTCUT_PREFIX + i18n.getString("filter.today"));
            autoCompletePopupBinding.removeSuggestion(SHORTCUT_PREFIX + i18n.getString("filter.next"));
            return null;
        };
        autoCompleteLangListener.apply(null);
        currentLangController.addListener(autoCompleteLangListener);
        currentLangController.addBeforeChangeListener(autoCompleteBeforeLangListener);
        autoCompletePopupBinding.enable();

        // filter box shortcuts & HACKS
        filterBox.setOnKeyReleased(evt -> {

            if (evt.getCode() == KeyCode.ESCAPE) {
                filterBox.clear();
                evt.consume();
            }
            // select all
            else if (evt.getCode() == KeyCode.ENTER && !evt.isShortcutDown()) {
                filterBox.selectAll();
            }
            // add task or send feedback
            else if (evt.getCode() == KeyCode.ENTER && evt.isShortcutDown()) {
                String text = filterBox.getText();
                if (text.isEmpty()) return;

                TDTask tdTask = controller.parseTask(text);
                if (tdTask == null) {
                    if (Dialogs.showYesNoDialog(
                            theStage,
                            i18n.getString("feedback.title"),
                            String.format(i18n.getString("feedback.msg.confirm"), text),
                            i18n.getString("button.send"),
                            i18n.getString("button.cancel"),
                            "",
                            1
                    ) == Dialogs.Result.YES)
                        feedback(text);
                } else {
                    Command cmd = new AddTaskCommand(tdTask, this);
                    undoRedoController.executeThenPushCommand(cmd);
                }
                evt.consume();
            }
            // for some reason, key events are consumed by TextField unless a modifier
            // is used
            // we have to duplicate code..
            else if (evt.getCode() == KeyCode.F1 && !evt.isShiftDown()) {
                controller.showHelpFile(this);
                evt.consume();
            } else if (evt.getCode() == KeyCode.F5) {
                refreshTasks();
                evt.consume();
            }
        });
    }

    public Stage getStage() {
        return theStage;
    }

    public ObservableList<TDTask> getTasks() {
        return tasks;
    }

    public void exit() {
        if (waitForTracking()) return;
        if (waitForSave()) return;

        beforeClosingController.doAll();

        Platform.exit();
    }

    public void deleteSelectedItems() {
        if (table.getSelectionModel().isEmpty()) return;
        ObservableList<TDTask> items = table.getSelectionModel().getSelectedItems();

        Command cmd = new DelTasksCommand(this, items);
        undoRedoController.executeThenPushCommand(cmd);
    }

    public boolean deleteTasks(Collection<TDTask> tdTasks) {
        if (!prefsController.getPrefBoolean(pConfirm_Delete)) {
            tasks.removeAll(tdTasks);
            refreshTasks();
            return true;
        }

        int c = tdTasks.size();
        String msg;
        if (c == 1)
            msg = String.format(i18n.getString("confirm-delete.msg.single"), tdTasks.iterator().next().getInfo());
        else
            msg = (String.format(i18n.getString("confirm-delete.msg.many"), c));

        if (Dialogs.showYesNoDialog(
                theStage,
                i18n.getString("confirm-delete.title"),
                msg,
                i18n.getString("button.delete"),
                i18n.getString("button.cancel"),
                "",
                2
        ) == Dialogs.Result.YES) {
            tasks.removeAll(tdTasks);
            refreshTasks();
            return true;
        }
        return false;
    }

    private boolean feedback(final String txt) {
        Stage stageWaiting = Dialogs.createWaitingDialog(theStage, i18n.getString("m.working"), "", i18n.getString("button.dismiss"));
        BooleanProperty state = new SimpleBooleanProperty(true);

        new Thread(() -> {
            state.set(internetController.sendFeedback(txt));
            Platform.runLater(stageWaiting::hide);
        }).start();
        stageWaiting.showAndWait();

        if (state.get()) {
            Dialogs.showMessageDialog(
                    theStage,
                    i18n.getString("feedback.title"),
                    i18n.getString("feedback.msg.success"),
                    i18n.getString("button.dismiss")
            );
        } else {
            if (Dialogs.showYesNoDialog(
                    theStage,
                    i18n.getString("feedback.title"),
                    i18n.getString("feedback.msg.fail"),
                    i18n.getString("button.dismiss"),
                    i18n.getString("button.try-again"),
                    "",
                    2
            ) == Dialogs.Result.NO) {
                return feedback(txt);
            }
        }
        filterBox.selectAll();
        return true;
    }

    public boolean addTask(TDTask tdTask) {
        tasks.add(tdTask);
        filterBox.clear();
        refreshTasks();
        table.getSelectionModel().select(tdTask);
        return true;
    }

    public boolean addAllTasks(Collection<TDTask> tdTasks) {
        tasks.addAll(tdTasks);
        refreshTasks();
        for (TDTask tdTask : tdTasks) {
            table.getSelectionModel().select(tdTask);
        }
        return true;
    }

    public void refreshTasks() {
        table.getSelectionModel().clearSelection();
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        LocalDate week = today.plusWeeks(1);
        int nToday[] = {0}, nTomorrow[] = {0}, nWeek[] = {0}, nLater[] = {0}, nTooLate[] = {0};

        filteredTasks.forEach(tdTask -> {
            LocalDate taskDue = tdTask.getDueDate();
            if (taskDue != null) {
                if (taskDue.isBefore(today)) {
                    if (!tdTask.isDone())
                        nTooLate[0]++;
                } else if (taskDue.isEqual(today)) {
                    nToday[0]++;
                    nWeek[0]++;
                } else if (taskDue.isEqual(tomorrow)) {
                    nTomorrow[0]++;
                    nWeek[0]++;
                } else if (taskDue.isAfter(today) && taskDue.isBefore(week))
                    nWeek[0]++;
            } else {
                nLater[0]++;
            }
        });

        UIHelper.statusBar
                .updateNumbers(filteredTasks.size(), nToday[0], nTomorrow[0], nWeek[0], nLater[0], nTooLate[0]);
    }

    public void filterTasks() {
        String txt = filterBox.getText();
        if (txt.isEmpty()) {
            txt = prefsController.getPref(pFilter_Query);
        }

        if (txt.equalsIgnoreCase(FILTER_PREFIX + i18n.getString("filter.all"))) {
            filteredTasks.setPredicate(showDoneTasks.get() ? FILTER_ALL : FILTER_ALL.and(FILTER_NOT_DONE));
        } else if (txt.equalsIgnoreCase(FILTER_PREFIX + i18n.getString("filter.today"))) {
            filteredTasks.setPredicate(showDoneTasks.get() ? FILTER_TODAY : FILTER_TODAY.and(FILTER_NOT_DONE));
        } else if (txt.equalsIgnoreCase(FILTER_PREFIX + i18n.getString("filter.next"))) {
            filteredTasks.setPredicate(showDoneTasks.get() ? FILTER_TOMORROW : FILTER_TOMORROW.and(FILTER_NOT_DONE));
        } else if (txt.equalsIgnoreCase(FILTER_PREFIX + i18n.getString("filter.week"))) {
            filteredTasks.setPredicate(showDoneTasks.get() ? FILTER_WEEK : FILTER_WEEK.and(FILTER_NOT_DONE));
        } else if (txt.equalsIgnoreCase(FILTER_PREFIX + i18n.getString("filter.some-day"))) {
            filteredTasks.setPredicate(showDoneTasks.get() ? FILTER_SOME_DAY : FILTER_SOME_DAY.and(FILTER_NOT_DONE));
        } else if (txt.equalsIgnoreCase(FILTER_PREFIX + i18n.getString("filter.overdue"))) {
            filteredTasks.setPredicate(FILTER_OVER_DUE);
        } else {
            String[] queries = txt.toLowerCase().split("\\|");
            filteredTasks.setPredicate((p) -> {
                if (!showDoneTasks.get() && p.isDone())
                    return false;
                for (String query : queries) if (filterKeywords(p.getInfo(), query.split("\\s"))) return true;
                return false;
            });
        }

        refreshTasks();
    }

    public boolean editTask(TDTask tdTask, TDTask.TDTaskField tdTaskField, Object newValue) {
        switch (tdTaskField) {
            case Done:
                tdTask.doneProperty().set((boolean) newValue);
                filterTasks();
                break;
            case Priority:
                tdTask.setPriority((String) newValue);
                table.sort();
                break;
            case CreationDate:
                tdTask.setStartDate((LocalDate) newValue);
                filterTasks();
                break;
            case CompletionDate:
                tdTask.setDone((LocalDate) newValue);
                filterTasks();
                break;
            case DueDate:
                tdTask.setDueDate((LocalDate) newValue);
                filterTasks();
                break;
            case Info:
                tdTask.setInfo((String) newValue);
                filterTasks();
                break;
        }
        table.getSelectionModel().select(tdTask);
        table.requestFocus();
        unsavedController.setSaved(false);
        return true;
    }

    public boolean waitForTracking() {
        if (!trackingController.isTracking())
            return false;

        Dialogs.Result result = Dialogs.showYesNoDialog(
                theStage,
                i18n.getString("dropbox.warning.title"),
                String.format(i18n.getString("track.warning.tracking"), trackingController.getTrackedTask().getInfo()),
                i18n.getString("button.stop-tracking"),
                i18n.getString("button.return"),
                "",
                2);
        if (result == Dialogs.Result.YES) {
            btnPlayStop.fire();
            return false;
        }
        return true;
    }

    public boolean waitForSave() {
        if (unsavedController.isSaved())
            return false;

        Dialogs.Result result = Dialogs.showYesNoDialog(
                theStage,
                i18n.getString("file.warning.title"),
                i18n.getString("file.warning.unsaved-change"),
                i18n.getString("button.save"),
                i18n.getString("button.discard"),
                i18n.getString("button.cancel"),
                3);
        if (result == Dialogs.Result.YES) {
            UIHelper.io.saveFile(false);
        } else if (result == Dialogs.Result.CANCEL) {
            return true;
        }
        return false;
    }

    public void checkForUpdate(final boolean interactive) {
        String[] version = new String[]{""};
        Runnable processResult = () -> {
            if (version[0].isEmpty()) {
                if (interactive && Dialogs.showYesNoDialog(
                        theStage,
                        i18n.getString("update.title"),
                        i18n.getString("update.msg.error"),
                        i18n.getString("button.dismiss"),
                        i18n.getString("button.try-again"),
                        "",
                        2
                ) == Dialogs.Result.NO) {
                    checkForUpdate(true);
                }
            } else if (version[0].equalsIgnoreCase(APP_VERSION)) {
                if (interactive)
                    Dialogs.showMessageDialog(
                            theStage,
                            i18n.getString("update.title"),
                            i18n.getString("update.msg.no"),
                            i18n.getString("button.dismiss")
                    );
            } else {
                if (Dialogs.showYesNoDialog(
                        theStage,
                        i18n.getString("update.title"),
                        String.format(i18n.getString("update.msg.yes"), version[0]),
                        i18n.getString("button.dismiss"),
                        i18n.getString("button.update"),
                        "",
                        2
                ) == Dialogs.Result.NO) {
                    getHostServices().showDocument(Globals.APP_HOME);
                }
            }
        };

        if (interactive) {
            Stage stageWaiting = Dialogs.createWaitingDialog(theStage, i18n.getString("m.working"), "", i18n.getString("button.dismiss"));
            new Thread(() -> {
                version[0] = internetController.getLatestVersion();
                Platform.runLater(stageWaiting::hide);
            }).start();
            stageWaiting.showAndWait();
            processResult.run();
        } else
            new Thread(() -> {
                version[0] = internetController.getLatestVersion();
                Platform.runLater(processResult);
            }).start();
    }

    // Password: kcdf)4SedtSi&?Fs
    // Salt: jshy2Df
    // Key: 3BCrIaPzS5dqelYDAgqZhQ==
    // IV: 59FVE3g+klTYvtgFUgAOhg==
}