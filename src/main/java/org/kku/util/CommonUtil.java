package org.kku.util;

public class CommonUtil
{
  private CommonUtil()
  {
  }

  public static void sleep(int milliSeconds)
  {
    try
    {
      Thread.currentThread().sleep(milliSeconds);
    }
    catch (InterruptedException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
