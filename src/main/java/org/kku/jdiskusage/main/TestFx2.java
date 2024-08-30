package org.kku.jdiskusage.main;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.kku.jdiskusage.javafx.scene.chart.TreeMapNode;
import org.kku.jdiskusage.javafx.scene.chart.TreeMapSquarifyAlgoritm;
import javafx.application.Application;
import javafx.stage.Stage;

public class TestFx2
  extends Application
{
  @Override
  public void start(Stage stage)
  {
    TreeMapSquarifyAlgoritm algo;
    Consumer<List<TreeMapNode>> handler;
    List<TreeMapNode> treeNodeList;

    algo = new TreeMapSquarifyAlgoritm(0, 0, 60, 40, getTreeNodeList(), getHandler());
    algo.evaluate();
  }

  private Consumer<List<TreeMapNode>> getHandler()
  {
    return (tmnList) -> {
      tmnList.forEach(tmn -> {
        System.out.println(tmn);
      });
    };
  }

  private List<TreeMapNode> getTreeNodeList()
  {
    return new MyTreeMapNode(24, true).getChildList();
  }

  class MyTreeMapNode
    extends TreeMapNode
  {
    private boolean m_root;
    private long m_size;

    MyTreeMapNode(int size, boolean root)
    {
      m_size = size;
      m_root = root;
    }

    MyTreeMapNode(int size)
    {
      m_size = size;
    }

    @Override
    public String getTooltipText()
    {
      return "";
    }

    @Override
    public String getName()
    {
      return "" + m_size;
    }

    @Override
    public long getSize()
    {
      return m_size;
    }

    @Override
    protected List<TreeMapNode> initChildList()
    {
      if (m_root)
      {
        List<TreeMapNode> list;

        list = new ArrayList<>();
        list.add(new MyTreeMapNode(6));
        list.add(new MyTreeMapNode(6));
        list.add(new MyTreeMapNode(4));
        list.add(new MyTreeMapNode(3));
        list.add(new MyTreeMapNode(2));
        list.add(new MyTreeMapNode(2));
        list.add(new MyTreeMapNode(1));

        return list;
      }

      return new ArrayList<>();
    }
  }

  public static void main(String[] args)
  {
    launch();
  }
}