package org.kku.jdiskusage.util;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;

public class LanguageList
{
  private static final LanguageList instance = new LanguageList();
  private static final String ENGLISH_LANGUAGE = "English";

  private List<Language> m_languageList;
  private Map<String, Language> m_languageByNameMap;

  private LanguageList()
  {
  }

  public static LanguageList getInstance()
  {
    return instance;
  }

  public Language getDefault()
  {
    return getLanguageByNameMap().get(ENGLISH_LANGUAGE);
  }

  public Language getLanguage(String name)
  {
    return getLanguageByNameMap().get(name);
  }

  public Map<String, Language> getLanguageByNameMap()
  {
    if (m_languageByNameMap == null)
    {
      m_languageByNameMap = getList().stream().collect(Collectors.toMap(Language::getName, Function.identity()));
    }

    return m_languageByNameMap;
  }

  public List<Language> getList()
  {
    if (m_languageList == null)
    {
      Properties props;
      List<LanguageList.Language> list;

      try
      {
        props = new Properties();
        list = new ArrayList<>();
        try (InputStream stream = getClass().getResourceAsStream("/language.properties"))
        {
          props.load(stream);
          props.entrySet().stream().map(entry -> new Language((String) entry.getKey(), (String) entry.getValue()))
              .collect(Collectors.toCollection(() -> list));
        }
        // Always add the default language at index 0
        list.add(0, new Language(ENGLISH_LANGUAGE, ""));

        m_languageList = list;
        return m_languageList;
      }
      catch (Exception e)
      {
        Log.log.error(e, "Failed to collect languages");
        m_languageList = Collections.emptyList();
      }
    }

    return m_languageList;
  }

  static public class Language
  {
    private final String mi_name;
    private final String mi_language;
    private final Locale mi_locale;

    private Language(String name, String language)
    {
      mi_name = name;
      mi_language = language;
      mi_locale = new Locale(language);
    }

    public String getName()
    {
      return mi_name;
    }

    public String getLanguage()
    {
      return mi_language;
    }

    public Locale getLocale()
    {
      return mi_locale;
    }

    @Override
    public String toString()
    {
      return mi_name;
    }
  }
}