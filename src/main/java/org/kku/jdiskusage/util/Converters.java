package org.kku.jdiskusage.util;

import java.nio.file.Path;
import java.util.function.Function;

import org.kku.jdiskusage.conf.Language;
import org.kku.jdiskusage.conf.LanguageConfiguration;

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

  public static Converter<Language> getLanguageConverter()
  {
    return new Converter<Language>((s) -> LanguageConfiguration.getInstance().getLanguageById(s), (e) -> e.getName());
  }

  public static Converter<Path> getPathConverter()
  {
    return new Converter<Path>(Path::of, Path::toString);
  }

  public static <E extends Enum<E>> Converter<E> getEnumConverter(Class<E> enumClass)
  {
    return new Converter<E>((s) -> Enum.valueOf(enumClass, s), (e) -> e.name());
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