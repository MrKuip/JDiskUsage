package org.kku.jdiskusage.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

  public void scan(String pathName)
  {
    DirNode dirNode;
    Path path;

    path = Paths.get(pathName);
    dirNode = new Scan().scan(path);

    new Print().print(dirNode);
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

    @Override
    public String toString()
    {
      return getPath().getFileName().toString();
    }
  }

  private class DirNode
    extends AbstractNode
  {
    private List<NodeIF> mi_childrenList = new ArrayList<>();

    public DirNode(Path path)
    {
      super(path);
    }

    @Override
    public boolean isDirectory()
    {
      return true;
    }

    public <T extends AbstractNode> T addChild(T dirNode)
    {
      mi_childrenList.add(dirNode);
      return dirNode;
    }

    public List<NodeIF> getChildren()
    {
      return mi_childrenList;
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

      return 0;
    }
  }

  private class Scan
  {
    public DirNode scan(Path rootPath)
    {
      DirNode rootNode;

      rootNode = new DirNode(rootPath);
      scan(rootNode, rootPath);

      return rootNode;
    }

    private void scan(DirNode parentNode, Path currentPath)
    {
      // String attributeIds;
      // Map<Long, MyPath> pathByInodeMap = new HashMap<>();

      // attributeIds = "unix:"
      // +
      // Stream.of(UnixAttribute.values()).map(UnixAttribute::getId).collect(Collectors.joining(","));

      try
      {
        Files.list(currentPath).forEach(path -> {
          if (Files.isDirectory(path))
          {
            scan(parentNode.addChild(new DirNode(path)), path);
          }
          else
          {
            parentNode.addChild(new FileNode(path));
          }
        });
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }

      /*
       * Files.walk(path).filter(Files::isRegularFile).forEach(file -> { try {
       * Map<String, Object> attributes; Long inode;
       * 
       * attributes = Files.readAttributes(file, attributeIds); inode = (Long)
       * UnixAttribute.INODE.get(attributes);
       * 
       * MyPath v = pathByInodeMap.computeIfAbsent(inode, key -> new MyPath(file,
       * attributes)); } catch (Exception e) { e.printStackTrace(); } }); });
       */
    }
  }

  private class Print
  {
    private int mi_indent = 0;
    private Map<Integer, String> mi_indentMap = new HashMap<>();

    public void print(DirNode dirNode)
    {
      dirNode.getChildren().forEach(node -> {
        System.out.println(getIndent() + node);
        if (node.isDirectory())
        {
          mi_indent++;
          print((DirNode) node);
          mi_indent--;
        }
      });
    }

    public String getIndent()
    {
      return mi_indentMap.computeIfAbsent(mi_indent, indent -> String.join("", Collections.nCopies(indent, "  ")));
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
      new FileTree().scan(args.length >= 1 ? args[0] : "/usr/local/kees/projecten/own/jdiskusage/jdiskusage");
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
}
