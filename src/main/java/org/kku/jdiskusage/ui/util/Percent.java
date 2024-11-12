package org.kku.jdiskusage.ui.util;

import java.io.IOException;
import java.util.Formattable;
import java.util.Formatter;

public class Percent
    implements Formattable
{
  public static final Percent PERCENT_100 = new Percent(1.0);
  public static final Percent PERCENT_0 = new Percent(0.0);

  private double m_percent;

  public Percent(double numerator, double denominator)
  {
    m_percent = numerator / denominator;
  }

  public Percent(double percent)
  {
    m_percent = percent;
  }

  public double getPercent()
  {
    return m_percent;
  }

  @Override
  public void formatTo(Formatter formatter, int flags, int width, int precision)
  {
    String format;

    format = String.format("%%%d.%df", width, precision);

    try
    {
      formatter.out().append(String.format(format, getPercent() * 100.0));
    }
    catch (IOException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}