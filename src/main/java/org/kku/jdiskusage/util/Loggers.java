package org.kku.jdiskusage.util;

import java.util.logging.Level;
import org.kku.common.util.Log;

public class Loggers
{
  static public final Log.MyLogger log = Log.log;
  static public final Log.MyLogger treemap = Log.createLogger("treemap", "treemap", 10000000, 10, Level.INFO);
  static public final Log.MyLogger javafx = Log.createLogger("javafx", Level.INFO);

  private Loggers()
  {
  }
}
