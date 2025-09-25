package org.kku.jdiskusage.javafx.scene.chart;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import org.kku.common.util.TailCall;
import org.kku.common.util.TailCalls;
import org.kku.jdiskusage.util.Loggers;

/**
 * Algorithm to 'squarify' rectangles in a treemap in order to better compare and select.
 * 
 * @see https://www.win.tue.nl/~vanwijk/stm.pdf
 * 
 */

public class TreeMapSquarifyAlgoritm
{
  private final int m_x;
  private final int m_y;
  private final int m_width;
  private final int m_height;
  private final List<TreeMapNode> m_treeNodeList;
  private final double m_sumSize;
  private final double m_areaFactor;
  private final Consumer<List<TreeMapNode>> m_handler;
  private double m_rowListSum;

  public TreeMapSquarifyAlgoritm(int x, int y, int width, int height, List<TreeMapNode> treeNodeList,
      Consumer<List<TreeMapNode>> handler)
  {
    m_x = x;
    m_y = y;
    m_width = width;
    m_height = height;
    m_treeNodeList = new ArrayList<>(treeNodeList);
    m_treeNodeList.sort(Comparator.comparingDouble((TreeMapNode::getSize)).reversed());
    m_sumSize = m_treeNodeList.stream().mapToDouble(TreeMapNode::getSize).sum();
    m_areaFactor = (m_width * m_height) / m_sumSize;
    m_handler = handler;

    Loggers.treemap.setLevel(Level.FINEST);
  }

  private int counter;

  public void evaluate()
  {
    TailCall<Void> tc;

    counter = 0;

    // Use tail recursion because the stack will grow enormously when using normal recursion
    tc = evaluate(m_x, m_y, m_width, m_height, m_treeNodeList);
    tc.invoke();

    if (m_handler != null)
    {
      m_handler.accept(m_treeNodeList);
    }

    System.out.println("Count=" + counter);
  }

  private TailCall<Void> evaluate(int x, int y, int width, int height, List<TreeMapNode> treeNodeList)
  {
    int index;
    List<TreeMapNode> rowList;

    Loggers.treemap.finest("evaluate: x=%d, y=%d, width=%d, height=%d", x, y, width, height);

    rowList = new ArrayList<>();
    m_rowListSum = 0.0;
    index = 0;
    while (index < treeNodeList.size())
    {
      TreeMapNode treeNode;

      treeNode = treeNodeList.get(index);
      if (rowList.isEmpty())
      {
        if (treeNode.getSize() > 0.0001)
        {
          rowList.add(treeNode);
          m_rowListSum += treeNode.getSize();
        }
        index++;
      }
      else
      {
        if (isRatioGettingWorse(width, height, rowList, treeNode))
        {
          return fixateRowList(treeNodeList, index, rowList, x, y, width, height);
        }
        else
        {
          if (treeNode.getSize() > 0.0001)
          {
            rowList.add(treeNode);
            m_rowListSum += treeNode.getSize();
          }
          index++;
        }
      }
    }

    if (!rowList.isEmpty())
    {
      fixateRowList(null, -1, rowList, x, y, width, height);
    }

    return TailCalls.done(null);
  }

  private TailCall<Void> fixateRowList(List<TreeMapNode> treeNodeList, int currentTreeNodeListIndex,
      List<TreeMapNode> rowList, int x, int y, int remainingWidth, int remainingHeight)
  {
    if (remainingWidth >= remainingHeight)
    {
      int nodeX, nodeY, nodeWidth, nodeHeight;
      int rowListSize;
      int allocatedRowHeight;

      nodeX = 0;
      nodeWidth = (int) Math.ceil((m_rowListSum * m_areaFactor) / remainingHeight);
      if (nodeWidth > remainingWidth)
      {
        nodeWidth = remainingWidth;
      }
      allocatedRowHeight = 0;

      rowListSize = rowList.size();
      for (int i = 0; i < rowListSize; i++)
      {
        TreeMapNode tn;

        tn = rowList.get(i);
        if (i == 0)
        {
          // First node always starts at 0.
          nodeY = 0;
        }
        else
        {
          // Next starts always just under the previous one
          nodeY = allocatedRowHeight;
        }

        if (i == rowListSize - 1)
        {
          // Last node takes remaining space
          nodeHeight = remainingHeight - allocatedRowHeight;
        }
        else
        {
          int remainingSpace;

          nodeHeight = (int) Math.ceil((tn.getSize() * m_areaFactor) / nodeWidth);
          if (nodeHeight > remainingHeight)
          {
            nodeHeight = remainingHeight;
          }
          remainingSpace = remainingHeight - allocatedRowHeight - nodeHeight;
          if (remainingSpace <= 1)
          {
            // In any circumstance leave at least one pixel for the last node
            nodeHeight = (remainingHeight - allocatedRowHeight) - 1;
          }
          if (nodeWidth == 0 && remainingSpace > 2)
          {
            // Let this node be 1 pixel if there is remaining space (and always 1 pixel for the last node!)
            nodeHeight = 1;
          }
        }

        allocatedRowHeight += nodeHeight;

        tn.setBounds(x + nodeX, y + nodeY, nodeWidth, nodeHeight);
        Loggers.treemap.finest("squarified: tn=%s, x=%d, y=%d, width=%d, height=%d", tn.getName(), tn.getX(), tn.getY(),
            tn.getWidth(), tn.getHeight());
        counter++;
      }

      if (treeNodeList != null)
      {
        int nWidth = nodeWidth;
        if (remainingHeight > 0 && remainingWidth - nWidth > 0)
        {
          return TailCalls.call(() -> evaluate(x + nWidth, y, remainingWidth - nWidth, remainingHeight,
              treeNodeList.subList(currentTreeNodeListIndex, treeNodeList.size())));
        }
      }
    }
    else
    {
      int nodeX, nodeY, nodeWidth, nodeHeight;
      int rowListSize;
      int allocatedRowWidth;

      nodeY = 0;
      nodeHeight = (int) (Math.ceil((m_rowListSum * m_areaFactor) / remainingWidth));
      if (nodeHeight > remainingHeight)
      {
        nodeHeight = remainingHeight;
      }
      allocatedRowWidth = 0;

      rowListSize = rowList.size();
      for (int i = 0; i < rowListSize; i++)
      {
        TreeMapNode tn;

        tn = rowList.get(i);
        if (i == 0)
        {
          // First node always starts at 0.
          nodeX = 0;
        }
        else
        {
          // Next starts always just after the previous one
          nodeX = allocatedRowWidth;
        }

        if (i == rowListSize - 1)
        {
          // Last node takes remaining space
          nodeWidth = remainingWidth - allocatedRowWidth;
        }
        else
        {
          int remainingSpace;

          nodeWidth = (int) Math.ceil((tn.getSize() * m_areaFactor) / nodeHeight);
          if (nodeWidth > remainingWidth)
          {
            nodeWidth = remainingWidth;
          }
          remainingSpace = remainingWidth - allocatedRowWidth - nodeWidth;
          if (remainingSpace < 1)
          {
            // In any circumstance leave at least one pixel for the last node
            nodeWidth = (remainingWidth - allocatedRowWidth) - 1;
          }
          if (nodeHeight == 0 && remainingSpace > 2)
          {
            // Let this node be 1 pixel if there is remaining space (and always 1 pixel for the last node!)
            nodeWidth = 1;
          }
        }

        allocatedRowWidth += nodeWidth;

        tn.setBounds(x + nodeX, y + nodeY, nodeWidth, nodeHeight);
        Loggers.treemap.finest("squarified: tn=%s, x=%d, y=%d, width=%d, height=%d", tn.getName(), tn.getX(), tn.getY(),
            tn.getWidth(), tn.getHeight());
        counter++;
      }

      if (treeNodeList != null)
      {
        int nHeight = nodeHeight;
        if (remainingHeight - nHeight > 0 && remainingWidth > 0)
        {
          return TailCalls.call(() -> evaluate(x, y + nHeight, remainingWidth, remainingHeight - nHeight,
              treeNodeList.subList(currentTreeNodeListIndex, treeNodeList.size())));
        }
      }
    }

    return TailCalls.done(null);
  }

  private boolean isRatioGettingWorse(int width, int height, List<TreeMapNode> rowList, TreeMapNode treeNode)
  {
    double ratioBefore;
    double ratioAfter;
    TreeMapNode tmn;

    tmn = rowList.get(rowList.size() - 1);

    if (width >= height)
    {
      double rowHeight;
      double rowWidth;

      rowWidth = (m_rowListSum * m_areaFactor) / height;
      rowHeight = (tmn.getSize() * m_areaFactor) / rowWidth;
      ratioBefore = Math.max(rowHeight / rowWidth, rowWidth / rowHeight);

      rowWidth = ((m_rowListSum + treeNode.getSize()) * m_areaFactor) / height;
      rowHeight = (treeNode.getSize() * m_areaFactor) / rowWidth;
      ratioAfter = Math.max(rowHeight / rowWidth, rowWidth / rowHeight);
    }
    else
    {
      double rowHeight;
      double rowWidth;

      rowHeight = (m_rowListSum * m_areaFactor) / width;
      rowWidth = (tmn.getSize() * m_areaFactor) / rowHeight;
      ratioBefore = Math.max(rowHeight / rowWidth, rowWidth / rowHeight);

      rowHeight = ((m_rowListSum + treeNode.getSize()) * m_areaFactor) / width;
      rowWidth = (treeNode.getSize() * m_areaFactor) / rowHeight;
      ratioAfter = Math.max(rowHeight / rowWidth, rowWidth / rowHeight);
    }

    Loggers.treemap.finest("ratio %3.2f -> %3.2f when adding node %s with size %d", ratioBefore, ratioAfter,
        tmn.getName(), tmn.getSize());

    ratioBefore = Math.abs(ratioBefore - 1);
    ratioAfter = Math.abs(ratioAfter - 1);

    return ratioBefore < ratioAfter;
  }

}
