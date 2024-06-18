package org.kku.jdiskusage.ui.common;

import static org.kku.jdiskusage.ui.util.TranslateUtil.translate;
import org.kku.fonticons.ui.FxIcon;
import org.kku.fonticons.ui.FxIcon.IconSize;
import org.kku.jdiskusage.ui.util.IconUtil;
import org.tbee.javafx.scene.layout.MigPane;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class NotificationView
  extends VBox
{
  public NotificationView()
  {
  }

  public void addMessage(String title, String text)
  {
    getChildren().add(0,
        new MessageItemView(new FxIcon("information").size(IconSize.LARGE).getImageView(), title, text, 10));
    new EventHandler();

    addEventHandler();
  }

  public void addTask(Task<?> task)
  {
    getChildren().add(new TaskItemView<>(task));
  }

  class TaskItemView<T>
    extends MigPane
  {
    private final Node graphic;
    private final ProgressBar progressBar;
    private final Label titleText;
    private final Label messageText;
    private final Button cancelButton;
    private final Task<?> mi_task;

    TaskItemView(Task<?> task)
    {
      super("wrap3", "[][grow][]", "");

      mi_task = task;
      mi_task.stateProperty().addListener((o, oldValue, newValue) -> {
        if (newValue == Worker.State.CANCELLED || newValue == Worker.State.FAILED || newValue == Worker.State.READY
            || newValue == Worker.State.SUCCEEDED)
        {
          NotificationView.this.getChildren().remove(this);
        }
      });

      getStyleClass().add("task-item");

      graphic = IconUtil.createIconNode("magnify", IconSize.LARGE);
      titleText = new Label();
      titleText.textProperty().bind(task.titleProperty());
      titleText.setStyle("-fx-font-weight: bold");
      messageText = new Label();
      messageText.textProperty().bind(task.messageProperty());
      progressBar = new ProgressBar();
      progressBar.setMaxWidth(Double.MAX_VALUE);
      progressBar.setMaxHeight(8);
      progressBar.progressProperty().bind(task.progressProperty());
      cancelButton = new Button("Cancel", IconUtil.createIconNode("cancel", IconSize.SMALLER));
      cancelButton.setTooltip(new Tooltip("Cancel Task"));
      cancelButton.setOnAction(evt -> {
        task.cancel();
      });

      add(graphic, "cell 0 0, span 1 3, aligny center");
      add(titleText, "cell 1 0");
      add(cancelButton, "cell 2 0, span 1 3, aligny center");
      add(messageText, "cell 1 1");
      add(progressBar, "cell 1 2, span 3, grow");
    }
  }

  class MessageItemView
    extends MigPane
  {
    MessageItemView(Node graphicNode, String title, String text, int durationInSeconds)
    {
      super("wrap3", "[][grow]", "");

      Label titleLabel;
      Label messageLabel;

      new Timeline(new KeyFrame(Duration.seconds(durationInSeconds), ae -> {
        NotificationView.this.getChildren().remove(this);
      })).play();

      getStyleClass().add("task-item");

      titleLabel = new Label();
      titleLabel.setText(title);
      titleLabel.setStyle("-fx-font-weight: bold");

      messageLabel = new Label();
      messageLabel.setText(translate(text));

      add(graphicNode, "cell 0 0, span 1 2, aligny center");
      add(titleLabel, "cell 1 0");
      add(messageLabel, "cell 1 1");
    }
  }
}