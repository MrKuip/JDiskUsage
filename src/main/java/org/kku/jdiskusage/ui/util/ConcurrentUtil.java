package org.kku.jdiskusage.ui.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

// Example: https://stackoverflow.com/questions/29733004/properly-doing-multithreading-and-thread-pools-with-javafx-tasks

public class ConcurrentUtil
{
  static final ConcurrentUtil m_instance = new ConcurrentUtil();
  static final String DEFAULT_EXECUTOR = "DEFAULT_EXECUTOR";

  final Map<String, Executor> m_executorByNameMap = new HashMap<>();

  private ConcurrentUtil()
  {
  }

  static public ConcurrentUtil getInstance()
  {
    return m_instance;
  }

  public Executor getDefaultExecutor()
  {
    return getExecutor(DEFAULT_EXECUTOR);
  }

  public void run(Runnable run)
  {
  }

  private Executor getExecutor(String executorName)
  {
    return m_executorByNameMap.computeIfAbsent(executorName, (key) -> {
      return Executors.newSingleThreadExecutor(runnable -> {
        Thread thread;

        thread = new Thread(runnable);
        thread.setDaemon(true);

        return thread;
      });
    });
  }
}
