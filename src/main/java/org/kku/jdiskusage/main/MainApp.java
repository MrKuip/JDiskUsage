package org.kku.jdiskusage.main;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public final class MainApp
  extends Application
{
  public static void main(final String[] args)
  {
    launch(args);
  }

  @Override
  public void start(final Stage stage)
  {
    final var root = new Group();
    final var scene = new Scene(root, 400, 400);
    final var pane = new BorderPane();
    final var menuBar = new MenuBar();
    final var menu = new Menu("File");
    final var file = new MenuItem("Save As");
    final var textArea = new TextArea();

    file.setOnAction(e -> new FileChooser().showSaveDialog(scene.getWindow()));

    menu.getItems().add(file);
    menuBar.getMenus().add(menu);

    pane.prefHeightProperty().bind(scene.heightProperty());
    pane.prefWidthProperty().bind(scene.widthProperty());
    pane.setTop(menuBar);
    pane.setCenter(textArea);
    root.getChildren().add(pane);

    stage.setScene(scene);
    stage.show();
  }
}