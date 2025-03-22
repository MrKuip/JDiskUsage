package org.kku.jdiskusage.util;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.kku.common.util.OperatingSystemUtil;
import org.kku.common.util.OperatingSystemUtil.OperatingSystem;
import org.kku.jdiskusage.util.FileTree.DirNode;
import org.kku.jdiskusage.util.FileTree.FileNode;

class ScanPath
{
  private int mi_numberOfFiles;
  private int mi_numberOfDirectories;
  private ScanListenerIF mi_scanListener;

  public ScanPath(ScanListenerIF scanListener)
  {
    mi_scanListener = scanListener;
  }

  public DirNode scan(List<Path> directoryList)
  {
    DirNode rootNode;

    if (directoryList.size() > 1)
    {
      Path rootDir;

      rootDir = directoryList.get(0).getParent();
      rootNode = new DirNode(true, rootDir, directoryList.size());
    }
    else
    {
      rootNode = null;
    }

    for (int i = 0; i < directoryList.size(); i++)
    {
      Path path;

      path = directoryList.get(i);
      try
      {
        ScanVisitor visitor = new ScanVisitor(path, rootNode);
        Files.walkFileTree(path, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE, visitor);
        visitor.waitForExecutors();
        if (!visitor.isCancelled())
        {
          rootNode = visitor.getRootDirNode();
        }
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }
    }

    if (mi_scanListener != null)
    {
      mi_scanListener.progress(null, mi_numberOfDirectories, mi_numberOfFiles, true);
    }

    return rootNode;
  }

  private class ScanVisitor
    extends SimpleFileVisitor<Path>
  {
    private Stack<DirNode> mi_parentNodeStack = new Stack<>();
    private DirNode mi_rootDirNode;
    private boolean mi_cancelled;
    private ExecutorService executor = Executors.newFixedThreadPool(1);
    private String mi_fileStoreName;
    private OperatingSystem mi_operatingSystem;

    private ScanVisitor(Path path, DirNode rootDirNode)
    {
      mi_rootDirNode = rootDirNode;
      mi_parentNodeStack.push(rootDirNode);
      mi_operatingSystem = OperatingSystemUtil.getOperatingSystem();
      mi_fileStoreName = getFileStoreName(path);
    }

    public void waitForExecutors()
    {
      executor.shutdown();
    }

    public boolean isCancelled()
    {
      return mi_cancelled;
    }

    public DirNode getRootDirNode()
    {
      return mi_rootDirNode;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException
    {
      DirNode node;
      DirNode parentNode;

      if (mi_cancelled)
      {
        return FileVisitResult.SKIP_SUBTREE;
      }

      if (!Objects.equals(mi_fileStoreName, getFileStoreName(attrs)))
      {
        return FileVisitResult.SKIP_SUBTREE;
      }

      node = new DirNode(mi_rootDirNode == null, dir);
      if (mi_rootDirNode == null)
      {
        mi_rootDirNode = node;
      }
      parentNode = mi_parentNodeStack.peek();
      if (parentNode != null)
      {
        parentNode.addChild(node);
      }
      mi_parentNodeStack.push(node);
      mi_numberOfDirectories++;

      return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc)
    {
      mi_parentNodeStack.pop();
      return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
    {
      FileNode node;

      if (attrs.isRegularFile())
      {
        node = new FileNode(file, attrs);
        // Speed up the scan and init the filenode in another thread
        executor.submit(() -> node.init(file, attrs));
        mi_parentNodeStack.peek().addChild(node);
        mi_numberOfFiles++;
      }

      if (mi_scanListener != null)
      {
        mi_cancelled = mi_scanListener.progress(file, mi_numberOfDirectories, mi_numberOfFiles, false);
      }

      return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException io)
    {
      return FileVisitResult.SKIP_SUBTREE;
    }

    private String getFileStoreName(Path path)
    {
      try
      {
        return mi_operatingSystem.getFileStoreId(Files.readAttributes(path, BasicFileAttributes.class));
      }
      catch (IOException e)
      {
        return "";
      }
    }

    private String getFileStoreName(BasicFileAttributes attrs)
    {
      return mi_operatingSystem.getFileStoreId(attrs);
    }
  }
}