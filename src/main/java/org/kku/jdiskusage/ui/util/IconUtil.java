package org.kku.jdiskusage.ui.util;

import org.kku.fonticons.ui.FxIcon;
import org.kku.fonticons.ui.FxIcon.IconSize;
import javafx.scene.Node;

public class IconUtil
{
  private IconUtil()
  {
  }

  public static Node createIconNode(String iconName)
  {
    return createIconNode(iconName, IconSize.SMALLER);
  }

  public static Node createIconNode(String iconName, IconSize iconSize)
  {
    return createFxIcon(iconName, iconSize).getIconLabel();
  }

  public static FxIcon createFxIcon(String iconName, IconSize iconSize)
  {
    return new FxIcon(iconName).size(iconSize);
  }
}
