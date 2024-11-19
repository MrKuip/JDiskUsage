package org.kku.jdiskusage.ui.common;

import java.util.Objects;
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

  public Filter(String filterType, String filterValue, Predicate<FileNodeIF> fileNodePredicate)
  {
    this(filterType, "is", filterValue, fileNodePredicate);
  }

  public Filter(String filterType, String filterOperator, String filterValue, Predicate<FileNodeIF> fileNodePredicate)
  {
    mi_filterType = filterType;
    mi_filterOperator = filterOperator;
    mi_filterValue = filterValue;
    mi_fileNodePredicate = fileNodePredicate;
    mi_hashCode = Objects.hash(mi_filterType, mi_filterOperator, mi_filterValue);
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
    if (obj == null || !(obj instanceof Filter filter))
    {
      return false;
    }

    if (!Objects.equals(filter.getFilterType(), getFilterType()))
    {
      return false;
    }

    if (!Objects.equals(filter.getFilterOperator(), getFilterOperator()))
    {
      return false;
    }

    return Objects.equals(filter.getFilterValue(), getFilterValue());
  }
}