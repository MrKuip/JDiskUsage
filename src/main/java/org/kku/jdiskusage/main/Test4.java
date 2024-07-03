package org.kku.jdiskusage.main;

import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Test4
{
  private void doIT()
  {
    Pattern p;

    p = Pattern.compile(".*jar");

    Stream.of("haha", "haha.jar", "Long name.jar", "no jar").forEach(text -> {
      System.out.println(text + " -> " + p.matcher(text).matches());
    });
  }

  public static void main(String[] args)
  {
    new Test4().doIT();
  }
}
