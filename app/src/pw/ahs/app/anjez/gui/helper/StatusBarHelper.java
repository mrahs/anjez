/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

  This file is part of Anjez.
  Anjez is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/


package pw.ahs.app.anjez.gui.helper;

import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.shape.SVGPath;
import pw.ahs.app.anjez.model.TDDuration;
import pw.ahs.app.anjez.model.TDTask;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static pw.ahs.app.anjez.Globals.*;
import static pw.ahs.app.anjez.controller.PrefsController.pShow_Status_Bar;

public class StatusBarHelper {

    private final Label lblTotal = new Label();
    private final Label lblToday = new Label();
    private final Label lblTomorrow = new Label();
    private final Label lblThisWeek = new Label();
    private final Label lblLater = new Label();
    private final Label lblTooLate = new Label();

    private final Label lblTotalText = new Label();
    private final Label lblTodayText = new Label();
    private final Label lblTomorrowText = new Label();
    private final Label lblThisWeekText = new Label();
    private final Label lblLaterText = new Label();
    private final Label lblTooLateText = new Label();

    private static StatusBarHelper instance = null;

    public static StatusBarHelper getInstance() {
        if (instance == null)
            instance = new StatusBarHelper();
        return instance;
    }

    private StatusBarHelper() {
    }

    public void updateNumbers(int total, int today, int tomorrow, int week, int later, int tooLate) {
        if (total == 0) {
            lblTotal.setText("");
            if (view.getTasks().size() > 0)
                lblTotalText.setText("0 " + i18n.getString("m.of") + " " + view.getTasks().size());
            else
                lblTotalText.setText(i18n.getString("status.no-tasks"));
        } else {
            lblTotal.setText(total + "");
            lblTotalText.setText(" " + i18n.getString("m.of") + " " + view.getTasks().size() + " " + (view.getTasks().size() > 1 ? i18n.getString("m.tasks") : i18n.getString("m.task")));
        }

        if (today == 0) {
            lblTodayText.setText("");
            lblToday.setText("");
            lblToday.getStyleClass().removeAll("notify-today");
        } else {
            lblToday.setText(today + "");
            lblTodayText.setText(" " + i18n.getString("status.today"));
            lblToday.getStyleClass().addAll("notify-today");
        }

        if (tomorrow == 0) {
            lblTomorrow.setText("");
            lblTomorrowText.setText("");
        } else {
            lblTomorrow.setText(tomorrow + "");
            lblTomorrowText.setText(" " + i18n.getString("status.tomorrow"));
        }

        if (week == 0) {
            lblThisWeek.setText("");
            lblThisWeekText.setText("");
        } else {
            lblThisWeek.setText(week + "");
            lblThisWeekText.setText(" " + i18n.getString("status.this-week"));
        }

        if (later == 0) {
            lblLater.setText("");
            lblLaterText.setText("");
        } else {
            lblLater.setText(later + "");
            lblLaterText.setText(" " + i18n.getString("status.some-day"));
        }

        if (tooLate == 0) {
            lblTooLate.setText("");
            lblTooLateText.setText("");
            lblTooLate.getStyleClass().removeAll("notify-too-late");
        } else {
            lblTooLate.setText(tooLate + "");
            lblTooLateText.setText(" " + i18n.getString("status.overdue"));
            lblTooLate.getStyleClass().addAll("notify-too-late");
        }
    }

    public void init(
            GridPane statusBar,
            Tooltip[] tooltips,
            ObservableList<TDTask> tasks,
            TextField filterBox,
            TableView<TDTask> table,
            BooleanProperty showDoneTasks,
            Button btnPlayStop
    ) {
        statusBar.setId("status-bar");

        // don't layout when not visible (hide place holder)
        statusBar.managedProperty().bind(statusBar.visibleProperty());

        statusBar.setAlignment(Pos.BASELINE_CENTER);
        statusBar.setHgap(5);
        ColumnConstraints ccTotal = new ColumnConstraints(); // total tasks
        ColumnConstraints ccToday = new ColumnConstraints(); // today's tasks
        ColumnConstraints ccTomorrow = new ColumnConstraints(); // tomorrow's tasks
        ColumnConstraints ccThisWeek = new ColumnConstraints(); // this week's tasks
        ColumnConstraints ccLater = new ColumnConstraints(); // later tasks
        ColumnConstraints ccTooLate = new ColumnConstraints(); // too late tasks
        ColumnConstraints ccTrack = new ColumnConstraints(); // tracking controls
        ColumnConstraints ccShowDone = new ColumnConstraints(); // show/hide done task

        ccTotal.setHgrow(Priority.ALWAYS);
        ccToday.setHgrow(Priority.ALWAYS);
        ccTomorrow.setHgrow(Priority.ALWAYS);
        ccThisWeek.setHgrow(Priority.ALWAYS);
        ccLater.setHgrow(Priority.ALWAYS);
        ccTooLate.setHgrow(Priority.ALWAYS);
        ccTrack.setHgrow(Priority.NEVER);
        ccShowDone.setHgrow(Priority.NEVER);

        statusBar.getColumnConstraints().addAll(ccTotal, ccToday, ccTomorrow, ccThisWeek, ccLater, ccTooLate, ccTrack, ccShowDone);

        lblTotal.setTooltip(tooltips[2]);
        lblTotalText.setTooltip(tooltips[3]);

        lblToday.setTooltip(tooltips[4]);
        lblTodayText.setTooltip(tooltips[5]);

        lblTomorrow.setTooltip(tooltips[6]);
        lblTomorrowText.setTooltip(tooltips[7]);

        lblThisWeek.setTooltip(tooltips[8]);
        lblThisWeekText.setTooltip(tooltips[9]);

        lblLater.setTooltip(tooltips[10]);
        lblLaterText.setTooltip(tooltips[11]);

        lblTooLate.setTooltip(tooltips[12]);
        lblTooLateText.setTooltip(tooltips[13]);

        lblTotalText.setMaxWidth(Double.MAX_VALUE);
        lblTodayText.setMaxWidth(Double.MAX_VALUE);
        lblTomorrowText.setMaxWidth(Double.MAX_VALUE);
        lblThisWeekText.setMaxWidth(Double.MAX_VALUE);
        lblLaterText.setMaxWidth(Double.MAX_VALUE);
        lblTooLateText.setMaxWidth(Double.MAX_VALUE);

        HBox containerTotal = new HBox(lblTotal, lblTotalText);
        HBox containerToday = new HBox(lblToday, lblTodayText);
        HBox containerTomorrow = new HBox(lblTomorrow, lblTomorrowText);
        HBox containerThisWeek = new HBox(lblThisWeek, lblThisWeekText);
        HBox containerLater = new HBox(lblLater, lblLaterText);
        HBox containerTooLate = new HBox(lblTooLate, lblTooLateText);

        HBox.setHgrow(lblTotalText, Priority.ALWAYS);
        HBox.setHgrow(lblTodayText, Priority.ALWAYS);
        HBox.setHgrow(lblTomorrowText, Priority.ALWAYS);
        HBox.setHgrow(lblThisWeekText, Priority.ALWAYS);
        HBox.setHgrow(lblLaterText, Priority.ALWAYS);
        HBox.setHgrow(lblTooLateText, Priority.ALWAYS);

        containerTotal.setCursor(Cursor.HAND);
        containerToday.setCursor(Cursor.HAND);
        containerTomorrow.setCursor(Cursor.HAND);
        containerThisWeek.setCursor(Cursor.HAND);
        containerLater.setCursor(Cursor.HAND);
        containerTooLate.setCursor(Cursor.HAND);


        containerTotal.setOnMouseClicked(mouseEvent -> {
            filterBox.requestFocus();
            filterBox.setText(FILTER_PREFIX + i18n.getString("filter.all"));
            filterBox.selectAll();
            view.filterTasks();
        });
        containerToday.setOnMouseClicked(mouseEvent -> {
            filterBox.requestFocus();
            filterBox.setText(FILTER_PREFIX + i18n.getString("filter.today"));
            filterBox.selectAll();
            view.filterTasks();
        });
        containerTomorrow.setOnMouseClicked(mouseEvent -> {
            filterBox.requestFocus();
            filterBox.setText(FILTER_PREFIX + i18n.getString("filter.next"));
            filterBox.selectAll();
            view.filterTasks();
        });
        containerThisWeek.setOnMouseClicked(mouseEvent -> {
            filterBox.requestFocus();
            filterBox.setText(FILTER_PREFIX + i18n.getString("filter.week"));
            filterBox.selectAll();
            view.filterTasks();
        });
        containerLater.setOnMouseClicked(mouseEvent -> {
            filterBox.requestFocus();
            filterBox.setText(FILTER_PREFIX + i18n.getString("filter.some-day"));
            filterBox.selectAll();
            view.filterTasks();
        });
        containerTooLate.setOnMouseClicked(mouseEvent -> {
            filterBox.requestFocus();
            filterBox.setText(FILTER_PREFIX + i18n.getString("filter.overdue"));
            filterBox.selectAll();
            view.filterTasks();
        });


        CheckBox cbShowDoneTasks = new CheckBox();
        cbShowDoneTasks.setTooltip(tooltips[14]);
        cbShowDoneTasks.selectedProperty().bindBidirectional(showDoneTasks);

        table.setOnMouseEntered(mouseEvent -> {
            if (!statusBar.isVisible() && trackingController.isTracking())
                statusBar.setVisible(true);
        });
        table.setOnMouseExited(mouseEvent -> statusBar.setVisible(prefsController.getPrefBoolean(pShow_Status_Bar)));
        statusBar.setOnMouseEntered(mouseEvent -> statusBar.setVisible(true));
        statusBar.setOnMouseExited(mouseEvent -> statusBar.setVisible(prefsController.getPrefBoolean(pShow_Status_Bar)));

        statusBar.addRow(0,
                containerTotal,
                containerToday,
                containerTomorrow,
                containerThisWeek,
                containerLater,
                containerTooLate,
                initTimeTrackingControls(
                        tooltips,
                        table,
                        btnPlayStop
                ),
                cbShowDoneTasks);

        Function<String, Void> statusLangListener = (s) -> {
            cbShowDoneTasks.getTooltip().setText(i18n.getString("m.show-hide-done-task"));
            lblToday.getTooltip().setText(i18n.getString("status.filter-today"));
            lblTodayText.getTooltip().setText(i18n.getString("status.filter-today"));

            lblTomorrow.getTooltip().setText(i18n.getString("status.filter-next"));
            lblTomorrowText.getTooltip().setText(i18n.getString("status.filter-next"));

            lblThisWeek.getTooltip().setText(i18n.getString("status.filter-week"));
            lblThisWeekText.getTooltip().setText(i18n.getString("status.filter-week"));

            lblTotal.getTooltip().setText(i18n.getString("status.filter-all"));
            lblTotalText.getTooltip().setText(i18n.getString("status.filter-all"));

            lblLater.getTooltip().setText(i18n.getString("status.filter-some-day"));
            lblLaterText.getTooltip().setText(i18n.getString("status.filter-some-day"));

            lblTooLate.getTooltip().setText(i18n.getString("status.filter-overdue"));
            lblTooLateText.getTooltip().setText(i18n.getString("status.filter-overdue"));

            if (tasks.isEmpty()) {
                lblTotalText.setText(i18n.getString("status.no-tasks"));
                lblTodayText.setText("");
                lblTomorrowText.setText("");
                lblThisWeekText.setText("");
            } else {
                lblTotalText.setText(" " + i18n.getString("m.of") + " " + tasks.size() + " " + (tasks.size() > 1 ? i18n.getString("m.tasks") : i18n.getString("m.task")));
                if (!lblTodayText.getText().isEmpty())
                    lblTodayText.setText(" " + i18n.getString("status.today"));
                if (!lblTodayText.getText().isEmpty())
                    lblTomorrowText.setText(" " + i18n.getString("status.tomorrow"));
                if (!lblThisWeekText.getText().isEmpty())
                    lblThisWeekText.setText(" " + i18n.getString("status.this-week"));
                if (!lblLaterText.getText().isEmpty())
                    lblLaterText.setText(" " + i18n.getString("status.some-day"));
                if (!lblTooLateText.getText().isEmpty())
                    lblTooLateText.setText(" " + i18n.getString("status.overdue"));
            }

            return null;
        };
        statusLangListener.apply(null);
        // add language listener
        currentLangController.addListener(statusLangListener);
    }

    private static HBox initTimeTrackingControls(
            Tooltip[] tooltips,
            TableView<TDTask> table,
            Button btnPlayStop
    ) {
        SimpleBooleanProperty showSessionDuration = new SimpleBooleanProperty(true);
        final ScheduledThreadPoolExecutor[] schedulerTracking = new ScheduledThreadPoolExecutor[1];

        Label lblDuration = new Label("00");
        lblDuration.setCursor(Cursor.HAND);
        lblDuration.setTooltip(tooltips[1]);
        lblDuration.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getButton() == MouseButton.PRIMARY && trackingController.getTrackedTask() != null) {
                table.getSelectionModel().clearSelection();
                table.getSelectionModel().select(trackingController.getTrackedTask());
            } else if (mouseEvent.getButton() == MouseButton.SECONDARY) {
                showSessionDuration.set(!showSessionDuration.get());
                if (showSessionDuration.get())
                    lblDuration.getTooltip().setText(i18n.getString("track.session-duration") + "\n" + i18n.getString("track.duration-msg"));
                else
                    lblDuration.getTooltip().setText(i18n.getString("track.total-duration") + "\n" + i18n.getString("track.duration-msg"));
                if (trackingController.isTracking())
                    lblDuration.setText(TDDuration.formatDuration(trackingController.getDurationSeconds() + (showSessionDuration.get() ? 0 : trackingController.getTrackedTask().getDuration().getSeconds())));
                else if (trackingController.getTrackedTask() != null) {
                    lblDuration.setText(TDDuration.formatDuration(showSessionDuration.get() ? trackingController.getDurationSeconds() : trackingController.getTrackedTask().getDuration().getSeconds()));
                }
            }
        });

        btnPlayStop.setId("button-play-stop");
        btnPlayStop.setMaxSize(20, 20);
        btnPlayStop.setMinSize(10, 10);
        SVGPath svgPathTriangle = new SVGPath();
        svgPathTriangle.setContent("M 0 0 L 8 4 L 0 8 Z");
        svgPathTriangle.setId("button-play-icon");
        SVGPath svgPathSquare = new SVGPath();
        svgPathSquare.setContent("M 0 0 H 8 V 8 H 0 Z");
        svgPathSquare.setId("button-stop-icon");
        btnPlayStop.setGraphic(svgPathTriangle);
        btnPlayStop.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        btnPlayStop.setOnAction(actionEvent -> {
            if (trackingController.isTracking()) {
                // stop
                trackingController.stopTracking();
                lblDuration.setText(TDDuration.formatDuration(showSessionDuration.get() ? trackingController.getDurationSeconds() : trackingController.getTrackedTask().getDuration().getSeconds()));
                btnPlayStop.setGraphic(svgPathTriangle);
                schedulerTracking[0].shutdownNow();
                unsavedController.setSaved(false);
            } else {
                // play
                trackingController.startTracking(table.getSelectionModel().getSelectedItem());
                btnPlayStop.setGraphic(svgPathSquare);
                lblDuration.setText("00");
                schedulerTracking[0] = new ScheduledThreadPoolExecutor(1);
                schedulerTracking[0].scheduleAtFixedRate(() ->
                        Platform.runLater(() ->
                                lblDuration.setText(
                                        TDDuration.formatDuration(
                                                trackingController.getDurationSeconds() + (showSessionDuration.get() ? 0 : trackingController.getTrackedTask().getDuration().getSeconds())
                                        )
                                )
                        )
                        , 0, 1, TimeUnit.SECONDS);
            }
        });
        btnPlayStop.disableProperty().bind(new BooleanBinding() {
            {
                bind(table.getSelectionModel().selectedIndexProperty());
            }

            @Override
            protected boolean computeValue() {
                return !trackingController.isTracking() && (table.getSelectionModel().getSelectedIndex() < 0 || table.getSelectionModel().getSelectedItems().size() > 1);
            }
        });

        HBox layout = new HBox(btnPlayStop, lblDuration);
        layout.setSpacing(2);
        layout.setAlignment(Pos.CENTER);

        beforeClosingController.addAction(() -> {
            if (schedulerTracking[0] != null && !schedulerTracking[0].isShutdown()) {
                schedulerTracking[0].shutdownNow();
                schedulerTracking[0] = null;
            }
        });

        Function<String, Void> trackingLangListener = newLang -> {
            if (showSessionDuration.get())
                lblDuration.getTooltip().setText(i18n.getString("track.session-duration") + "\n" + i18n.getString("track.duration-msg"));
            else
                lblDuration.getTooltip().setText(i18n.getString("track.total-duration") + "\n" + i18n.getString("track.duration-msg"));

            return null;
        };
        trackingLangListener.apply(null);
        currentLangController.addListener(trackingLangListener);

        return layout;
    }

}
