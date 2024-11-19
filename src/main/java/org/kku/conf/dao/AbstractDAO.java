package org.kku.conf.dao;

import java.util.List;
import org.kku.conf.Configuration;
import org.kku.conf.ConfigurationItem;
import org.kku.conf.ConfigurationManager;

public abstract class AbstractDAO<C extends Configuration, CI extends ConfigurationItem>
{
  private final Class<C> m_configurationClass;

  protected AbstractDAO(Class<C> configurationClass)
  {
    m_configurationClass = configurationClass;
  }

  protected C getConfiguration()
  {
    C configuration;

    configuration = ConfigurationManager.getInstance().get(m_configurationClass);
    assert configuration != null : "Failed to load configuration for " + m_configurationClass;

    return configuration;
  }

  public abstract CI create();

  public abstract void insert(CI item);

  public abstract void remove(CI item);

  public abstract List<CI> selectAll();
}
