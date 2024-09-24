package org.kku.jdiskusage.main;

import java.lang.foreign.Arena;

public class Main2
{
  public static void main(String[] args) throws Throwable
  {
    try (var arena = Arena.ofConfined())
    {
      var array = new LargeArray<Point>(arena, 10L, Point.DESCRIPTOR);

      // populate array
      for (long i = 0; i < array.length(); i++)
      {
        var point = new Point(arena);
        point.setX((int) i);
        point.setY((int) i * 2);
        array.set(i, point);
      }

      // show modification of element in array
      var midPoint = array.get(5L);
      midPoint.setX(42);
      midPoint.setY(117);

      // print array contents
      for (long i = 0; i < array.length(); i++)
      {
        System.out.printf("array[%d] = %s%n", i, array.get(i));
      }
    }
  }
}