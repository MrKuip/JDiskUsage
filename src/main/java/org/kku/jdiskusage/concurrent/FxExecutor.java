package org.kku.jdiskusage.concurrent;

import org.kku.jdiskusage.ui.util.ConcurrentUtil;

public class FxExecutor
{
  private static FxExecutor instance = new FxExecutor();

  private FxExecutor()
  {
  }

  public static <T, P extends ProgressDataIF> void execute(FxTask<T, P> fxTask)
  {
    ConcurrentUtil.getInstance().getDefaultExecutor().submit(fxTask);
  }
}