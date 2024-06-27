
package org.kku.jdiskusage.main;

import java.util.Locale;
import org.kku.jdiskusage.util.Translator;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Test
{
  public Test()
  {
    test2();
  }

  public void test1()
  {
    String key;

    key = "Preferences";

    System.out.println(key + " -> " + Translator.getTranslatedText(key));

    System.out.println("To nl");
    Translator.getInstance().changeLocale(new Locale("nl"));
    System.out.println(key + " -> " + Translator.getTranslatedText(key));

    System.out.println("To en");
    Translator.getInstance().changeLocale(new Locale("en"));
    System.out.println(key + " -> " + Translator.getTranslatedText(key));
  }

  public void test2()
  {
    StringProperty p;
    StringProperty p2;

    p = new SimpleStringProperty();
    p.set("Preferences");

    p2 = Translator.translatedTextProperty(p.get());
    System.out.println(p.get() + " = " + p2.get());

    p.bind(p2);

    p.addListener((a, oldValue, newValue) -> { System.out.println("p  changed to " + newValue); });
    p2.addListener((a, oldValue, newValue) -> { System.out.println("p2 changed to " + newValue); });

    p2.set("hallo");
    p2.set("hallo1");
    p2.set("hallo2");

    System.out.println("To nl");
    Translator.getInstance().changeLocale(new Locale("nl"));
    try
    {
      Thread.sleep(5000);
    }
    catch (InterruptedException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    System.out.println("To en");
    Translator.getInstance().changeLocale(new Locale("en"));
    try
    {
      Thread.sleep(5000);
    }
    catch (InterruptedException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    System.out.println("To nl");
    Translator.getInstance().changeLocale(new Locale("nl"));
    try
    {
      Thread.sleep(5000);
    }
    catch (InterruptedException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public static void main(String[] args)
  {
    new Test();
  }
}