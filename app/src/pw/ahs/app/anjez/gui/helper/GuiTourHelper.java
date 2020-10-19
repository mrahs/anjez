/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

  This file is part of Anjez.
  Anjez is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/

package pw.ahs.app.anjez.gui.helper;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Popup;
import javafx.util.Duration;
import pw.ahs.app.anjez.gui.PopOver;

import java.util.concurrent.atomic.AtomicBoolean;

import static pw.ahs.app.anjez.Globals.i18n;

public class GuiTourHelper {
    private Pane root;
    private TextField filterBox;
    private MenuButton menuBtn;
    private TableView table;
    private Pane statusBar;
    private Button btnPlayStop;

    private static GuiTourHelper instance = null;

    public static GuiTourHelper getInstance() {
        if (instance == null)
            instance = new GuiTourHelper();
        return instance;
    }

    private GuiTourHelper() {
    }

    public void init(Pane root, TextField filterBox, MenuButton menuBtn, TableView table, Pane statusBar, Button btnPlayStop) {
        this.root = root;
        this.filterBox = filterBox;
        this.menuBtn = menuBtn;
        this.table = table;
        this.statusBar = statusBar;
        this.btnPlayStop = btnPlayStop;
    }

    public void showGuiTour() {
        Popup[] popup = new Popup[1];
        IntegerProperty currentStep = new SimpleIntegerProperty(-1);
        AtomicBoolean adjustingPopup = new AtomicBoolean(false);

        ChoiceBox<Object> title = new ChoiceBox<>();
        title.setId("tour-title");
        title.getSelectionModel().selectedIndexProperty().addListener((observableValue, oldVal, newVal) -> {
            if (adjustingPopup.get()) return;
            currentStep.set(newVal.intValue());
        });
        title.getItems().setAll(
                i18n.getString("tour.welcome.title"),
                i18n.getString("tour.filter.title"),
                i18n.getString("tour.feedback.title"),
                i18n.getString("tour.menu.title"),
                i18n.getString("tour.table.title"),
                i18n.getString("tour.status-bar-filters.title"),
                i18n.getString("tour.status-bar-tracking.title"),
                i18n.getString("tour.status-bar-show-done.title")
        );

        String[] messages = new String[8];
        messages[0] = i18n.getString("tour.welcome.msg1") + "\n"
                + i18n.getString("tour.welcome.msg2") + "\n" +
                i18n.getString("tour.welcome.msg3");
        messages[1] = i18n.getString("tour.filter.msg");
        messages[2] = i18n.getString("tour.feedback.msg");
        messages[3] = i18n.getString("tour.menu.msg");
        messages[4] = i18n.getString("tour.table.msg");
        messages[5] = i18n.getString("tour.status-bar-filters.msg");
        messages[6] = i18n.getString("tour.status-bar-tracking.msg");
        messages[7] = i18n.getString("tour.status-bar-show-done.msg");

        Region[] targets = new Region[8];
        targets[0] = root;
        targets[1] = filterBox;
        targets[2] = filterBox;
        targets[3] = menuBtn;
        targets[4] = table;
        targets[5] = statusBar;
        targets[6] = btnPlayStop;
        targets[7] = statusBar;

        PopOver.ArrowLocation[] arrowLocations = new PopOver.ArrowLocation[8];
        arrowLocations[0] = PopOver.ArrowLocation.NO_ARROW;
        arrowLocations[1] = PopOver.ArrowLocation.TOP_CENTER;
        arrowLocations[2] = PopOver.ArrowLocation.TOP_CENTER;
        arrowLocations[3] = PopOver.ArrowLocation.TOP_RIGHT;
        arrowLocations[4] = PopOver.ArrowLocation.LEFT_CENTER;
        arrowLocations[5] = PopOver.ArrowLocation.BOTTOM_CENTER;
        arrowLocations[6] = PopOver.ArrowLocation.BOTTOM_RIGHT;
        arrowLocations[7] = PopOver.ArrowLocation.BOTTOM_RIGHT;

        String[] navLabels = new String[3];
        navLabels[0] = i18n.getString("button.dismiss");
        navLabels[1] = i18n.getString("tour.next");
        navLabels[2] = i18n.getString("tour.previous");

        Hyperlink linkBack = new Hyperlink();
        Hyperlink linkNext = new Hyperlink();

        linkNext.setOnAction(evt -> currentStep.set(currentStep.get() + 1));
        linkBack.setOnAction(evt -> currentStep.set(currentStep.get() - 1));

        Region filler = new Region();
        HBox.setHgrow(filler, Priority.ALWAYS);
        HBox layoutButtons = new HBox(linkBack, filler, linkNext);
        layoutButtons.setAlignment(Pos.BASELINE_CENTER);
        layoutButtons.setSpacing(5);

        Text textMsg = new Text();
        textMsg.setTextAlignment(TextAlignment.JUSTIFY);
        textMsg.setFont(Font.font("System", FontWeight.NORMAL, 14));

        VBox layout = new VBox(title, textMsg, new Separator(), layoutButtons);
        layout.setAlignment(Pos.CENTER);
        layout.setSpacing(5);

        currentStep.addListener((observableValue, oldVal, newVal) -> {
            if (popup[0] != null) popup[0].hide();
            int i = newVal.intValue();

            if (i > 7 || i < 0) {
                return;
            }

            adjustingPopup.set(true);
            title.getSelectionModel().select(i);
            adjustingPopup.set(false);

            textMsg.setText(messages[i]);

            if (i == 7) {
                linkNext.setText(navLabels[0]);
            } else {
                linkNext.setText(navLabels[1]);
            }

            if (i == 0) {
                linkBack.setText(navLabels[0]);
                String[] msgs = messages[0].split("\n");
                Text msg1 = new Text(msgs[0]);
                Text msg2 = new Text(msgs[1]);
                Text msg3 = new Text(msgs[2]);
                msg1.setTextAlignment(TextAlignment.CENTER);
                msg2.setTextAlignment(TextAlignment.CENTER);
                msg3.setTextAlignment(TextAlignment.CENTER);
                msg1.setFont(textMsg.getFont());
                msg2.setFont(textMsg.getFont());
                msg3.setFont(textMsg.getFont());
                VBox layoutText = new VBox(msg1, msg2, msg3);
                layoutText.setAlignment(Pos.CENTER);
                layoutText.setSpacing(4);

                layout.getChildren().remove(1);
                layout.getChildren().add(1, layoutText);

                // animation
                msg1.setOpacity(0);
                msg2.setOpacity(0);
                msg3.setOpacity(0);
                layoutButtons.setVisible(false);

                Timeline timeline = new Timeline();
                timeline.setDelay(Duration.millis(300));
                timeline.getKeyFrames().addAll(
                        new KeyFrame(Duration.millis(0), new KeyValue(msg1.opacityProperty(), 0)),
                        new KeyFrame(Duration.millis(500), new KeyValue(msg1.opacityProperty(), 1)),
                        new KeyFrame(Duration.millis(1500), new KeyValue(msg1.opacityProperty(), 1), new KeyValue(msg2.opacityProperty(), 0)),
                        new KeyFrame(Duration.millis(2000), new KeyValue(msg2.opacityProperty(), 1)),
                        new KeyFrame(Duration.millis(3000), new KeyValue(msg2.opacityProperty(), 1), new KeyValue(msg3.opacityProperty(), 0)),
                        new KeyFrame(Duration.millis(3500), new KeyValue(msg3.opacityProperty(), 1)),
                        new KeyFrame(Duration.millis(4000), new KeyValue(msg3.opacityProperty(), 1))
                );
                timeline.setOnFinished(evt -> layoutButtons.setVisible(true));
                timeline.play();
            } else {
                linkBack.setText(navLabels[2]);

                layout.getChildren().remove(1);
                layout.getChildren().add(1, textMsg);
            }
            popup[0] = PopOver.showPopup(targets[i], layout, arrowLocations[i]);
        });

        currentStep.set(0);
    }
}
