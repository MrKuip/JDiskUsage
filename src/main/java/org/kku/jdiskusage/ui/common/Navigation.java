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
  private List<NavigationItem> m_historyList = new ArrayList<NavigationItem>();
  private NavigationItem m_currentItem;
  private BooleanProperty m_homeNavigationDisabled = new SimpleBooleanProperty(true);
  private BooleanProperty m_forwardNavigationDisabled = new SimpleBooleanProperty(true);
  private BooleanProperty m_backNavigationDisabled = new SimpleBooleanProperty(true);
  private boolean m_navigationToInProgress;

  public Navigation(DiskUsageData diskUsageData)
  {
    m_diskUsageData = diskUsageData;

    m_diskUsageData.selectedTreeItemProperty().addListener((_, _, newValue) -> addNavigationItem(newValue));
  }

  private void addNavigationItem(TreeItem<FileNodeIF> treeItem)
  {
    NavigationItem item;

    if (treeItem == null)
    {
      return;
    }

    if (m_navigationToInProgress)
    {
      // navigation through home()/forward()/back() is in progress -> do not add new navigation item
      return;
    }

    // Navigate to a new treeItem causes the removal of all navigation items that are after the 
    //   current navigation item.
    // So if you go back(), back(), back() and then choose a new tree item the 3 'back' items are 
    //   removed from the history list.
    if (m_currentItem != null)
    {
      int currentIndex;
      currentIndex = m_historyList.indexOf(m_currentItem);
      m_historyList = m_historyList.subList(0, currentIndex + 1);
    }

    item = new NavigationItem(treeItem);
    m_historyList.add(item);
    item.evaluate();
  }

  /**
   * Navigation action that navigates to the first item in the history list
   */
  public void home()
  {
    navigateTo(0);
  }

  /**
   * Navigation action that navigates to the next item in the history list
   */
  public void forward()
  {
    navigateTo(m_historyList.indexOf(m_currentItem) + 1);
  }

  /**
   * Navigation action that navigates to the previous item in the history list
   */
  public void back()
  {
    navigateTo(m_historyList.indexOf(m_currentItem) - 1);
  }

  /**
   * Navigate to an item in the history list.
   * 
   * @param toIndex the index of the item in the history list
   */
  private void navigateTo(int toIndex)
  {
    if (!m_navigationToInProgress && toIndex >= 0 && toIndex < m_historyList.size())
    {
      NavigationItem item;

      item = m_historyList.get(toIndex);
      try
      {
        m_navigationToInProgress = true;
        item.navigateTo();
      }
      finally
      {
        m_navigationToInProgress = false;
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

    public void evaluate()
    {
      int currentIndex;

      currentIndex = m_historyList.indexOf(this);

      m_currentItem = this;
      m_homeNavigationDisabled.set(m_historyList.isEmpty());
      m_forwardNavigationDisabled.set(currentIndex < 0 || currentIndex >= (m_historyList.size() - 1));
      m_backNavigationDisabled.set(currentIndex <= 0);
    }

    public void navigateTo()
    {
      evaluate();
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
