package org.kku.jdiskusage.javafx.scene.chart;

import java.util.List;
import java.util.stream.Stream;

abstract public class TreeMapNode
    implements TreeMapNodeIF
{
  private int m_depth;
  private double m_x;
  private double m_y;
  private double m_width;
  private double m_height;
  private TreeMapNode m_parent;
  private List<TreeMapNode> m_childList;
  private int m_colorIndex = -1;

  public TreeMapNode()
  {
  }

  @Override
  public void setDepth(int depth)
  {
    m_depth = depth;
  }

  @Override
  public int getDepth()
  {
    return m_depth;
  }

  public void setColorIndex(int colorIndex)
  {
    m_colorIndex = colorIndex;
  }

  public int getColorIndex()
  {
    if (m_colorIndex == -1)
    {
      m_colorIndex = getParent().getColorIndex();
    }

    return m_colorIndex;
  }

  public boolean isLeaf()
  {
    return !hasChildren();
  }

  public TreeMapNode getParent()
  {
    return m_parent;
  }

  private void setParent(TreeMapNode parent)
  {
    m_parent = parent;
  }

  public boolean hasChildren()
  {
    return !getChildList().isEmpty();
  }

  abstract protected List<TreeMapNode> initChildList();

  public List<TreeMapNode> getChildList()
  {
    if (m_childList == null)
    {
      m_childList = initChildList();
      m_childList.stream().forEach(tmn -> {
        tmn.setDepth(getDepth() + 1);
        tmn.setParent(this);
      });
    }

    return m_childList;
  }

  public Stream<TreeMapNode> streamNode()
  {
    if (!hasChildren())
    {
      return Stream.of(this);
    }

    return Stream.concat(Stream.of(this), getChildList().stream().flatMap(TreeMapNode::streamNode));
  }

  @Override
  public double getX()
  {
    return m_x;
  }

  @Override
  public double getY()
  {
    return m_y;
  }

  @Override
  public double getWidth()
  {
    return m_width;
  }

  @Override
  public double getHeight()
  {
    return m_height;
  }

  @Override
  public void setBounds(double x, double y, double width, double height)
  {
    m_x = x;
    m_y = y;
    m_width = width;
    m_height = height;
  }

  @Override
  public String toString()
  {
    return String.format("TreeMapNode[%s, size=%3.2f] x=%3.2f, y=%3.2f, width=%3.2f, height=%3.2f", getName(),
        getSize(), m_x, m_y, m_width, m_height);
  }

}