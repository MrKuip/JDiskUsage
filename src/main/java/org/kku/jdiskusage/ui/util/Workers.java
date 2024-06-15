package org.kku.jdiskusage.ui.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

// Example: https://stackoverflow.com/questions/29733004/properly-doing-multithreading-and-thread-pools-with-javafx-tasks

public class Workers
{
  static final Workers m_instance = new Workers();

  final Map<String, Executor> m_executorByNameMap = new HashMap<>();

  private Workers()
  {
  }

  static public Workers getInstance()
  {
    return m_instance;
  }

  public Executor getDefaultExecutor()
  {
    return getExecutor("DEFAULT");
  }

  public Executor getExecutor(String executorName)
  {
    return m_executorByNameMap.computeIfAbsent(executorName, (key) -> {
      return Executors.newFixedThreadPool(1, runnable -> {
        Thread t = new Thread(runnable);
        t.setDaemon(true);
        return t;
      });
    });
  }
}
