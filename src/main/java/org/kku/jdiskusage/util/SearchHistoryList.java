package org.kku.jdiskusage.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.kku.jdiskusage.util.AppProperties.AppProperty;
import org.kku.jdiskusage.util.Converters.Converter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class SearchHistoryList
{
  static private final int MAX_HISTORY = 30;
  private final ObservableList<Search> m_searchHistoryList;

  SearchHistoryList(List<Search> searchHistoryList)
  {
    m_searchHistoryList = FXCollections.observableArrayList();
    m_searchHistoryList.addAll(searchHistoryList);
  }

  public static SearchHistoryList getInstance()
  {
    return getSearchHistoryAppProperty().get();
  }

  public ObservableList<Search> searchHistoryListProperty()
  {
    return m_searchHistoryList;
  }

  public void addSearch(String text, boolean isRegex)
  {
    LinkedHashSet<Search> set;

    // do not allow duplicates in the history
    set = new LinkedHashSet<>(m_searchHistoryList);
    set.addFirst(new Search(text, isRegex));

    m_searchHistoryList.clear();
    m_searchHistoryList.addAll(set);

    save();
  }

  private void save()
  {
    getSearchHistoryAppProperty().set(this);
  }

  private static AppProperty<SearchHistoryList> getSearchHistoryAppProperty()
  {
    return AppSettings.SEARCH_HISTORY.forSubject(SearchHistoryList.class, new SearchHistoryList(new ArrayList<>()));
  }

  public static Converter<SearchHistoryList> getConverter()
  {
    return new Converter<SearchHistoryList>(
        (s) -> new SearchHistoryList(Stream.of(s.split("###")).filter(Predicate.not(StringUtils::isEmpty))
            .limit(MAX_HISTORY).map(searchString -> Search.getConverter().fromString(searchString)).toList()),
        (rsc) -> rsc.searchHistoryListProperty().stream().map(pathList -> Search.getConverter().toString(pathList))
            .limit(MAX_HISTORY).collect(Collectors.joining("###")));
  }

  public static class Search
  {
    private final String mi_text;
    private final boolean mi_regex;

    private Search(String text, boolean regex)
    {
      mi_text = text;
      mi_regex = regex;
    }

    public String getText()
    {
      return mi_text;
    }

    public boolean isRegex()
    {
      return mi_regex;
    }

    public static Converter<Search> getConverter()
    {
      return new Converter<Search>((s) -> {
        String[] items = s.split(",");
        if (items.length == 2)
        {
          return new Search(items[0], Boolean.parseBoolean(items[1]));
        }
        return null;
      }, (search) -> Arrays.asList(search.getText(), search.isRegex()).stream().map(Object::toString)
          .collect(Collectors.joining(",")));
    }

    @Override
    public int hashCode()
    {
      return getText().hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
      if (!(obj instanceof Search search))
      {
        return false;
      }

      if (!Objects.equals(getText(), search.getText()))
      {
        return false;
      }

      if (isRegex() != search.isRegex())
      {
        return false;
      }

      return true;
    }

    @Override
    public String toString()
    {
      return getText() + (isRegex() ? " (regex)" : "");
    }
  }
}