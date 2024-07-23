package org.kku.jdiskusage.util;

public class Performance
{
  private Performance()
  {
  }

  public static PerformancePoint measure(String format, Object... args)
  {
    return new PerformancePoint(String.format(format, args));
  }

  static public class PerformancePoint
      implements AutoCloseable
  {
    private final String mi_text;
    private final StopWatch mi_stopWatch;

    private PerformancePoint(String text)
    {
      mi_text = text;
      mi_stopWatch = new StopWatch();
      mi_stopWatch.start();
    }

    public void ready()
    {
      Log.log.info("%s took %d msec", mi_text, mi_stopWatch.getElapsedTime());
    }

    @Override
    public void close()
    {
      ready();
    }
  }
}
