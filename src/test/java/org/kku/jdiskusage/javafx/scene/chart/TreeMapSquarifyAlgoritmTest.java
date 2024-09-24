package org.kku.jdiskusage.javafx.scene.chart;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;

public class TreeMapSquarifyAlgoritmTest
{
  //@Test
  public void testAlgorithm()
  {
    TestTreeMapNode root;

    // This is the example used in the paper: https://www.win.tue.nl/~vanwijk/stm.pdf
    root = new TestTreeMapNode("root", 24);
    root.setBounds(0, 0, 6, 4);
    root.add(new TestTreeMapNode("a", 6), 0, 0, 3, 2);
    root.add(new TestTreeMapNode("b", 6), 0, 2, 3, 2);
    root.add(new TestTreeMapNode("c", 4), 3, 0, 2, 2);
    root.add(new TestTreeMapNode("d", 3), 5, 0, 1, 2);
    root.add(new TestTreeMapNode("e", 2), 3, 2, 2, 1);
    root.add(new TestTreeMapNode("f", 2), 3, 3, 2, 1);
    root.add(new TestTreeMapNode("g", 1), 5, 2, 1, 1);

    testTreeMap(root);
  }

  @Test
  public void testAlgorithm2()
  {
    TestTreeMapNode root;

    root = new TestTreeMapNode("root", 36);
    root.setBounds(0, 0, 6, 6);
    root.add(new TestTreeMapNode("a", 9), 0, 0, 3, 3);
    root.add(new TestTreeMapNode("b", 9), 0, 3, 3, 3);
    root.add(new TestTreeMapNode("c", 9), 3, 0, 3, 3);
    root.add(new TestTreeMapNode("d", 9), 3, 3, 3, 3);

    testTreeMap(root);
  }

  @Test
  public void testAlgorithm3()
  {
    TestTreeMapNode root;

    root = new TestTreeMapNode("root", 16);
    root.setBounds(0, 0, 4, 4);
    root.add(new TestTreeMapNode("a", 4), 0, 0, 2, 2);
    root.add(new TestTreeMapNode("b", 4), 0, 2, 2, 2);
    root.add(new TestTreeMapNode("c", 4), 2, 0, 2, 2);
    root.add(new TestTreeMapNode("d", 1), 2, 2, 1, 1);
    root.add(new TestTreeMapNode("e", 1), 2, 3, 1, 1);
    root.add(new TestTreeMapNode("f", 1), 3, 2, 1, 1);
    root.add(new TestTreeMapNode("g", 1), 3, 3, 1, 1);

    testTreeMap(root);
  }

  public void testTreeMap(TestTreeMapNode root)
  {
    TreeMapSquarifyAlgoritm algo;
    List<TreeMapNode> tmnList;
    SquarifyHandler handler;
    long expectedSurface;
    long actualSurface;

    tmnList = root.getChildList();

    handler = new SquarifyHandler();
    algo = new TreeMapSquarifyAlgoritm(root.getX(), root.getY(), root.getWidth(), root.getHeight(), tmnList,
        handler.getHandler());
    algo.evaluate();

    tmnList.forEach(tmn -> {
      if (tmn instanceof TestTreeMapNode ttmn)
      {
        assertEquals(ttmn.getExpectedRect().x, tmn.getX(), "x of " + tmn.getName());
        assertEquals(ttmn.getExpectedRect().y, tmn.getY(), "y of " + tmn.getName());
        assertEquals(ttmn.getExpectedRect().width, tmn.getWidth(), "width of " + tmn.getName());
        assertEquals(ttmn.getExpectedRect().height, tmn.getHeight(), "height of " + tmn.getName());
      }
    });

    // WATCH OUT: This algorithm converts to pixels. a pixels is a point in an int array.
    //   So surfaces are subject to rounding. This is why some amount of the surface of the parent
    //   is not occupied by the surface of it's children

    // The sum of the surface of all nodes must less the surface of the root
    expectedSurface = root.getSize();
    actualSurface = tmnList.stream().mapToInt(tmn -> tmn.getWidth() * tmn.getHeight()).sum();
    assertTrue(actualSurface <= expectedSurface, "The surface of the root (=" + expectedSurface
        + ") should be greater or equals than the sum of its children (=" + actualSurface + ")");

    tmnList.forEach(tmn -> {
      // The boundaries of the children can never exceed the boundary of the parent
      assertTrue(tmn.getX() + tmn.getWidth() <= root.getWidth());
      assertTrue(tmn.getY() + tmn.getHeight() <= root.getHeight());
    });
  }

  private class SquarifyHandler
  {
    public Consumer<List<TreeMapNode>> getHandler()
    {
      return (tmnList) -> {
        tmnList.forEach(tmn -> {
        });
      };
    }
  }

  static class TestTreeMapNode
    extends TreeMapNode
  {
    record Rect(int x, int y, int width, int height) {}

    private final String m_name;
    private final long m_size;
    private List<TestTreeMapNode> m_tmnList = new ArrayList<>();
    private Rect m_expectedRect;

    TestTreeMapNode(String name, long size)
    {
      m_name = name;
      m_size = size;
    }

    public void add(TestTreeMapNode tmn, int expectedX, int expectedY, int expectedWidth, int expectedHeight)
    {
      m_tmnList.add(tmn);
      tmn.m_expectedRect = new Rect(expectedX, expectedY, expectedWidth, expectedHeight);
    }

    public Rect getExpectedRect()
    {
      return m_expectedRect;
    }

    @Override
    public String getTooltipText()
    {
      return "";
    }

    @Override
    public String getName()
    {
      return m_name;
    }

    @Override
    public long getSize()
    {
      return m_size;
    }

    @Override
    protected List<TreeMapNode> initChildList()
    {
      return m_tmnList.stream().map(TreeMapNode.class::cast).toList();
    }

    @Override
    public String toString()
    {
      return m_name;
    }
  }
}
