/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

  This file is part of Anjez.
  Anjez is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/

package pw.ahs.app.anjez.gui.helper;

import javafx.collections.transformation.SortedList;
import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import pw.ahs.app.anjez.command.Command;
import pw.ahs.app.anjez.command.EditTaskCommand;
import pw.ahs.app.anjez.gui.UIHelper;
import pw.ahs.app.anjez.gui.tablecell.CheckBoxTableCell;
import pw.ahs.app.anjez.gui.tablecell.ComboBoxTableCell;
import pw.ahs.app.anjez.gui.tablecell.DatePickerTableCell;
import pw.ahs.app.anjez.gui.tablecell.TextFieldTableCell;
import pw.ahs.app.anjez.model.TDDuration;
import pw.ahs.app.anjez.model.TDTask;

import java.time.LocalDate;
import java.util.function.Function;

import static pw.ahs.app.anjez.Globals.*;

public class TableHelper {

    private static TableHelper instance = null;

    public static TableHelper getInstance() {
        if (instance == null)
            instance = new TableHelper();
        return instance;
    }

    private TableHelper() {
    }

    @SuppressWarnings("unchecked") // generics! (find it at // generics!)
    public void init(
            TableView<TDTask> table,
            SortedList<TDTask> sortedTasks,
            ContextMenu[] cMenus,
            Button btnPlayStop
    ) {
        // columns
        int colMinSize = 25;
        int colDateMinSize = 100;
        int colDateMaxSize = 150;

        TableColumn<TDTask, Boolean> colDone;
        TableColumn<TDTask, String> colPriority;
        TableColumn<TDTask, LocalDate> colStartDate;
        TableColumn<TDTask, LocalDate> colDueDate;
        TableColumn<TDTask, LocalDate> colDoneDate;
        TableColumn<TDTask, String> colInfo;
        TableColumn<TDTask, TDDuration> colDuration;

        colDone = new TableColumn<>();
        colDone.setId("Column_Done");
        colDone.setMinWidth(colMinSize);
        colDone.setMaxWidth(colMinSize * 2);
        colDone.setPrefWidth(colDone.getMaxWidth());
        colDone.setCellValueFactory(data -> data.getValue().doneProperty());
        colDone.setCellFactory(CheckBoxTableCell::new);
        colDone.setOnEditCommit(event -> {
            undoRedoController.pushCommand(
                    new EditTaskCommand(
                            view,
                            event.getRowValue(),
                            TDTask.TDTaskField.Done,
                            event.getOldValue(),
                            event.getNewValue()
                    )
            );
            view.filterTasks();
            unsavedController.setSaved(false);
        });
        cMenus[1].getItems().add(new MenuItem(i18n.getString("col.done")));
        colDone.setContextMenu(cMenus[1]);

        colDoneDate = new TableColumn<>();
        colDoneDate.setId("Column_Done_Date");
        colDoneDate.setMinWidth(colDateMinSize);
        colDoneDate.setMaxWidth(colDateMaxSize);
        colDoneDate.setPrefWidth(colDoneDate.getMinWidth());
        colDoneDate.setComparator(THE_DATE_COMPARATOR);
        colDoneDate.setCellValueFactory(data -> data.getValue().doneDateProperty());
        colDoneDate.setCellFactory(data -> new DatePickerTableCell<>());
        colDoneDate.setOnEditCommit(event -> {
            Command cmd = new EditTaskCommand(
                    view,
                    event.getRowValue(),
                    TDTask.TDTaskField.CompletionDate,
                    event.getOldValue(),
                    event.getNewValue()
            );
            undoRedoController.executeThenPushCommand(cmd);
        });
        cMenus[2].getItems().add(new MenuItem(i18n.getString("col.done-date")));
        colDoneDate.setContextMenu(cMenus[2]);

        colPriority = new TableColumn<>();
        colPriority.setId("Column_Priority");
        colPriority.setMinWidth(colMinSize);
        colPriority.setMaxWidth(colMinSize * 2);
        colPriority.setPrefWidth(colPriority.getMaxWidth());
        colPriority.setComparator(THE_TEXT_COMPARATOR);
        colPriority.setCellValueFactory(data -> data.getValue().priorityProperty());
        colPriority.setCellFactory(ComboBoxTableCell.forTableColumn(TDTask.PRIORITY_CHARS, new StringConverter<String>() {
            @Override
            public String toString(String s) {
                if (s.isEmpty()) return "";
                if (TDTask.PRIORITY_CHARS.contains(s))
                    return i18n.getString("todo-txt." + s);
                else
                    return PRIORITY_CHARS_I18N.get(s);
            }

            @Override
            public String fromString(String s) {
                if (s.isEmpty()) return "";
                if (TDTask.PRIORITY_CHARS.contains(s))
                    return i18n.getString("todo-txt." + s);
                else
                    return PRIORITY_CHARS_I18N.get(s);
            }
        }));
        colPriority.setOnEditCommit(event -> {
            Command cmd = new EditTaskCommand(
                    view,
                    event.getRowValue(),
                    TDTask.TDTaskField.Priority,
                    event.getOldValue(),
                    event.getNewValue()
            );
            undoRedoController.executeThenPushCommand(cmd);
        });
        // a workaround to refresh column values
        currentLangController.addListener((s) -> {
            if (colPriority.isVisible()) {
                colPriority.setVisible(false);
                colPriority.setVisible(true);
            }
            return null;
        });
        cMenus[3].getItems().add(new MenuItem(i18n.getString("col.priority")));
        colPriority.setContextMenu(cMenus[3]);

        colStartDate = new TableColumn<>();
        colStartDate.setId("Column_Start_Date");
        colStartDate.setMinWidth(colDateMinSize);
        colStartDate.setMaxWidth(colDateMaxSize);
        colStartDate.setPrefWidth(colStartDate.getMinWidth());
        colStartDate.setComparator(THE_DATE_COMPARATOR);
        colStartDate.setCellValueFactory(data -> data.getValue().startDateProperty());
        colStartDate.setCellFactory(data -> new DatePickerTableCell<>());
        colStartDate.setOnEditCommit(event -> {
            Command cmd = new EditTaskCommand(
                    view,
                    event.getRowValue(),
                    TDTask.TDTaskField.CreationDate,
                    event.getOldValue(),
                    event.getNewValue()
            );
            undoRedoController.executeThenPushCommand(cmd);
        });
        cMenus[4].getItems().add(new MenuItem(i18n.getString("col.start-date")));
        colStartDate.setContextMenu(cMenus[4]);

        colDueDate = new TableColumn<>();
        colDueDate.setId("Column_Due_Date");
        colDueDate.setMinWidth(colDateMinSize);
        colDueDate.setMaxWidth(colDateMaxSize);
        colDueDate.setPrefWidth(colDueDate.getMinWidth());
        colDueDate.setComparator(THE_DATE_COMPARATOR);
        colDueDate.setCellValueFactory(data -> data.getValue().dueDateProperty());
        colDueDate.setCellFactory(data -> new DatePickerTableCell<>());
        colDueDate.setOnEditCommit(event -> {
            Command cmd = new EditTaskCommand(
                    view,
                    event.getRowValue(),
                    TDTask.TDTaskField.DueDate,
                    event.getOldValue(),
                    event.getNewValue()
            );
            undoRedoController.executeThenPushCommand(cmd);
        });
        cMenus[5].getItems().add(new MenuItem(i18n.getString("col.due-date")));
        colDueDate.setContextMenu(cMenus[5]);

        colInfo = new TableColumn<>();
        colInfo.setId("Column_Info");
        colInfo.setMinWidth(colMinSize);
        colInfo.setPrefWidth(colMinSize * 10);
        colInfo.setComparator(THE_TEXT_COMPARATOR);
        colInfo.setCellValueFactory(data -> data.getValue().infoProperty());
        colInfo.setCellFactory(TextFieldTableCell.forTableColumn());
        colInfo.setOnEditCommit(event -> {
            Command cmd = new EditTaskCommand(
                    view,
                    event.getRowValue(),
                    TDTask.TDTaskField.Info,
                    event.getOldValue(),
                    event.getNewValue()
            );
            undoRedoController.executeThenPushCommand(cmd);
        });
        cMenus[6].getItems().add(new MenuItem(i18n.getString("col.info")));
        colInfo.setContextMenu(cMenus[6]);

        colDuration = new TableColumn<>();
        colDuration.setId("Column_Duration");
        colDuration.setMinWidth(colMinSize);
        colDuration.setPrefWidth(colDateMinSize);
        colDuration.setCellValueFactory(data -> data.getValue().durationProperty());
        colDuration.setEditable(false);
        cMenus[7].getItems().add(new MenuItem(i18n.getString("col.duration")));
        colDuration.setContextMenu(cMenus[7]);

        MenuItem cMenuTableDel = new MenuItem();
        cMenuTableDel.setAccelerator(KeyCombination.valueOf("DELETE"));
        cMenuTableDel.setOnAction(actionEvent -> view.deleteSelectedItems());

        MenuItem cMenuTableEdit = new MenuItem();
        cMenuTableEdit.setAccelerator(KeyCombination.valueOf("F2"));
        cMenuTableEdit.setOnAction(actionEvent -> {
            TableColumn selectedColumn = table.getSelectionModel().getSelectedCells().get(0).getTableColumn();
            // generics!
            table.edit(table.getSelectionModel().getSelectedIndex(), selectedColumn);
        });

        MenuItem cMenuTableCopy = new MenuItem();
        cMenuTableCopy.setAccelerator(KeyCombination.valueOf("shortcut+c"));
        cMenuTableCopy.setOnAction(actionEvent -> controller.copy(table.getSelectionModel().getSelectedItems()));

        MenuItem cMenuTableTrackDetails = new MenuItem();
        cMenuTableTrackDetails.setAccelerator(KeyCombination.valueOf("alt+enter"));
        cMenuTableTrackDetails.setOnAction(actionEvent ->
                UIHelper.tracking.showTrackingDetails(view.getStage(), table.getSelectionModel().getSelectedItem())
        );

        MenuItem cMenuTablePlayStop = new MenuItem();
        cMenuTablePlayStop.setOnAction(actionEvent -> {
            btnPlayStop.fire();
            if (trackingController.isTracking())
                cMenuTablePlayStop.setText(i18n.getString("c-menu.stop"));
            else
                cMenuTablePlayStop.setText(i18n.getString("c-menu.play"));
        });

        cMenus[0].getItems().addAll(
                cMenuTableEdit,
                cMenuTableDel,
                cMenuTableCopy,
                cMenuTablePlayStop,
                cMenuTableTrackDetails
        );
        table.setContextMenu(cMenus[0]);

        table.setOnContextMenuRequested(contextMenuEvent -> {
            if (table.getEditingCell() != null) {
                table.getContextMenu().hide();
                return;
            }

            int c = table.getSelectionModel().getSelectedIndices().size();
            if (c < 1) {
                contextMenuEvent.consume();
                table.getContextMenu().hide();
            } else if (c > 1) {
                cMenuTableEdit.setVisible(false);
                cMenuTableDel.setVisible(true);
                cMenuTableCopy.setVisible(true);
                cMenuTablePlayStop.setVisible(false);
                cMenuTableTrackDetails.setVisible(false);
            } else if (c == 1) {
                cMenuTableEdit.setVisible(true);
                cMenuTableDel.setVisible(true);
                cMenuTableCopy.setVisible(true);
                if (!trackingController.isTracking() ||
                        table.getSelectionModel().getSelectedItem().equals(trackingController.getTrackedTask())) {
                    if (trackingController.isTracking())
                        cMenuTablePlayStop.setText(i18n.getString("c-menu.stop"));
                    else
                        cMenuTablePlayStop.setText(i18n.getString("c-menu.play"));
                    cMenuTablePlayStop.setVisible(true);
                } else {
                    cMenuTablePlayStop.setVisible(false);
                }
                cMenuTableTrackDetails.setVisible(true);
            }
        });

        Label lblTblPlaceHolderTitle = new Label("");
        lblTblPlaceHolderTitle.setId("table-place-holder-title");
        Label lblTblPlaceHolderMsg = new Label("");
        VBox tblPlaceHolder = new VBox(
                lblTblPlaceHolderTitle,
                new Separator(),
                lblTblPlaceHolderMsg
        );
        tblPlaceHolder.setSpacing(5);
        tblPlaceHolder.setMaxHeight(Region.USE_PREF_SIZE);
        tblPlaceHolder.setMaxWidth(Region.USE_PREF_SIZE);

        table.setEditable(true);
        table.setTableMenuButtonVisible(true);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        table.getSelectionModel().setCellSelectionEnabled(true);
        table.getColumns().addAll(colInfo, colPriority, colStartDate, colDueDate, colDone, colDoneDate, colDuration);
        table.setPlaceholder(tblPlaceHolder);
        table.addEventFilter(KeyEvent.ANY, event -> {
            if (!event.getCode().isArrowKey()) return;
            TableView.TableViewSelectionModel<TDTask> tableSelectionModel = table.getSelectionModel();
            if (!tableSelectionModel.isEmpty()) return;
            if (event.getEventType() != KeyEvent.KEY_RELEASED) return;
            if (tableSelectionModel.isCellSelectionEnabled())
                tableSelectionModel.select(0, table.getVisibleLeafColumn(0));
            else
                tableSelectionModel.selectFirst();
            event.consume();
        });

        table.setItems(sortedTasks);
        sortedTasks.comparatorProperty().bind(table.comparatorProperty());

        Function<String, Void> tableLangListener = (s) -> {
            colDone.setText(i18n.getString("col.done"));
            colDoneDate.setText(i18n.getString("col.done-date"));
            colPriority.setText(i18n.getString("col.priority"));
            colStartDate.setText(i18n.getString("col.start-date"));
            colDueDate.setText(i18n.getString("col.due-date"));
            colInfo.setText(i18n.getString("col.info"));
            colDuration.setText(i18n.getString("col.duration"));

            cMenuTableDel.setText(i18n.getString("c-menu.del"));
            cMenuTableEdit.setText(i18n.getString("c-menu.edit"));
            cMenuTableCopy.setText(i18n.getString("c-menu.copy"));
            cMenuTablePlayStop.setText(trackingController.isTracking() ? i18n.getString("c-menu.stop") : i18n.getString("c-menu.play"));
            cMenuTableTrackDetails.setText(i18n.getString("c-menu.track-details"));

            lblTblPlaceHolderTitle.setText(i18n.getString("m.table-no-tasks.title"));
            lblTblPlaceHolderMsg.setText(i18n.getString("m.table-no-tasks.msg"));

            return null;
        };
        tableLangListener.apply(null);

        // add language listener
        currentLangController.addListener(tableLangListener);
    }
}
