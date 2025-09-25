package org.kku.jdiskusage.ui.common;

import java.util.function.Function;
import org.kku.jdiskusage.util.FileTree.DirNode;
import org.kku.jdiskusage.util.FileTree.FileNodeIF;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;

public class FileNodeIterator
{
  final FileNodeIF mi_dirNode;
  private boolean mi_disableCancel;
  private boolean mi_cancel;
  private double mi_oneProcentOfTotalCount;
  private int mi_currentCount;
  private int mi_maxCount = Integer.MAX_VALUE;
  private long mi_timeout = -1;
  private BooleanProperty mi_stoppedOnMaxCountProperty;
  private BooleanProperty mi_stoppedOnTimeoutProperty;
  private IntegerProperty mi_progressProperty;

  public FileNodeIterator(FileNodeIF dirNode)
  {
    mi_dirNode = dirNode;
  }

  public void enableProgress(IntegerProperty progressProperty)
  {
    mi_progressProperty = progressProperty;
    mi_progressProperty.set(0);
  }

  public void setStoppedOnMaxCountProperty(BooleanProperty stoppedOnMaxCountProperty)
  {
    mi_stoppedOnMaxCountProperty = stoppedOnMaxCountProperty;
    mi_stoppedOnMaxCountProperty.set(false);
  }

  public void setStoppedOnTimeoutProperty(BooleanProperty stoppedOnTimeoutProperty)
  {
    mi_stoppedOnTimeoutProperty = stoppedOnTimeoutProperty;
    mi_stoppedOnTimeoutProperty.set(false);
  }

  public void setMaxCount(int maxCount)
  {
    mi_maxCount = maxCount;
  }

  public void setTimeoutInSeconds(int timeoutInSeconds)
  {
    mi_timeout = System.currentTimeMillis() + (timeoutInSeconds * 1000);
  }

  public void cancel()
  {
    if (!mi_cancel)
    {
      mi_cancel = true;
    }
  }

  public void forEach(Function<FileNodeIF, Boolean> action)
  {
    // If progress is reported we first need to know how many files there are
    if (mi_progressProperty != null)
    {
      IntegerProperty progressProperty;

      progressProperty = mi_progressProperty;
      try
      {
        mi_disableCancel = true;
        mi_currentCount = 0;
        mi_progressProperty = null;
        forEach(FileNodeIF::isFile, mi_dirNode);
      }
      finally
      {
        mi_progressProperty = progressProperty;
        mi_disableCancel = false;
        mi_oneProcentOfTotalCount = mi_currentCount / 100.0;
        mi_currentCount = 0;
      }
    }

    forEach(action, mi_dirNode);
  }

  private void forEach(Function<FileNodeIF, Boolean> action, FileNodeIF node)
  {
    if (checkCancel())
    {
      return;
    }

    if (action.apply(node))
    {
      mi_currentCount++;
      if (mi_progressProperty != null)
      {
        mi_progressProperty.set((int) (mi_currentCount / mi_oneProcentOfTotalCount));
      }
    }

    if (node.isDirectory())
    {
      ((DirNode) node).getChildList().forEach(n -> forEach(action, n));
    }
  }

  private boolean checkCancel()
  {
    if (mi_disableCancel)
    {
      return false;
    }

    if (mi_cancel)
    {
      return true;
    }

    if (mi_currentCount >= mi_maxCount)
    {
      mi_cancel = true;
      mi_stoppedOnMaxCountProperty.set(true);
      return true;
    }

    if (mi_timeout > 0 && System.currentTimeMillis() > mi_timeout)
    {
      mi_cancel = true;
      mi_stoppedOnTimeoutProperty.set(true);
      return true;
    }

    return false;
  }

}
