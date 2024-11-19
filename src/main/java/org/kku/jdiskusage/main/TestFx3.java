package org.kku.jdiskusage.main;

import java.lang.foreign.AddressLayout;
import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import org.kku.jdiskusage.util.Performance;
import org.kku.jdiskusage.util.Performance.PerformancePoint;
import javafx.application.Application;
import javafx.stage.Stage;

public class TestFx3
  extends Application
{
  @Override
  public void start(Stage stage) throws Exception
  {
    try
    {
      test1();
    }
    catch (Throwable e)
    {
      e.printStackTrace();
    }
    System.exit(1);
  }

  private void test1() throws Throwable
  {
    Path path;

    path = Path.of("/home/kees/test.java");
    try (PerformancePoint p = Performance.measure("rdfind: %s", path))
    {
      //getMD5(path, Files.size(path));
    }

    Linker linker = Linker.nativeLinker();

    MemorySegment symbol = linker.defaultLookup().find("strlen").orElseThrow();
    MethodHandle strlen = linker.downcallHandle(symbol,
        FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS));

    Arena arena = Arena.ofAuto();
    MemorySegment str = arena.allocateFrom("Hello");
    long len = (long) strlen.invoke(str);
    System.out.println("len=" + len);

    System.exit(1);
  }

  public long getMD5(Path path, long size)
  {
    long result;

    result = 0;
    try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ))
    {
      ByteBuffer bb;
      MessageDigest md;

      Arena arena = Arena.ofConfined();
      MemorySegment ms = channel.map(FileChannel.MapMode.READ_ONLY, 0, size, arena);

      for (int i = 0; i < size; i++)
      {
        byte b;

        b = ms.getAtIndex(AddressLayout.JAVA_BYTE, i);
        result += ms.getAtIndex(AddressLayout.JAVA_BYTE, i);
        System.out.print(b);
      }

      arena.close();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }

    return result;
  }

  public static void main(String[] args)
  {
    launch();
  }
}