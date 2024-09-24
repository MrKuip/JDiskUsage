package org.kku.jdiskusage.concurrent;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javafx.application.Platform;
import javafx.concurrent.Task;

public class FxTask<T, P extends ProgressDataIF>
  extends Task<T>
{
  private Function<P, T> mi_runNow;
  private Consumer<T> mi_runLater;
  private Supplier<P> mi_progressData;

  public FxTask()
  {
  }

  public FxTask(FxTaskIF<T, P> task)
  {
    mi_runNow = task::runNow;
    mi_runLater = task::runLater;
    mi_progressData = task::createProgressData;
  }

  public FxTask(Function<P, T> runNow, Consumer<T> runLater, Supplier<P> progressData)
  {
    mi_runNow = runNow;
    mi_runLater = runLater;
    mi_progressData = progressData;
  }

  public void execute()
  {
    FxExecutor.execute(this);
  }

  public void setRunNow(Function<P, T> runNow)
  {
    mi_runNow = runNow;
  }

  public void setRunLater(Consumer<T> runLater)
  {
    mi_runLater = runLater;
  }

  public void setProgressData(Supplier<P> progressData)
  {
    mi_progressData = progressData;
  }

  @Override
  public T call()
  {
    T result;

    result = mi_runNow.apply(mi_progressData.get());
    if (mi_runLater != null)
    {
      Platform.runLater(() -> {
        mi_runLater.accept(result);
      });
    }

    return result;
  }
}