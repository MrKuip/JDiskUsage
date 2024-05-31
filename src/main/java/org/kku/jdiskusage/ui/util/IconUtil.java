package org.kku.jdiskusage.ui.util;

import org.kku.fonticons.ui.FxIcon;
import org.kku.fonticons.ui.FxIcon.IconSize;
import javafx.scene.Node;

public class IconUtil
{
  private IconUtil()
  {
  }

  public static Node createImageView(String iconName, IconSize iconSize)
  {
    return new FxIcon(iconName).size(iconSize).getCanvas();
  }
}
