/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

  This file is part of Anjez.
  Anjez is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/

package pw.ahs.app.anjez.gui.tablecell;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;

public class CheckBoxTableCell<S> extends TableCell<S, Boolean> {
    private final CheckBox cb;
    private TableColumn<S, Boolean> tableColumn;
    private BooleanProperty lastBoundProperty;
    private boolean cameFromAction;

    private CheckBoxTableCell() {
        super.getStyleClass().addAll("check-box-table-cell");
        super.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        super.setAlignment(Pos.BASELINE_CENTER);

        tableColumn = null;
        lastBoundProperty = null;
        cameFromAction = false;

        cb = new CheckBox();
        cb.setMaxWidth(Double.MAX_VALUE);
        cb.setAlignment(Pos.CENTER);

        cb.setOnAction(actionEvent -> {
            cameFromAction = true;
            startEdit();
            // commitEdit is called inside startEdit !!
        });
    }

    public CheckBoxTableCell(TableColumn<S, Boolean> tableColumn) {
        this();
        this.tableColumn = tableColumn;
    }

    @Override
    public void startEdit() {
        super.startEdit();
        // the following line is necessary for the edit event to be complete
        super.getTableView().edit(getIndex(), getTableColumn());
        if (cameFromAction) cameFromAction = false;
        else cb.setSelected(!cb.isSelected());
        commitEdit(cb.isSelected());
    }

    @Override
    protected void updateItem(Boolean aBoolean, boolean empty) {
        super.updateItem(aBoolean, false);

        if (empty || aBoolean == null) {
            super.setGraphic(null);
            super.setText(null);
        } else {
            ObservableValue observableValue = tableColumn.getCellObservableValue(getIndex());
            if (observableValue instanceof BooleanProperty) {
                if (lastBoundProperty != null) cb.selectedProperty().unbindBidirectional(lastBoundProperty);
                lastBoundProperty = (BooleanProperty) observableValue;
                cb.selectedProperty().bindBidirectional(lastBoundProperty);
            }

            cb.disableProperty().bind(Bindings.not(
                    super.getTableView().editableProperty().and(
                            super.getTableColumn().editableProperty()).and(
                            super.editableProperty())
            ));

            // calling cb.setSelected(aBoolean) here is redundant and might cause errors

            super.setGraphic(cb);
        }
    }
}