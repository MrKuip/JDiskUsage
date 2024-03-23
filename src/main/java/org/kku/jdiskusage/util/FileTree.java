package org.kku.jdiskusage.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
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

  public interface FileNodeIF
  {
    public String getName();

    public boolean isDirectory();

    public long getSize();

    public long getLastModifiedTime();

    public Stream<FileNodeIF> streamNode();

    public Stream<FileNodeWithPath> streamNodeWithPath();

    default public boolean isFile()
    {
      return !isDirectory();
    }

    static public Comparator<FileNodeIF> getSizeComparator()
    {
      return Comparator.comparing(FileNodeIF::getSize).reversed();
    }
  }

  public class FileNodeWithPath
  {
    private final String m_parentPath;
    private final FileNodeIF m_node;

    public FileNodeWithPath(String parentPath, FileNodeIF node)
    {
      m_parentPath = parentPath;
      m_node = node;
    }
  }

  public static abstract class AbstractFileNode
      implements FileNodeIF
  {
    private final String m_pathName;

    protected AbstractFileNode(String pathName)
    {
      m_pathName = pathName;
    }

    @Override
    public String getName()
    {
      return m_pathName.intern();
    }

    @Override
    public Stream<FileNodeWithPath> streamNodeWithPath()
    {
      return null;
    }

    @Override
    public String toString()
    {
      return getName();
    }
  }

  public static class DirNode
    extends AbstractFileNode
  {
    private List<FileNodeIF> mi_nodeList = new ArrayList<>();
    private long mi_fileSize = -1;

    private DirNode(boolean root, Path path)
    {
      super(root ? path.toString() : path.getFileName().toString());
    }

    private DirNode(Path path)
    {
      this(false, path);
    }

    @Override
    public long getSize()
    {
      if (mi_fileSize == -1)
      {
        mi_fileSize = mi_nodeList.stream().map(FileNodeIF::getSize).reduce(0l, Long::sum);
      }

      return mi_fileSize;
    }

    @Override
    public long getLastModifiedTime()
    {
      return -1;
    }

    @Override
    public boolean isDirectory()
    {
      return true;
    }

    public <T extends AbstractFileNode> T addNode(T dirNode)
    {
      mi_nodeList.add(dirNode);
      return dirNode;
    }

    public List<FileNodeIF> getNodeList()
    {
      return mi_nodeList;
    }

    @Override
    public Stream<FileNodeIF> streamNode()
    {
      return Stream.concat(Stream.of(this), getNodeList().stream().flatMap(FileNodeIF::streamNode));
    }
  }

  public static class FileNode
    extends AbstractFileNode
  {
    private final int mi_inodeNumber;
    private final int mi_numberOfLinks;
    private final long mi_size;
    private final long mi_lastModifiedTime;

    private FileNode(Path path)
    {
      super(path.getFileName().toString());

      Map<String, Object> unixAttributes;
      BasicFileAttributes basicAttributes;

      unixAttributes = null;
      basicAttributes = null;

      try
      {
        unixAttributes = Files.readAttributes(path, ATTRIBUTE_IDS);
      }
      catch (IOException e)
      {
      }

      mi_inodeNumber = unixAttributes == null ? -1 : ((Long) UnixAttribute.INODE.get(unixAttributes)).intValue();
      mi_numberOfLinks = unixAttributes == null ? -1
          : ((Integer) UnixAttribute.NUMBER_OF_LINKS.get(unixAttributes)).intValue();

      try
      {
        basicAttributes = Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
      }
      catch (IOException e)
      {
      }

      mi_size = basicAttributes == null ? -1 : basicAttributes.size();
      mi_lastModifiedTime = basicAttributes == null ? -1 : basicAttributes.lastModifiedTime().toMillis();
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

    @Override
    public long getSize()
    {
      return mi_size;
    }

    public int getInodeNumber()
    {
      return mi_inodeNumber;
    }

    @Override
    public long getLastModifiedTime()
    {
      return mi_lastModifiedTime;
    }

    @Override
    public Stream<FileNodeIF> streamNode()
    {
      return Stream.of(this);
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
      try
      {
        if (currentPath.toFile().canRead())
        {
          try (Stream<Path> stream = Files.list(currentPath))
          {
            stream.forEach(path -> {
              if (mi_cancel)
              {
                return;
              }

              if (Files.isDirectory(path))
              {
                mi_numberOfDirectories++;
                scan(parentNode.addNode(new DirNode(path)), path);
              }
              else
              {
                mi_numberOfFiles++;
                parentNode.addNode(new FileNode(path));
                if (m_scanListener != null)
                {
                  mi_cancel = m_scanListener.progress(path, mi_numberOfDirectories, mi_numberOfFiles, false);
                }
              }
            });
          }
        }
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }
    }
  }

  private static class Print
  {
    private int mi_indent = 0;
    private Map<Integer, String> mi_indentMap = new HashMap<>();

    public void print(DirNode dirNode)
    {
      dirNode.getNodeList().forEach(node -> {
        System.out.printf("%-10d%s%s%n", node.getSize(), getIndent(), node);
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

  static public void main(String[] args)
  {
    try
    {
      File file = new File(args.length >= 1 ? args[0] : "/home/kees");
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
      new Print().print(dirNode);

      System.out.println("ready");

      /*
       * for (;;) { System.gc(); System.out.println("total:" +
       * Runtime.getRuntime().totalMemory() + ", free:" +
       * Runtime.getRuntime().freeMemory() + ", Used:" +
       * (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
       * CommonUtil.sleep(1000); }
       */
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
}
