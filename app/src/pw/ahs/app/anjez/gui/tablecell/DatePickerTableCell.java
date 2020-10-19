/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

  This file is part of Anjez.
  Anjez is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/

package pw.ahs.app.anjez.gui.tablecell;

import javafx.geometry.Pos;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableCell;
import javafx.scene.input.KeyCode;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DatePickerTableCell<S> extends TableCell<S, LocalDate> {


    private DatePicker dp;

    public DatePickerTableCell() {
        super.setAlignment(Pos.BASELINE_CENTER);
    }

    @Override
    public void startEdit() {
        if (!super.isEditable() || !super.getTableView().isEditable() || !super.getTableColumn().isEditable()) return;

        super.startEdit();
        if (dp == null) {
            dp = new DatePicker();

            dp.setConverter(new StringConverter<LocalDate>() {
                @Override
                public String toString(LocalDate localDate) {
                    if (localDate != null) return localDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
                    return "";
                }

                @Override
                public LocalDate fromString(String s) {
                    return LocalDate.parse(s, DateTimeFormatter.ISO_LOCAL_DATE);
                }
            });

            dp.getEditor().setOnKeyReleased(event -> {
                if (event.getCode().equals(KeyCode.ESCAPE)) cancelEdit();
                else if (event.getCode().equals(KeyCode.ENTER)) commitEdit(dp.getValue());
                event.consume();
            });
        }
        dp.setValue(getItem() == null ? LocalDate.now() : super.getItem());
        super.setText(null);
        super.setGraphic(dp);
        dp.getEditor().selectAll();
        dp.getEditor().requestFocus();
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        super.setText(getItemString());
        super.setGraphic(null);
    }

    @Override
    public void commitEdit(LocalDate localDate) {
        super.commitEdit(localDate);
    }

    @Override
    protected void updateItem(LocalDate localDate, boolean empty) {
        super.updateItem(localDate, empty);

        if (empty || localDate == null) {
            super.setGraphic(null);
            super.setText(null);
        } else if (isEditing()) {
            if (dp != null) dp.setValue(super.getItem());
            super.setText(null);
            super.setGraphic(dp);
        } else {
            super.setText(getItemString());
            super.setGraphic(null);
        }
    }

    private String getItemString() {
        return super.getItem() == null ? "" : super.getItem().format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

}
