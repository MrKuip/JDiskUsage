package org.kku.jdiskusage.ui.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

// Example: https://stackoverflow.com/questions/29733004/properly-doing-multithreading-and-thread-pools-with-javafx-tasks

public class ConcurrentUtil
{
  static final ConcurrentUtil m_instance = new ConcurrentUtil();
  static final String DEFAULT_EXECUTOR = "DEFAULT_EXECUTOR";

  final Map<String, ThreadPoolExecutor> m_executorByNameMap = new HashMap<>();

  private ConcurrentUtil()
  {
  }

  static public ConcurrentUtil getInstance()
  {
    return m_instance;
  }

  public ThreadPoolExecutor getDefaultExecutor()
  {
    return getExecutor(DEFAULT_EXECUTOR);
  }

  public void run(Runnable run)
  {
  }

  private ThreadPoolExecutor getExecutor(String executorName)
  {
    return m_executorByNameMap.computeIfAbsent(executorName, (key) -> {
      return new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), (runnable -> {
        Thread thread;

        thread = new Thread(runnable);
        thread.setDaemon(true);

        return thread;
      }));
    });
  }
}
