/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

  This file is part of Anjez.
  Anjez is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/

package pw.ahs.app.anjez.gui.tablecell;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.util.Collection;

public class ComboBoxTableCell<S, T> extends TableCell<S, T> {
    private final ObservableList<T> items;
    private ComboBox<T> comboBox;
    private final StringConverter<T> stringConverter;

    public static <S, T> Callback<TableColumn<S, T>, TableCell<S, T>> forTableColumn(
            final Collection<T> items, final StringConverter<T> stringConverter) {
        return list -> new ComboBoxTableCell<>(items, stringConverter);
    }

    private ComboBoxTableCell(Collection<T> items, StringConverter<T> stringConverter) {
        super.setAlignment(Pos.BASELINE_CENTER);
        super.getStyleClass().addAll("combo-box-table-cell");
        this.items = FXCollections.observableArrayList(items);
        this.stringConverter = stringConverter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startEdit() {
        if (!super.isEditable() || !super.getTableView().isEditable() || !super.getTableColumn().isEditable()) {
            return;
        }

        if (comboBox == null) {
            comboBox = new ComboBox<>(items);
            comboBox.setConverter(stringConverter);
            comboBox.setMaxWidth(Double.MAX_VALUE);
            comboBox.setOnKeyTyped(keyEvent -> {
                if (items.isEmpty()) return;
                if (keyEvent.isAltDown() || keyEvent.isControlDown() || keyEvent.isMetaDown() || keyEvent.isShiftDown() || keyEvent.isShortcutDown())
                    return;
                String c = keyEvent.getCharacter().toLowerCase();
                ObservableList<T> filtered = items.filtered(p -> p.toString().toLowerCase().startsWith(c));
                if (filtered.isEmpty()) return;
                comboBox.getSelectionModel().select(filtered.get(0));
            });
            comboBox.getSelectionModel().selectedItemProperty().addListener((ov, oldValue, newValue) -> {
                if (super.isEditing()) {
                    commitEdit(newValue);
                }
            });
        }

        comboBox.getSelectionModel().select(super.getItem());

        super.startEdit();
        super.setText(null);
        super.setGraphic(comboBox);
        comboBox.requestFocus();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cancelEdit() {
        super.cancelEdit();

        super.setText(stringConverter.toString(super.getItem()));
        super.setGraphic(null);
    }

    @Override
    public void commitEdit(T t) {
        super.commitEdit(t);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        if (isEmpty()) {
            super.setText(null);
            super.setGraphic(null);
        } else {
            if (isEditing()) {
                if (comboBox != null) {
                    comboBox.getSelectionModel().select(super.getItem());
                }
                super.setText(null);
                super.setGraphic(comboBox);
            } else {
                super.setText(stringConverter.toString(super.getItem()));
                super.setGraphic(null);
            }
        }
    }
}
