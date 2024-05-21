package org.kku.jdiskusage.javafx.scene.control;

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
}
