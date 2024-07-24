// WorkerStateUI.java
package org.kku.jdiskusage.ui.util;

import javafx.application.Platform;
import javafx.beans.binding.When;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class TaskPaneItem<T>
  extends GridPane
{
  private final Label title = new Label("");
  private final Label message = new Label("");
  private final Label running = new Label("");
  private final Label state = new Label("");
  private final Label totalWork = new Label("");
  private final Label workDone = new Label("");
  private final Label progress = new Label("");
  private final TextArea value = new TextArea("");
  private final TextArea exception = new TextArea("");
  private final ProgressBar progressBar = new ProgressBar();

  public TaskPaneItem()
  {
    addUI();
  }

  public TaskPaneItem(Worker<T> worker)
  {
    addUI();
    bindToWorker(worker);

    Platform.runLater(() -> {
      Scene scene = new Scene(this);
      Stage stage = new Stage();
      stage.initModality(Modality.NONE);
      stage.setScene(scene);
      stage.show();

      worker.runningProperty().addListener((o, oldValue, newValue) -> {
        if (!newValue)
        {
          stage.hide();
        }
      });
    });
  }

  private void addUI()
  {
    value.setPrefColumnCount(20);
    value.setPrefRowCount(3);
    exception.setPrefColumnCount(20);
    exception.setPrefRowCount(3);
    this.setHgap(5);
    this.setVgap(5);
    addRow(0, new Label("Title:"), title);
    addRow(1, new Label("Message:"), message);
    addRow(2, new Label("Running:"), running);
    addRow(3, new Label("State:"), state);
    addRow(4, new Label("Total Work:"), totalWork);
    addRow(5, new Label("Work Done:"), workDone);
    addRow(6, new Label("Progress:"), new HBox(2, progressBar, progress));
    addRow(7, new Label("Value:"), value);
    addRow(8, new Label("Exception:"), exception);
  }

  public void bindToWorker(final Worker<T> worker)
  {
    // Bind Labels to the properties of the worker
    title.textProperty().bind(worker.titleProperty());
    message.textProperty().bind(worker.messageProperty());
    running.textProperty().bind(worker.runningProperty().asString());
    state.textProperty().bind(worker.stateProperty().asString());
    totalWork.textProperty().bind(new When(worker.totalWorkProperty().isEqualTo(-1)).then("Unknown")
        .otherwise(worker.totalWorkProperty().asString()));
    workDone.textProperty().bind(new When(worker.workDoneProperty().isEqualTo(-1)).then("Unknown")
        .otherwise(worker.workDoneProperty().asString()));
    progress.textProperty().bind(new When(worker.progressProperty().isEqualTo(-1)).then("Unknown")
        .otherwise(worker.progressProperty().multiply(100.0).asString("%.2f%%")));
    progressBar.progressProperty().bind(worker.progressProperty());
    value.textProperty().bind(worker.valueProperty().asString());

    // Display the exception message when an exception occurs in the worker
    worker.exceptionProperty().addListener((prop, oldValue, newValue) -> {
      if (newValue != null)
      {
        exception.setText(newValue.getMessage());
      }
      else
      {
        exception.setText("");
      }
    });
  }
}
