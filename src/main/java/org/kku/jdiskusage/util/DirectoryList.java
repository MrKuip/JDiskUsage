package org.kku.jdiskusage.util;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class DirectoryList
{
  private final List<Directory> mi_directoryList;

  DirectoryList(List<Directory> directoryList)
  {
    mi_directoryList = directoryList == null ? new ArrayList<>() : directoryList;
  }

  public static DirectoryList empty()
  {
    return new DirectoryList(new ArrayList<>());
  }

  public List<Directory> getDirectoryList()
  {
    return mi_directoryList;
  }

  public record Directory(String name, Path path) {
    public static Directory fromText(String[] text)
    {
      if (text.length == 2)
      {
        return new Directory(text[0], Path.of(text[1]));
      }

      return null;
    }

    @Override
    public String toString()
    {
      if (!StringUtils.isEmpty(name))
      {
        return name;
      }

      return path.getName(path.getNameCount() - 1).toString();
    }
  }
}