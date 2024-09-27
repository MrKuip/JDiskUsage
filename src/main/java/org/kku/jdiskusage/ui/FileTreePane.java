package org.kku.jdiskusage.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.kku.jdiskusage.javafx.scene.control.BreadCrumbBar;
import org.kku.jdiskusage.javafx.scene.control.MyTreeTableColumn;
import org.kku.jdiskusage.javafx.scene.control.MyTreeTableView;
import org.kku.jdiskusage.ui.DiskUsageView.DiskUsageData;
import org.kku.jdiskusage.ui.util.FormatterFactory;
import org.kku.jdiskusage.util.FileTree.DirNode;
import org.kku.jdiskusage.util.FileTree.FileNodeIF;
import org.kku.jdiskusage.util.FileTree.FilterIF;
import org.kku.jdiskusage.util.Log;
import org.kku.jdiskusage.util.OperatingSystemUtil;
import org.kku.jdiskusage.util.preferences.AppPreferences;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.BorderPane;

public class FileTreePane
{
  private final DiskUsageData m_diskUsageData;
  private FileTreeView m_fileTreeView;
  private BorderPane m_treePane;
  private BreadCrumbBar<FileNodeIF> m_breadcrumbBar = new BreadCrumbBar<>();
  private TreeTableView<FileNodeIF> m_treeTableView;

  public FileTreePane(DiskUsageData diskUsageData)
  {
    m_diskUsageData = diskUsageData;
    m_treePane = new BorderPane();
  }

  public Node getNode()
  {
    return m_treePane;
  }

  public void setFilter(FilterIF filter)
  {
    m_fileTreeView.setFilter(filter);
  }

  public BreadCrumbBar<FileNodeIF> getBreadCrumbBar()
  {
    return m_breadcrumbBar;
  }

  public void createTreeTableView(DirNode dirNode)
  {
    m_fileTreeView = new FileTreeView(dirNode);
    m_treeTableView = m_fileTreeView.createComponent();

    m_breadcrumbBar.treeItem().bind(m_diskUsageData.selectedTreeItemProperty());
    m_breadcrumbBar.selectedTreeItem().addListener((o, oldValue, newValue) -> {
      m_diskUsageData.getTreePaneData().navigateTo(newValue);
    });

    m_treePane.setCenter(m_treeTableView);

    m_diskUsageData.selectedTreeItemProperty().bind(m_treeTableView.getSelectionModel().selectedItemProperty());
    m_diskUsageData.selectedTreeItemProperty().addListener(this::evaluateExpand);
  }

  private final void evaluateExpand(ObservableValue<? extends TreeItem<FileNodeIF>> observable,
      TreeItem<FileNodeIF> oldTreeItem, TreeItem<FileNodeIF> newTreeItem)
  {
    if (AppPreferences.autoExpandTreeNode.get())
    {
      if (newTreeItem != null)
      {
        Platform.runLater(() -> {
          if (!newTreeItem.isExpanded())
          {
            Log.log.fine("auto expand:" + newTreeItem);
            newTreeItem.setExpanded(true);
          }
        });
      }
    }

    if (AppPreferences.autoCollapseTreeNode.get())
    {
      if (oldTreeItem != null)
      {
        // Do not collapse if the old node is a parent node of the new node
        if (!Stream.iterate(newTreeItem, Objects::nonNull, TreeItem::getParent).filter(ti -> oldTreeItem == ti)
            .findFirst().isPresent())
        {
          Platform.runLater(() -> {
            if (oldTreeItem.isExpanded())
            {
              Log.log.fine("auto collapse:" + oldTreeItem);
              oldTreeItem.setExpanded(false);
            }
          });
        }
      }
    }
  }

  public void navigateTo(TreeItem<FileNodeIF> treeItem)
  {
    if (treeItem == null)
    {
      return;
    }

    if (m_treeTableView != null)
    {

      m_fileTreeView.selectItem(treeItem);
    }
  }

  private static class FileTreeView
  {
    private DirNode m_dirNode;
    private FilterIF mi_filter = (fn) -> true;
    private MyTreeTableView<FileNodeIF> mi_treeTableView;

    public FileTreeView(DirNode dirNode)
    {
      m_dirNode = dirNode;
    }

    public TreeTableView<FileNodeIF> createComponent()
    {
      MyTreeTableColumn<FileNodeIF, String> treeTableColumn1;
      MyTreeTableColumn<FileNodeIF, Long> treeTableColumn2;
      MyTreeTableColumn<FileNodeIF, Double> treeTableColumn3;
      MyTreeTableColumn<FileNodeIF, Integer> treeTableColumn4;

      mi_treeTableView = new MyTreeTableView<FileNodeIF>(getClass().getSimpleName(), new FileTreeItem(m_dirNode));

      treeTableColumn1 = mi_treeTableView.addColumn("File");
      treeTableColumn1.setColumnCount(20);
      treeTableColumn1.setCellValueGetter((treeItem) -> treeItem.getValue().getName());

      treeTableColumn2 = mi_treeTableView.addColumn("File size");
      treeTableColumn2.setColumnCount(8);
      treeTableColumn2.setCellValueFormatter(FormatterFactory.createStringFormatFormatter("%,d"));
      treeTableColumn2.setCellValueAlignment(Pos.BASELINE_RIGHT);
      treeTableColumn2.setCellValueGetter((treeItem) -> treeItem.getValue().getSize());

      treeTableColumn2 = mi_treeTableView.addColumn("Number of files");
      treeTableColumn2.setColumnCount(8);
      treeTableColumn2.setCellValueFormatter(FormatterFactory.createStringFormatFormatter("%,d"));
      treeTableColumn2.setCellValueAlignment(Pos.BASELINE_RIGHT);
      treeTableColumn2.setCellValueGetter((treeItem) -> treeItem.getValue().getNumberOfFiles());

      treeTableColumn3 = mi_treeTableView.addColumn("%");
      treeTableColumn3.setColumnCount(5);
      treeTableColumn3.setCellValueFormatter(FormatterFactory.createStringFormatFormatter("%3.2f %%"));
      treeTableColumn3.setCellValueAlignment(Pos.BASELINE_RIGHT);
      treeTableColumn3.setCellValueGetter((treeItem) -> {
        if (treeItem.getParent() != null)
        {
          return treeItem.getValue().getSize() * 100.0 / treeItem.getParent().getValue().getSize();
        }
        else
        {
          return 100.0;
        }
      });

      if (OperatingSystemUtil.isLinux())
      {
        treeTableColumn4 = mi_treeTableView.addColumn("Number of links to file");
        treeTableColumn4.setColumnCount(6);
        treeTableColumn4.setCellValueAlignment(Pos.BASELINE_RIGHT);
        treeTableColumn4.setCellValueGetter((treeItem) -> treeItem.getValue().getNumberOfLinks());
      }

      selectFirstItem();

      return mi_treeTableView;
    }

    public void setFilter(FilterIF filter)
    {
      mi_filter = filter;

      mi_treeTableView.setRoot(new FileTreeItem(m_dirNode.filter(filter)));
    }

    private void selectFirstItem()
    {
      selectItem(mi_treeTableView.getSelectionModel().getModelItem(0));
    }

    private void selectItem(TreeItem<FileNodeIF> treeItem)
    {
      mi_treeTableView.getSelectionModel().select(treeItem);
      expandTo(treeItem);
      mi_treeTableView.scrollTo(mi_treeTableView.getSelectionModel().getSelectedIndex());
      mi_treeTableView.requestFocus();
    }

    private void expandTo(TreeItem<FileNodeIF> treeItem)
    {
      if (treeItem instanceof FileTreeView.FileTreeItem fileTreeItem)
      {
        AtomicReference<TreeItem<FileNodeIF>> currentTreeNode = new AtomicReference<>();
        AtomicReference<Boolean> walkTreeFailed = new AtomicReference<>();

        walkTreeFailed.set(false);

        currentTreeNode.set(mi_treeTableView.getRoot());
        mi_treeTableView.getRoot().setExpanded(true);
        fileTreeItem.getPathFromRootList().forEach(fileNode -> {
          if (!walkTreeFailed.get())
          {
            Optional<TreeItem<FileNodeIF>> childTreeItem;

            childTreeItem = currentTreeNode.get().getChildren().stream()
                .filter(ti -> Objects.equals(ti.getValue().getName(), fileNode.getName())).findFirst();
            childTreeItem.ifPresentOrElse(ti -> {
              ti.setExpanded(true);
              currentTreeNode.set(ti);
            }, () -> walkTreeFailed.set(true));
          }
        });

        mi_treeTableView.requestFocus();
        mi_treeTableView.getSelectionModel().select(currentTreeNode.get());
      }
    }

    private class FileTreeItem
      extends TreeItem<FileNodeIF>
    {
      private boolean mi_isFirstTimeChildren = true;
      private List<FileNodeIF> mi_pathFromRootList;

      private FileTreeItem(FileNodeIF node)
      {
        super(node);

        // Release memory
        expandedProperty().addListener((observable, wasExpanded, isExpanded) -> {
          if (wasExpanded && !isExpanded && !mi_isFirstTimeChildren)
          {
            super.getChildren().clear();
            mi_isFirstTimeChildren = true;
          }
        });

      }

      private void initPathFromRootList()
      {
        getPathFromRootList();
      }

      public List<FileNodeIF> getPathFromRootList()
      {
        if (mi_pathFromRootList == null)
        {
          mi_pathFromRootList = new ArrayList<>();
          if (getParent() instanceof FileTreeItem treeFileItem)
          {
            mi_pathFromRootList.addAll(treeFileItem.getPathFromRootList());
            mi_pathFromRootList.add(getValue());
          }
        }
        return mi_pathFromRootList;
      }

      @Override
      public ObservableList<TreeItem<FileNodeIF>> getChildren()
      {
        if (mi_isFirstTimeChildren)
        {
          mi_isFirstTimeChildren = false;

          // First getChildren() call, so we actually go off and
          // determine the children of the File contained in this TreeItem.
          super.getChildren().setAll(buildChildren(this));

          // Remember the path to the root because the treeitem children are cleared at certain points in time and
          //   now it is possible to find the new treeitem with this path
          super.getChildren().stream().map(FileTreeItem.class::cast).forEach(FileTreeItem::initPathFromRootList);
        }

        return super.getChildren();
      }

      @Override
      public boolean isLeaf()
      {
        return !getValue().isDirectory();
      }

      private ObservableList<TreeItem<FileNodeIF>> buildChildren(TreeItem<FileNodeIF> TreeItem)
      {
        FileNodeIF node;

        node = TreeItem.getValue();
        if (node != null && node.isDirectory())
        {
          List<FileNodeIF> nodeList;

          nodeList = ((DirNode) node).getChildList();
          if (!nodeList.isEmpty())
          {
            return nodeList.stream().filter(fileNode -> fileNode.isDirectory() || mi_filter.accept(fileNode))
                .sorted(FileNodeIF.getSizeComparator()).map(FileTreeItem::new)
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
          }
        }

        return FXCollections.emptyObservableList();
      }
    }
  }

}