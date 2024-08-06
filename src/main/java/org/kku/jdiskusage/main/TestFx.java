package org.kku.jdiskusage.main;

import java.util.Locale;
import org.kku.jdiskusage.util.LanguageList.Language;
import javafx.application.Application;
import javafx.beans.property.SimpleObjectProperty;
import javafx.stage.Stage;

public class TestFx
  extends Application
{
  @Override
  public void start(Stage stage)
  {
    SimpleObjectProperty<Locale> localeProperty;
    SimpleObjectProperty<Language> languageProperty;

    localeProperty = new SimpleObjectProperty<Locale>();
    languageProperty = new SimpleObjectProperty<Language>();

    bindBidirectionel(localeProperty, languageProperty, new Convert<>()
    {
      @Override
      public Language from(Locale fromValue)
      {
        return null;
      }

      @Override
      public Locale to(Language toValue)
      {
        // TODO Auto-generated method stub
        return null;
      }
    });
  }

  private void bindBidirectionel(SimpleObjectProperty<Locale> localeProperty,
      SimpleObjectProperty<Language> languageProperty, Convert<Locale, Language> converter)
  {
    localeProperty.addListener((o, oldValue, newValue) -> {
      languageProperty.set(converter.from(newValue));
    });
    languageProperty.addListener((o, oldValue, newValue) -> {
      localeProperty.set(converter.to(newValue));
    });
  }

  private interface Convert<From, To>
  {
    public To from(From fromValue);

    public From to(To toValue);
  }

  public static void main(String[] args)
  {
    launch();
  }
}