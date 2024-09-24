package org.kku.jdiskusage.main;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.kku.jdiskusage.util.FileTree;
import org.kku.jdiskusage.util.FileTree.UnixAttribute;
import org.kku.jdiskusage.util.Log;
import org.kku.jdiskusage.util.OperatingSystemUtil;
import org.kku.jdiskusage.util.Performance;
import org.kku.jdiskusage.util.Performance.PerformancePoint;

import javafx.application.Application;
import javafx.stage.Stage;

public class TestFx4
  extends Application
{
  @Override
  public void start(Stage stage) throws Exception
  {
    test1();
    System.exit(1);
  }

  private ThreadLocal<ByteBuffer> m_byteBuffer = ThreadLocal.withInitial(() -> null);
  private ThreadLocal<MessageDigest> m_messageDigest = ThreadLocal.withInitial(() -> {
    try
    {
      return MessageDigest.getInstance("SHA-512");
    }
    catch (Exception e)
    {
      Log.log.error(e, "Exception while creating messageDigest SHA-512");
      return null;
    }
  });

  private void test1() throws Exception
  {
    DuplicateVisitor duplicateVisitor;
    Path path;

    path = Path.of("/media/kees/CubeSSD/backup/scheveningen/snapshot_002_2024_03_17__18_00_01/projecten/matiss/");
    path = Path.of("/home/kees/test");
    path = Path
        .of("/media/kees/CubeSSD/backup/scheveningen/snapshot_002_2024_03_17__18_00_01/projecten/matiss/7_3_canada");
    path = Path.of(
        "/media/kees/CubeSSD/backup/scheveningen/snapshot_002_2024_03_17__18_00_01/projecten/matiss/7_3_canada/branches/genericFilter/");
    path = Path.of("/media/kees/CubeSSD/backup/scheveningen/snapshot_002_2024_03_17__18_00_01/");
    path = Path.of("/media/kees/CubeSSD/backup/scheveningen/snapshot_002_2024_03_17__18_00_01/projecten/matiss/");
    path = Path.of("/home/kees");
    path = Path.of(
        "/media/kees/CubeSSD/backup/scheveningen/snapshot_002_2024_03_17__18_00_01/projecten/matiss/7_3_canada/branches/");
    duplicateVisitor = new DuplicateVisitor(path);
    try (PerformancePoint p = Performance.measure("rdfind: %s", path))
    {
      try (PerformancePoint p1 = Performance.measure("walk tree: %s", path))
      {
        Files.walkFileTree(path, duplicateVisitor);
      }

      try (PerformancePoint p1 = Performance.measure("find duplicates: %s", path);
          BufferedWriter writer = Files.newBufferedWriter(Path.of("/tmp/result.txt")))
      {
        AtomicInteger numberOfDuplicates = new AtomicInteger(0);
        AtomicLong sizeOfDuplicates = new AtomicLong(0);

        duplicateVisitor.getDuplicateList().forEach(duplicate -> {
          if (duplicate.hasDuplicates())
          {
            DuplicateVisitor.MyFile firstOccurence = duplicate.getFirstOccurence();

            runAndLog(() -> {
              writer.write("> " + firstOccurence.getFileKey() + "-" + firstOccurence.getNumberOfLinks() + " : "
                  + firstOccurence.getPath());
              writer.newLine();
            });
            numberOfDuplicates.incrementAndGet();
            duplicate.getDuplicateList().forEach(file -> {
              runAndLog(() -> {
                writer.write("> " + file.getFileKey() + "-" + file.getNumberOfLinks() + " : " + file.getPath());
                writer.newLine();
                numberOfDuplicates.incrementAndGet();
                sizeOfDuplicates.addAndGet(file.getSize());
              });
            });
            runAndLog(() -> { writer.newLine(); });
          }
        });

        System.out
            .println("There were " + duplicateVisitor.getNumberOfCRCCalculations() + " crc calculations executed");
        System.out
            .println("There were " + duplicateVisitor.getNumberOfMD5Calculations() + " md5 calculations executed");
        System.out.println("It seems like you have " + duplicateVisitor.getDuplicateList().size() + " files with "
            + numberOfDuplicates.get() + " duplicates");
        System.out.println("Totally, " + sizeOfDuplicates.get() + " bytes can be reduced.");
      }
    }

    //System.exit(1);
  }

  private void runAndLog(RunnableWithLog runnable)
  {
    try
    {
      runnable.run();
    }
    catch (Exception e)
    {
      Log.log.error(e, "exception");
    }
  }

  private interface RunnableWithLog
  {
    public void run() throws Exception;
  }

  private class DuplicateVisitor
      implements FileVisitor<Path>
  {
    private final Map<Long, List<MyFile>> mi_filesBySizeMap = new HashMap<>();
    private List<Duplicate> mi_duplicateList;
    private MessageDigest mi_messageDigest;
    private final long mi_blockSize;
    private int mi_numberOfDuplicates;
    private long mi_sizeOfDuplicates;
    private int mi_numberOfCRCCalculations;
    private int mi_numberOfDigestCalculations;
    private int mi_numberOfFileChannelOpen;
    private int mi_previousNumberOfFileChannelOpen;

    public DuplicateVisitor(Path dir) throws IOException
    {
      mi_blockSize = Files.getFileStore(dir).getBlockSize();
    }

    public long getBlockSize()
    {
      return mi_blockSize;
    }

    public int getNumberOfMD5Calculations()
    {
      return mi_numberOfDigestCalculations;
    }

    public int getNumberOfCRCCalculations()
    {
      return mi_numberOfCRCCalculations;
    }

    public List<Duplicate> getDuplicateList()
    {
      if (mi_duplicateList == null)
      {
        mi_duplicateList = new ArrayList<>();
        // entries that have a list of 1 do not contain duplicates!

        mi_filesBySizeMap.entrySet().parallelStream().filter(entry -> entry.getValue().size() != 1).forEach(entry -> {

          Map<Integer, Duplicate> duplicateByIndexMap;

          Log.log.finest("evaluate size[%d] %d possible duplicates [#=%d, #size=%d]", entry.getKey(),
              entry.getValue().size(), mi_numberOfDuplicates, mi_sizeOfDuplicates);

          duplicateByIndexMap = new HashMap<>();
          for (int i = 0; i < entry.getValue().size(); i++)
          {
            MyFile file1;

            file1 = entry.getValue().get(i);
            if (file1.getSize() == 0)
            {
              return;
            }

            if (file1.isConsumed())
            {
              continue;
            }

            Log.log.finest("evaluate: %s", file1.getPath());

            for (int j = i; j < entry.getValue().size(); j++)
            {
              MyFile file2;

              if (i == j)
              {
                continue;
              }
              file2 = entry.getValue().get(j);

              if (file2.isConsumed())
              {
                continue;
              }
              Log.log.finest("  check: %s", file2.getPath());

              try (Arena arena = Arena.ofConfined())
              {
                try
                {
                  if (Objects.equals(file1.getFileKey(), file2.getFileKey())
                      || (Arrays.equals(file1.getDigestFirstBlock(arena), file2.getDigestFirstBlock(arena)) && Arrays
                          .equals(file1.getDigestRemainingBlocks(arena), file2.getDigestRemainingBlocks(arena))))
                  {
                    file2.setConsumed(true);
                    mi_numberOfDuplicates++;
                    mi_sizeOfDuplicates += file2.getSize();

                    duplicateByIndexMap.computeIfAbsent(i, key -> new Duplicate(file1)).add(file2);
                  }
                }
                catch (Exception ex)
                {
                  ex.printStackTrace();
                }
              }
            }
          }

          if (!duplicateByIndexMap.isEmpty())
          {
            synchronized (mi_duplicateList)
            {
              mi_duplicateList.addAll(duplicateByIndexMap.values().stream().toList());
            }
          }
        });
      }

      return mi_duplicateList;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException
    {
      if (!Files.isReadable(dir))
      {
        return FileVisitResult.SKIP_SUBTREE;
      }

      return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
    {
      if (attrs.isRegularFile() && Files.isReadable(file))
      {
        mi_filesBySizeMap.computeIfAbsent(attrs.size(), (size) -> new ArrayList<>()).add(new MyFile(file, attrs));
      }

      return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException
    {
      return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException
    {
      return FileVisitResult.CONTINUE;
    }

    private class MyFile
    {
      private Path mii_path;
      private long mii_size;
      private Object mii_fileKey;
      private boolean mii_consumed;
      private byte[] mii_digestOfFirstBlock;
      private byte[] mii_digestOfRemainingBlocks;
      private int mii_numberOfLinks;

      public MyFile(Path path, BasicFileAttributes attrs)
      {
        mii_path = path;
        mii_size = attrs.size();
        mii_fileKey = attrs.fileKey();

        if (OperatingSystemUtil.isLinux() || OperatingSystemUtil.isMacOS())
        {
          try
          {
            Map<String, Object> unixAttributes;

            unixAttributes = Files.readAttributes(path, FileTree.UNIX_ATTRIBUTE_IDS);
            if (unixAttributes != null)
            {
              mii_numberOfLinks = ((Integer) UnixAttribute.NUMBER_OF_LINKS.get(unixAttributes)).intValue();
            }
          }
          catch (Exception ex)
          {
            Log.log.error(ex, "Failed to read unix attributes for %s", path);
          }
        }
      }

      public int getNumberOfLinks()
      {
        return mii_numberOfLinks;
      }

      public byte[] getDigestFirstBlock(Arena arena)
      {
        if (mii_digestOfFirstBlock == null)
        {
          long size;

          size = getBlockSize();
          if (size > mii_size)
          {
            size = mii_size;
          }
          mii_digestOfFirstBlock = calculateDigest(arena, 0, size);
          if (size <= getBlockSize())
          {
            mii_digestOfRemainingBlocks = mii_digestOfFirstBlock;
          }
        }

        return mii_digestOfFirstBlock;
      }

      public byte[] getDigestRemainingBlocks(Arena arena)
      {
        if (mii_digestOfRemainingBlocks == null)
        {
          mii_digestOfRemainingBlocks = calculateDigest(arena, getBlockSize(), mii_size - getBlockSize());
        }

        return mii_digestOfRemainingBlocks;
      }

      public byte[] calculateDigest(Arena arena, long offset, long size)
      {
        if (mii_size != 0)
        {
          mi_numberOfDigestCalculations++;

          try (FileChannel channel = FileChannel.open(mii_path, StandardOpenOption.READ))
          {
            ByteBuffer bb;
            MessageDigest md;
            MemorySegment ms;

            ms = channel.map(FileChannel.MapMode.READ_ONLY, offset, size, arena);
            bb = ms.asByteBuffer();

            md = m_messageDigest.get();
            md.reset();
            md.update(bb);
            return md.digest();
          }
          catch (Exception e)
          {
            e.printStackTrace();
          }
        }

        return new byte[]
        {};
      }

      public void setConsumed(boolean consumed)
      {
        mii_consumed = consumed;
      }

      public boolean isConsumed()
      {
        return mii_consumed;
      }

      public long getSize()
      {
        return mii_size;
      }

      public Path getPath()
      {
        return mii_path;
      }

      public Object getFileKey()
      {
        return mii_fileKey;
      }
    }

    private class Duplicate
    {
      private MyFile mii_firstOccurence;
      private List<MyFile> mii_duplicateList = new ArrayList<>();

      Duplicate(MyFile file)
      {
        mii_firstOccurence = file;
      }

      public MyFile getFirstOccurence()
      {
        return mii_firstOccurence;
      }

      public List<MyFile> getDuplicateList()
      {
        return mii_duplicateList;
      }

      public boolean hasDuplicates()
      {
        return !mii_duplicateList.isEmpty();
      }

      private void add(MyFile file)
      {
        mii_duplicateList.add(file);
      }
    }

  }

  ByteBuffer getByteBuffer(int size)
  {
    ByteBuffer bb;

    bb = m_byteBuffer.get();
    if (bb == null || bb.capacity() < size)
    {
      int s = ((int) Math.ceil(size / 4096.0)) * 4096;
      bb = s > 4096 ? ByteBuffer.allocateDirect(s) : ByteBuffer.allocate(s);
      m_byteBuffer.set(bb);
    }
    else
    {
      bb.clear();
    }

    return bb;
  }

  public static void main(String[] args)
  {
    launch();
  }
}