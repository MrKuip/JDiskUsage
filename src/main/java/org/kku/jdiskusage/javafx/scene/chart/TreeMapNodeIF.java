package org.kku.jdiskusage.javafx.scene.chart;

public interface TreeMapNodeIF
{
  public String getName();

  public void setDepth(int depth);

  public int getDepth();

  public long getSize();

  public int getX();

  public int getY();

  public int getWidth();

  public int getHeight();

  public void setBounds(int x, int y, int width, int height);

  public String getTooltipText();
}