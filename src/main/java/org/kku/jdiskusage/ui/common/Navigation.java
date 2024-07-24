package org.kku.jdiskusage.ui.common;

import java.util.ArrayList;
import java.util.List;
import org.kku.jdiskusage.ui.DiskUsageView.DiskUsageData;
import org.kku.jdiskusage.util.FileTree.FileNodeIF;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.TreeItem;

public class Navigation
{
  private final DiskUsageData m_diskUsageData;
  private List<NavigationItem> m_navigationItemList = new ArrayList<NavigationItem>();
  private NavigationItem m_currentNavigationItem;
  private BooleanProperty m_homeNavigationDisabled = new SimpleBooleanProperty(true);
  private BooleanProperty m_forwardNavigationDisabled = new SimpleBooleanProperty(true);
  private BooleanProperty m_backNavigationDisabled = new SimpleBooleanProperty(true);
  private NavigationItem m_navigatingTo;

  public Navigation(DiskUsageData diskUsageData)
  {
    m_diskUsageData = diskUsageData;

    m_diskUsageData.selectedTreeItemProperty().addListener((o, oldValue, newValue) -> addNavigationItem(newValue));
  }

  private void addNavigationItem(TreeItem<FileNodeIF> treeItem)
  {
    NavigationItem item;
    int currentIndex;

    if (treeItem == null)
    {
      return;
    }

    if (m_currentNavigationItem != null)
    {
      // This is a navigation caused by home/forward/back
      if (treeItem.equals(m_currentNavigationItem.mii_treeItem))
      {
        return;
      }

      //currentIndex = m_navigationItemList.indexOf(m_currentNavigationItem);
      //m_navigationItemList = m_navigationItemList.subList(0, currentIndex + 1);
    }

    item = new NavigationItem(treeItem);
    m_navigationItemList.add(item);
    item.evaluateWidgets();
  }

  public void home()
  {
    navigateTo(0);
  }

  public void forward()
  {
    navigateTo(m_navigationItemList.indexOf(m_currentNavigationItem) + 1);
  }

  public void back()
  {
    navigateTo(m_navigationItemList.indexOf(m_currentNavigationItem) - 1);
  }

  private void navigateTo(int toIndex)
  {
    if (m_navigatingTo == null && toIndex >= 0 && toIndex < m_navigationItemList.size())
    {
      m_navigatingTo = m_navigationItemList.get(toIndex);
      try
      {
        m_navigatingTo.navigateTo();
      }
      finally
      {
        m_navigatingTo = null;
      }
    }
  }

  public BooleanProperty homeNavigationDisabledProperty()
  {
    return m_homeNavigationDisabled;
  }

  public BooleanProperty backNavigationDisabledProperty()
  {
    return m_backNavigationDisabled;
  }

  public BooleanProperty forwardNavigationDisabledProperty()
  {
    return m_forwardNavigationDisabled;
  }

  private class NavigationItem
  {
    private final TreeItem<FileNodeIF> mii_treeItem;

    private NavigationItem(TreeItem<FileNodeIF> treeItem)
    {
      mii_treeItem = treeItem;
    }

    public void evaluateWidgets()
    {
      int currentIndex;

      currentIndex = m_navigationItemList.indexOf(this);

      m_currentNavigationItem = this;
      m_homeNavigationDisabled.set(m_navigationItemList.isEmpty());
      m_forwardNavigationDisabled.set(currentIndex < 0 || currentIndex >= (m_navigationItemList.size() - 1));
      m_backNavigationDisabled.set(currentIndex <= 0);
    }

    public void navigateTo()
    {
      evaluateWidgets();
      m_diskUsageData.getTreePaneData().navigateTo(mii_treeItem);
    }

    @Override
    public int hashCode()
    {
      return mii_treeItem.hashCode();
    }

    @Override
    public boolean equals(Object o)
    {
      if (!(o instanceof NavigationItem navigationItem))
      {
        return false;
      }

      return navigationItem.mii_treeItem == mii_treeItem;
    }

    @Override
    public String toString()
    {
      return "Navigation: " + mii_treeItem.getValue().getName();
    }
  }
}