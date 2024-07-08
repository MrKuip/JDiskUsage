package org.kku.jdiskusage.ui;

import org.controlsfx.control.BreadCrumbBar;
import org.kku.jdiskusage.ui.DiskUsageView.DiskUsageData;
import org.kku.jdiskusage.util.FileTree.DirNode;
import org.kku.jdiskusage.util.FileTree.FileNodeIF;
import org.kku.jdiskusage.util.FileTree.FilterIF;
import org.tbee.javafx.scene.layout.MigPane;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;

public class FileTreePane
{
  private final DiskUsageData mi_diskUsageData;
  private FileTreeView mi_fileTreeView;
  private MigPane mi_treePane;
  private BreadCrumbBar<FileNodeIF> mi_breadCrumbBar;
  TreeTableView<FileNodeIF> mi_treeTableView;

  public FileTreePane(DiskUsageData diskUsageData)
  {
    mi_diskUsageData = diskUsageData;
    mi_treePane = new MigPane();
  }

  public Node getNode()
  {
    return mi_treePane;
  }

  public void setFilter(FilterIF filter)
  {
    mi_fileTreeView.setFilter(filter);
  }

  public void createTreeTableView(DirNode dirNode)
  {
    mi_fileTreeView = new FileTreeView(dirNode);
    mi_treeTableView = mi_fileTreeView.createComponent();
    mi_treeTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
      mi_diskUsageData.getTabPaneData().itemSelected();
    });
    mi_breadCrumbBar.selectedCrumbProperty().bind(mi_treeTableView.getSelectionModel().selectedItemProperty());
    mi_breadCrumbBar.setAutoNavigationEnabled(false);
    mi_breadCrumbBar.setOnCrumbAction((e) -> {
      mi_diskUsageData.getNavigation().navigateTo(e.getSelectedCrumb());
    });

    mi_treePane.add(mi_treeTableView, "dock center");

    mi_treeTableView.getSelectionModel().selectedItemProperty()
        .addListener((o, oldValue, newValue) -> mi_diskUsageData.getSelectedTreeItemProperty().set(newValue));
  }

  public void navigateTo(TreeItem<FileNodeIF> treeItem)
  {
    if (treeItem == null)
    {
      return;
    }

    if (mi_treeTableView != null)
    {
      Tab selectedTab;

      // Make sure the path to select is expanded, because selection alone doesn't
      // expand the tree
      TreeItem<FileNodeIF> parentTreeItem = treeItem;
      while ((parentTreeItem = parentTreeItem.getParent()) != null)
      {
        parentTreeItem.setExpanded(true);
      }

      mi_treeTableView.getSelectionModel().select(treeItem);

      // Scroll to the selected item to make sure it is visible for the user
      // mi_treeTableView.scrollTo(mi_treeTableView.getSelectionModel().getSelectedIndex());

      mi_diskUsageData.getTabPaneData().mi_contentByTabId.clear();
      selectedTab = mi_diskUsageData.getTabPaneData().mi_tabPane.getSelectionModel().getSelectedItem();
      mi_diskUsageData.getTabPaneData().fillContent(selectedTab, mi_diskUsageData.getSelectedTreeItem());

      Platform.runLater(() -> mi_treeTableView.requestFocus());
    }
  }

  public BreadCrumbBar<FileNodeIF> createBreadCrumbBar()
  {
    mi_breadCrumbBar = new BreadCrumbBar<>();
    return mi_breadCrumbBar;
  }
}