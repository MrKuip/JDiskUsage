package org.kku.jdiskusage.ui.common;

import java.util.function.Predicate;
import org.kku.jdiskusage.util.FileTree.FileNodeIF;
import org.kku.jdiskusage.util.FileTree.FilterIF;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class Filter
    implements FilterIF
{
  private final int mi_hashCode;
  private final String mi_filterType;
  private final String mi_filterOperator;
  private final String mi_filterValue;
  private final Predicate<FileNodeIF> mi_fileNodePredicate;
  private BooleanProperty mi_filterDisabled = new SimpleBooleanProperty(true);

  public Filter(String filterName, String filterValue, Predicate<FileNodeIF> fileNodePredicate)
  {
    this(filterName, "is", filterValue, fileNodePredicate);
  }

  public Filter(String filterName, String filterOperator, String filterValue, Predicate<FileNodeIF> fileNodePredicate)
  {
    mi_filterType = filterName;
    mi_filterOperator = filterOperator;
    mi_filterValue = filterValue;
    mi_fileNodePredicate = fileNodePredicate;
    mi_hashCode = (filterName + filterValue).hashCode();
  }

  @Override
  public boolean accept(FileNodeIF fileNode)
  {
    return getPredicate().test(fileNode);
  }

  public void disable(boolean disable)
  {
    mi_filterDisabled.set(disable);
  }

  public BooleanProperty disabledProperty()
  {
    return mi_filterDisabled;
  }

  public boolean isDisabled()
  {
    return mi_filterDisabled.get();
  }

  public String getFilterKey()
  {
    return getFilterType() + "-" + getFilterOperator();
  }

  public String getFilterType()
  {
    return mi_filterType;
  }

  public String getFilterOperator()
  {
    return mi_filterOperator;
  }

  public String getFilterValue()
  {
    return mi_filterValue;
  }

  public Predicate<FileNodeIF> getPredicate()
  {
    return mi_fileNodePredicate;
  }

  @Override
  public int hashCode()
  {
    return mi_hashCode;
  }

  @Override
  public boolean equals(Object obj)
  {
    Filter filter;

    if (obj == null || !(obj instanceof Filter))
    {
      return false;
    }

    filter = (Filter) obj;
    if (!filter.getFilterType().equals(getFilterType()))
    {
      return false;
    }

    if (!filter.getFilterOperator().equals(getFilterOperator()))
    {
      return false;
    }

    return filter.getFilterValue().equals(getFilterValue());
  }
}