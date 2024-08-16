package org.kku.jdiskusage.main;

import org.kku.jdiskusage.util.CommonUtil;

public class Test
{
  public void test()
  {
    Thread thread;
    StringBuffer name;

    name = new StringBuffer();
    for (int i = 0; i < 1000000; i++)
    {
      name.append("a");
    }

    System.out.println("name = " + name.toString());

    thread = new Thread(() -> {
      for (;;)
      {
        print();
      }
    });
    thread.setName(name.toString());
    thread.start();
  }

  private void print()
  {
    CommonUtil.sleep(1000);
    System.out.print(".");
  }

  public static void main(String[] args)
  {
    new Test().test();
    new Test().test();
  }
}