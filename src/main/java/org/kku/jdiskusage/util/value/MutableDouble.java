package org.kku.jdiskusage.util.value;

public class MutableDouble
{
  private double value;

  public MutableDouble(double value)
  {
    this.value = value;
  }

  public double get()
  {
    return value;
  }

  public void set(double value)
  {
    this.value = value;
  }

  public void add(double delta)
  {
    this.value += delta;
  }

  public void subtract(double delta)
  {
    this.value -= delta;
  }

  @Override
  public String toString()
  {
    return Double.toString(value);
  }
}