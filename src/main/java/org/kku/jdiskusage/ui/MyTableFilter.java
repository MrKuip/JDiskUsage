package org.kku.jdiskusage.ui;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import javafx.beans.InvalidationListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.TableView;
import javafx.util.Subscription;

public class MyTableFilter<E>
    implements ObservableList<E>
{
  private final TableView<E> m_tableView;
  private ObservableList<E> m_filteredList;

  public MyTableFilter(TableView<E> tableView)
  {
    m_tableView = tableView;
  }

  @SuppressWarnings("unchecked")
  public void apply()
  {
    m_filteredList = m_tableView.getItems();

    @SuppressWarnings("rawtypes")
    ObservableList list = m_filteredList;
    list.add(Totals.SUM);
  }

  private ObservableList<E> getItems()
  {
    return m_filteredList;
  }

  public enum Totals
  {
    SUM("Sum");

    private final String mi_text;

    Totals(String text)
    {
      mi_text = text;
    }

    public String getText()
    {
      return mi_text;
    }
  }

  @Override
  public void forEach(Consumer<? super E> action)
  {
    getItems().forEach(action);
  }

  @Override
  public void addListener(ListChangeListener<? super E> listener)
  {
    getItems().addListener(listener);
  }

  @Override
  public void removeListener(ListChangeListener<? super E> listener)
  {
    getItems().removeListener(listener);
  }

  @Override
  public void addListener(InvalidationListener listener)
  {
    getItems().addListener(listener);
  }

  @Override
  public boolean addAll(E... elements)
  {
    return getItems().addAll(elements);
  }

  @Override
  public boolean setAll(E... elements)
  {
    return getItems().setAll(elements);
  }

  @Override
  public boolean setAll(Collection<? extends E> col)
  {
    return getItems().setAll(col);
  }

  @Override
  public void removeListener(InvalidationListener listener)
  {
    getItems().removeListener(listener);
  }

  @Override
  public boolean removeAll(E... elements)
  {
    return getItems().removeAll(elements);
  }

  @Override
  public boolean retainAll(E... elements)
  {
    return getItems().retainAll(elements);
  }

  @Override
  public void remove(int from, int to)
  {
    getItems().remove(from, to);
  }

  @Override
  public Subscription subscribe(Runnable invalidationSubscriber)
  {
    return getItems().subscribe(invalidationSubscriber);
  }

  @Override
  public FilteredList<E> filtered(Predicate<E> predicate)
  {
    return getItems().filtered(predicate);
  }

  @Override
  public SortedList<E> sorted(Comparator<E> comparator)
  {
    return getItems().sorted(comparator);
  }

  @Override
  public SortedList<E> sorted()
  {
    return getItems().sorted();
  }

  @Override
  public int size()
  {
    return getItems().size() + 1;
  }

  @Override
  public boolean isEmpty()
  {
    return getItems().isEmpty();
  }

  @Override
  public boolean contains(Object o)
  {
    return getItems().contains(o);
  }

  @Override
  public Iterator<E> iterator()
  {
    return getItems().iterator();
  }

  @Override
  public Object[] toArray()
  {
    return getItems().toArray();
  }

  @Override
  public <T> T[] toArray(T[] a)
  {
    return getItems().toArray(a);
  }

  @Override
  public boolean add(E e)
  {
    return getItems().add(e);
  }

  @Override
  public boolean remove(Object o)
  {
    return getItems().remove(o);
  }

  @Override
  public boolean containsAll(Collection<?> c)
  {
    return getItems().containsAll(c);
  }

  @Override
  public boolean addAll(Collection<? extends E> c)
  {
    return getItems().addAll(c);
  }

  @Override
  public boolean addAll(int index, Collection<? extends E> c)
  {
    return getItems().addAll(index, c);
  }

  @Override
  public boolean removeAll(Collection<?> c)
  {
    return getItems().removeAll(c);
  }

  @Override
  public boolean retainAll(Collection<?> c)
  {
    return getItems().retainAll(c);
  }

  @Override
  public void replaceAll(UnaryOperator<E> operator)
  {
    getItems().replaceAll(operator);
  }

  @Override
  public <T> T[] toArray(IntFunction<T[]> generator)
  {
    return getItems().toArray(generator);
  }

  @Override
  public void sort(Comparator<? super E> c)
  {
    getItems().sort(c);
  }

  @Override
  public void clear()
  {
    getItems().clear();
  }

  @Override
  public boolean equals(Object o)
  {
    return getItems().equals(o);
  }

  @Override
  public int hashCode()
  {
    return getItems().hashCode();
  }

  @Override
  public E get(int index)
  {
    return getItems().get(index);
  }

  @Override
  public E set(int index, E element)
  {
    return getItems().set(index, element);
  }

  @Override
  public void add(int index, E element)
  {
    getItems().add(index, element);
  }

  @Override
  public boolean removeIf(Predicate<? super E> filter)
  {
    return getItems().removeIf(filter);
  }

  @Override
  public E remove(int index)
  {
    return getItems().remove(index);
  }

  @Override
  public int indexOf(Object o)
  {
    return getItems().indexOf(o);
  }

  @Override
  public int lastIndexOf(Object o)
  {
    return getItems().lastIndexOf(o);
  }

  @Override
  public ListIterator<E> listIterator()
  {
    return getItems().listIterator();
  }

  @Override
  public ListIterator<E> listIterator(int index)
  {
    return getItems().listIterator(index);
  }

  @Override
  public List<E> subList(int fromIndex, int toIndex)
  {
    return getItems().subList(fromIndex, toIndex);
  }

  @Override
  public Spliterator<E> spliterator()
  {
    return getItems().spliterator();
  }

  @Override
  public Stream<E> stream()
  {
    return getItems().stream();
  }

  @Override
  public Stream<E> parallelStream()
  {
    return getItems().parallelStream();
  }

}
