
package org.kku.jdiskusage.main;

import javafx.beans.property.SimpleDoubleProperty;

public class Test
{
  public Test()
  {
    test2();
  }

  public void test2()
  {
    SimpleDoubleProperty p;

    p = new SimpleDoubleProperty();

    p.addListener((a) -> { System.out.println("invalid"); });
    p.addListener((a, b, c) -> { System.out.println("changed"); });

    p.set(10.0);
    p.set(11.0);
  }

  public static void main(String[] args)
  {
    new Test();
  }
}