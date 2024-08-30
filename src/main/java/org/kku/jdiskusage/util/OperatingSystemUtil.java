package org.kku.jdiskusage.util;

import java.nio.file.attribute.BasicFileAttributes;

public class OperatingSystemUtil
{
  private static OperatingSystem OPERATING_SYSTEM;

  private OperatingSystemUtil()
  {
  }

  public static boolean isLinux()
  {
    return getOperatingSystem().isLinux();
  }

  public static boolean isWindows()
  {
    return getOperatingSystem().isWindows();
  }

  public static OperatingSystem getOperatingSystem()
  {
    if (OPERATING_SYSTEM == null)
    {
      OPERATING_SYSTEM = switch (System.getProperty("os.name").toLowerCase())
      {
        case "linux" -> new Linux();
        case "windows" -> new Windows();
        default -> new Unknown();
      };
    }

    return OPERATING_SYSTEM;
  }

  static class Linux
    extends OperatingSystem
  {
    private static final String DEV = "(dev=";

    @Override
    public String getFileStoreId(BasicFileAttributes attr)
    {
      String fileKey;

      fileKey = attr.fileKey().toString();
      if (fileKey.startsWith(DEV))
      {
        int index;

        fileKey = fileKey.substring(DEV.length());
        index = fileKey.indexOf(',');
        if (index != -1)
        {
          return fileKey.substring(0, index);
        }
      }

      return super.getFileStoreId(attr);
    }

    @Override
    public boolean isLinux()
    {
      return true;
    }
  }

  static public class Windows
    extends OperatingSystem
  {
    @Override
    public boolean isWindows()
    {
      return true;
    }
  }

  static class Unknown
    extends OperatingSystem
  {
  }

  public static abstract class OperatingSystem
  {
    public String getFileStoreId(BasicFileAttributes attr)
    {
      return "";
    }

    public boolean isLinux()
    {
      return false;
    }

    public boolean isWindows()
    {
      return false;
    }
  }
}
