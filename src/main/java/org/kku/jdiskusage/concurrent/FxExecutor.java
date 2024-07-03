package org.kku.jdiskusage.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class FxExecutor
{
  private static FxExecutor instance = new FxExecutor();

  private ExecutorService m_executor = Executors.newSingleThreadExecutor(new ThreadFactory()
  {
    @Override
    public Thread newThread(Runnable r)
    {
      Thread thread;

      thread = new Thread();
      thread.setDaemon(true);

      return thread;
    }
  });

  private FxExecutor()
  {
  }

  public static <T> void execute(FxTask<T> fxTask)
  {
    instance.m_executor.execute(fxTask);
  }
}