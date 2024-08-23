package org.kku.jdiskusage.javafx.scene.chart;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.kku.jdiskusage.util.TailCall;
import org.kku.jdiskusage.util.TailCalls;

public class TreeMapSquarifyAlgoritm
{
  private final double m_x;
  private final double m_y;
  private final double m_width;
  private final double m_height;
  private final List<TreeMapNode> m_treeNodeList;
  private final double m_sumSize;
  private final double m_areaFactor;

  public TreeMapSquarifyAlgoritm(String parentName, double x, double y, double width, double height,
      List<TreeMapNode> treeNodeList)
  {
    m_x = x;
    m_y = y;
    m_width = width;
    m_height = height;
    m_treeNodeList = new ArrayList<>(treeNodeList);
    m_treeNodeList.sort(Comparator.comparingDouble((TreeMapNode::getSize)).reversed());
    m_sumSize = m_treeNodeList.stream().mapToDouble(TreeMapNode::getSize).sum();
    m_areaFactor = Math.sqrt((m_width * m_height) / m_sumSize);
  }

  public void evaluate()
  {
    TailCall<Void> tc;

    // Use tail recursion because the stack will grow enormously when using normal recursion
    tc = evaluate(m_x / m_areaFactor, m_y / m_areaFactor, m_width / m_areaFactor, m_height / m_areaFactor,
        m_treeNodeList);
    tc.invoke();
  }

  private TailCall<Void> evaluate(double x, double y, double width, double height, List<TreeMapNode> treeNodeList)
  {
    int index;
    List<TreeMapNode> rowList;

    if (width < 0.1 && height < 0.1)
    {
      return TailCalls.done(null);
    }

    rowList = new ArrayList<>();
    index = 0;
    while (index < treeNodeList.size())
    {
      TreeMapNode treeNode;

      treeNode = treeNodeList.get(index);
      if (rowList.isEmpty())
      {
        rowList.add(treeNode);
        index++;
      }
      else
      {
        if (isRatioWorseWhenTreeNodeisAddedToRow(width, height, rowList, treeNode))
        {
          return fixRowAndEvaluateNewRow(treeNodeList, index, rowList, x, y, width, height);
        }
        else
        {
          rowList.add(treeNode);
          index++;
        }
      }
    }

    if (!rowList.isEmpty())
    {
      fixRowAndEvaluateNewRow(null, -1, rowList, x, y, width, height);
    }

    return TailCalls.done(null);
  }

  private TailCall<Void> fixRowAndEvaluateNewRow(List<TreeMapNode> treeNodeList, int currentTreeNodeListIndex,
      List<TreeMapNode> rowList, double x, double y, double width, double height)
  {
    double area;
    double remainingWidth;
    double remainingHeight;

    area = rowList.stream().mapToDouble(TreeMapNode::getSize).sum();
    if (width > height)
    {
      double rowWidth;
      double nodeY;

      rowWidth = area / height;
      remainingWidth = width - rowWidth;
      remainingHeight = height;
      nodeY = y;

      for (int i = 0; i < rowList.size(); i++)
      {
        TreeMapNode tn;
        double nodeHeight;

        tn = rowList.get(i);
        nodeHeight = tn.getSize() / rowWidth;

        tn.setBounds(x * m_areaFactor, nodeY * m_areaFactor, rowWidth * m_areaFactor, nodeHeight * m_areaFactor);
        nodeY += nodeHeight;
      }

      if (treeNodeList != null)
      {
        final double x1;

        x1 = x + rowWidth;
        return TailCalls.call(() -> evaluate(x1, y, remainingWidth, remainingHeight,
            treeNodeList.subList(currentTreeNodeListIndex, treeNodeList.size())));
      }
    }
    else
    {
      double rowHeight;
      double nodeX;

      rowHeight = area / width;
      remainingWidth = width;
      remainingHeight = height - rowHeight;
      nodeX = x;

      for (int i = 0; i < rowList.size(); i++)
      {
        TreeMapNode tn;
        double nodeWidth;

        tn = rowList.get(i);
        nodeWidth = tn.getSize() / rowHeight;

        tn.setBounds(nodeX * m_areaFactor, y * m_areaFactor, nodeWidth * m_areaFactor, rowHeight * m_areaFactor);
        nodeX += nodeWidth;
      }

      if (treeNodeList != null)
      {
        return TailCalls.call(() -> evaluate(x, y + rowHeight, remainingWidth, remainingHeight,
            treeNodeList.subList(currentTreeNodeListIndex, treeNodeList.size())));
      }
    }

    return TailCalls.done(null);
  }

  private boolean isRatioWorseWhenTreeNodeisAddedToRow(double width, double height, List<TreeMapNode> rowList,
      TreeMapNode treeNode)
  {
    double ratioBefore;
    double ratioAfter;
    double length;
    double area;
    double rowLength;

    length = (width > height) ? height : width;

    area = rowList.stream().mapToDouble(TreeMapNodeIF::getSize).sum();
    rowLength = area / length;
    ratioBefore = (rowList.get(0).getSize() / rowLength) / rowLength;
    ratioBefore = Math.abs(ratioBefore - 1);

    area += treeNode.getSize();
    rowLength = area / length;
    ratioAfter = (rowList.get(0).getSize() / rowLength) / rowLength;
    ratioAfter = Math.abs(ratioAfter - 1);

    //System.out.printf("ratio %3.2f -> %3.2f%n", ratioBefore, ratioAfter);

    return ratioBefore < ratioAfter;
  }

}