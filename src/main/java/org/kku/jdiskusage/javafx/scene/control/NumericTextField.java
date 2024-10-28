package org.kku.jdiskusage.javafx.scene.control;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.beans.property.ObjectProperty;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.util.StringConverter;

public class NumericTextField<T extends Number>
  extends TextField
{
  private final TextFormatter<T> m_textFormatter;

  private NumericTextField(TextFormatter<T> textFormatter)
  {
    m_textFormatter = textFormatter;

    setTextFormatter(textFormatter);
  }

  public T getValue()
  {
    return m_textFormatter.getValue();
  }

  public void setValue(T value)
  {
    m_textFormatter.setValue(value);
  }

  public ObjectProperty<T> valueProperty()
  {
    return m_textFormatter.valueProperty();
  }

  public static NumericTextField<Integer> integerField()
  {
    return new NumericTextField<>(createIntegerTextFormatter());
  }

  public static NumericTextField<Double> doubleField()
  {
    return doubleField("#0.000");
  }

  public static NumericTextField<Double> doubleField(String pattern)
  {
    return new NumericTextField<>(createDoubleTextFormatter(pattern));
  }

  private static TextFormatter<Integer> createIntegerTextFormatter()
  {
    TextFormatter<Integer> tf;
    Pattern pattern;
    Matcher matcher;

    pattern = Pattern.compile("[-+]?[0-9]*");
    matcher = pattern.matcher("");

    tf = new TextFormatter<>(new IntegerConverter(), 0, c -> {

      matcher.reset(c.getControlNewText());
      if (!matcher.matches())
      {
        return null;
      }

      return c;
    });

    return tf;
  }

  private static TextFormatter<Double> createDoubleTextFormatter(String pattern)
  {
    TextFormatter<Double> tf;
    DecimalFormat format;

    format = new DecimalFormat(pattern);
    tf = new TextFormatter<>(new DoubleConverter(format), 0.0, c -> {
      ParsePosition parsePosition;
      Object object;

      if (c.getControlNewText().isEmpty())
      {
        return c;
      }

      parsePosition = new ParsePosition(0);
      object = format.parse(c.getControlNewText(), parsePosition);
      if ((object == null) || ((parsePosition.getIndex()) < (c.getControlNewText().length())))
      {
        return null;
      }
      else
      {
        return c;
      }
    });

    return tf;
  }

  static class DoubleConverter
    extends StringConverter<Double>
  {
    private final DecimalFormat mi_format;

    public DoubleConverter(DecimalFormat format)
    {
      mi_format = format;
    }

    @Override
    public String toString(Double s)
    {
      return mi_format.format(s);
    }

    @Override
    public Double fromString(String s)
    {
      try
      {
        double value;
        String text;

        // Why is parse called twice?
        // to let the value have the same number decimal places as seen on the screen.
        // if we parse once and the input is 10.11111111 then 10.1111111 is in the value 
        //   but 10.111 is on the screen.
        // If we parse twice and the input is 10.11111111 then format gets us 10.111 and
        //   then parse will get 10.111 in the value
        value = mi_format.parse(s).doubleValue();
        text = mi_format.format(value);
        value = mi_format.parse(text).doubleValue();

        return Double.valueOf(value);
      }
      catch (ParseException e)
      {
        return 0.0;
      }
    }
  }

  static class IntegerConverter
    extends StringConverter<Integer>
  {
    public IntegerConverter()
    {
    }

    @Override
    public String toString(Integer value)
    {
      if (value == null) return "";

      return String.valueOf(value.intValue());
    }

    @Override
    public Integer fromString(String s)
    {
      if (s == null) return Integer.valueOf(0);

      return Integer.valueOf(s);
    }
  }
}
