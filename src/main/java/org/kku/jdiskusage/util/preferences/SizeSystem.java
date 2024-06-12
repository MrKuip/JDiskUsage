package org.kku.jdiskusage.util.preferences;

public enum SizeSystem
{
  SI(1000),
  BINARY(1024);

  private final long mi_factor;

  SizeSystem(long factor)
  {
    mi_factor = factor;
  }

  public long getFactor()
  {
    return mi_factor;
  }

  public String getFileSize(double size)
  {
    if (size > 1000000000)
    {
      return String.format("%,.1f Gb", (size / Math.pow(getFactor(), 3)));
    }
    else if (size > 1000000)
    {
      return String.format("%.1f Mb", (size / Math.pow(getFactor(), 2)));
    }
    else if (size > 1000)
    {
      return String.format("%.1f Kb", (size / getFactor()));
    }

    return ((int) size) + " bytes";
  }
}