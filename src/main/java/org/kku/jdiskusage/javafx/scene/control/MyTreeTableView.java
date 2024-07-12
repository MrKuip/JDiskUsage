package org.kku.jdiskusage.javafx.scene.control;

import static org.kku.jdiskusage.ui.util.TranslateUtil.translate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.StackPane;
import javafx.scene.text.TextAlignment;

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
    Label label;
    StackPane stack;

    column = translate(new MyTreeTableColumn<S, R>());
    column.setId(name);

    label = translate(new Label(name));
    label.setWrapText(true);
    label.setAlignment(Pos.CENTER);
    label.setTextAlignment(TextAlignment.CENTER);

    stack = new StackPane();
    stack.getChildren().add(label);
    stack.prefWidthProperty().bind(column.widthProperty().subtract(5));
    label.prefWidthProperty().bind(stack.prefWidthProperty());

    column.setGraphic(stack);

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
