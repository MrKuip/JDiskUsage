package org.kku.jdiskusage.ui.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class FormatterFactory
{
  private FormatterFactory()
  {
  }

  static public FormatterIF<Long> createByteFormatter()
  {
    return new ByteFormatter();
  }

  static public <T> FormatterIF<T> createStringFormatFormatter(String format)
  {
    return new StringFormatFormatter<>(format);
  }

  static public FormatterIF<Date> createSimpleDateFormatter(String pattern)
  {
    return new DateFormatter(pattern);
  }

  static private class ByteFormatter
    extends AbstractFormatter<Long>
  {
    private ByteFormatter()
    {
      super("");
    }

    @Override
    public String format(Long fileSize)
    {
      //String size = AppPreferences.sizeSystemPreference.get().getFileSize(fileSize);
      return "";
    }
  }

  static private class DateFormatter
    extends AbstractFormatter<Date>
  {
    private final SimpleDateFormat m_dateFormat;

    private DateFormatter(String format)
    {
      super(format);

      m_dateFormat = new SimpleDateFormat(format);
    }

    @Override
    public String format(Date date)
    {
      return m_dateFormat.format(date);
    }
  }

  static private class StringFormatFormatter<T>
    extends AbstractFormatter<T>
  {
    private StringFormatFormatter(String format)
    {
      super(format);
    }

    @Override
    public String format(T object)
    {
      if (object == null)
      {
        return "";
      }

      return String.format(getFormat(), object);
    }
  }
}
