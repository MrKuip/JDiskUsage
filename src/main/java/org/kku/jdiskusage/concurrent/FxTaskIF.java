package org.kku.jdiskusage.concurrent;

public interface FxTaskIF<T, P>
{
  public T runNow(P progress);

  public void runLater(T result);

  public P createProgressData();
}