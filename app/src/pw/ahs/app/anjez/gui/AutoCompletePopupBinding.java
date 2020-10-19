/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

  This file is part of Anjez.
  Anjez is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/

package pw.ahs.app.anjez.gui;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.EventHandler;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Point2D;
import javafx.scene.control.Control;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.stage.Popup;

import java.util.function.Function;

public class AutoCompletePopupBinding {

    private static final double LIST_CELL_HEIGHT = 24;
    private static final double LIST_VIEW_MAX_HEIGHT = 300;
    private final ObservableList<String> items = FXCollections.observableArrayList();
    private final FilteredList<String> filteredItems = items.filtered(s -> true);
    private Control target = null;
    private boolean disabled = true;
    private Popup popup;
    private final ListView<String> listView = new ListView<>(filteredItems);
    private Function<String, Void> onItemChosen = null;
    private final ChangeListener<Boolean> targetFocusedListener = (observable, oldValue, newValue) -> {
        if (oldValue)
            hide();
    };
    private final EventHandler<KeyEvent> targetEventHandler = evt -> {
        if (!popup.isShowing()) return;

        if (evt.getEventType() == KeyEvent.KEY_PRESSED && evt.getCode() == KeyCode.DOWN) {
            if (listView.getSelectionModel().isEmpty()) {
                evt.consume();
                listView.getSelectionModel().selectFirst();
            }
        } else if (evt.getEventType() == KeyEvent.KEY_RELEASED && evt.getCode() == KeyCode.ESCAPE) {
            evt.consume();
            hide();
        }
    };

    public AutoCompletePopupBinding() {
        Runnable initPopup = () -> {
            popup = new Popup();
            popup.getContent().addAll(listView);
            popup.setAutoFix(true);
            popup.setAutoHide(true);
            popup.setHideOnEscape(true);
            popup.setOnShown(evt -> {
                if (target.getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT)
                    popup.setX(popup.getX() - popup.getWidth());
            });
        };
        if (Platform.isFxApplicationThread()) {
            initPopup.run();
        } else Platform.runLater(initPopup);

        listView.setEditable(false);
        listView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        listView.addEventFilter(KeyEvent.KEY_PRESSED, evt -> {
            if (listView.getSelectionModel().isEmpty()) return;

            if (evt.getCode() == KeyCode.ENTER) fire();
            else if (evt.getCode() == KeyCode.UP
                    && listView.getSelectionModel().getSelectedIndex() == 0) {
                listView.getSelectionModel().clearSelection();
            }
        });
        listView.setOnMouseClicked(evt -> {
            if (evt.getButton() == MouseButton.PRIMARY) fire();
        });
        listView.setMaxHeight(LIST_VIEW_MAX_HEIGHT);
        listView.setFixedCellSize(LIST_CELL_HEIGHT);
        listView.prefHeightProperty().bind(Bindings.size(listView.getItems()).multiply(LIST_CELL_HEIGHT));

//        filteredItems.addListener((ListChangeListener<String>) c -> {
//            while (c.next())
//                if (c.wasRemoved() || c.wasAdded())
//                    if (filteredItems.isEmpty()) hide();
//                    else show();
//        });
    }

    public void setDisabled(boolean state) {
        disabled = state;
        if (disabled) hide();
    }

    public void enable() {
        setDisabled(false);
    }

    public void disable() {
        setDisabled(true);
    }
//
//    public void setSourceProperty(StringProperty textProperty) {
//        if (textProperty != null) {
//            textProperty.addListener((observable, oldValue, newValue) -> setCurrentText(newValue));
//        }
//    }

    public void setCurrentText(String text) {
        if (text.isEmpty()) {
            filteredItems.setPredicate(s -> true);
        } else {
            filteredItems.setPredicate(s -> s.startsWith(text));
        }
        if (filteredItems.isEmpty()) return;
        show();
    }

    public void setOnItemChosen(Function<String, Void> function) {
        onItemChosen = function;
    }

    public void setTarget(TextField textField) {
        if (target != null) {
            target.focusedProperty().removeListener(targetFocusedListener);
            target.removeEventFilter(KeyEvent.ANY, targetEventHandler);
        }
        target = textField;
        if (target != null) {
            target.focusedProperty().addListener(targetFocusedListener);
            target.addEventFilter(KeyEvent.ANY, targetEventHandler);
        }
    }

    public void addSuggestion(String suggestion) {
        if (items.contains(suggestion)) return;
        items.add(suggestion);
    }

    public void removeSuggestion(String suggestion) {
        items.remove(suggestion);
    }

    private void show() {
        if (disabled || target == null) return;
        Point2D point2D = target.localToScreen(0, 0);
        if (Platform.isFxApplicationThread())
            popup.show(target, point2D.getX(), point2D.getY() + target.getHeight());
        else Platform.runLater(() -> popup.show(target, point2D.getX(), point2D.getY() + target.getHeight()));
    }

    private void hide() {
        if (Platform.isFxApplicationThread())
            popup.hide();
        else Platform.runLater(popup::hide);
    }

    private void fire() {
        if (onItemChosen != null)
            onItemChosen.apply(listView.getSelectionModel().getSelectedItem());
        hide();
    }
}
