package org.kku.jdiskusage.util;

import java.nio.file.Path;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.kku.jdiskusage.util.DirectoryChooser.PathList;

public class Converters
{
  public static Converter<String> getStringConverter()
  {
    return new Converter<String>(String::toString, String::toString);
  }

  public static Converter<Long> getLongConverter()
  {
    return new Converter<Long>(Long::valueOf, (value) -> value.toString());
  }

  public static Converter<Double> getDoubleConverter()
  {
    return new Converter<Double>(Double::valueOf, (value) -> value.toString());
  }

  public static Converter<Integer> getIntegerConverter()
  {
    return new Converter<Integer>(Integer::valueOf, (value) -> value.toString());
  }

  public static Converter<Boolean> getBooleanConverter()
  {
    return new Converter<Boolean>(Boolean::valueOf, (value) -> value.toString());
  }

  public static Converter<Locale> getLocaleConverter()
  {
    return new Converter<Locale>(Locale::forLanguageTag, Locale::toLanguageTag);
  }

  public static Converter<Path> getPathConverter()
  {
    return new Converter<Path>(Path::of, Path::toString);
  }

  public static <E extends Enum<E>> Converter<E> getEnumConverter(Class<E> enumClass)
  {
    return new Converter<E>((s) -> Enum.valueOf(enumClass, s), (e) -> e.name());
  }

  public static Converter<PathList> getPathListConverter()
  {
    return new Converter<PathList>(
        (s) -> new PathList(Stream.of(s.split(",")).map(fileName -> Path.of(fileName)).toList()),
        (pl) -> pl.getPathList().stream().map(Path::toString).collect(Collectors.joining(",")));
  }

  static class Converter<T>
  {
    private Function<String, T> mi_fromString;
    private Function<T, String> mi_toString;

    Converter(Function<String, T> fromString, Function<T, String> toString)
    {
      mi_toString = toString;
      mi_fromString = fromString;
    }

    public T fromString(String s)
    {
      if (s == null)
      {
        return null;
      }
      return mi_fromString.apply(s);
    }

    public String toString(T value)
    {
      if (value == null)
      {
        return null;
      }
      return mi_toString.apply(value);
    }
  }
}