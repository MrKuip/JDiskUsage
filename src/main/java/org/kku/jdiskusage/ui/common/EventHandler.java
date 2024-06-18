package org.kku.jdiskusage.ui.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventHandler<T extends EventType>
{
  private Map<T, List<EventListenerIF<T>>> mi_listenerByEventTypeMap = new HashMap<>();

  void addEventHandler(T type, EventListenerIF<T> listener)
  {
    getListenerList(type).add(listener);
  }

  void removeEventHandler(T type, EventListenerIF<T> listener)
  {
    getListenerList(type).remove(listener);
  }

  public void fireEvent(T type)
  {
    getListenerList(type).forEach(listener -> listener.handleEvent(type));
  }

  private List<EventListenerIF<T>> getListenerList(T type)
  {
    return mi_listenerByEventTypeMap.computeIfAbsent(type, t -> new ArrayList<>());
  }

  public interface EventListenerIF<T>
  {
    public void handleEvent(T event);
  }

  public static class EventType
  {
  }
}
