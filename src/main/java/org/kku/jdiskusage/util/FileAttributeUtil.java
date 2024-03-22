package org.kku.jdiskusage.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;

public class FileAttributeUtil
{
  public FileAttributeUtil()
  {
  }

  private void execute(File file) throws Exception
  {
    Path path;
    Map<String, Object> attributes;

    path = file.toPath();

    Arrays.asList("basic", "unix", "dos", "posix").forEach(type ->
    {
      try
      {
        Files.readAttributes(path, type + ":*").forEach((k, v) -> System.out.println(type + "  " + k + " = " + v));
      }
      catch (IOException e)
      {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    });

    System.out.println("File.size=" + Files.size(path));
    Files.readAttributes(path, "basic:fileKey").forEach((k, v) -> System.out.println(k + " = " + v));
    Files.readAttributes(path, "unix:ino").forEach((k, v) -> System.out.println(k + " = " + v));
    Files.readAttributes(path, "unix:ino,fileKey").forEach((k, v) -> System.out.println(k + " = " + v));
  }

  public static void main(String[] args) throws Exception
  {
    new FileAttributeUtil().execute(new File("file.txt"));
  }

}