/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

  This file is part of Anjez.
  Anjez is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/

package pw.ahs.app.anjez.gui.tablecell;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.util.Callback;

public class TextFieldTableCell<S> extends TableCell<S, String> {

    private TextField textField;
    private boolean ignoreEmptyText;

    public static <S> Callback<TableColumn<S, String>, TableCell<S, String>> forTableColumn() {
        return sStringTableColumn -> new TextFieldTableCell<>();
    }

    private TextFieldTableCell() {
        this.getStyleClass().addAll("text-field-table-cell");
        ignoreEmptyText = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startEdit() {
        super.startEdit();

        if (isEditing()) {
            if (textField == null) {
                textField = new TextField();
                textField.setOnKeyReleased(keyEvent -> {
                    if (keyEvent.getCode().equals(KeyCode.ENTER)) {
                        commitEdit(textField.getText());
                    } else if (keyEvent.getCode() == KeyCode.ESCAPE) {
                        cancelEdit();
                    }
                });
                textField.focusedProperty().addListener((observableValue, oldValue, newValue) -> {
                    if (!newValue)
                        commitEdit(textField.getText());
                });
            }

            super.setText(null);
            super.setGraphic(textField);
            textField.setText(super.getItem());
            textField.selectAll();
            textField.requestFocus();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cancelEdit() {
        super.cancelEdit();
        super.setText(getItem());
        super.setGraphic(null);
    }

    @Override
    public void commitEdit(String s) {
        if (ignoreEmptyText && s.isEmpty()) cancelEdit();
        else super.commitEdit(s);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (isEmpty()) {
            super.setText(null);
            super.setGraphic(null);
        } else {
            if (isEditing()) {
                if (textField != null) {
                    textField.setText(super.getItem());
                }
                super.setText(null);
                super.setGraphic(textField);
            } else {
                super.setText(super.getItem());
                super.setGraphic(null);
            }
        }
    }
}
