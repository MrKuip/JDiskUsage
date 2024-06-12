package org.kku.jdiskusage.util;

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