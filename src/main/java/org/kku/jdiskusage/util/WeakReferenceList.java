package org.kku.jdiskusage.util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class WeakReferenceList<T>
{
  private List<WeakReference<T>> list;

  public WeakReferenceList()
  {
    list = new ArrayList<>();
  }

  public void add(T element)
  {
    cleanUp();
    list.add(new WeakReference<>(element));
  }

  public List<T> getElements()
  {
    cleanUp();
    return list.stream().map(WeakReference::get).toList();
  }

  private void cleanUp()
  {
    Iterator<WeakReference<T>> iterator = list.iterator();
    while (iterator.hasNext())
    {
      WeakReference<T> weakRef = iterator.next();
      if (weakRef.get() == null)
      {
        iterator.remove();
      }
    }
  }
}