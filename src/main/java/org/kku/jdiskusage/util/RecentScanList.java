package org.kku.jdiskusage.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.kku.jdiskusage.util.Converters.Converter;

public class RecentScanList
{
  static private final int MAX_SCANS = 10;
  private final List<PathList> mi_recentScanList;

  RecentScanList(List<PathList> recentScanList)
  {
    mi_recentScanList = recentScanList == null ? new ArrayList<>() : recentScanList;
  }

  public List<PathList> getRecentScanList()
  {
    return mi_recentScanList;
  }

  public static RecentScanList empty()
  {
    return new RecentScanList(Collections.emptyList());
  }

  @Override
  public int hashCode()
  {
    return mi_recentScanList.hashCode();
  }

  @Override
  public boolean equals(Object o)
  {
    if (!(o instanceof RecentScanList recentScanList))
    {
      return false;
    }

    if (getRecentScanList().size() != recentScanList.getRecentScanList().size())
    {
      return false;
    }

    for (int i = 0; i < getRecentScanList().size(); i++)
    {
      if (!Objects.equals(getRecentScanList().get(i), recentScanList.getRecentScanList().get(i)))
      {
        return false;
      }
    }

    return true;
  }

  public static Converter<RecentScanList> getConverter()
  {
    Converter<PathList> pathListConverter;

    pathListConverter = PathList.getConverter();

    return new Converter<RecentScanList>(
        (s) -> new RecentScanList(Stream.of(s.split("###")).filter(Predicate.not(StringUtils::isEmpty)).limit(MAX_SCANS)
            .map(pathListString -> pathListConverter.fromString(pathListString)).toList()),
        (rsc) -> rsc.getRecentScanList().stream().map(pathList -> pathListConverter.toString(pathList)).limit(MAX_SCANS)
            .collect(Collectors.joining("###")));
  }

  public RecentScanList add(int i, PathList pathList)
  {
    return new RecentScanList(
        Stream.concat(Stream.of(pathList), mi_recentScanList.stream()).distinct().limit(MAX_SCANS).toList());
  }
}