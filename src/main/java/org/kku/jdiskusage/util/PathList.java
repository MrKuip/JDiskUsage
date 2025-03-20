package org.kku.jdiskusage.util;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.kku.common.util.StringUtils;
import org.kku.jdiskusage.util.Converters.Converter;

public class PathList
{
  private final List<Path> mi_directoryList;
  private final String mi_description;

  PathList(List<Path> directoryList)
  {
    mi_directoryList = directoryList == null ? new ArrayList<>() : directoryList;
    mi_description = initDescription();
  }

  public List<Path> getPathList()
  {
    return mi_directoryList;
  }

  public static PathList empty()
  {
    return new PathList(Collections.emptyList());
  }

  public static PathList of(Path path)
  {
    return new PathList(Arrays.asList(path));
  }

  public static PathList of(Path... pathArray)
  {
    return new PathList(Arrays.asList(pathArray));
  }

  public static PathList of(List<Path> pathList)
  {
    return new PathList(pathList);
  }

  public boolean isEmpty()
  {
    return getPathList().isEmpty();
  }

  private String initDescription()
  {
    List<Path> pathList;
    List<String> pathNameList;
    int commonPrefixIndex;
    int commonPrefixIndex2;
    boolean commonPrefixIndexFound;
    String shortestDirectoryName;
    StringBuilder description;

    pathList = getPathList();
    if (pathList.size() == 0)
    {
      return "";
    }

    if (pathList.size() == 1)
    {
      return pathList.get(0).toString();
    }

    pathNameList = pathList.stream().map(Path::toString).toList();

    shortestDirectoryName = pathNameList.stream().min(Comparator.comparing(String::length)).get();
    commonPrefixIndex = 0;
    commonPrefixIndexFound = false;

    for (int index = 0; index < shortestDirectoryName.length(); index++)
    {
      for (int dirIndex = 0; dirIndex < pathNameList.size(); dirIndex++)
      {
        if (shortestDirectoryName.charAt(index) != pathNameList.get(dirIndex).charAt(index))
        {
          commonPrefixIndex = index;
          commonPrefixIndexFound = true;
          break;
        }
      }

      if (commonPrefixIndexFound)
      {
        break;
      }
    }

    description = new StringBuilder();
    if (commonPrefixIndex > 0)
    {
      description.append(shortestDirectoryName.substring(0, commonPrefixIndex));
    }
    commonPrefixIndex2 = commonPrefixIndex;

    description.append(" -> ");
    description.append(
        pathNameList.stream().map(pn -> "'" + pn.substring(commonPrefixIndex2) + "'").collect(Collectors.joining(" ")));

    return description.toString();
  }

  @Override
  public int hashCode()
  {
    return toString().hashCode();
  }

  @Override
  public boolean equals(Object o)
  {
    if (!(o instanceof PathList pathList))
    {
      return false;
    }

    return Objects.equals(pathList.toString(), toString());
  }

  public static Converter<PathList> getConverter()
  {
    return new Converter<PathList>(
        (s) -> new PathList(Stream.of(s.split(",")).filter(Predicate.not(StringUtils::isEmpty))
            .map(fileName -> Path.of(fileName)).toList()),
        (pl) -> pl.getPathList().stream().map(Path::toString).collect(Collectors.joining(",")));
  }

  @Override
  public String toString()
  {
    return mi_description;
  }
}