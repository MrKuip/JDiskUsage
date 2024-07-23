package org.kku.jdiskusage.ui;

import org.controlsfx.control.BreadCrumbBar;
import org.kku.jdiskusage.ui.DiskUsageView.DiskUsageData;
import org.kku.jdiskusage.util.FileTree.DirNode;
import org.kku.jdiskusage.util.FileTree.FileNodeIF;
import org.kku.jdiskusage.util.FileTree.FilterIF;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.BorderPane;

public class FileTreePane
{
  private final DiskUsageData mi_diskUsageData;
  private FileTreeView mi_fileTreeView;
  private BorderPane mi_treePane;
  private BreadCrumbBar<FileNodeIF> mi_breadCrumbBar;
  TreeTableView<FileNodeIF> mi_treeTableView;

  public FileTreePane(DiskUsageData diskUsageData)
  {
    mi_diskUsageData = diskUsageData;
    mi_treePane = new BorderPane();
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
    mi_breadCrumbBar.selectedCrumbProperty().bind(mi_diskUsageData.selectedTreeItemProperty());
    mi_breadCrumbBar.setAutoNavigationEnabled(false);
    mi_breadCrumbBar.setOnCrumbAction((e) -> {
      mi_diskUsageData.getNavigation().navigateTo(e.getSelectedCrumb());
    });

    mi_treePane.setCenter(mi_treeTableView);

    mi_diskUsageData.selectedTreeItemProperty().bind(mi_treeTableView.getSelectionModel().selectedItemProperty());
  }

  public void navigateTo(TreeItem<FileNodeIF> treeItem)
  {
    if (treeItem == null)
    {
      return;
    }

    if (mi_treeTableView != null)
    {
      TreeItem<FileNodeIF> parentTreeItem;

      // Make sure the path to select is expanded, because selection alone doesn't
      // expand the tree
      parentTreeItem = treeItem;
      while ((parentTreeItem = parentTreeItem.getParent()) != null)
      {
        parentTreeItem.setExpanded(true);
      }

      mi_treeTableView.getSelectionModel().select(treeItem);

      // Scroll to the selected item to make sure it is visible for the user
      // mi_treeTableView.scrollTo(mi_treeTableView.getSelectionModel().getSelectedIndex());

      //mi_diskUsageData.getTabPaneData().mi_contentByTabId.clear();
      //selectedTab = mi_diskUsageData.getTabPaneData().mi_tabPane.getSelectionModel().getSelectedItem();
      //mi_diskUsageData.getTabPaneData().fillContent(selectedTab, mi_diskUsageData.getSelectedTreeItem());

      Platform.runLater(() -> mi_treeTableView.requestFocus());
    }
  }

  public BreadCrumbBar<FileNodeIF> createBreadCrumbBar()
  {
    mi_breadCrumbBar = new BreadCrumbBar<>();
    return mi_breadCrumbBar;
  }
}