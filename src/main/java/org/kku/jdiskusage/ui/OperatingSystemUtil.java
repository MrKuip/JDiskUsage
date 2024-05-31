package org.kku.jdiskusage.ui;

public class OperatingSystemUtil
{
  private OperatingSystemUtil()
  {
  }

  public static boolean isLinux()
  {
    return System.getProperty("os.name").toLowerCase().contains("linux");
  }
}
