package org.kku.jdiskusage.ui;

import java.util.List;
import java.util.stream.Collectors;
import org.kku.jdiskusage.util.DiskUsageProperties;
import org.kku.jdiskusage.util.FileTree.DirNode;
import org.kku.jdiskusage.util.FileTree.FileNodeIF;
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

  public TreeTableView<FileNodeIF> createComponent()
  {
    TreeTableView<FileNodeIF> node;

    node = new TreeTableView<FileNodeIF>(new FileTreeItem(m_dirNode));

    TreeTableColumn<FileNodeIF, String> treeTableColumn1 = new TreeTableColumn<>("File");
    TreeTableColumn<FileNodeIF, Long> treeTableColumn2 = new TreeTableColumn<>("Size");
    TreeTableColumn<FileNodeIF, Double> treeTableColumn3 = new TreeTableColumn<>("%");

    treeTableColumn1.setPrefWidth(DiskUsageProperties.TREE_TABLE_COLUMN1_SIZE.getDouble(200.0));
    treeTableColumn1.widthProperty().addListener(DiskUsageProperties.TREE_TABLE_COLUMN1_SIZE.getChangeListener());

    treeTableColumn2.setPrefWidth(DiskUsageProperties.TREE_TABLE_COLUMN2_SIZE.getDouble(100.0));
    treeTableColumn2.widthProperty().addListener(DiskUsageProperties.TREE_TABLE_COLUMN2_SIZE.getChangeListener());

    treeTableColumn2.setPrefWidth(DiskUsageProperties.TREE_TABLE_COLUMN3_SIZE.getDouble(100.0));
    treeTableColumn2.widthProperty().addListener(DiskUsageProperties.TREE_TABLE_COLUMN3_SIZE.getChangeListener());

    treeTableColumn1.setCellValueFactory(new Callback<CellDataFeatures<FileNodeIF, String>, ObservableValue<String>>()
    {
      @Override
      public ObservableValue<String> call(CellDataFeatures<FileNodeIF, String> p)
      {
        return new ReadOnlyObjectWrapper<String>(p.getValue().getValue().getName());
      }
    });

    treeTableColumn2.setCellValueFactory(new Callback<CellDataFeatures<FileNodeIF, Long>, ObservableValue<Long>>()
    {
      @Override
      public ObservableValue<Long> call(CellDataFeatures<FileNodeIF, Long> p)
      {
        return new ReadOnlyObjectWrapper<Long>(p.getValue().getValue().getSize());
      }
    });

    treeTableColumn3.setCellValueFactory(new Callback<CellDataFeatures<FileNodeIF, Double>, ObservableValue<Double>>()
    {
      @Override
      public ObservableValue<Double> call(CellDataFeatures<FileNodeIF, Double> p)
      {
        double percentage;

        if (p.getValue().getParent() != null)
        {
          percentage = p.getValue().getValue().getSize() * 100.0 / p.getValue().getParent().getValue().getSize();
        }
        else
        {
          percentage = 100.0;
        }

        return new ReadOnlyObjectWrapper<Double>(percentage);
      }
    });

    node.getColumns().add(treeTableColumn1);
    node.getColumns().add(treeTableColumn2);
    node.getColumns().add(treeTableColumn3);

    return node;
  }

  public static class FileTreeItem
    extends TreeItem<FileNodeIF>
  {
    // We do the children and leaf testing only once, and then set these
    // booleans to false so that we do not check again during this
    // run. A more complete implementation may need to handle more
    // dynamic file system situations (such as where a folder has files
    // added after the TreeView is shown). Again, this is left as an
    // exercise for the reader.
    private boolean isFirstTimeChildren = true;

    public FileTreeItem(FileNodeIF node)
    {
      super(node);

      /*
      expandedProperty().addListener((observable, wasExpanded, isExpanded) ->
      {
        if (wasExpanded && !isExpanded && !isFirstTimeChildren)
        {
          super.getChildren().clear();
          isFirstTimeChildren = true;
        }
      });
      */
    }

    @Override
    public ObservableList<TreeItem<FileNodeIF>> getChildren()
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

    private ObservableList<TreeItem<FileNodeIF>> buildChildren(TreeItem<FileNodeIF> TreeItem)
    {
      FileNodeIF node;

      node = TreeItem.getValue();
      if (node != null && node.isDirectory())
      {
        List<FileNodeIF> nodeList;

        nodeList = ((DirNode) node).getNodeList();
        if (!nodeList.isEmpty())
        {
          return nodeList.stream().sorted(FileNodeIF.getSizeComparator()).map(FileTreeItem::new)
              .collect(Collectors.toCollection(FXCollections::observableArrayList));
        }
      }

      return FXCollections.emptyObservableList();
    }
  }
}
