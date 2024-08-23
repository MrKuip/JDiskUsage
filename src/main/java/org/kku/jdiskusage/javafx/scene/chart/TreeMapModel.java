package org.kku.jdiskusage.javafx.scene.chart;

public class TreeMapModel
{
  private TreeMapNode m_root;

  public TreeMapModel(TreeMapNode root)
  {
    m_root = root;
  }

  public TreeMapNode getRootNode()
  {
    return m_root;
  }
}