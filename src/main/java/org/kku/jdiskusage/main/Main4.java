package org.kku.jdiskusage.main;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import org.kku.common.util.StopWatch;

public class Main4
{
  public static void main(String[] args) throws Throwable
  {
    Path root = Path.of("/usr/local/kees2");
    MyFileVisitor visitor = new MyFileVisitor();
    StopWatch sw = new StopWatch();
    sw.start();
    Files.walkFileTree(root, visitor);
    System.out.println("number of files:" + visitor.getFileCount() + " in " + sw.getElapsedTime() + " msec.");
  }

  static class MyFileVisitor
    extends SimpleFileVisitor<Path>
  {
    private int mi_fileCount = 0;

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
    {
      mi_fileCount++;
      if (mi_fileCount % 100000 == 0) System.out.print(".");
      return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
    {
      mi_fileCount++;
      if (mi_fileCount % 100000 == 0) System.out.print(".");
      return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException io)
    {
      return FileVisitResult.SKIP_SUBTREE;
    }

    public int getFileCount()
    {
      return mi_fileCount;
    }
  }
}