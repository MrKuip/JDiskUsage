package org.kku.jdiskusage.util;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class CommonUtil
{
  private CommonUtil()
  {
  }

  public static long getMidnight()
  {
    Calendar calendar;

    calendar = new GregorianCalendar();
    calendar.set(Calendar.HOUR_OF_DAY, 23); //anything 0 - 23
    calendar.set(Calendar.MINUTE, 59);
    calendar.set(Calendar.SECOND, 59);

    return calendar.getTime().toInstant().toEpochMilli();
  }

  public static void sleep(int milliSeconds)
  {
    try
    {
      Thread.sleep(milliSeconds);
    }
    catch (InterruptedException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
