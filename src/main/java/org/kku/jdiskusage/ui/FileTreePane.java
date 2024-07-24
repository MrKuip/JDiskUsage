package org.kku.jdiskusage.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.controlsfx.control.BreadCrumbBar;
import org.kku.jdiskusage.javafx.scene.control.MyTreeTableColumn;
import org.kku.jdiskusage.javafx.scene.control.MyTreeTableView;
import org.kku.jdiskusage.ui.DiskUsageView.DiskUsageData;
import org.kku.jdiskusage.ui.util.FormatterFactory;
import org.kku.jdiskusage.util.FileTree.DirNode;
import org.kku.jdiskusage.util.FileTree.FileNodeIF;
import org.kku.jdiskusage.util.FileTree.FilterIF;
import org.kku.jdiskusage.util.OperatingSystemUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
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
      mi_diskUsageData.getTreePaneData().navigateTo(e.getSelectedCrumb());
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
      expandTo(treeItem);

      treeItem.setExpanded(true);

      mi_treeTableView.getSelectionModel().select(treeItem);

      Platform.runLater(() -> mi_treeTableView.requestFocus());
    }
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

  public BreadCrumbBar<FileNodeIF> createBreadCrumbBar()
  {
    mi_breadCrumbBar = new BreadCrumbBar<>();
    return mi_breadCrumbBar;
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
      MyTreeTableView<FileNodeIF>.SelectedItem selectedPathList;

      mi_filter = filter;

      selectedPathList = mi_treeTableView.getSelectedItem();

      mi_treeTableView.setRoot(new FileTreeItem(m_dirNode.filter(filter)));

      mi_treeTableView.initSelectedItem(selectedPathList);
    }

    private void selectFirstItem()
    {
      selectItem(mi_treeTableView.getSelectionModel().getModelItem(0));
    }

    private void selectItem(TreeItem<FileNodeIF> treeItem)
    {
      Platform.runLater(() -> {
        mi_treeTableView.getSelectionModel().select(treeItem);
        mi_treeTableView.getSelectionModel().getSelectedItem().setExpanded(true);
      });
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
        System.out.println("root path[" + getValue() + "]=" + getPathFromRootList());
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