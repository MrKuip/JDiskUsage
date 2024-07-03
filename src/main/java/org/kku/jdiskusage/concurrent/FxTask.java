package org.kku.jdiskusage.concurrent;

import java.util.function.Consumer;
import java.util.function.Supplier;
import javafx.application.Platform;
import javafx.concurrent.Task;

public class FxTask<T>
  extends Task<T>
{
  private Supplier<T> mi_runNow;
  private Consumer<T> mi_runLater;

  public FxTask()
  {
  }

  public FxTask(Supplier<T> async, Consumer<T> sync)
  {
    mi_runNow = async;
    mi_runLater = sync;
  }

  public void execute()
  {
    FxExecutor.execute(this);
  }

  public void setRunNow(Supplier<T> runNow)
  {
    mi_runNow = runNow;
  }

  public void setRunLater(Consumer<T> runLater)
  {
    mi_runLater = runLater;
  }

  @Override
  public T call()
  {
    T result;

    result = mi_runNow.get();
    if (mi_runLater != null)
    {
      Platform.runLater(() -> {
        mi_runLater.accept(result);
      });
    }

    return result;
  }
}