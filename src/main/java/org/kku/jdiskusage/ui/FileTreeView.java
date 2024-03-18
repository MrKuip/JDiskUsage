package org.kku.jdiskusage.ui;

import java.util.List;
import org.kku.jdiskusage.util.FileTree.DirNode;
import org.kku.jdiskusage.util.FileTree.NodeIF;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

public class FileTreeView
{
  private DirNode m_dirNode;

  public FileTreeView(DirNode dirNode)
  {
    m_dirNode = dirNode;
  }

  public TreeView<NodeIF> getComponent()
  {
    return new TreeView<NodeIF>(new FileTreeItem(m_dirNode));
  }

  public class FileTreeItem
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
          ObservableList<TreeItem<NodeIF>> children;

          children = FXCollections.observableArrayList();
          for (NodeIF child : nodeList)
          {
            children.add(new FileTreeItem(child));
          }

          return children;
        }
      }

      return FXCollections.emptyObservableList();
    }
  }
}
