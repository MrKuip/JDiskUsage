package org.kku.jdiskusage.javafx.scene.chart;

import java.util.List;
import java.util.stream.Stream;

abstract public class TreeMapNode
    implements TreeMapNodeIF
{
  private int m_depth;
  private int m_x;
  private int m_y;
  private int m_width;
  private int m_height;
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

  public TreeMapNode getChild(String name)
  {
    return getChildList().stream().filter(tmn -> name.equals(tmn.getName())).findFirst().orElse(null);
  }

  public List<TreeMapNode> getChildList()
  {
    if (m_childList == null)
    {
      m_childList = initChildList();
      for (TreeMapNode tmn : m_childList)
      {
        tmn.setDepth(getDepth() + 1);
        tmn.setParent(this);
      }
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
  public int getX()
  {
    return m_x;
  }

  @Override
  public int getY()
  {
    return m_y;
  }

  @Override
  public int getWidth()
  {
    return m_width;
  }

  @Override
  public int getHeight()
  {
    return m_height;
  }

  @Override
  public void setBounds(int x, int y, int width, int height)
  {
    m_x = x;
    m_y = y;
    m_width = width;
    m_height = height;

    assert m_x >= 0;
    assert m_y >= 0;
    assert m_width > 0;
    assert m_height > 0;
  }

  public boolean contains(int x, int y)
  {
    return (x > m_x && x <= m_x + m_width) && (y > m_y && y <= m_y + m_height);
  }

  public TreeMapNode getNodeAt(int x, int y)
  {
    TreeMapNode node;

    node = getChildList().stream().filter(tmn -> tmn.contains(x, y)).findFirst().orElse(null);
    if (node != null)
    {
      TreeMapNode childNode;
      childNode = node.getNodeAt(x, y);
      return childNode != null ? childNode : node;
    }

    return null;
  }

  @Override
  public String toString()
  {
    return String.format("TreeMapNode[%s, size=%d] x=%d, y=%d, width=%d, height=%d", getName(), getSize(), m_x, m_y,
        m_width, m_height);
  }
}