
package org.kku.jdiskusage.main;

import java.nio.file.Path;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.kku.jdiskusage.util.AppProperties;
import org.kku.jdiskusage.util.AppPropertyExtensionIF;
import org.kku.jdiskusage.util.DirectoryChooser.PathList;

public class Test implements AppPropertyExtensionIF
{
  public Test()
  {
    test();
  }

  public void test()
  {
    System.out.println("--- before ----");
    System.out.println(getProps().getPathLists(AppProperties.RECENT_SCANS));

    PathList pathList = PathList.of(Path.of("C:/Kees2"));
    System.out.println("---- add -----");
    Stream.concat(Stream.of(pathList), getProps().getPathLists(AppProperties.RECENT_SCANS).stream())
        .forEach(System.out::println);
    System.out.println("---- distinct -----");
    Stream.concat(Stream.of(pathList), getProps().getPathLists(AppProperties.RECENT_SCANS).stream()).distinct()
        .forEach(System.out::println);
    System.out.println(" ---- to list ----");
    System.out.println(Stream.concat(Stream.of(pathList), getProps().getPathLists(AppProperties.RECENT_SCANS).stream())
        .distinct().toList());
    System.out.println(" ---- to props ----");
    getProps().setPathLists(AppProperties.RECENT_SCANS, Stream
        .concat(Stream.of(pathList), getProps().getPathLists(AppProperties.RECENT_SCANS).stream()).distinct().toList());

    addPath(PathList.of(Path.of("C:/Kees2")));
    addPath(PathList.of(Path.of("C:/Kees1")));
    addPath(PathList.of(Path.of("C:/Kees3"), Path.of("C:/Kees1")));

    System.out.println("--- after ----");
    System.out.println(getProps().getPathLists(AppProperties.RECENT_SCANS));
  }

  public void addPath(PathList pathList)
  {
    getProps().setPathLists(AppProperties.RECENT_SCANS,
        Stream.concat(Stream.of(pathList), getProps().getPathLists(AppProperties.RECENT_SCANS).stream()).distinct()
            .limit(10).collect(Collectors.toList()));
  }

  public static void main(String[] args)
  {
    new Test();
  }
}