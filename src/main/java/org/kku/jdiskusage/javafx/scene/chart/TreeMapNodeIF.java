package org.kku.jdiskusage.javafx.scene.chart;

public interface TreeMapNodeIF
{
  public String getName();

  public void setDepth(int depth);

  public int getDepth();

  public double getSize();

  public double getX();

  public double getY();

  public double getWidth();

  public double getHeight();

  public void setBounds(double x, double y, double width, double height);

  public String getTooltipText();
}