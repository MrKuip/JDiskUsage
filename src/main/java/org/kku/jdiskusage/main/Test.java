
package org.kku.jdiskusage.main;

import org.kku.jdiskusage.util.AppPropertyExtensionIF;

public class Test implements AppPropertyExtensionIF
{
  public Test()
  {
    test2();
  }

  public void test2()
  {
    /*
    System.out.println("get:" + AppProperties2.WIDTH.forSubject(this).get(10l));
    AppProperties2.WIDTH.forSubject(this).set(12l);
    System.out.println("get:" + AppProperties2.WIDTH.forSubject(this).get(11l));
    */
  }

  public static void main(String[] args)
  {
    new Test();
  }
}