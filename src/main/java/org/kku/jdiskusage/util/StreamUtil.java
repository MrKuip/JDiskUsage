package org.kku.jdiskusage.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class StreamUtil
{

  public static <T> TopCollector<T> createTopCollector(Comparator<? super T> comparator, int limit)
  {
    return new TopCollector<>(comparator, limit);
  }

  static private class TopCollector<T>
      implements Collector<T, List<T>, List<T>>
  {
    private final int mi_limit;
    private final Comparator<? super T> mi_comparator;
    private List<T> mi_list = new ArrayList<>();
    private T mi_least;

    private TopCollector(Comparator<? super T> comparator, int limit)
    {
      mi_comparator = comparator;
      mi_limit = limit;
      mi_list = new ArrayList<>();
    }

    @Override
    public Supplier<List<T>> supplier()
    {
      return ArrayList::new;
    }

    @Override
    public BiConsumer<List<T>, T> accumulator()
    {
      return (list, item) -> evaluate(item);
    }

    @Override
    public BinaryOperator<List<T>> combiner()
    {
      return (list1, list2) -> {
        list1.addAll(list2);
        return list1;
      };
    }

    @Override
    public Function<List<T>, List<T>> finisher()
    {
      return (resultList) -> { resultList.addAll(mi_list); return resultList; };
    }

    @Override
    public Set<Characteristics> characteristics()
    {
      return Set.of(Characteristics.UNORDERED);
    }

    public void evaluate(T o)
    {
      if (mi_list.size() < mi_limit)
      {
        add(o);
      }
      else
      {
        if (mi_comparator.compare(mi_least, o) > 0)
        {
          mi_list.remove(mi_limit - 1);
          add(o);
        }
      }
    }

    private void add(T o)
    {
      mi_list.add(o);
      mi_list.sort(mi_comparator);
      mi_least = mi_list.get(mi_list.size() - 1);
    }
  }
}
