package org.kku.jdiskusage.ui.common;

import org.kku.jdiskusage.util.OperatingSystemUtil;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class FullScreen
{
  private Stage mi_initialStage;
  private Stage mi_fullScreenStage;
  private Stage mi_currentStage;

  public FullScreen(Stage stage)
  {
    mi_initialStage = stage;
    mi_currentStage = mi_initialStage;
  }

  public Stage getCurrentStage()
  {
    return mi_currentStage;
  }

  private Stage getFullscreenStage()
  {
    if (mi_fullScreenStage == null)
    {
      mi_fullScreenStage = new Stage();
      mi_fullScreenStage.setTitle(mi_initialStage.getTitle());
      mi_fullScreenStage.setMaximized(true);
      mi_fullScreenStage.setResizable(false);
      mi_fullScreenStage.initStyle(StageStyle.UNDECORATED);
    }

    return mi_fullScreenStage;
  }

  public void toggleFullScreen()
  {
    if (OperatingSystemUtil.isLinux())
    {
      Scene currentScene;

      currentScene = mi_currentStage.getScene();
      mi_currentStage.setScene(null);
      mi_currentStage.hide();
      mi_currentStage = mi_initialStage == mi_currentStage ? getFullscreenStage() : mi_initialStage;
      mi_currentStage.setScene(currentScene);
      mi_currentStage.show();
    }
    else
    {
      mi_initialStage.setFullScreen(!mi_initialStage.isFullScreen());
    }
  }
}