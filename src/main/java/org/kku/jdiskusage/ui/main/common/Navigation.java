package org.kku.jdiskusage.ui.main.common;

import java.util.ArrayList;
import java.util.List;
import org.kku.jdiskusage.ui.main.DiskUsageView.DiskUsageData;
import org.kku.jdiskusage.util.FileTree.FileNodeIF;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.TreeItem;

public class Navigation
{
  private final DiskUsageData mi_diskUsageData;
  private List<NavigationItem> mi_navigationItemList = new ArrayList<NavigationItem>();
  private NavigationItem mi_currentNavigationItem;
  private BooleanProperty mi_homeNavigationDisabled = new SimpleBooleanProperty(true);
  private BooleanProperty mi_forwardNavigationDisabled = new SimpleBooleanProperty(true);
  private BooleanProperty mi_backNavigationDisabled = new SimpleBooleanProperty(true);
  private NavigationItem mi_navigatingTo;

  public Navigation(DiskUsageData diskUsageData)
  {
    mi_diskUsageData = diskUsageData;
  }

  public void navigateTo(TreeItem<FileNodeIF> treeItem)
  {
    NavigationItem item;

    if (mi_navigatingTo != null)
    {
      return;
    }

    item = new NavigationItem(treeItem);
    mi_navigationItemList.add(item);

    item.navigateTo();
  }

  public void home()
  {
    navigateTo(0);
  }

  public void forward()
  {
    navigateTo(mi_navigationItemList.indexOf(mi_currentNavigationItem) + 1);
  }

  public void back()
  {
    navigateTo(mi_navigationItemList.indexOf(mi_currentNavigationItem) - 1);
  }

  private void navigateTo(int toIndex)
  {
    if (mi_navigatingTo == null && toIndex >= 0 && toIndex < mi_navigationItemList.size())
    {
      mi_navigatingTo = mi_navigationItemList.get(toIndex);
      try
      {
        mi_navigatingTo.navigateTo();
      }
      finally
      {
        mi_navigatingTo = null;
      }
    }
  }

  public BooleanProperty homeNavigationDisabledProperty()
  {
    return mi_homeNavigationDisabled;
  }

  public BooleanProperty backNavigationDisabledProperty()
  {
    return mi_backNavigationDisabled;
  }

  public BooleanProperty forwardNavigationDisabledProperty()
  {
    return mi_forwardNavigationDisabled;
  }

  private class NavigationItem
  {
    private final TreeItem<FileNodeIF> mii_treeItem;

    private NavigationItem(TreeItem<FileNodeIF> treeItem)
    {
      mii_treeItem = treeItem;
    }

    public void navigateTo()
    {
      int currentIndex;

      currentIndex = mi_navigationItemList.indexOf(this);

      mi_currentNavigationItem = this;
      mi_homeNavigationDisabled.set(mi_navigationItemList.isEmpty());
      mi_forwardNavigationDisabled.set(currentIndex < 0 || currentIndex >= (mi_navigationItemList.size() - 1));
      mi_backNavigationDisabled.set(currentIndex <= 0);

      mi_diskUsageData.getTreePaneData().navigateTo(mii_treeItem);
    }
  }
}