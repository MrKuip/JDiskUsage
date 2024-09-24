package org.kku.jdiskusage.main;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SequenceLayout;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.VarHandle;

import javafx.application.Application;
import javafx.stage.Stage;

public class TestFx2
  extends Application
{
  @Override
  public void start(Stage stage) throws Exception
  {
    test1();
    System.exit(1);
  }

  private void test1()
  {
    SequenceLayout pointLayout;

    pointLayout = MemoryLayout.sequenceLayout(10,
        MemoryLayout.structLayout(ValueLayout.JAVA_INT.withName("x"), ValueLayout.JAVA_INT.withName("y")));

    VarHandle xHandle = pointLayout.varHandle(PathElement.sequenceElement(), PathElement.groupElement("x"));
    VarHandle yHandle = pointLayout.varHandle(PathElement.sequenceElement(), PathElement.groupElement("y"));

    MemorySegment segment = Arena.ofAuto().allocate(pointLayout);

    for (int i = 0; i < pointLayout.elementCount(); i++)
    {
      xHandle.set(segment, 0L, i, i);
      yHandle.set(segment, 0L, i, i + 100);
    }

    System.out.println("x[9]=" + xHandle.get(segment, 0, 9));
    System.out.println("y[9]=" + yHandle.get(segment, 0, 9));
  }

  class PointArray
  {
    Point addPoint(int x, int y)
    {
      return new Point(x, y);
    }
  }

  class Point
  {
    public Point(int x, int y)
    {
    }

    public int getX()
    {
      return 0;
    }

    public int getY()
    {
      return 0;
    }
  }

  public static void main(String[] args)
  {
    launch();
  }
}