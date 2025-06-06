package org.kku.jdiskusage.util;

import java.io.File;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.kku.common.util.Log;
import org.kku.common.util.OperatingSystemUtil;
import org.kku.common.util.StopWatch;
import org.kku.jdiskusage.ui.DiskUsageView;

public class FileTree
{
  public enum UnixAttribute
  {
    NUMBER_OF_LINKS("nlink"),
    INODE("ino"),
    DEF("dev"),
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

    public <T> T get(Map<String, T> attributeMap)
    {
      return attributeMap.get(getId());
    }
  }

  public final static String UNIX_ATTRIBUTE_IDS;

  static
  {
    UNIX_ATTRIBUTE_IDS = "unix:"
        + Stream.of(UnixAttribute.values()).map(UnixAttribute::getId).collect(Collectors.joining(","));
  }

  private final List<Path> m_directoryList;
  private ScanListenerIF m_scanListener;

  public FileTree(Path directory)
  {
    this(Arrays.asList(directory));
  }

  public FileTree(List<Path> directoryList)
  {
    m_directoryList = directoryList;
  }

  public void setScanListener(ScanListenerIF scanListener)
  {
    m_scanListener = scanListener;
  }

  public DirNode scan()
  {
    return new ScanPath(m_scanListener).scan(m_directoryList);
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

    public String getFileSubType();

    public boolean isDirectory();

    public long getSize();

    public long getNumberOfFiles();

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
    private DirNode m_parentNode;

    protected AbstractFileNode(Path path)
    {
      this(path.toString());
    }

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
      return m_parentNode;
    }

    @Override
    public void setParent(DirNode parentNode)
    {
      m_parentNode = parentNode;
    }

    @Override
    public String getAbsolutePath()
    {
      return (getParent() != null && getParent().getParent() != null ? (getParent().getAbsolutePath() + File.separator)
          : "") + getName();
    }

    @Override
    public int hashCode()
    {
      return m_pathName.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
      if (!(obj instanceof AbstractFileNode afn))
      {
        return false;
      }

      if (!Objects.equals(m_pathName, afn.m_pathName))
      {
        return false;
      }

      if (!Objects.equals(m_parentNode, afn.m_parentNode))
      {
        return false;
      }

      return true;
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
    private List<FileNodeIF> mi_childList;
    private long mi_fileSize = -1;
    private long mi_numberOfFiles = -1;

    DirNode(boolean root, Path path)
    {
      this(root, path, -1);
    }

    DirNode(boolean root, Path path, int size)
    {
      super(root ? path : path.getFileName());
      if (size > 0)
      {
        initList(size);
      }
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
    public String getFileSubType()
    {
      return "";
    }

    @Override
    public long getSize()
    {
      if (mi_fileSize == -1)
      {
        mi_fileSize = getChildList().stream().map(FileNodeIF::getSize).reduce(0l, Long::sum);
      }

      return mi_fileSize;
    }

    @Override
    public long getNumberOfFiles()
    {
      if (mi_numberOfFiles == -1)
      {
        mi_numberOfFiles = getChildList().stream().map(FileNodeIF::getNumberOfFiles).reduce(0l, Long::sum);
      }

      return mi_numberOfFiles;
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
      getChildList().add(node);
      return node;
    }

    public <T extends AbstractFileNode> boolean removeChild(T dirNode)
    {
      dirNode.setParent(null);
      return getChildList().remove(dirNode);
    }

    public List<FileNodeIF> getChildList()
    {
      if (mi_childList == null)
      {
        mi_childList = new ArrayList<>();
      }
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
    private static Pattern typePattern = Pattern.compile("[a-zA-Z_-]*");
    private static FileNameMap fileNameMap = URLConnection.getFileNameMap();

    private String mi_fileType;
    private String mi_fileSubType;
    private int mi_inodeNumber = -1;
    private int mi_numberOfLinks = -1;
    private long mi_size;
    private long mi_lastModifiedTime;

    FileNode(Path path, BasicFileAttributes basicAttributes)
    {
      super(path.getFileName());

      initFileType(getName());
    }

    public void init(Path path, BasicFileAttributes basicAttributes)
    {
      Map<String, Object> unixAttributes;

      unixAttributes = null;
      if (OperatingSystemUtil.isLinux() || OperatingSystemUtil.isMacOS())
      {
        try
        {
          unixAttributes = Files.readAttributes(path, UNIX_ATTRIBUTE_IDS);
          if (unixAttributes != null)
          {
            mi_inodeNumber = ((Long) UnixAttribute.INODE.get(unixAttributes)).intValue();
            mi_numberOfLinks = ((Integer) UnixAttribute.NUMBER_OF_LINKS.get(unixAttributes)).intValue();
          }
        }
        catch (Exception ex)
        {
          Log.log.error(ex, "Failed to read unix attributes for %s", path);
        }
      }

      mi_size = basicAttributes == null ? -1 : basicAttributes.size();
      mi_lastModifiedTime = basicAttributes == null ? -1 : basicAttributes.lastModifiedTime().toMillis();
    }

    private void initFileType(String name)
    {
      int index;

      String type = fileNameMap.getContentTypeFor(name);
      if (type != null)
      {
        index = type.indexOf('/');
        if (index != -1)
        {
          mi_fileType = type.substring(0, index);
          mi_fileSubType = type.substring(index + 1);
          return;
        }
      }

      index = name.lastIndexOf('/');
      if (index != -1)
      {
        name = name.substring(index + 1);
      }

      index = name.lastIndexOf('\\');
      if (index != -1)
      {
        name = name.substring(index + 1);
      }

      index = name.lastIndexOf(".");
      if (index != -1)
      {
        name = name.substring(index + 1);
        if (name.length() < 10 && typePattern.matcher(name).matches())
        {
          mi_fileType = DiskUsageView.getNoneText();
          mi_fileSubType = name.toLowerCase();
          return;
        }
      }

      mi_fileType = DiskUsageView.getNoneText();
      mi_fileSubType = DiskUsageView.getNoneText();
    }

    @Override
    public String getFileType()
    {
      return mi_fileType;
    }

    @Override
    public String getFileSubType()
    {
      return mi_fileSubType;
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
    public long getNumberOfFiles()
    {
      return 1;
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
      for (FileNodeIF node : currentNode.getChildList())
      {
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
      String pathName = "/";
      //pathName = "/home/kees";
      Path path = Path.of(args.length >= 1 ? args[0] : pathName);
      StopWatch sw = new StopWatch();
      FileTree ft = new FileTree(path);
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
            previousTime = currentTime;
          }
          return false;
        }
      });
      sw.start();
      DirNode dirNode = ft.scan();
      System.out.println("scan took " + sw.getElapsedTime() + " msec.");
      new Print().print(dirNode);

      System.out.println("ready");
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
}
