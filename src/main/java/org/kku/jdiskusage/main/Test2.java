package org.kku.jdiskusage.main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.kku.jdiskusage.util.Log;

public class Test2
{
  public static void main(String[] args)
  {
    Log.log.info("Line without newline");
    Log.log.info("Line %s newline", "without");
    Log.log.info("Line %s newline%n", "with");

    test(Path.of("/tmp"));
    try
    {
      Path path;
      path = Path.of("/tmp");
      if (path != null)
      {
        Path parent = path.getParent();
        if (parent != null)
        {
          Files.createDirectories(parent);
        }
      }
    }
    catch (IOException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public static void test(Path path)
  {
    System.out.println(path);
  }
}
