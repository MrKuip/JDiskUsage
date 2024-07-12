package org.kku.jdiskusage.main;

import org.kku.jdiskusage.javafx.scene.control.MyTableColumn;
import org.kku.jdiskusage.javafx.scene.control.MyTableView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class TestFx
  extends Application
{
  @Override
  public void start(Stage stage)
  {
    Scene scene;
    MyTableView<String> tableView;
    MyTableColumn<String, Void> filterColumn;

    tableView = new MyTableView<>("id1");
    filterColumn = tableView.addColumn("Filter");
    tableView.addColumn(filterColumn, "Filter1");

    scene = new Scene(tableView);
    stage.setScene(scene);
    stage.show();
  }

  public static void main(String[] args)
  {
    launch();
  }
}