package org.kku.jdiskusage.ui.util;

import org.kku.fonticons.ui.FxIcon;
import org.kku.fonticons.ui.FxIcon.IconSize;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class IconUtil
{
  private IconUtil()
  {
  }

  public static Node createImageNode(String iconName, IconSize iconSize)
  {
    return new ImageView(createImage(iconName, iconSize));
  }

  public static Image createImage(String iconName, IconSize iconSize)
  {
    return new FxIcon(iconName).getImage(iconSize);
  }
}
