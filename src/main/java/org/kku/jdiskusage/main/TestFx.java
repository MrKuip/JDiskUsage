package org.kku.jdiskusage.main;

import java.nio.file.Path;
import java.util.Arrays;
import org.kku.jdiskusage.javafx.scene.chart.TreeMapChart;
import org.kku.jdiskusage.javafx.scene.chart.TreeMapModel;
import org.kku.jdiskusage.javafx.scene.chart.TreeMapNode;
import org.kku.jdiskusage.ui.TreeMapChartFormPane.PathNodeTreeMapNode;
import org.kku.jdiskusage.util.FileTree.DirNode;
import org.kku.jdiskusage.util.ScanPath;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class TestFx
  extends Application
{
  @Override
  public void start(Stage stage)
  {
    Scene scene;

    scene = new Scene(getPane());

    stage.setTitle("TextFx");
    stage.setScene(scene);
    stage.setWidth(1200.0);
    stage.setHeight(800.0);
    stage.setOnCloseRequest((_) -> { Platform.exit(); System.exit(1); });
    stage.show();
  }

  private Pane getPane()
  {
    BorderPane pane;
    Button button;
    Path path;
    TreeMapModel<TreeMapNode> model;
    TreeMapChart<TreeMapNode> chart;
    DirNode dirNode;
    Label label;

    path = Path.of("/usr/local/kees/home/kees", ".cache/mozilla/firefox/glj0rnvv.default-esr/");
    dirNode = new ScanPath(null).scan(Arrays.asList(path));
    model = new TreeMapModel<>(PathNodeTreeMapNode.create(dirNode));
    chart = new TreeMapChart<>();
    chart.setModel(model);

    label = new Label();

    chart.setOnMouseMoved((ae) -> {
      label.setText("" + chart.getNodeAt((int) ae.getX(), (int) ae.getY()));
    });

    button = new Button("Refresh");
    button.setOnAction((ae) -> {
      chart.refresh();
    });

    pane = new BorderPane();
    pane.setCenter(chart);
    pane.setTop(button);
    pane.setBottom(label);

    return pane;
  }

  public static void main(String[] args)
  {
    launch();
  }
}