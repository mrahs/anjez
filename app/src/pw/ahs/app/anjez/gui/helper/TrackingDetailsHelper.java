/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

  This file is part of Anjez.
  Anjez is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/


package pw.ahs.app.anjez.gui.helper;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import pw.ahs.app.anjez.gui.Dialogs;
import pw.ahs.app.anjez.model.TDDuration;
import pw.ahs.app.anjez.model.TDTask;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static pw.ahs.app.anjez.Globals.*;

public class TrackingDetailsHelper {

    private static TrackingDetailsHelper instance = null;

    public static TrackingDetailsHelper getInstance() {
        if (instance == null)
            instance = new TrackingDetailsHelper();
        return instance;
    }

    private TrackingDetailsHelper() {
    }

    public void showTrackingDetails(Stage theStage, TDTask tdTask) {

        if (tdTask.getInstants().isEmpty()) {
            Dialogs.showMessageDialog(
                    theStage,
                    String.format(i18n.getString("track.details.title"), tdTask.getInfo()),
                    String.format(i18n.getString("track.details.no-details"), tdTask.getInfo()),
                    i18n.getString("button.dismiss")
            );
            return;
        }

        // data
        ObservableList<String[]> items = FXCollections.observableArrayList();
        ObservableList<Instant> data = tdTask.getInstants();
        boolean even = false;
        Instant previousInstant = null;
        for (Instant instant : data) {
            if (even)
                items.add(new String[]{
                        LocalDateTime.ofInstant(previousInstant, ZoneId.systemDefault()).format(DATE_TIME_FORMATTER),
                        LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).format(DATE_TIME_FORMATTER),
                        TDDuration.formatDuration(instant.minusSeconds(previousInstant.getEpochSecond()).getEpochSecond())
                });
            even = !even;
            previousInstant = instant;
        }

        // controls
        VBox layout = new VBox();
        Stage stage = Dialogs.createUtilityDialog(theStage, String.format(i18n.getString("track.details.title"), tdTask.getInfo()), layout);

        Button btnDismiss = new Button(i18n.getString("button.dismiss"));
        btnDismiss.setOnAction(actionEvent -> stage.hide());
        btnDismiss.setCancelButton(true);

        Button btnClear = new Button(i18n.getString("button.clear"));
        btnClear.setOnAction(evt -> {
            if (Dialogs.showYesNoDialog(
                    theStage,
                    i18n.getString("confirm-delete.title"),
                    i18n.getString("track.confirm-delete"),
                    i18n.getString("button.delete"),
                    i18n.getString("button.cancel"),
                    "",
                    2) == Dialogs.Result.YES) {
                tdTask.getInstants().clear();
                unsavedController.setSaved(false);
                btnDismiss.fire();
            }
        });

        TableView<String[]> tblDetails = new TableView<>(items);
        tblDetails.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<String[], String> colFrom = new TableColumn<>(i18n.getString("track.details.from"));
        colFrom.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[0]));
        TableColumn<String[], String> colTo = new TableColumn<>(i18n.getString("track.details.to"));
        colTo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[1]));
        TableColumn<String[], String> colDur = new TableColumn<>(i18n.getString("track.details.duration"));
        colDur.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[2]));

        tblDetails.getColumns().addAll(colFrom, colTo, colDur);

        TextField tfTotal = new TextField(tdTask.getDuration().toString());
        tfTotal.setEditable(false);

        Label lblTotal = new Label(i18n.getString("track.details.total-duration"));
        lblTotal.setLabelFor(tfTotal);

        TextField tfTotalSessions = new TextField("" + items.size());
        tfTotalSessions.setEditable(false);
        tfTotalSessions.setPrefColumnCount(3);

        Label lblTotalSessions = new Label(i18n.getString("track.details.sessions"));
        lblTotalSessions.setLabelFor(tfTotalSessions);

        HBox subLayout = new HBox(lblTotal, tfTotal, lblTotalSessions, tfTotalSessions);
        HBox.setHgrow(tfTotal, Priority.ALWAYS);
        HBox.setHgrow(lblTotal, Priority.NEVER);
        HBox.setHgrow(tfTotalSessions, Priority.SOMETIMES);
        HBox.setHgrow(lblTotalSessions, Priority.NEVER);
        subLayout.setSpacing(2);
        subLayout.setAlignment(Pos.BASELINE_CENTER);

        HBox layoutButtons = new HBox(btnClear, btnDismiss);
        layoutButtons.setSpacing(3);
        layoutButtons.setAlignment(Pos.CENTER);

        layout.getChildren().addAll(tblDetails, subLayout, layoutButtons);
        layout.setSpacing(3);
        layout.setPrefSize(400, 200);
        layout.setAlignment(Pos.CENTER);

        stage.setOnCloseRequest(windowEvent -> {
            windowEvent.consume();
            btnDismiss.fire();
        });

        btnDismiss.requestFocus();
        stage.show();
    }

}
