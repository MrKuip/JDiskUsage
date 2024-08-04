package org.kku.jdiskusage.main;

import org.kku.jdiskusage.ui.NumericTextField;
import org.tbee.javafx.scene.layout.MigPane;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class TestFx
  extends Application
{
  @Override
  public void start(Stage stage)
  {
    Scene scene;
    NumericTextField<Double> textField1;
    NumericTextField<Double> textField2;
    NumericTextField<Integer> textField3;
    MigPane pane;

    pane = new MigPane("wrap 2");

    pane.add(new Label("doubleField(#)"));
    textField1 = NumericTextField.doubleField("#");
    textField1.setOnAction(ae -> System.out.println("value=" + textField1.getValue()));
    pane.add(textField1);

    pane.add(new Label("doubleField(#0.000)"));
    textField2 = NumericTextField.doubleField("#0.000");
    textField2.setOnAction(ae -> {
      System.out.println("value=" + textField2.getValue());
    });
    pane.add(textField2);

    pane.add(new Label("integerField()"));
    textField3 = NumericTextField.integerField();
    textField3.setOnAction(ae -> {
      System.out.println("value=" + textField3.getValue());
    });
    pane.add(textField3);

    pane.add(new TextField("123"));
    pane.add(new TextField("456"));
    pane.add(new TextField("789"));

    scene = new Scene(pane);
    stage.setScene(scene);
    stage.show();
  }

  public static void main(String[] args)
  {
    launch();
  }
}