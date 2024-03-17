package org.kku.jdiskusage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main
{
  private enum UnixAttribute
  {
    NUMBER_OF_LINKS("nlink"),
    INODE("ino"),
    FILE_SIZE("size");

    private final String m_id;

    UnixAttribute(String id)
    {
      this.m_id = id;
    }

    public String getId()
    {
      return m_id;
    }

    <T> T get(Map<String, Object> attributeMap)
    {
      return (T) attributeMap.get(getId());
    }
  }

  public Main()
  {

  }

  private void scan(String dir) throws Exception
  {
    String attributeIds;
    Path dirPath;
    Map<Long, MyPath> pathByInodeMap = new HashMap<>();

    attributeIds = "unix:"
        + Stream.of(UnixAttribute.values()).map(UnixAttribute::getId).collect(Collectors.joining(","));

    dirPath = Paths.get(dir);
    Files.list(dirPath).sorted().forEach(path -> {
      int previousSize;

      try
      {
        System.out.print("walk: " + path);
        previousSize = pathByInodeMap.size();
        Files.walk(path).filter(Files::isRegularFile).forEach(file -> {
          try
          {
            Map<String, Object> attributes;
            Long inode;

            attributes = Files.readAttributes(file, attributeIds);
            inode = (Long) UnixAttribute.INODE.get(attributes);

            MyPath v = pathByInodeMap.computeIfAbsent(inode, key -> new MyPath(file, attributes));
          }
          catch (Exception e)
          {
            e.printStackTrace();
          }
        });
        System.out.println("new files: " + (pathByInodeMap.size() - previousSize));
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    });

    pathByInodeMap.entrySet().forEach(entry -> {
      System.out.printf("%d %d %d %s%n", entry.getValue().mi_numberOfLinks, entry.getValue().mi_fileSize,
          entry.getKey(), entry.getValue().mi_fileName);
    });
  }

  private static class MyPath
  {
    private final String mi_fileName;
    private final int mi_numberOfLinks;
    private final long mi_fileSize;

    public MyPath(Path file, Map<String, Object> attributes)
    {
      mi_fileName = file.toString();
      mi_numberOfLinks = UnixAttribute.NUMBER_OF_LINKS.get(attributes);
      mi_fileSize = UnixAttribute.FILE_SIZE.get(attributes);
    }

    @Override
    public String toString()
    {
      return mi_numberOfLinks + " " + mi_fileSize + " " + mi_fileName;
    }
  }

  static public void main(String[] args)
  {
    try
    {
      new Main().scan(args.length >= 1 ? args[0] : "/media/kees/CubeSSD/export/hoorn");
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
}
