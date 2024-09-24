package org.kku.jdiskusage.conf.dao;

import java.util.List;

import org.kku.conf.dao.AbstractDAO;
import org.kku.jdiskusage.conf.Language;
import org.kku.jdiskusage.conf.LanguageConfiguration;

public class LanguageDAO
  extends AbstractDAO<LanguageConfiguration, Language>
{
  public LanguageDAO()
  {
    super(LanguageConfiguration.class);
  }

  @Override
  public Language create()
  {
    Language language;

    language = new Language();

    return language;
  }

  @Override
  public void insert(Language language)
  {
    getConfiguration().add(language);
  }

  @Override
  public void remove(Language language)
  {
    getConfiguration().remove(language);
  }

  @Override
  public List<Language> selectAll()
  {
    return getConfiguration().getList();
  }
}