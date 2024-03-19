package org.kku.jdiskusage.ui;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.kku.jdiskusage.util.FileTree.DirNode;
import org.kku.jdiskusage.util.FileTree.NodeIF;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableColumn.CellDataFeatures;
import javafx.scene.control.TreeTableView;
import javafx.util.Callback;

public class FileTreeView
{
  private DirNode m_dirNode;

  public FileTreeView(DirNode dirNode)
  {
    m_dirNode = dirNode;
  }

  public TreeTableView<NodeIF> createComponent()
  {
    TreeTableView<NodeIF> node;

    node = new TreeTableView<NodeIF>(new FileTreeItem(m_dirNode));

    TreeTableColumn<NodeIF, String> treeTableColumn1 = new TreeTableColumn<>("File");
    TreeTableColumn<NodeIF, Long> treeTableColumn2 = new TreeTableColumn<>("Size");

    treeTableColumn1.setCellValueFactory(new Callback<CellDataFeatures<NodeIF, String>, ObservableValue<String>>()
    {
      @Override
      public ObservableValue<String> call(CellDataFeatures<NodeIF, String> p)
      {
        return new ReadOnlyObjectWrapper<String>(p.getValue().getValue().getName());
      }
    });

    treeTableColumn2.setCellValueFactory(new Callback<CellDataFeatures<NodeIF, Long>, ObservableValue<Long>>()
    {
      @Override
      public ObservableValue<Long> call(CellDataFeatures<NodeIF, Long> p)
      {
        return new ReadOnlyObjectWrapper<Long>(p.getValue().getValue().getSize());
      }
    });

    node.getColumns().add(treeTableColumn1);
    node.getColumns().add(treeTableColumn2);

    return node;
  }

  public static class FileTreeItem
    extends TreeItem<NodeIF>
  {
    // We do the children and leaf testing only once, and then set these
    // booleans to false so that we do not check again during this
    // run. A more complete implementation may need to handle more
    // dynamic file system situations (such as where a folder has files
    // added after the TreeView is shown). Again, this is left as an
    // exercise for the reader.
    private boolean isFirstTimeChildren = true;

    public FileTreeItem(NodeIF node)
    {
      super(node);

      expandedProperty().addListener((observable, wasExpanded, isExpanded) ->
      {
        System.out.println("expanded:" + toString());
        if (wasExpanded && !isExpanded && !isFirstTimeChildren)
        {
          super.getChildren().clear();
          isFirstTimeChildren = true;
        }
      });
    }

    @Override
    public ObservableList<TreeItem<NodeIF>> getChildren()
    {
      if (isFirstTimeChildren)
      {
        isFirstTimeChildren = false;

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

    private ObservableList<TreeItem<NodeIF>> buildChildren(TreeItem<NodeIF> TreeItem)
    {
      NodeIF node;

      node = TreeItem.getValue();
      if (node != null && node.isDirectory())
      {
        List<NodeIF> nodeList;

        nodeList = ((DirNode) node).getNodeList();
        if (!nodeList.isEmpty())
        {
          return nodeList.stream().sorted(Comparator.comparing(NodeIF::getSize).reversed()).map(FileTreeItem::new)
              .collect(Collectors.toCollection(FXCollections::observableArrayList));
        }
      }

      return FXCollections.emptyObservableList();
    }
  }
}
