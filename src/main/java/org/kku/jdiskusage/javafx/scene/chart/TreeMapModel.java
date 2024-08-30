package org.kku.jdiskusage.javafx.scene.chart;

public class TreeMapModel<T extends TreeMapNode>
{
  private final T m_root;

  public TreeMapModel(T root)
  {
    m_root = root;
  }

  public T getRootNode()
  {
    return m_root;
  }
}