package calendar.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Maintains the state of the calendar by adding and removing events.
 * Handles event storage by date and ensures uniqueness.
 * Supports retrival of events.
 * package-private utility class for model.
 */
class EventCalendar {

  private final Map<LocalDate, Set<Event>> eventsByDate;
  private final Set<EventKey> uniqueKeys;

  EventCalendar() {
    this.eventsByDate = new TreeMap<>();
    this.uniqueKeys = new HashSet<>();
  }

  /**
   * Adds an event to the calendar.
   */
  void addEvent(Event event) {
    LocalDate startDate = event.getStart().toLocalDate();
    LocalDate endDate = event.getEnd().toLocalDate();

    LocalDate current = startDate;
    while (!current.isAfter(endDate)) {
      eventsByDate.computeIfAbsent(current, k -> new HashSet<>()).add(event);
      current = current.plusDays(1);
    }

    EventKey key = new EventKey(event.getSubject(), event.getStart(), event.getEnd());
    uniqueKeys.add(key);
  }

  /**
   * Removes an event from the calendar.
   */
  void removeEvent(Event event) {
    LocalDate startDate = event.getStart().toLocalDate();
    LocalDate endDate = event.getEnd().toLocalDate();

    LocalDate current = startDate;
    while (!current.isAfter(endDate)) {
      Set<Event> events = eventsByDate.get(current);
      if (events != null) {
        events.remove(event);
        if (events.isEmpty()) {
          eventsByDate.remove(current);
        }
      }
      current = current.plusDays(1);
    }

    EventKey key = new EventKey(event.getSubject(), event.getStart(), event.getEnd());
    uniqueKeys.remove(key);
  }

  /**
   * Finds an event by subject and start time.
   */
  Event findEvent(String subject, LocalDateTime start) {
    LocalDate date = start.toLocalDate();
    Set<Event> events = eventsByDate.get(date);

    if (events == null) {
      return null;
    }

    for (Event event : events) {
      if (event.getSubject().equals(subject) && event.getStart().equals(start)) {
        return event;
      }
    }

    return null;
  }

  /**
   * Gets all events on a specific date, sorted by start time.
   */
  List<Ievent> getEventsByDate(LocalDate date) {
    Set<Event> events = eventsByDate.get(date);
    if (events == null) {
      return new ArrayList<>();
    }
    List<Event> eventList = new ArrayList<>(events);
    eventList.sort(Comparator.comparing(Event::getStart));

    List<Ievent> result = new ArrayList<>();
    result.addAll(eventList);
    return result;
  }

  /**
   * Gets all events in a date/time range.
   */
  List<Ievent> getEventsByRange(LocalDateTime start, LocalDateTime end) {
    Set<Event> resultSet = new HashSet<>();

    LocalDate startDate = start.toLocalDate();
    LocalDate endDate = end.toLocalDate();

    LocalDate current = startDate;
    while (!current.isAfter(endDate)) {
      Set<Event> dayEvents = eventsByDate.get(current);
      if (dayEvents != null) {
        for (Event event : dayEvents) {
          if (!event.getEnd().isBefore(start) && !event.getStart().isAfter(end)) {
            resultSet.add(event);
          }
        }
      }
      current = current.plusDays(1);
    }

    List<Event> result = new ArrayList<>(resultSet);
    result.sort(Comparator.comparing(Event::getStart));

    List<Ievent> ieventResult = new ArrayList<>();
    ieventResult.addAll(result);
    return ieventResult;
  }

  /**
   * Gets all events across all dates.
   */
  List<Ievent> getAllEvents() {
    Set<Event> allEventsSet = new HashSet<>();
    for (Set<Event> events : eventsByDate.values()) {
      allEventsSet.addAll(events);
    }

    List<Event> allEvents = new ArrayList<>(allEventsSet);
    allEvents.sort(Comparator.comparing(Event::getStart));

    List<Ievent> result = new ArrayList<>();
    result.addAll(allEvents);
    return result;
  }

  /**
   * Checks if an event key already exists.
   */
  boolean containsKey(EventKey key) {
    return uniqueKeys.contains(key);
  }

  /**
   * Checks if user is busy at a specific date/time.
   */
  boolean isBusy(LocalDateTime dateTime) {
    LocalDate date = dateTime.toLocalDate();
    Set<Event> events = eventsByDate.get(date);

    if (events == null) {
      return false;
    }

    for (Event event : events) {
      if (!dateTime.isBefore(event.getStart()) && dateTime.isBefore(event.getEnd())) {
        return true;
      }
    }

    return false;
  }
}