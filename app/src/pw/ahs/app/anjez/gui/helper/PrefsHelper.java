/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

  This file is part of Anjez.
  Anjez is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/


package pw.ahs.app.anjez.gui.helper;

import javafx.beans.property.BooleanProperty;
import javafx.collections.ObservableList;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import pw.ahs.app.anjez.gui.App;
import pw.ahs.app.anjez.gui.Dialogs;
import pw.ahs.app.anjez.model.TDTask;

import static pw.ahs.app.anjez.Globals.*;
import static pw.ahs.app.anjez.controller.PrefsController.*;

public class PrefsHelper {

    private TableView<TDTask> table;
    private BooleanProperty showDoneTasks;
    private GridPane statusBar;
    private final String cssCustom = getClass().getResource("/pw/ahs/app/anjez/gui/css/custom-style.bss").toExternalForm();
    private Font fontAr = null;
    private Font fontEn = null;

    private static PrefsHelper instance = null;

    public static PrefsHelper getInstance() {
        if (instance == null)
            instance = new PrefsHelper();
        return instance;
    }

    private PrefsHelper() {
    }

    public void init(
            TableView<TDTask> table,
            BooleanProperty showDoneTasks,
            GridPane statusBar
    ) {
        this.table = table;
        this.showDoneTasks = showDoneTasks;
        this.statusBar = statusBar;
        beforeClosingController.addAction(this::save);
    }

    public void apply() {
        // last opened file is applied in start

        // this will trigger all language listeners
        currentLangController.setLang(prefsController.getPref(pLang));

        boolean forgetSortState = !prefsController.getPrefBoolean(pRemember_Sort_State);
        boolean forgetColOrder = !prefsController.getPrefBoolean(pRemember_Column_Order);
        boolean forgetColV = !prefsController.getPrefBoolean(pRemember_Column_Visibility);

        for (TableColumn col : table.getColumns()) {
            if (forgetSortState)
                prefsController.restoreToDefault(col.getId() + "_Sort_Order");
            if (forgetColOrder)
                prefsController.restoreToDefault(col.getId() + "_Order");
            if (forgetColV) {
                prefsController.restoreToDefault(col.getId() + "_Visibility");
            }
            col.setVisible(prefsController.getPrefBoolean(col.getId() + "_Visibility"));
        }

        // Columns Sort State
        ObservableList<TableColumn<TDTask, ?>> columns = table.getColumns();
        ObservableList<TableColumn<TDTask, ?>> sortOrderList = table.getSortOrder();
        sortOrderList.clear();
        sortOrderList.addAll(columns
                .filtered(col -> !prefsController.getPref(col.getId() + "_Sort_Order").equals("-1"))
                .sorted((col1, col2) -> {
                    String key1 = col1.getId() + "_Sort_Order";
                    String key2 = col2.getId() + "_Sort_Order";
                    int i1 = prefsController.getPrefInt(key1);
                    int i2 = prefsController.getPrefInt(key2);
                    return i1 - i2;
                }));

        // Columns Order
        columns.sort((col1, col2) -> {
            String key1 = col1.getId() + "_Order";
            String key2 = col2.getId() + "_Order";

            int i1 = prefsController.getPrefInt(key1);
            int i2 = prefsController.getPrefInt(key2);
            return i1 - i2;
        });

        // Filter Query will be taken directly from prefs

        // Show/Hide done task
        showDoneTasks.set(prefsController.getPrefBoolean(pShow_Done_Task));

        // Show status bar
        statusBar.setVisible(prefsController.getPrefBoolean(pShow_Status_Bar));

        // Access Token need not to be applied

        // Auto Save Timer
        autoSaveController.setInterval(prefsController.getPrefInt(pAuto_Save_Interval));

        // Custom Style
        if (prefsController.getPrefBoolean(pUse_Custom_Style))
            view.getStage().getScene().getStylesheets().addAll(cssCustom);
        else
            view.getStage().getScene().getStylesheets().removeAll(cssCustom);

        // Custom Fonts
        applyFontsStyle();

        // Confirm Delete will be taken directly from prefs

        // Update is applied in startup
    }

    public void applyFontsStyle() {
        if (prefsController.getPrefBoolean(pUse_Custom_Font)) {
            if (fontAr == null)
                fontAr = Font.loadFont(App.class.getResourceAsStream("/pw/ahs/app/anjez/res/DroidKufi-Regular.ttf"), 14);
            if (fontEn == null)
                fontEn = Font.loadFont(App.class.getResourceAsStream("/pw/ahs/app/anjez/res/Ubuntu-R.ttf"), 14);
            if (currentLangController.getLang().equals("ar")) {
                view.getStage().getScene().getRoot().getStyleClass().removeAll("font-en");
                view.getStage().getScene().getRoot().getStyleClass().addAll("font-ar");
            } else {
                view.getStage().getScene().getRoot().getStyleClass().removeAll("font-ar");
                view.getStage().getScene().getRoot().getStyleClass().addAll("font-en");
            }
        } else {
            view.getStage().getScene().getRoot().getStyleClass().removeAll("font-en", "font-ar");
        }
    }

    public void show() {
        VBox layout = new VBox();
        Stage stage = Dialogs.createUtilityDialog(view.getStage(), i18n.getString("prefs.title"), layout);

        TextField tfCurrentFile = new TextField(prefsController.getPref(pFile_Path));
        tfCurrentFile.setEditable(false);
        tfCurrentFile.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        Label lblCurrentFile = new Label(i18n.getString("prefs.current-file"));
        lblCurrentFile.setLabelFor(tfCurrentFile);

        ChoiceBox<String> cbLang = new ChoiceBox<>();
        cbLang.getItems().addAll("English", "العربية");
        cbLang.getSelectionModel().select(THE_LANG_NAME_CODE_CONVERTER.toString(prefsController.getPref(pLang)));
        Label lblLang = new Label(i18n.getString("prefs.lang"));
        lblLang.setLabelFor(cbLang);

        CheckBox cbSorting = new CheckBox(i18n.getString("prefs.remember.sort-state"));
        cbSorting.setSelected(prefsController.getPrefBoolean(pRemember_Sort_State));

        CheckBox cbFile = new CheckBox(i18n.getString("prefs.remember.last-file"));
        cbFile.setSelected(prefsController.getPrefBoolean(pRemember_File_Path));

        CheckBox cbOrder = new CheckBox(i18n.getString("prefs.remember.column-order"));
        cbOrder.setSelected(prefsController.getPrefBoolean(pRemember_Column_Order));

        CheckBox cbVisi = new CheckBox(i18n.getString("prefs.remember.column-visibility"));
        cbVisi.setSelected(prefsController.getPrefBoolean(pRemember_Column_Visibility));

        CheckBox cbShowDoneTasks = new CheckBox(i18n.getString("prefs.show-done-task"));
        cbShowDoneTasks.setSelected(showDoneTasks.get());

        CheckBox cbShowStatusBar = new CheckBox(i18n.getString("prefs.show-status-bar"));
        cbShowStatusBar.setSelected(prefsController.getPrefBoolean(pShow_Status_Bar));

        CheckBox cbUseCustomStyles = new CheckBox(i18n.getString("prefs.use-custom-style"));
        cbUseCustomStyles.setSelected(prefsController.getPrefBoolean(pUse_Custom_Style));

        CheckBox cbUseCustomFonts = new CheckBox(i18n.getString("prefs.use-custom-font"));
        cbUseCustomFonts.setSelected(prefsController.getPrefBoolean(pUse_Custom_Font));

        CheckBox cbConfirmDelete = new CheckBox(i18n.getString("prefs.confirm-delete"));
        cbConfirmDelete.setSelected(prefsController.getPrefBoolean(pConfirm_Delete));

        CheckBox cbUpdate = new CheckBox(i18n.getString("prefs.update"));
        cbUpdate.setSelected(prefsController.getPrefBoolean(pCheck_For_Update_On_Startup));

        TextField tfAutoSave = new TextField(prefsController.getPref(pAuto_Save_Interval)) {
            @Override
            public void replaceSelection(String s) {
                if (s.matches("[0-9]*"))
                    super.replaceSelection(s);
            }

            @Override
            public void replaceText(int i, int i2, String s) {
                if (s.matches("[0-9]*"))
                    super.replaceText(i, i2, s);
            }
        };
        Label lblAutoSave = new Label(i18n.getString("prefs.auto-save-interval"));
        lblAutoSave.setLabelFor(tfAutoSave);

        TextField tfFilterQuery = new TextField(prefsController.getPref(pFilter_Query));
        tfFilterQuery.setPromptText(i18n.getString("prefs.enter-filter-query"));
        Label lblFilterQuery = new Label(i18n.getString("prefs.when-filter-box-empty"));
        lblFilterQuery.setLabelFor(tfFilterQuery);

        Button buttonSave = new Button(i18n.getString("button.save"));
        buttonSave.setOnAction(actionEvent -> {
            // Language
            prefsController.setPref(pLang, THE_LANG_NAME_CODE_CONVERTER.toString(cbLang.getSelectionModel().getSelectedItem()));

            // Remember
            prefsController.setPref(pRemember_File_Path, "" + cbFile.isSelected());
            prefsController.setPref(pRemember_Sort_State, "" + cbSorting.isSelected());
            prefsController.setPref(pRemember_Column_Order, "" + cbOrder.isSelected());
            prefsController.setPref(pRemember_Column_Visibility, "" + cbVisi.isSelected());

            // Filter Query
            String txt = tfFilterQuery.getText();
            // Since default filter query is not empty
            // and we want the default query to be set only from initOptions method
            // we use this if
            if (txt.isEmpty())
                prefsController.restoreToDefault(pFilter_Query);
            else
                prefsController.setPref(pFilter_Query, txt);

            // Show/Hide done tasks
            // Note: this option will be set twice! because there is a listener on showDoneTasks value to keep values in sync.
            prefsController.setPref(pShow_Done_Task, "" + cbShowDoneTasks.isSelected());

            // Show Status Bar
            prefsController.setPref(pShow_Status_Bar, "" + cbShowStatusBar.isSelected());

            // Auto Save Timer
            prefsController.setPref(pAuto_Save_Interval, tfAutoSave.getText());

            // Use Custom Style
            prefsController.setPref(pUse_Custom_Style, "" + cbUseCustomStyles.isSelected());

            // Use Custom Fonts
            prefsController.setPref(pUse_Custom_Font, "" + cbUseCustomFonts.isSelected());

            // Confirm Delete
            prefsController.setPref(pConfirm_Delete, "" + cbConfirmDelete.isSelected());

            // Update
            prefsController.setPref(pCheck_For_Update_On_Startup, "" + cbUpdate.isSelected());

            apply();
            stage.hide();
        });
        buttonSave.setDefaultButton(true);

        Button buttonRestore = new Button(i18n.getString("button.restore-defaults"));
        buttonRestore.setOnAction(actionEvent -> {
            prefsController.restoreToDefault();
            apply();
            stage.hide();
        });

        Button buttonCancel = new Button(i18n.getString("button.cancel"));
        buttonCancel.setOnAction(actionEvent -> stage.hide());
        buttonCancel.setCancelButton(true);

        HBox layoutLang = new HBox();
        layoutLang.setAlignment(Pos.BASELINE_CENTER);
        layoutLang.setSpacing(2);
        layoutLang.getChildren().addAll(lblLang, cbLang);

        HBox layoutButtons = new HBox();
        layoutButtons.setAlignment(Pos.BASELINE_CENTER);
        layoutButtons.setSpacing(5);
        layoutButtons.getChildren().addAll(buttonSave, buttonRestore, buttonCancel);

        TilePane layoutCheckBoxes = new TilePane(
                cbFile, cbSorting, cbOrder, cbVisi, cbShowDoneTasks, cbShowStatusBar,
                cbUseCustomStyles, cbUseCustomFonts, cbConfirmDelete, cbUpdate
        );
        layoutCheckBoxes.setAlignment(Pos.BASELINE_LEFT);
        layoutCheckBoxes.setTileAlignment(Pos.BASELINE_LEFT);
        layoutCheckBoxes.setHgap(3);
        layoutCheckBoxes.setVgap(3);

        layout.setAlignment(Pos.CENTER_LEFT);
        layout.getChildren().addAll(
                layoutLang,
                lblCurrentFile, tfCurrentFile,
                layoutCheckBoxes,
                lblAutoSave, tfAutoSave,
                lblFilterQuery, tfFilterQuery,
                layoutButtons);
        layout.setSpacing(3);
        layout.setPrefSize(400, 300);

        buttonCancel.requestFocus();

        stage.setOnCloseRequest(windowEvent -> {
            windowEvent.consume();
            buttonCancel.fire();
        });

        stage.show();
    }

    public void save() {
        // Language is set in preferences dialog

        // File
        if (!prefsController.getPrefBoolean(pRemember_File_Path)) {
            prefsController.restoreToDefault(pFile_Path);
        }

        boolean remSortState = prefsController.getPrefBoolean(pRemember_Sort_State);
        boolean remColOrder = prefsController.getPrefBoolean(pRemember_Column_Order);
        boolean remColV = prefsController.getPrefBoolean(pRemember_Column_Visibility);

        if (remColOrder || remColV || remSortState) {
            ObservableList columns = table.getColumns();
            ObservableList sortOrderList = table.getSortOrder();
            for (int i = 0; i < columns.size(); i++) {
                TableColumn col = (TableColumn) columns.get(i);
                String id = col.getId();
                // Columns Order
                if (remColOrder) prefsController.setPref(id + "_Order", "" + i);
                // Columns Visibility
                if (remColV) prefsController.setPref(id + "_Visibility", "" + col.isVisible());
                // Columns Sort State
                if (remSortState) {
                    prefsController.setPref(id + "_Sort_Order", "" + sortOrderList.indexOf(col));
                    prefsController.setPref(id + "_Sort_Type", col.getSortType().equals(TableColumn.SortType.ASCENDING) ? "a" : "d");
                }
            }
        }

        // Filter Query is set in preferences dialog
        // Done Filter is set in preferences dialog
        // Access Token is set in DropBoxController method
        // Auto Save Timer is set in preferences dialog
        // Use Custom Styles is set in preferences dialog
        // Use Custom Fonts is set in preferences dialog
        // Update is set in preferences dialog

        if (!prefsController.savePrefs(PREFS_FILE_NAME))
            Dialogs.showMessageDialog(
                    view.getStage(),
                    i18n.getString("file.error.title"),
                    i18n.getString("file.error.can-not-open-prefs"),
                    i18n.getString("button.dismiss")
            );
    }
}
