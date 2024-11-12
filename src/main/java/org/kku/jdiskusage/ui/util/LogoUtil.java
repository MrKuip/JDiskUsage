package org.kku.jdiskusage.ui.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.IntStream;
import javafx.scene.image.Image;

public class LogoUtil
{
  private LogoUtil()
  {
  }

  public static List<Image> getLogoList()
  {
    return IntStream.rangeClosed(1, 200).mapToObj(px -> load(px)).filter(image -> image != null).toList();
  }

  private static Image load(int px)
  {
    String name;
    name = "/logo/JDiskUsage-" + px + "px.png";

    try (InputStream is = LogoUtil.class.getResourceAsStream(name))
    {
      return is == null ? null : new Image(is);
    }
    catch (IOException e)
    {
      return null;
    }
  }
}
