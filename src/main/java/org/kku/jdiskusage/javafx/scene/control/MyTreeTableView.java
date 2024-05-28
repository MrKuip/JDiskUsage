package org.kku.jdiskusage.javafx.scene.control;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;

public class MyTreeTableView<S>
  extends TreeTableView<S>
{
  public MyTreeTableView(String id, TreeItem<S> root)
  {
    super(root);
    setId(id);
  }

  public <R> MyTreeTableColumn<S, R> addColumn(String name)
  {
    MyTreeTableColumn<S, R> column;
    column = new MyTreeTableColumn<S, R>(name, name);
    getColumns().add(column);
    return column;
  }

  public TreeItem<S> getSelectedTreeItem()
  {
    return getSelectionModel().getSelectedItem();
  }

  public SelectedItem getSelectedItem()
  {
    return new SelectedItem();
  }

  public void initSelectedItem(SelectedItem selectedPath)
  {
  }

  public class SelectedItem
  {
    private List<TreeItem<S>> mi_selectedTreeItemPathList;

    private SelectedItem()
    {
      init();
    }

    private void init()
    {
      TreeItem<S> treeItem;

      mi_selectedTreeItemPathList = new ArrayList<>();

      treeItem = getSelectedTreeItem();
      while (treeItem != null)
      {
        mi_selectedTreeItemPathList.add(treeItem);
        treeItem = treeItem.getParent();
      }

      Collections.reverse(mi_selectedTreeItemPathList);
    }
  }
}
