/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

  This file is part of Anjez.
  Anjez is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/


package pw.ahs.app.anjez.gui.helper;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import pw.ahs.app.anjez.gui.Dialogs;

import static pw.ahs.app.anjez.Globals.i18n;
import static pw.ahs.app.anjez.Globals.view;

public class ShortcutSheetHelper {
    private Tooltip[] tooltips;
    private static ShortcutSheetHelper instance = null;

    public static ShortcutSheetHelper getInstance() {
        if (instance == null)
            instance = new ShortcutSheetHelper();
        return instance;
    }

    private ShortcutSheetHelper() {
    }

    public void init(Tooltip[] tooltips) {
        this.tooltips = tooltips;
    }

    public void showShortcutSheet() {
        // data
        String[][] keymap = new String[][]{
                {i18n.getString("shortcuts-sheet.keymap.focus-menu"), "F10"},
//                {i18n.getString("shortcuts-sheet.keymap.switch-lang"), "CTRL + SHIFT"},
                {i18n.getString("shortcuts-sheet.keymap.new"), "CTRL + N"},
                {i18n.getString("shortcuts-sheet.keymap.open"), "CTRL + O"},
                {i18n.getString("shortcuts-sheet.keymap.reload"), "CTRL + R"},
                {i18n.getString("shortcuts-sheet.keymap.save"), "CTRL + S"},
                {i18n.getString("shortcuts-sheet.keymap.save-as"), "CTRL + SHIFT + S"},
                {i18n.getString("shortcuts-sheet.keymap.open-dropbox"), "CTRL + ALT + O"},
                {i18n.getString("shortcuts-sheet.keymap.reload-dropbox"), "CTRL + ALT + R"},
                {i18n.getString("shortcuts-sheet.keymap.save-dropbox"), "CTRL + ALT + S"},
                {i18n.getString("shortcuts-sheet.keymap.copy"), "CTRL + C"},
                {i18n.getString("shortcuts-sheet.keymap.undo"), "CTRL + U"},
                {i18n.getString("shortcuts-sheet.keymap.redo"), "CTRL + R"},
                {i18n.getString("shortcuts-sheet.keymap.refresh"), "F5"},
                {i18n.getString("shortcuts-sheet.keymap.filter"), "CTRL + F or F3"},
                {i18n.getString("shortcuts-sheet.keymap.auto-complete"), "CTRL + SPACE"},
                {i18n.getString("shortcuts-sheet.keymap.prefs"), "CTRL + P"},
                {i18n.getString("shortcuts-sheet.keymap.help"), "F1"},
                {i18n.getString("shortcuts-sheet.keymap.tour"), "SHIFT + F1"},
                {i18n.getString("shortcuts-sheet.keymap.sheet"), "ALT + ?"},
                {i18n.getString("shortcuts-sheet.keymap.tracking-details"), "ALT + ENTER"},
                {i18n.getString("shortcuts-sheet.keymap.about"), "CTRL + F1"},
                {i18n.getString("shortcuts-sheet.keymap.close"), "CTRL + W"},
                {i18n.getString("shortcuts-sheet.keymap.delete"), "DELETE"},
                {i18n.getString("shortcuts-sheet.keymap.edit"), "F2"},
                {i18n.getString("shortcuts-sheet.keymap.filter-select"), "ESCAPE"},
                {i18n.getString("shortcuts-sheet.keymap.splash"), "CTRL + SHIFT + F1"}
        };

        String[][] filterShortcuts = new String[][]{
                {i18n.getString("shortcuts-sheet.filter.all"), ":all"},
                {i18n.getString("shortcuts-sheet.filter.today"), ":today"},
                {i18n.getString("shortcuts-sheet.filter.tomorrow"), ":next"},
                {i18n.getString("shortcuts-sheet.filter.this-week"), ":week"},
                {i18n.getString("shortcuts-sheet.filter.some-day"), ":later"},
                {i18n.getString("shortcuts-sheet.filter.overdue"), ":overdue"},
        };

        String[][] insertShortcuts = new String[][]{
                {i18n.getString("shortcuts-sheet.insert.today"), "&today"},
                {i18n.getString("shortcuts-sheet.insert.tomorrow"), "&tomorrow"},
        };

        ObservableList<String[]> items = FXCollections.observableArrayList(filterShortcuts);
        items.addAll(keymap);
        items.addAll(insertShortcuts);

        // controls
        VBox layout = new VBox();
        Stage stage = Dialogs.createUtilityDialog(view.getStage(), i18n.getString("shortcuts-sheet.title"), layout);

        Button button = new Button(i18n.getString("button.dismiss"));
        button.setOnAction(evt -> stage.hide());
        button.setDefaultButton(true);
        button.setCancelButton(true);

        TableColumn<String[], String> colAction = new TableColumn<>(i18n.getString("shortcuts-sheet.col.action"));
        colAction.setCellValueFactory(p -> new SimpleObjectProperty<>(p.getValue()[0]));
        TableColumn<String[], String> colShortcut = new TableColumn<>(i18n.getString("shortcuts-sheet.col.shortcut"));
        colShortcut.setCellValueFactory(p -> new SimpleObjectProperty<>(p.getValue()[1]));

        TableView<String[]> table = new TableView<>(items);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getColumns().setAll(colAction, colShortcut);
        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        table.setPlaceholder(new Label());

        TextField tfFilter = new TextField();
        tfFilter.setPromptText(i18n.getString("filter.prompt"));
        tfFilter.textProperty().addListener((observableValue, s, s2) -> {
            if (s2.isEmpty())
                table.setItems(items);
            else
                table.setItems(items.filtered(p ->
                        p[0].toLowerCase().contains(s2.toLowerCase()) || p[1].toLowerCase().contains(s2.toLowerCase())));
        });

        ToggleButton tbKeymap = new ToggleButton(i18n.getString("shortcuts-sheet.show-keymap.title"));
        tbKeymap.setTooltip(tooltips[15]);
        tbKeymap.getTooltip().setText(i18n.getString("shortcuts-sheet.show-keymap.tooltip"));
        tbKeymap.getStyleClass().addAll("left-pill");
        tbKeymap.setSelected(true);
        tbKeymap.selectedProperty().addListener((observableValue, aBoolean, aBoolean2) -> {
            if (aBoolean2)
                items.addAll(keymap);
            else
                items.removeAll(keymap);
        });

        ToggleButton tbFilter = new ToggleButton(i18n.getString("shortcuts-sheet.show-filter.title"));
        tbFilter.setTooltip(tooltips[16]);
        tbFilter.getTooltip().setText(i18n.getString("shortcuts-sheet.show-filter.tooltip"));
        tbFilter.getStyleClass().addAll("center-pill");
        tbFilter.setSelected(true);
        tbFilter.selectedProperty().addListener((observableValue, aBoolean, aBoolean2) -> {
            if (aBoolean2)
                items.addAll(filterShortcuts);
            else
                items.removeAll(filterShortcuts);
        });

        ToggleButton tbInsert = new ToggleButton(i18n.getString("shortcuts-sheet.show-insert.title"));
        tbInsert.setTooltip(tooltips[17]);
        tbInsert.getTooltip().setText(i18n.getString("shortcuts-sheet.show-insert.tooltip"));
        tbInsert.getStyleClass().addAll("right-pill");
        tbInsert.setSelected(true);
        tbInsert.selectedProperty().addListener((observableValue, aBoolean, aBoolean2) -> {
            if (aBoolean2)
                items.addAll(insertShortcuts);
            else
                items.removeAll(insertShortcuts);
        });

        HBox layoutFilter = new HBox(tfFilter, new HBox(tbKeymap, tbFilter, tbInsert));
        HBox.setHgrow(tfFilter, Priority.ALWAYS);
        layoutFilter.setSpacing(3);
        layoutFilter.setAlignment(Pos.CENTER);

        layout.getChildren().addAll(layoutFilter, table, button);
        layout.setSpacing(5);
        layout.setAlignment(Pos.CENTER);
        layout.setPrefSize(400, 200);

        Scene scene = stage.getScene();
        scene.getAccelerators().put(KeyCombination.valueOf("shortcut+F"), () -> {
            tfFilter.selectAll();
            tfFilter.requestFocus();
        });

        tfFilter.requestFocus();

        stage.setOnCloseRequest(windowEvent -> {
            windowEvent.consume();
            button.fire();
        });

        stage.show();
    }
}
