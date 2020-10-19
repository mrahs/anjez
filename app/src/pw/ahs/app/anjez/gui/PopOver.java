/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

  This file is part of Anjez.
  Anjez is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/

package pw.ahs.app.anjez.gui;


import javafx.animation.FadeTransition;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.stage.Popup;
import javafx.stage.Window;
import javafx.util.Duration;

public class PopOver {

    public static Popup showPopup(Region target, Node content, ArrowLocation arrowLocation) {
        StackPane container = new StackPane(content);

        StackPane stackPane = new StackPane(container);
        stackPane.setStyle("-fx-background-color: transparent");
        stackPane.setPickOnBounds(false);
        stackPane.setMinSize(100, 100);
        FadeTransition fade = new FadeTransition(Duration.millis(200), stackPane);
        fade.setAutoReverse(false);
        fade.setCycleCount(1);

        int sign = target.getEffectiveNodeOrientation() == NodeOrientation.LEFT_TO_RIGHT ? 1 : -1;
        // for some reason, in RTL mode the popup has small unexpected indent
        double indent = 6;
        Popup popup = new Popup();
        popup.getScene().setRoot(stackPane);

        popup.setOnShown(evt -> {
            switch (arrowLocation) {
                case TOP_CENTER:
                    popup.setX(popup.getX() - popup.getWidth() / 2);
                    break;
                case TOP_RIGHT:
                    if (sign > 0) popup.setX(popup.getX() - popup.getWidth());
                    else popup.setX(popup.getX() - indent);
                    break;
                case LEFT_CENTER:
                    if (sign > 0)
                        popup.setX(popup.getX() - target.getWidth() / 2);
                    else
                        popup.setX(popup.getX() - (popup.getWidth() - target.getWidth() / 2));
                    popup.setY(popup.getY() - popup.getHeight() / 2);
                    break;
                case BOTTOM_CENTER:
                    popup.setX(popup.getX() - popup.getWidth() / 2);
                    popup.setY(popup.getY() - popup.getHeight());
                    break;
                case BOTTOM_RIGHT:
                    if (sign > 0) popup.setX(popup.getX() - popup.getWidth());
                    else popup.setX(popup.getX() - indent);
                    popup.setY(popup.getY() - popup.getHeight());
                    break;
                case NO_ARROW:
                    popup.setX(popup.getX() - popup.getWidth() / 2);
                    popup.setY(popup.getY() - popup.getHeight() / 2);
                    break;
            }
            fade.setFromValue(0);
            fade.setToValue(.95);
            fade.play();
        });

        SimpleDoubleProperty xOffset = new SimpleDoubleProperty(0);
        SimpleDoubleProperty yOffset = new SimpleDoubleProperty(0);
        stackPane.setOnMousePressed(evt -> {
            xOffset.set(evt.getScreenX());
            yOffset.set(evt.getScreenY());
        });
        stackPane.setOnMouseDragged(evt -> {
            Window window = stackPane.getScene().getWindow();
            double deltaX = evt.getScreenX() - xOffset.get();
            double deltaY = evt.getScreenY() - yOffset.get();

            window.setX(window.getX() + deltaX);
            window.setY(window.getY() + deltaY);

            xOffset.set(evt.getScreenX());
            yOffset.set(evt.getScreenY());
        });

        // when node effective orientation is RTL, the nodes (0,0)
        // is at right top corner
        Point2D nodeCoordinate = sign > 0 ? target.localToScene(0, 0) : target.localToScene(target.getWidth(), 0);
        Point2D sceneCoordinate = new Point2D(target.getScene().getX(), target.getScene().getY());
        Point2D windowCoordinate = new Point2D(target.getScene().getWindow().getX(), target.getScene().getWindow().getY());

        double x = 0;
        double y = 0;

        switch (arrowLocation) {
            case TOP_CENTER:
                x = windowCoordinate.getX() + sceneCoordinate.getX() + nodeCoordinate.getX() + target.getWidth() / 2;
                y = windowCoordinate.getY() + sceneCoordinate.getY() + nodeCoordinate.getY() + target.getHeight();
                container.setStyle("-fx-padding: 20 10 10 10");
                break;
            case TOP_RIGHT:
                x = windowCoordinate.getX() + sceneCoordinate.getX() + nodeCoordinate.getX() + target.getWidth() * (sign > 0 ? 1 : 0);
                y = windowCoordinate.getY() + sceneCoordinate.getY() + nodeCoordinate.getY() + target.getHeight();
                container.setStyle("-fx-padding: 20 10 10 10");
                break;
            case LEFT_CENTER:
                x = windowCoordinate.getX() + sceneCoordinate.getX() + nodeCoordinate.getX() + target.getWidth() * (sign > 0 ? 1 : 0);
                y = windowCoordinate.getY() + sceneCoordinate.getY() + nodeCoordinate.getY() + target.getHeight() / 2;
                container.setStyle("-fx-padding: 10 10 10 20");
                break;
            case BOTTOM_CENTER:
                x = windowCoordinate.getX() + sceneCoordinate.getX() + nodeCoordinate.getX() + target.getWidth() / 2;
                y = windowCoordinate.getY() + sceneCoordinate.getY() + nodeCoordinate.getY();
                container.setStyle("-fx-padding: 10 10 20 10");
                break;
            case BOTTOM_RIGHT:
                x = windowCoordinate.getX() + sceneCoordinate.getX() + nodeCoordinate.getX() + target.getWidth() * (sign > 0 ? 1 : 0);
                y = windowCoordinate.getY() + sceneCoordinate.getY() + nodeCoordinate.getY();
                container.setStyle("-fx-padding: 10 10 20 10");
                break;
            case NO_ARROW:
                x = windowCoordinate.getX() + sceneCoordinate.getX() + nodeCoordinate.getX() + target.getWidth() / 2;
                y = windowCoordinate.getY() + sceneCoordinate.getY() + nodeCoordinate.getY() + target.getHeight() / 2;
                container.setStyle("-fx-padding: 10 10 10 10");
                break;
        }

        popup.show(target, x, y);
        createPathForPane(stackPane, arrowLocation);
        content.requestFocus();
        return popup;
    }

    private static void createPathForPane(Pane pane, ArrowLocation arrowLocation) {
        double radius = 5;
        double arrowSize = 10;
        double arrowOffset = 5;

        Path path = new Path();
        path.setStrokeWidth(.2);
        path.setStroke(Color.BLACK);
        path.setFill(Color.WHITE);
        path.setEffect(new DropShadow(5, 0, 0, Color.rgb(0, 0, 0, .5)));
        path.setManaged(false);

        pane.getChildren().add(0, path);

        switch (arrowLocation) {
            case TOP_CENTER:
                path.getElements().addAll(
                        new MoveTo(radius * 2, arrowSize),
                        new HLineTo(pane.getWidth() / 2 - arrowSize / 2),
                        new LineTo(pane.getWidth() / 2, 0),
                        new LineTo(pane.getWidth() / 2 + arrowSize / 2, arrowSize),
                        new HLineTo(pane.getWidth() - radius * 2),
                        new QuadCurveTo(pane.getWidth(), arrowSize, pane.getWidth(), arrowSize + radius * 2),
                        new VLineTo(pane.getHeight() + arrowSize - radius * 2),
                        new QuadCurveTo(pane.getWidth(), pane.getHeight() + arrowSize, pane.getWidth() - radius * 2, pane.getHeight() + arrowSize),
                        new HLineTo(radius * 2),
                        new QuadCurveTo(0, pane.getHeight() + arrowSize, 0, pane.getHeight() + arrowSize - radius * 2),
                        new VLineTo(arrowSize + radius * 2),
                        new QuadCurveTo(0, arrowSize, radius * 2, arrowSize)
                );
                break;
            case TOP_RIGHT:
                path.getElements().addAll(
                        new MoveTo(radius * 2, arrowSize),
                        new HLineTo(pane.getWidth() - radius * 2 - arrowOffset - arrowSize),
                        new LineTo(pane.getWidth() - radius * 2 - arrowOffset - arrowSize / 2, 0),
                        new LineTo(pane.getWidth() - radius * 2 - arrowOffset, arrowSize),
                        new HLineTo(pane.getWidth() - radius * 2),
                        new QuadCurveTo(pane.getWidth(), arrowSize, pane.getWidth(), arrowSize + radius * 2),
                        new VLineTo(pane.getHeight() + arrowSize - radius * 2),
                        new QuadCurveTo(pane.getWidth(), pane.getHeight() + arrowSize, pane.getWidth() - radius * 2, pane.getHeight() + arrowSize),
                        new HLineTo(radius * 2),
                        new QuadCurveTo(0, pane.getHeight() + arrowSize, 0, pane.getHeight() + arrowSize - radius * 2),
                        new VLineTo(arrowSize + radius * 2),
                        new QuadCurveTo(0, arrowSize, radius * 2, arrowSize)
                );
                break;
            case LEFT_CENTER:
                path.getElements().addAll(
                        new MoveTo(arrowSize + radius * 2, 0),
                        new HLineTo(pane.getWidth() + arrowSize - radius * 2),
                        new QuadCurveTo(pane.getWidth() + arrowSize, 0, pane.getWidth() + arrowSize, radius * 2),
                        new VLineTo(pane.getHeight() - radius * 2),
                        new QuadCurveTo(pane.getWidth() + arrowSize, pane.getHeight(), pane.getWidth() + arrowSize - radius * 2, pane.getHeight()),
                        new HLineTo(arrowSize + radius * 2),
                        new QuadCurveTo(arrowSize, pane.getHeight(), arrowSize, pane.getHeight() - radius * 2),
                        new VLineTo(pane.getHeight() / 2 + arrowSize / 2),
                        new LineTo(0, pane.getHeight() / 2),
                        new LineTo(arrowSize, pane.getHeight() / 2 - arrowSize / 2),
                        new VLineTo(radius * 2),
                        new QuadCurveTo(arrowSize, 0, arrowSize + radius * 2, 0)
                );
                break;
            case BOTTOM_CENTER:
                path.getElements().addAll(
                        new MoveTo(radius * 2, 0),
                        new HLineTo(pane.getWidth() - radius * 2),
                        new QuadCurveTo(pane.getWidth(), 0, pane.getWidth(), radius * 2),
                        new VLineTo(pane.getHeight() - arrowSize - radius * 2),
                        new QuadCurveTo(pane.getWidth(), pane.getHeight() - arrowSize, pane.getWidth() - radius * 2, pane.getHeight() - arrowSize),
                        new HLineTo(pane.getWidth() / 2 + arrowSize / 2),
                        new LineTo(pane.getWidth() / 2, pane.getHeight()),
                        new LineTo(pane.getWidth() / 2 - arrowSize / 2, pane.getHeight() - arrowSize),
                        new HLineTo(radius * 2),
                        new QuadCurveTo(0, pane.getHeight() - arrowSize, 0, pane.getHeight() - arrowSize - radius * 2),
                        new VLineTo(radius * 2),
                        new QuadCurveTo(0, 0, radius * 2, 0)
                );
                break;
            case BOTTOM_RIGHT:
                path.getElements().addAll(
                        new MoveTo(radius * 2, 0),
                        new HLineTo(pane.getWidth() - radius * 2),
                        new QuadCurveTo(pane.getWidth(), 0, pane.getWidth(), radius * 2),
                        new VLineTo(pane.getHeight() - arrowSize - radius * 2),
                        new QuadCurveTo(pane.getWidth(), pane.getHeight() - arrowSize, pane.getWidth() - radius * 2, pane.getHeight() - arrowSize),
                        new HLineTo(pane.getWidth() - radius * 2 - arrowOffset),
                        new LineTo(pane.getWidth() - radius * 2 - arrowOffset - arrowSize / 2, pane.getHeight()),
                        new LineTo(pane.getWidth() - radius * 2 - arrowOffset - arrowSize, pane.getHeight() - arrowSize),
                        new HLineTo(radius * 2),
                        new QuadCurveTo(0, pane.getHeight() - arrowSize, 0, pane.getHeight() - arrowSize - radius * 2),
                        new VLineTo(radius * 2),
                        new QuadCurveTo(0, 0, radius * 2, 0)
                );
                break;
            default:
                path.getElements().addAll(
                        new MoveTo(radius * 2, 0),
                        new HLineTo(pane.getWidth() - radius * 2),
                        new QuadCurveTo(pane.getWidth(), 0, pane.getWidth(), radius * 2),
                        new VLineTo(pane.getHeight() - radius * 2),
                        new QuadCurveTo(pane.getWidth(), pane.getHeight(), pane.getWidth() - radius * 2, pane.getHeight()),
                        new HLineTo(radius * 2),
                        new QuadCurveTo(0, pane.getHeight(), 0, pane.getHeight() - radius * 2),
                        new VLineTo(radius * 2),
                        new QuadCurveTo(0, 0, radius * 2, 0)
                );
        }
    }

    public static enum ArrowLocation {
        TOP_CENTER,
        TOP_RIGHT,
        LEFT_CENTER,
        BOTTOM_CENTER,
        BOTTOM_RIGHT,
        NO_ARROW
    }
}
