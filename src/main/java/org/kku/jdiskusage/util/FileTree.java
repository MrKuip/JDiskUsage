package org.kku.jdiskusage.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.kku.jdiskusage.ui.DiskUsageView;

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

    <T> T get(Map<String, T> attributeMap)
    {
      return attributeMap.get(getId());
    }
  }

  private static String ATTRIBUTE_IDS;

  {
    ATTRIBUTE_IDS = "unix:"
        + Stream.of(UnixAttribute.values()).map(UnixAttribute::getId).collect(Collectors.joining(","));
  }

  private final Path m_directory;
  private final List<Path> m_directoryList;
  private ScanListenerIF m_scanListener;

  public FileTree(Path directory)
  {
    this(directory, Arrays.asList(directory));
  }

  public FileTree(Path directory, List<Path> directoryList)
  {
    m_directory = directory;
    m_directoryList = directoryList;
  }

  public void setScanListener(ScanListenerIF scanListener)
  {
    m_scanListener = scanListener;
  }

  public DirNode scan()
  {
    return new ScanPath().scan(m_directory, m_directoryList);
  }

  public interface FilterIF
  {
    public boolean accept(FileNodeIF fileNode);
  }

  public interface FileNodeIF
  {
    public FileNodeIF getParent();

    public void setParent(DirNode dirNode);

    public String getName();

    public String getFileType();

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

    default public boolean isFile()
    {
      return !isDirectory();
    }

    static public Comparator<FileNodeIF> getSizeComparator()
    {
      return Comparator.comparing(FileNodeIF::getSize).reversed();
    }

    public String getAbsolutePath();
  }

  public static abstract class AbstractFileNode
      implements FileNodeIF
  {
    private final String m_pathName;
    private DirNode mi_parentNode;

    protected AbstractFileNode(String pathName)
    {
      m_pathName = pathName.intern();
    }

    @Override
    public String getName()
    {
      return m_pathName;
    }

    @Override
    public DirNode getParent()
    {
      return mi_parentNode;
    }

    @Override
    public void setParent(DirNode parentNode)
    {
      mi_parentNode = parentNode;
    }

    @Override
    public String getAbsolutePath()
    {
      return (getParent() != null ? (getParent().getAbsolutePath() + File.separator) : "") + getName();
    }

    @Override
    public String toString()
    {
      return getName() + " size=" + getSize();
    }
  }

  public static class DirNode
    extends AbstractFileNode
  {
    private List<FileNodeIF> mi_childList;
    private long mi_fileSize = -1;

    private DirNode(boolean root, Path path, int size)
    {
      super(root ? path.toString() : path.getFileName().toString());
      if (size > 0)
      {
        initList(size);
      }
    }

    private DirNode(Path path)
    {
      this(false, path, -1);
    }

    public DirNode(DirNode node)
    {
      super(node.getName());
      initList(node.getChildList().size());
    }

    public void initList(int size)
    {
      if (mi_childList == null)
      {
        mi_childList = new ArrayList<>(size);
      }
    }

    @Override
    public String getFileType()
    {
      return "";
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

    public <T extends AbstractFileNode> T addChild(T node)
    {
      node.setParent(this);
      mi_childList.add(node);
      return node;
    }

    public <T extends AbstractFileNode> boolean removeChild(T dirNode)
    {
      dirNode.setParent(null);
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

    public FileNodeIF filter(FilterIF filter)
    {
      return new FilterNodes(filter).filter(this);
    }
  }

  public static class FileNode
    extends AbstractFileNode
  {
    private final String mi_fileType;
    private final int mi_inodeNumber;
    private final int mi_numberOfLinks;
    private final long mi_size;
    private final long mi_lastModifiedTime;

    private FileNode(Path path)
    {
      super(path.getFileName().toString());

      mi_fileType = determineFileType(getName());

      Map<String, Object> unixAttributes;
      BasicFileAttributes basicAttributes;

      unixAttributes = null;
      basicAttributes = null;

      try
      {
        unixAttributes = Files.readAttributes(path, ATTRIBUTE_IDS);
      }
      catch (Exception e)
      {
      }

      mi_inodeNumber = unixAttributes == null ? -1 : ((Long) UnixAttribute.INODE.get(unixAttributes)).intValue();
      mi_numberOfLinks = unixAttributes == null ? -1
          : ((Integer) UnixAttribute.NUMBER_OF_LINKS.get(unixAttributes)).intValue();

      try
      {
        basicAttributes = Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
      }
      catch (Exception e)
      {
      }

      mi_size = basicAttributes == null ? -1 : basicAttributes.size();
      mi_lastModifiedTime = basicAttributes == null ? -1 : basicAttributes.lastModifiedTime().toMillis();
    }

    private String determineFileType(String name)
    {
      int index;

      index = name.lastIndexOf('/');
      if (index != -1)
      {
        name.substring(index + 1);
      }

      index = name.lastIndexOf('\\');
      if (index != -1)
      {
        name.substring(index + 1);
      }

      index = name.lastIndexOf(".");
      if (index > 0 && index != -1)
      {
        String type;

        type = name.substring(index + 1);
        if (StringUtils.isAllLetters(type))
        {
          return type.toLowerCase();
        }
      }

      return DiskUsageView.getNoneText();
    }

    @Override
    public String getFileType()
    {
      return mi_fileType;
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
  }

  private class ScanPath
  {
    private int mi_numberOfFiles;
    private int mi_numberOfDirectories;
    private boolean mi_cancel;
    private int mi_depth;

    public DirNode scan(Path directory, List<Path> directoryList)
    {
      DirNode rootNode;

      rootNode = new DirNode(true, directory, directoryList.size());
      directoryList.forEach(path -> {
        DirNode node;

        if (directoryList.size() > 1)
        {
          node = new DirNode(false, path, directoryList.size());
          rootNode.addChild(node);
        }
        else
        {
          node = rootNode;
        }

        scan(node, path);
      });
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
          try (Stream<Path> stream = Files.list(currentPath))
          {
            stream.sorted(c).forEach(path -> {
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
                parentNode.addChild(newFileNode);
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

  private static class FilterNodes
  {
    private final FilterIF mi_filter;

    public FilterNodes(FilterIF filter)
    {
      mi_filter = filter;
    }

    public DirNode filter(DirNode node)
    {
      DirNode rootNode;

      rootNode = new DirNode(node);
      filter(rootNode, node);

      return rootNode;
    }

    private void filter(DirNode parentNode, DirNode currentNode)
    {
      try (Stream<FileNodeIF> stream = currentNode.getChildList().stream())
      {
        stream.forEach(node -> {
          if (node.isDirectory())
          {
            DirNode newDirNode;

            newDirNode = new DirNode((DirNode) node);

            filter(parentNode.addChild(newDirNode), (DirNode) node);
            if (!newDirNode.hasChildren())
            {
              parentNode.removeChild(newDirNode);
            }
          }
          else
          {
            if (mi_filter.accept(node))
            {
              parentNode.addChild((FileNode) node);
            }
          }
        });
      }
    }
  }

  private static class Print
  {
    private int mi_indent = 0;
    private Map<Integer, String> mi_indentMap = new HashMap<>();

    public void print(DirNode dirNode)
    {
      dirNode.getChildList().forEach(node -> {
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
      FileTree ft = new FileTree(file.toPath());
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
}
