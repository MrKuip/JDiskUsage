package org.kku.jdiskusage.main;

import org.kku.fonticons.ui.FxIcon.IconSize;
import org.kku.jdiskusage.ui.util.IconUtil;
import org.kku.jdiskusage.util.ApplicationPropertyExtensionIF;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

public class Test
  extends Application
    implements ApplicationPropertyExtensionIF
{
  @Override
  public void start(Stage stage)
  {
    Scene scene;
    TabPane tabPane;
    ImageView imageView;
    Tab tab;
    FlowPane pane;
    
    pane = new FlowPane();
   
    imageView  = IconUtil.createImageView("account", IconSize.VERY_LARGE);
    tabPane = new TabPane();
    tabPane.setMinHeight(200);
    tabPane.setMaxHeight(200);
    tab = new Tab("hallo");
    System.out.print(tab.getStyle());
    tab.setGraphic(imageView);
    tabPane.getTabs().add(tab);
    
    
    pane.getChildren().addAll(tabPane);
    		
    scene = new Scene(pane);
    scene.getStylesheets().add("jdiskusage.css");

    stage.setTitle("JDiskUsage");
    stage.setScene(scene);
    stage.show();
  }

  public static void main(String[] args)
  {
    launch();
  }
}
