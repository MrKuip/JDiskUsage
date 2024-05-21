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
import java.util.HashSet;
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
  private List<FilterIF> m_filterList = new ArrayList<>();

  public FileTree(File directory)
  {
    m_directory = directory.toPath();
  }

  public void addFilter(FilterIF filter)
  {
    m_filterList.add(filter);
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

    return dirNode;
  }

  public interface FilterIF
  {
    public boolean accept(FileNodeIF fileNode);
  }

  public interface FileNodeIF
  {
    public String getName();

    public boolean isDirectory();

    public long getSize();

    default public int getNumberOfLinks()
    {
      return 0;
    }

    default public int getInodeNumber()
    {
      return 0;
    }

    public long getLastModifiedTime();

    public Stream<FileNodeIF> streamNode();

    public Stream<FileNodeWithPath> streamNodeWithPath(String parentPath);

    default public boolean isFile()
    {
      return !isDirectory();
    }

    static public Comparator<FileNodeIF> getSizeComparator()
    {
      return Comparator.comparing(FileNodeIF::getSize).reversed();
    }
  }

  /**
   * Remembering the complete path of a FileNode in AbstractFileNode will take a
   * lot of memory. The path is only shown in top50 pane. So this object builds up
   * the path after streaming.
   */
  public static class FileNodeWithPath
      implements FileNodeIF
  {
    private final String m_parentPath;
    private final FileNodeIF m_fileNode;

    public FileNodeWithPath(String parentPath, FileNodeIF fileNode)
    {
      m_parentPath = parentPath;
      m_fileNode = fileNode;
    }

    @Override
    public boolean isFile()
    {
      return m_fileNode.isFile();
    }

    public FileNodeIF getFileNode()
    {
      return m_fileNode;
    }

    public String getParentPath()
    {
      return m_parentPath + ((m_parentPath.isEmpty() || m_fileNode.isFile()) ? "" : "/")
          + (m_fileNode.isFile() ? "" : m_fileNode.getName());
    }

    @Override
    public String getName()
    {
      return getFileNode().getName();
    }

    @Override
    public boolean isDirectory()
    {
      return getFileNode().isDirectory();
    }

    @Override
    public long getSize()
    {
      return getFileNode().getSize();
    }

    @Override
    public long getLastModifiedTime()
    {
      return getFileNode().getLastModifiedTime();
    }

    @Override
    public Stream<FileNodeIF> streamNode()
    {
      return null;
    }

    @Override
    public Stream<FileNodeWithPath> streamNodeWithPath(String parentPath)
    {
      return null;
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
    public String toString()
    {
      return getName();
    }
  }

  public static class DirNode
    extends AbstractFileNode
  {
    private List<FileNodeIF> mi_childList = new ArrayList<>();
    private List<FileNodeIF> mi_filteredChildList = new ArrayList<>();
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
        mi_fileSize = mi_childList.stream().map(FileNodeIF::getSize).reduce(0l, Long::sum);
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

    public boolean hasChildren()
    {
      return !getChildList().isEmpty();
    }

    public <T extends AbstractFileNode> T addChild(T dirNode)
    {
      mi_childList.add(dirNode);
      return dirNode;
    }

    public <T extends AbstractFileNode> boolean removeChild(T dirNode)
    {
      return mi_childList.remove(dirNode);
    }

    public List<FileNodeIF> getChildList()
    {
      return mi_childList;
    }

    @Override
    public Stream<FileNodeIF> streamNode()
    {
      return Stream.concat(Stream.of(this), getChildList().stream().flatMap(FileNodeIF::streamNode));
    }

    @Override
    public Stream<FileNodeWithPath> streamNodeWithPath(String parentPath)
    {
      FileNodeWithPath fileNodeWithPath = new FileNodeWithPath(parentPath, this);
      return Stream.concat(Stream.of(fileNodeWithPath),
          getChildList().stream().map(fn -> new FileNodeWithPath(fileNodeWithPath.getParentPath(), fn))
              .flatMap(fnwp -> fnwp.getFileNode().streamNodeWithPath(fnwp.getParentPath())));
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

    @Override
    public int getNumberOfLinks()
    {
      return mi_numberOfLinks;
    }

    @Override
    public long getSize()
    {
      return mi_size;
    }

    @Override
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

    @Override
    public Stream<FileNodeWithPath> streamNodeWithPath(String parentPath)
    {
      return Stream.of(new FileNodeWithPath(parentPath, this));
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
    private int mi_depth;

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
          Comparator<Path> c;

          c = mi_depth == 0 ? Comparator.comparing(Path::toString) : (a, b) -> 0;
          try (Stream<Path> stream = Files.list(currentPath).sorted(c))
          {
            stream.forEach(path ->
            {
              if (mi_cancel)
              {
                return;
              }

              if (Files.isDirectory(path))
              {
                DirNode newDirNode;

                newDirNode = new DirNode(path);

                mi_numberOfDirectories++;
                try
                {
                  mi_depth++;
                  scan(parentNode.addChild(newDirNode), path);
                }
                finally
                {
                  mi_depth--;
                }
                if (!newDirNode.hasChildren())
                {
                  parentNode.removeChild(newDirNode);
                }
              }
              else
              {
                FileNode newFileNode;

                newFileNode = new FileNode(path);

                mi_numberOfFiles++;
                if (isAccepted(newFileNode))
                {
                  parentNode.addChild(newFileNode);
                }
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

    private boolean isAccepted(FileNode newFileNode)
    {
      if (m_filterList.isEmpty())
      {
        return true;
      }

      if (!m_filterList.stream().allMatch(filter -> filter.accept(newFileNode)))
      {
        return false;
      }

      return true;
    }
  }

  private static class Print
  {
    private int mi_indent = 0;
    private Map<Integer, String> mi_indentMap = new HashMap<>();

    public void print(DirNode dirNode)
    {
      dirNode.getChildList().forEach(node ->
      {
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
      File file = new File(args.length >= 1 ? args[0] : "/home/kees/test");
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
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  public static class UniqueInodeFilter
      implements FilterIF
  {
    private HashSet<Integer> m_evaluatedInodeNumberSet = new HashSet<>();

    @Override
    public boolean accept(FileNodeIF fileNode)
    {
      return m_evaluatedInodeNumberSet.add(fileNode.getInodeNumber());
    }
  }
}
