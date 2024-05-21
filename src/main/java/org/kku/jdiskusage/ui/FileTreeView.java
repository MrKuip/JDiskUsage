package org.kku.jdiskusage.ui;

import java.util.List;
import java.util.stream.Collectors;
import org.kku.jdiskusage.javafx.scene.control.MyTreeTableColumn;
import org.kku.jdiskusage.javafx.scene.control.MyTreeTableView;
import org.kku.jdiskusage.ui.util.FormatterFactory;
import org.kku.jdiskusage.util.FileTree.DirNode;
import org.kku.jdiskusage.util.FileTree.FileNodeIF;
import org.kku.jdiskusage.util.FileTree.FilterIF;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;

public class FileTreeView
{
  private DirNode m_dirNode;
  private FilterIF mi_filter = (fn) -> true;

  public FileTreeView(DirNode dirNode)
  {
    m_dirNode = dirNode;
  }

  public TreeTableView<FileNodeIF> createComponent()
  {
    MyTreeTableView<FileNodeIF> node;
    MyTreeTableColumn<FileNodeIF, String> treeTableColumn1;
    MyTreeTableColumn<FileNodeIF, Long> treeTableColumn2;
    MyTreeTableColumn<FileNodeIF, Double> treeTableColumn3;
    MyTreeTableColumn<FileNodeIF, Integer> treeTableColumn4;

    node = new MyTreeTableView<FileNodeIF>(getClass().getSimpleName(), new FileTreeItem(m_dirNode));

    treeTableColumn1 = node.addColumn("File");
    treeTableColumn1.initPersistentPrefWidth(200.0);
    treeTableColumn1.setCellValueGetter((treeItem) -> treeItem.getValue().getName());

    treeTableColumn2 = node.addColumn("Size");
    treeTableColumn2.initPersistentPrefWidth(100.0);
    treeTableColumn2.setCellValueFormatter(FormatterFactory.createStringFormatFormatter("%,d"));
    treeTableColumn2.setCellValueAlignment(Pos.CENTER_RIGHT);
    treeTableColumn2.setCellValueGetter((treeItem) -> treeItem.getValue().getSize());

    treeTableColumn3 = node.addColumn("%");
    treeTableColumn3.initPersistentPrefWidth(100.0);
    treeTableColumn3.setCellValueFormatter(FormatterFactory.createStringFormatFormatter("%3.2f %%"));
    treeTableColumn3.setCellValueAlignment(Pos.CENTER_RIGHT);
    treeTableColumn3.setCellValueGetter((treeItem) ->
    {
      if (treeItem.getParent() != null)
      {
        return treeItem.getValue().getSize() * 100.0 / treeItem.getParent().getValue().getSize();
      }
      else
      {
        return 100.0;
      }
    });

    treeTableColumn4 = node.addColumn("Number\nof inode\nlinks");
    treeTableColumn4.initPersistentPrefWidth(100.0);
    treeTableColumn4.setCellValueAlignment(Pos.CENTER_RIGHT);
    treeTableColumn4.setCellValueGetter((treeItem) -> treeItem.getValue().getNumberOfLinks());

    return node;
  }

  public void setFilter(FilterIF filter)
  {
    mi_filter = filter;
  }

  public class FileTreeItem
    extends TreeItem<FileNodeIF>
  {
    private boolean mi_isFirstTimeChildren = true;

    private FileTreeItem(FileNodeIF node)
    {
      super(node);

      // Release memory
      expandedProperty().addListener((observable, wasExpanded, isExpanded) ->
      {
        if (wasExpanded && !isExpanded && !mi_isFirstTimeChildren)
        {
          super.getChildren().clear();
          mi_isFirstTimeChildren = true;
        }
      });
    }

    private void reset()
    {
      mi_isFirstTimeChildren = true;
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
          return nodeList.stream().filter(mi_filter::accept).sorted(FileNodeIF.getSizeComparator())
              .map(FileTreeItem::new).collect(Collectors.toCollection(FXCollections::observableArrayList));
        }
      }

      return FXCollections.emptyObservableList();
    }
  }
}
