package org.kku.jdiskusage.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileTree
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

  public FileTree()
  {
  }

  private interface NodeIF
  {
    public Path getPath();

    public boolean isDirectory();
  }

  private abstract class AbstractNode
      implements NodeIF
  {
    private Path m_path;

    protected AbstractNode(Path path)
    {
      m_path = path;
    }

    @Override
    public Path getPath()
    {
      return m_path;
    }
  }

  private class DirNode
    extends AbstractNode
  {
    public DirNode(Path path)
    {
      super(path);
    }

    @Override
    public boolean isDirectory()
    {
      return true;
    }
  }

  private class FileNode
    extends AbstractNode
  {
    public FileNode(Path path)
    {
      super(path);
    }

    @Override
    public boolean isDirectory()
    {
      return false;
    }
  }

  private class Sorted
      implements Comparator<Path>
  {
    @Override
    public int compare(Path path1, Path path2)
    {
      int result;

      result = Boolean.compare(Files.isDirectory(path1), Files.isDirectory(path2));
      if (result != 0)
      {
        return result;
      }

      return String.compare(path1.getFileName(), path2.getFileName());
    }
  }

  private class Scan
  {
    private DirNode scan(Path rootPath) throws Exception
    {
      DirNode rootNode;

      rootNode = new DirNode(rootPath);
      scan(rootNode, rootPath);

      return rootNode;
    }

    private void scan(DirNode parentNode, Path currentPath) throws Exception
    {
      String attributeIds;
      Map<Long, MyPath> pathByInodeMap = new HashMap<>();

      attributeIds = "unix:"
          + Stream.of(UnixAttribute.values()).map(UnixAttribute::getId).collect(Collectors.joining(","));

      Files.list(currentPath).sorted(new Sorted()).forEach(path -> {
        if (Files.isDirectory(currentPath))
        {

        }

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
      });
    }
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
      new FileTree().scan(args.length >= 1 ? args[0] : "/media/kees/CubeSSD/export/hoorn");
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
}
