package org.kku.jdiskusage.ui.common;

import org.kku.jdiskusage.main.Main;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

public class FxDialog<R>
{
  private final Stage m_stage;
  private R m_result;

  public FxDialog(Parent content, R noResult)
  {
    Scene scene;

    m_stage = new Stage();
    scene = new Scene(content);
    scene.getStylesheets().add("jdiskusage.css");
    m_stage.initOwner(Main.getRootStage());
    m_stage.initModality(Modality.APPLICATION_MODAL);
    m_stage.initStyle(StageStyle.UNDECORATED);
    m_stage.setScene(scene);

    m_result = noResult;
  }

  public void setLocation(double x, double y)
  {
    m_stage.setX(x);
    m_stage.setY(y);

    m_stage.setOnShown(null);
  }

  public void setOnShown(EventHandler<WindowEvent> value)
  {
    m_stage.setOnShown(value);
  }

  public void showAndWait()
  {
    m_stage.showAndWait();
  }

  public void setResult(R result)
  {
    m_result = result;
  }

  public R getResult()
  {
    return m_result;
  }

  public void close()
  {
    m_stage.close();
  }
}
