package org.kku.jdiskusage.main;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import org.kku.jdiskusage.ui.DiskUsageView;
import org.kku.jdiskusage.ui.SizeDistributionFormPane.SizeDistributionBucket;
import org.kku.jdiskusage.ui.common.FileNodeIterator;
import org.kku.jdiskusage.util.FileTree;
import org.kku.jdiskusage.util.FileTree.DirNode;
import org.kku.jdiskusage.util.FileTree.FileNodeIF;
import org.kku.jdiskusage.util.StopWatch;

public class Test5
{

  private void doIT2()
  {
    String fileName;
    FileTree tree;
    DirNode dirNode;
    StopWatch sw;
    StopWatch sw2;
    StopWatch sw3;
    StopWatch sw4;
    record MyData(Long numberOfFiles, Long sizeOfFiles) {
    }
    Map<SizeDistributionBucket, MyData> map;
    final Map<SizeDistributionBucket, MyData2> map2;

    fileName = "/home/kees";
    fileName = "/media/kees/CubeSSD/export/hoorn/snapshot_001_2024_03_13__11_53_56";
    tree = new FileTree(Path.of(fileName));
    dirNode = tree.scan();
    sw = new StopWatch();
    sw2 = new StopWatch();
    sw3 = new StopWatch();
    sw4 = new StopWatch();

    map2 = new HashMap<>();
    for (int i = 0; i < 5; i++)
    {
      System.out.println("---- Stream ----- ");
      sw.start();
      map = dirNode.streamNode().filter(FileNodeIF::isFile).map(FileNodeIF::getSize)
          .collect(Collectors.groupingBy(SizeDistributionBucket::findBucket,
              Collectors.teeing(Collectors.counting(), Collectors.summingLong(a -> a / 1000000),
                  (numberOfFiles, sizeOfFiles) -> new MyData(numberOfFiles, sizeOfFiles))));
      if (i == 0) map.entrySet().forEach(e -> {
        System.out.printf("%-10s = %8d %8d%n", e.getKey(), e.getValue().numberOfFiles, e.getValue().sizeOfFiles);
      });
      sw.stop();

      System.out.println("---- Iterate ----- ");
      map2.clear();
      sw2.start();
      new FileNodeIterator(dirNode).forEach(fn -> {
        if (fn.isFile())
        {
          SizeDistributionBucket bucket = SizeDistributionBucket.findBucket(fn.getSize());
          MyData2 data = map2.computeIfAbsent(bucket, (a) -> new MyData2());
          data.numberOfFiles = data.numberOfFiles + 1;
          data.sizeOfFiles = data.sizeOfFiles + (fn.getSize() / 1000000);
        }
        return true;
      });
      if (i == 0) map2.entrySet().forEach(e -> {
        System.out.printf("%-10s = %8d %8d%n", e.getKey(), e.getValue().numberOfFiles, e.getValue().sizeOfFiles);
      });
      sw2.stop();

      System.out.println("---- Iterate ----- ");
      map2.clear();
      sw3.start();
      dirNode.streamNode().filter(FileNodeIF::isFile).forEach(fn -> {
        SizeDistributionBucket bucket = SizeDistributionBucket.findBucket(fn.getSize());
        MyData2 data = map2.computeIfAbsent(bucket, (a) -> new MyData2());
        data.numberOfFiles = data.numberOfFiles + 1;
        data.sizeOfFiles = data.sizeOfFiles + (fn.getSize() / 1000000);
      });
      if (i == 0) map2.entrySet().forEach(e -> {
        System.out.printf("%-10s = %8d %8d%n", e.getKey(), e.getValue().numberOfFiles, e.getValue().sizeOfFiles);
      });
      sw3.stop();

      System.out.println("stream took " + sw.getElapsedTime() + " msec");
      System.out.println("iterate took " + sw2.getElapsedTime() + " msec");
      System.out.println("iterate 2 took " + sw3.getElapsedTime() + " msec");
    }
  }

  static class MyData2
  {
    private long numberOfFiles;
    private long sizeOfFiles;
  }

  private void doIt()
  {
    String fileName;
    FileTree tree;
    DirNode dirNode;
    StopWatch sw;
    StopWatch sw2;
    StopWatch sw3;
    StopWatch sw4;
    int top;

    fileName = "/home/kees";
    fileName = "/media/kees/CubeSSD/export/hoorn/snapshot_001_2024_03_13__11_53_56";
    tree = new FileTree(Path.of(fileName));
    dirNode = tree.scan();
    sw = new StopWatch();
    sw2 = new StopWatch();
    sw3 = new StopWatch();
    sw4 = new StopWatch();
    top = 50;

    for (int i = 0; i < 5; i++)
    {
      MaxCollector<FileNodeIF> maxCollector;
      MaxCollector<FileNodeIF> maxCollector2;

      System.out.println("---- Stream ----- ");
      sw.start();
      dirNode.streamNode().filter(FileNodeIF::isFile).sorted(FileNodeIF.getSizeComparator()).limit(top)
          .collect(Collectors.toList()).forEach(fn -> System.out.printf("%-8d %s%n", fn.getSize(), fn.getName()));
      sw.stop();

      System.out.println();
      System.out.println("---- Iterate ----- ");
      sw2.start();
      maxCollector = new MaxCollector<>(FileNodeIF.getSizeComparator(), top);
      new FileNodeIterator(dirNode).forEach(fn -> {
        if (fn.isFile())
        {
          maxCollector.evaluate(fn);
        }
        return true;
      });
      maxCollector.getList().forEach(fn -> System.out.printf("%-8d %s%n", fn.getSize(), fn.getName()));
      sw2.stop();

      System.out.println("---- Iterate 2 --- ");
      sw3.start();
      maxCollector2 = new MaxCollector<>(FileNodeIF.getSizeComparator(), top);
      dirNode.streamNode().filter(FileNodeIF::isFile).forEach(maxCollector2::evaluate);
      maxCollector2.getList().forEach(fn -> System.out.printf("%-8d %s%n", fn.getSize(), fn.getName()));
      sw3.stop();

      System.out.println("---- Iterate 3 --- ");
      sw4.start();
      dirNode.streamNode().filter(FileNodeIF::isFile)
          .collect(new TopCollector<FileNodeIF>(FileNodeIF.getSizeComparator(), top))
          .forEach(fn -> System.out.printf("%-8d %s%n", fn.getSize(), fn.getName()));
      sw4.stop();

      System.out.println();
      System.out.println("stream took " + sw.getElapsedTime() + " msec");
      System.out.println("iterate took " + sw2.getElapsedTime() + " msec");
      System.out.println("iterate 2 took " + sw3.getElapsedTime() + " msec");
      System.out.println("iterate 3 took " + sw4.getElapsedTime() + " msec");
    }
  }

  static class MaxCollector<T>
  {
    private final int mi_limit;
    private final Comparator<? super T> mi_comparator;
    private List<T> mi_list = new ArrayList<>();
    private T mi_least;

    public MaxCollector(Comparator<? super T> comparator, int limit)
    {
      mi_comparator = comparator;
      mi_limit = limit;
      mi_list = new ArrayList<>();
    }

    public List<T> getList()
    {
      return mi_list;
    }

    public void evaluate(T o)
    {
      if (mi_list.size() < mi_limit)
      {
        add(o);
      }
      else
      {
        if (mi_comparator.compare(mi_least, o) > 0)
        {
          mi_list.remove(mi_limit - 1);
          add(o);
        }
      }
    }

    private void add(T o)
    {
      mi_list.add(o);
      mi_list.sort(mi_comparator);
      mi_least = mi_list.get(mi_list.size() - 1);
    }
  }

  static class TopCollector<T>
      implements Collector<T, List<T>, List<T>>
  {
    private final int mi_limit;
    private final Comparator<? super T> mi_comparator;
    private List<T> mi_list = new ArrayList<>();
    private T mi_least;

    public TopCollector(Comparator<? super T> comparator, int limit)
    {
      mi_comparator = comparator;
      mi_limit = limit;
      mi_list = new ArrayList<>();
    }

    @Override
    public Supplier<List<T>> supplier()
    {
      return ArrayList::new;
    }

    @Override
    public BiConsumer<List<T>, T> accumulator()
    {
      return (list, item) -> evaluate(item);
    }

    @Override
    public BinaryOperator<List<T>> combiner()
    {
      return (list1, list2) -> {
        list1.addAll(list2);
        return list1;
      };
    }

    @Override
    public Function<List<T>, List<T>> finisher()
    {
      return (resultList) -> { resultList.addAll(mi_list); return resultList; };
    }

    @Override
    public Set<Characteristics> characteristics()
    {
      return Set.of(Characteristics.UNORDERED);
    }

    public void evaluate(T o)
    {
      if (mi_list.size() < mi_limit)
      {
        add(o);
      }
      else
      {
        if (mi_comparator.compare(mi_least, o) > 0)
        {
          mi_list.remove(mi_limit - 1);
          add(o);
        }
      }
    }

    private void add(T o)
    {
      mi_list.add(o);
      mi_list.sort(mi_comparator);
      mi_least = mi_list.get(mi_list.size() - 1);
    }
  }

  void print(Path path)
  {
    System.out.println("--------------");
    System.out.println("path=" + path.toString());
    System.out.println("parent=" + path.getParent());
    System.out.println("root=" + path.getRoot());
  }

  private void doIt3()
  {
    String fileName = "/media/kees/CubeSSD/export/hoorn/snapshot_001_2024_03_13__11_53_56/projecten/matiss/7_3_canada_imperial_units/branches/fixForNumericTextField/trunk/.svn/pristine/1b/1bf639c1dc2fd4ce21ebbde658edefedcf2c2156.svn-base";

    System.out.println(fileName + " -> \n" + determineFileType(fileName));
  }

  private String determineFileType(String name)
  {
    int index;

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
      // if (StringUtils.isAllLetters(name))
      {
        return name.toLowerCase();
      }
    }

    return DiskUsageView.getNoneText();
  }

  public static void main(String[] args)
  {
    new Test5().doIt3();
  }
}
