package org.kku.jdiskusage.util;

public class SizeUtil
{
  public static String getFileSize(double size)
  {
    if (size > 1000000000)
    {
      return String.format("%.1f Gb", (size / 1000000000));
    }
    else if (size > 1000000)
    {
      return String.format("%.1f Mb", (size / 1000000));
    }
    else if (size > 1000)
    {
      return String.format("%.1f Kb", (size / 1000));
    }

    return ((int) size) + " bytes";
  }
}
