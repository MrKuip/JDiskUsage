package org.kku.jdiskusage.ui;

import org.kku.jdiskusage.javafx.scene.control.MyTableColumn;
import org.kku.jdiskusage.javafx.scene.control.MyTableView;
import org.kku.jdiskusage.util.FileTree.FileNodeIF;
import org.kku.jdiskusage.util.FileTree.FilterIF;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Test
  extends Application
{
  public static class Data
  {
    private final String m_id;
    private final String m_text;
    private final Integer m_number;

    public Data(String id, String text, Integer number)
    {
      m_id = id;
      m_number = number;
      m_text = text;
    }

    public String getId()
    {
      return m_id;
    }

    public String getText()
    {
      return m_text;
    }

    public Integer getNumber()
    {
      return m_number;
    }
  }

  public class MyFilter
      implements FilterIF
  {
    @Override
    public boolean accept(FileNodeIF path)
    {
      return path.getNumberOfLinks() < 1;
    }
  }

  @Override
  public void start(Stage stage)
  {
    Scene scene;
    MyTableView<Data> tableView;
    MyTableColumn<Data, String> firstColumn;
    MyTableColumn<Data, String> secondColumn;
    MyTableColumn<Data, Integer> thirdColumn;

    tableView = new MyTableView<>("Test");

    firstColumn = tableView.addColumn("id");
    firstColumn.initPersistentPrefWidth(100.0);
    firstColumn.setCellValueGetter(d -> d.getId());

    secondColumn = tableView.addColumn("text");
    secondColumn.initPersistentPrefWidth(100.0);
    secondColumn.setCellValueGetter((d) -> d.getText());

    thirdColumn = tableView.addColumn("number");
    thirdColumn.initPersistentPrefWidth(100.0);
    thirdColumn.setCellValueGetter((d) -> d.getNumber());

    tableView.getItems().add(new Data("callo", "lala", 10));
    tableView.getItems().add(new Data("daar", "rara", 40));
    tableView.getItems().add(new Data("aben", "mama", 20));
    tableView.getItems().add(new Data("ik", "papa", 50));

    scene = new Scene(tableView);
    stage.setTitle("JDiskUsage");
    stage.setScene(scene);
    stage.show();
  }

  public static void main(String[] args)
  {
    launch();
  }
}
