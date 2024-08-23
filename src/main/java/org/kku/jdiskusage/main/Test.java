package org.kku.jdiskusage.main;

import org.kku.jdiskusage.util.TailCall;
import org.kku.jdiskusage.util.TailCalls;

public class Test
{
  public void test()
  {
    System.out.println(factorial(3));
  }

  public static int factorial(final int number)
  {
    return factorialTailRec(1, number).invoke();
  }

  public static TailCall<Integer> factorialTailRec(final int factorial, final int number)
  {
    if (number == 1) return TailCalls.done(factorial);
    else return TailCalls.call(() -> factorialTailRec(factorial * number, number - 1));
  }

  public static void main(String[] args)
  {
    new Test().test();
  }
}