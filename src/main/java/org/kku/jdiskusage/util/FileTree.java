package org.kku.jdiskusage.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.kku.util.CommonUtil;

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

  private static String ATTRIBUTE_IDS;

  {
    ATTRIBUTE_IDS = "unix:"
        + Stream.of(UnixAttribute.values()).map(UnixAttribute::getId).collect(Collectors.joining(","));
  }

  private final Path m_directory;
  private ScanListenerIF m_scanListener;

  public FileTree(File directory)
  {
    m_directory = directory.toPath();
  }

  public void setScanListener(ScanListenerIF scanListener)
  {
    m_scanListener = scanListener;
  }

  public DirNode scan()
  {
    DirNode dirNode;
    Path path;

    path = m_directory;
    dirNode = new Scan().scan(path);

    // new Print().print(dirNode);

    return dirNode;
  }

  public interface NodeIF
  {
    public String getPathName();

    public boolean isDirectory();
  }

  private static abstract class AbstractNode
      implements NodeIF
  {
    private final String m_pathName;

    protected AbstractNode(String pathName)
    {
      m_pathName = pathName;
    }

    @Override
    public String getPathName()
    {
      return m_pathName;
    }

    @Override
    public String toString()
    {
      return getPathName();
    }
  }

  public static class DirNode
    extends AbstractNode
  {
    private List<NodeIF> mi_nodeList = new ArrayList<>();

    private DirNode(boolean root, Path path)
    {
      super(root ? path.toString() : path.getFileName().toString());
    }

    private DirNode(Path path)
    {
      this(false, path);
    }

    @Override
    public boolean isDirectory()
    {
      return true;
    }

    public <T extends AbstractNode> T addNode(T dirNode)
    {
      mi_nodeList.add(dirNode);
      return dirNode;
    }

    public List<NodeIF> getNodeList()
    {
      return mi_nodeList;
    }
  }

  public static class FileNode
    extends AbstractNode
  {
    private final int mi_inodeNumber;
    private final int mi_numberOfLinks;
    private final int mi_fileSize;

    private FileNode(Path path)
    {
      super(path.getFileName().toString());

      Map<String, Object> attributes;
      try
      {
        attributes = Files.readAttributes(path, ATTRIBUTE_IDS);
      }
      catch (IOException e)
      {
        mi_numberOfLinks = -1;
        mi_fileSize = -1;
        mi_inodeNumber = -1;
        return;
      }

      mi_numberOfLinks = UnixAttribute.NUMBER_OF_LINKS.get(attributes);
      mi_fileSize = ((Long) UnixAttribute.FILE_SIZE.get(attributes)).intValue();
      mi_inodeNumber = ((Long) UnixAttribute.INODE.get(attributes)).intValue();
    }

    @Override
    public boolean isDirectory()
    {
      return false;
    }

    public int getNumberOfLinks()
    {
      return mi_numberOfLinks;
    }

    public int getFileSize()
    {
      return mi_fileSize;
    }

    public int getInodeNumber()
    {
      return mi_inodeNumber;
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
    private int mi_numberOfFiles;
    private int mi_numberOfDirectories;
    private boolean mi_cancel;

    public DirNode scan(Path rootPath)
    {
      DirNode rootNode;

      rootNode = new DirNode(true, rootPath);
      scan(rootNode, rootPath);
      if (m_scanListener != null)
      {
        mi_cancel = m_scanListener.progress(null, mi_numberOfDirectories, mi_numberOfFiles, true);
      }

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
        Files.list(currentPath).forEach(path ->
        {
          if (mi_cancel)
          {
            return;
          }

          if (Files.isDirectory(path))
          {
            mi_numberOfDirectories++;
            scan(parentNode.addNode(new DirNode(path.getFileName())), path);
          }
          else
          {
            mi_numberOfFiles++;
            parentNode.addNode(new FileNode(path.getFileName()));
            if (m_scanListener != null)
            {
              mi_cancel = m_scanListener.progress(path, mi_numberOfDirectories, mi_numberOfFiles, false);
            }
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
      dirNode.getNodeList().forEach(node ->
      {
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
      File file = new File(args.length >= 1 ? args[0]
          : "/media/kees/CubeBackup/backup/hoorn/snapshot_001_2024_03_18__18_00_01/projecten");
      FileTree ft = new FileTree(file);
      ft.setScanListener(new ScanListenerIF()
      {
        long previousTime;

        @Override
        public boolean progress(Path currentFile, int numberOfDirectoriesEvaluated, int numberOfFilesEvaluated,
            boolean scanReady)
        {
          Long currentTime = System.currentTimeMillis();
          if (currentTime - 1000 > previousTime)
          {
            System.out.println("DIR:" + numberOfDirectoriesEvaluated + " FILES:" + numberOfFilesEvaluated);
            previousTime = currentTime;
          }
          return false;
        }
      });
      DirNode dirNode = ft.scan();

      System.out.println("ready");

      for (;;)
      {
        System.gc();
        System.out.println("total:" + Runtime.getRuntime().totalMemory() + ", free:" + Runtime.getRuntime().freeMemory()
            + ", Used:" + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
        CommonUtil.sleep(1000);
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
}
