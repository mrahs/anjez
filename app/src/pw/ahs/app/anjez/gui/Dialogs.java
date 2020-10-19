/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

  This file is part of Anjez.
  Anjez is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/

package pw.ahs.app.anjez.gui;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.util.Collection;

public class Dialogs {

    private static final int DEFAULT_DURATION = 200;

    public static Stage createUtilityDialog(
            Stage owner,
            String title,
            Pane root
    ) {
        Timeline fadeHide = new Timeline();
        Timeline fadeShow = new Timeline();
        Scene scene = new Scene(root);

        Stage stage = new Stage(StageStyle.UTILITY) {
            {
                fadeHide.setOnFinished(evt -> super.hide());
            }

            @Override
            public void hide() {
                fadeHide.play();
            }
        };
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(owner);
        stage.setTitle(title);

        fadeHide.setCycleCount(1);
        fadeHide.setAutoReverse(false);
        fadeHide.getKeyFrames().add(new KeyFrame(Duration.millis(DEFAULT_DURATION), new KeyValue(stage.opacityProperty(), 0)));

        fadeShow.setCycleCount(1);
        fadeShow.setAutoReverse(false);
        // a workaround to avoid nudges at start
        fadeShow.setDelay(Duration.millis(100));
        fadeShow.getKeyFrames().add(new KeyFrame(Duration.millis(DEFAULT_DURATION), new KeyValue(stage.opacityProperty(), 1)));

        scene.getStylesheets().addAll(owner.getScene().getStylesheets());
        stage.setScene(scene);

        stage.sizeToScene();
        stage.setOpacity(0);

        stage.setOnShown(evt -> {
            stage.setX(owner.getX() + owner.getWidth() / 2 - stage.getWidth() / 2);
            stage.setY(owner.getY() + owner.getHeight() / 2 - stage.getHeight() / 2);
            fadeShow.play();
        });

        return stage;
    }

    public static Stage createWaitingDialog(Stage owner, String title, String msg, String buttonText) {
        VBox layout = new VBox();
        Stage stage = createUtilityDialog(owner, title, layout);

        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setProgress(-1);
        progressIndicator.setMinSize(50, 50);

        Button button = new Button(buttonText);
        button.setOnAction(actionEvent -> stage.hide());
        button.setCancelButton(true);

        if (!msg.isEmpty()) layout.getChildren().addAll(new Label(msg));
        layout.getChildren().addAll(progressIndicator, button);
        layout.setSpacing(20);
        layout.setPadding(new Insets(10));
        layout.setAlignment(Pos.CENTER);

        stage.setOnCloseRequest(windowEvent -> {
            windowEvent.consume();
            stage.hide();
        });

        return stage;
    }

    public static String showDropBoxNewConnectionDialog(
            Stage owner,
            String authUrl,
            String title,
            String msgText,
            String lblAuthText,
            String lblVerifyText,
            String btnVerifyText,
            String btnCancelText) {

        VBox layout = new VBox();
        Stage stage = createUtilityDialog(owner, title, layout);

        Label lblMsg = new Label(msgText);
        lblMsg.setId("dropbox-new-msg");
        lblMsg.setWrapText(true);

        TextField tfAuthUrl = new TextField(authUrl);
        tfAuthUrl.setEditable(false);
        Label lblAuthUrl = new Label(lblAuthText);
        lblAuthUrl.setLabelFor(tfAuthUrl);

        TextField tfVerify = new TextField();
        Label lblVerify = new Label(lblVerifyText);
        lblVerify.setLabelFor(tfVerify);

        Button buttonVerify = new Button(btnVerifyText);
        buttonVerify.setOnAction(actionEvent -> stage.hide());
        buttonVerify.setDisable(true);
        buttonVerify.setDefaultButton(true);

        Button buttonCancel = new Button(btnCancelText);
        buttonCancel.setOnAction(actionEvent -> {
            tfVerify.clear();
            stage.hide();
        });
        buttonCancel.setCancelButton(true);

        tfVerify.textProperty().addListener((observableValue, oldVal, newVal) -> {
            if (newVal.length() != 43)
                buttonVerify.setDisable(true);
            else
                buttonVerify.setDisable(false);
        });

        HBox layoutButtons = new HBox(buttonVerify, buttonCancel);
        layoutButtons.setSpacing(5);
        layoutButtons.setAlignment(Pos.BASELINE_CENTER);

        layout.getChildren().addAll(lblMsg, new Separator(), lblAuthUrl, tfAuthUrl, lblVerify, tfVerify, layoutButtons);
        layout.setSpacing(5);
        layout.setStyle("-fx-padding: 5");

        stage.setOnCloseRequest(windowEvent -> {
            windowEvent.consume();
            buttonCancel.fire();
        });

        stage.showAndWait();

        return tfVerify.getText();
    }

    public static Result showYesNoDialog(
            Stage owner,
            String title,
            String msgText,
            String yesText,
            String noText,
            String cancelText,
            int buttonToFocus
    ) {
        return showYesNoDialog(owner, title, msgText, yesText, noText, cancelText, buttonToFocus, null);
    }

    public static Result showYesNoDialog(
            Stage owner,
            String title,
            String msgText,
            String yesText,
            String noText,
            String cancelText,
            int buttonToFocus,
            Node extraNode
    ) {
        VBox layout = new VBox();
        Stage stage = createUtilityDialog(owner, title, layout);

        SimpleObjectProperty<Result> result = new SimpleObjectProperty<>(Result.CANCEL);

        Text msg = new Text(msgText);
        msg.setTextAlignment(TextAlignment.CENTER);

        Button buttonYes = new Button(yesText);
        buttonYes.setOnAction(actionEvent -> {
            result.set(Result.YES);
            stage.hide();
        });
        buttonYes.setDefaultButton(true);

        Button buttonNo = new Button(noText);
        buttonNo.setOnAction(actionEvent -> {
            result.set(Result.NO);
            stage.hide();
        });

        Button buttonCancel = cancelText.isEmpty() ? null : new Button(cancelText);

        HBox layoutButtons = new HBox(buttonYes, buttonNo);
        layoutButtons.setSpacing(5);
        layoutButtons.setAlignment(Pos.BASELINE_CENTER);

        if (buttonCancel == null) {
            buttonNo.setCancelButton(true);
        } else {
            buttonCancel.setOnAction(actionEvent -> {
                result.set(Result.CANCEL);
                stage.hide();
            });
            buttonCancel.setCancelButton(true);
            layoutButtons.getChildren().add(buttonCancel);
        }

        layout.setSpacing(10);
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().add(msg);
        if (extraNode != null) layout.getChildren().add(extraNode);
        layout.getChildren().add(layoutButtons);
        layout.setMinHeight(100);
        layout.setMinWidth(300);

        if (buttonToFocus == 1)
            buttonYes.requestFocus();
        else if (buttonToFocus == 2)
            buttonNo.requestFocus();
        else if (buttonCancel != null)
            buttonCancel.requestFocus();
        else
            buttonNo.requestFocus();

        stage.setOnCloseRequest(windowEvent -> {
            windowEvent.consume();
            if (buttonCancel != null) buttonCancel.fire();
            else buttonNo.fire();
        });


        stage.showAndWait();

        return result.get();
    }

    public static void showMessageDialog(
            Stage owner,
            String title,
            String msgText,
            String buttonText
    ) {
        VBox layout = new VBox();
        Stage stage = createUtilityDialog(owner, title, layout);

        Text msg = new Text(msgText);
        msg.setTextAlignment(TextAlignment.CENTER);

        Button button = new Button(buttonText);
        button.setOnAction(actionEvent -> stage.hide());
        button.setDefaultButton(true);
        button.setCancelButton(true);

        layout.setSpacing(10);
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(msg, button);
        layout.setMinHeight(100);
        layout.setMinWidth(300);

        button.requestFocus();

        stage.setOnCloseRequest(windowEvent -> {
            windowEvent.consume();
            button.fire();
        });

        stage.show();
    }

    public static <T> T showInputDialog(
            Stage owner,
            String title,
            String msgText,
            String buttonInputText,
            String buttonCancelText,
            Collection<T> values,
            T initialValue
    ) {
        VBox layout = new VBox();
        Stage stage = createUtilityDialog(owner, title, layout);

        Text msg = new Text(msgText);
        msg.setTextAlignment(TextAlignment.CENTER);

        ChoiceBox<T> items = new ChoiceBox<>(FXCollections.observableArrayList(values));
        items.getSelectionModel().select(initialValue);

        SimpleObjectProperty<T> val = new SimpleObjectProperty<>(initialValue);

        Button buttonInput = new Button(buttonInputText);
        buttonInput.setOnAction(actionEvent -> {
            val.set(items.getSelectionModel().getSelectedItem());
            stage.hide();
        });
        buttonInput.setDefaultButton(true);

        Button buttonCancel = new Button(buttonCancelText);
        buttonCancel.setOnAction(actionEvent -> {
            val.set(null);
            stage.hide();
        });
        buttonCancel.setCancelButton(true);

        HBox layoutInput = new HBox(msg, items);
        layoutInput.setSpacing(3);
        layoutInput.setAlignment(Pos.BASELINE_CENTER);

        HBox layoutButtons = new HBox(buttonInput, buttonCancel);
        layoutButtons.setSpacing(5);
        layoutButtons.setAlignment(Pos.BASELINE_CENTER);

        layout.setSpacing(10);
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(layoutInput, layoutButtons);
        layout.setMinHeight(100);
        layout.setMinWidth(300);

        buttonInput.requestFocus();

        stage.setOnCloseRequest(windowEvent -> {
            windowEvent.consume();
            buttonCancel.fire();
        });

        stage.showAndWait();

        return val.get();
    }

    public static enum Result {
        YES, NO, CANCEL
    }
}
