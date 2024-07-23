package org.kku.jdiskusage.main;

import org.kku.jdiskusage.util.Log;

public class Test2
{
  public static void main(String[] args)
  {
    Log.log.info("Line without newline");
    Log.log.info("Line %s newline", "without");
    Log.log.info("Line %s newline%n", "with");
  }
}
