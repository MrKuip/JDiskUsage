package org.kku.jdiskusage.main;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class Test2
  extends Application
{
  @Override
  public void start(Stage stage)
  {
    IntStream.range(0, 20).forEach(cc -> {
      System.out.println(cc + " -> " + getColumnCountWidth(cc));
    });
  }

  private static Map<Integer, Double> m_columnWidthByColumnCountMap = new HashMap<>();

  static public double getColumnCountWidth(int columnCount)
  {
    return m_columnWidthByColumnCountMap.computeIfAbsent(columnCount, cc -> {
      TextField field;
      Scene scene;

      field = new TextField();
      scene = new Scene(field);
      field.applyCss();
      field.setPrefColumnCount(columnCount);

      return field.prefWidth(-1);
    });
  }

  public static void main(String[] args)
  {
    launch();
  }
}
