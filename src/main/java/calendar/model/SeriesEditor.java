package calendar.model;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles editing of event series with different strategies.
 * package-private utility class for model.
 */
class SeriesEditor {

  private final EventCalendar calendar;
  private final SystemValidator validator;
  private final EventUpdater propertyUpdater;

  private enum SeriesHandling {
    KEEP_SERIES,
    SPLIT_SERIES,
    REPLACE_SERIES
  }

  SeriesEditor(EventCalendar calendar, SystemValidator validator,
               EventUpdater propertyUpdater) {
    this.calendar = calendar;
    this.validator = validator;
    this.propertyUpdater = propertyUpdater;
  }

  /**
   * Edits multiple events with appropriate series handling.
   */
  void editMultiple(List<Event> eventsToEdit, String property, String newValue,
                    boolean isStartPropertyChange) {
    SeriesHandling handling = isStartPropertyChange
        ? SeriesHandling.SPLIT_SERIES
        : SeriesHandling.KEEP_SERIES;

    applyEditsToEvents(eventsToEdit, property, newValue, handling);
  }

  /**
   * Edits entire series with appropriate handling.
   */
  void editAll(List<Event> eventsToEdit, String property, String newValue,
               boolean isStartPropertyChange) {
    SeriesHandling handling = isStartPropertyChange
        ? SeriesHandling.REPLACE_SERIES
        : SeriesHandling.KEEP_SERIES;

    applyEditsToEvents(eventsToEdit, property, newValue, handling);
  }

  /**
   * Applies edits to a list of events with specified series handling strategy.
   */
  private void applyEditsToEvents(List<Event> eventsToEdit, String property, String newValue,
                                  SeriesHandling seriesHandling) {

    List<EventKey> oldKeys = collectOldKeys(eventsToEdit);
    List<Event> newEvents = createUpdatedEvents(eventsToEdit, property, newValue,
        seriesHandling, oldKeys);

    EventSeries oldSeries = eventsToEdit.isEmpty() ? null : eventsToEdit.get(0).getSeries();
    EventSeries newSeries = createNewSeries(newEvents, oldSeries, seriesHandling);

    applyChanges(eventsToEdit, newEvents, oldKeys, oldSeries, newSeries, seriesHandling);
  }

  /**
   * Collects the keys of events to be edited.
   */
  private List<EventKey> collectOldKeys(List<Event> events) {
    List<EventKey> oldKeys = new ArrayList<>();
    for (Event event : events) {
      oldKeys.add(new EventKey(event.getSubject(), event.getStart(), event.getEnd()));
    }
    return oldKeys;
  }

  /**
   * Creates updated versions of events and validates them.
   */
  private List<Event> createUpdatedEvents(List<Event> eventsToEdit, String property,
                                          String newValue, SeriesHandling seriesHandling,
                                          List<EventKey> oldKeys) {
    List<Event> newEvents = new ArrayList<>();
    boolean preserveSeries = (seriesHandling == SeriesHandling.KEEP_SERIES);

    for (Event event : eventsToEdit) {
      Event newEvent = propertyUpdater.updateProperty(event, property, newValue, preserveSeries);

      EventKey newKey = new EventKey(newEvent.getSubject(), newEvent.getStart(),
          newEvent.getEnd());
      validator.validateEventsEdit(newKey, oldKeys);

      newEvents.add(newEvent);
    }

    return newEvents;
  }

  /**
   * Creates a new series if needed based on handling strategy.
   */
  private EventSeries createNewSeries(List<Event> newEvents, EventSeries oldSeries,
                                              SeriesHandling seriesHandling) {
    if (seriesHandling == SeriesHandling.KEEP_SERIES
        || oldSeries == null
        || newEvents.isEmpty()) {
      return null;
    }

    LocalTime newStartTime = newEvents.get(0).getStart().toLocalTime();
    LocalTime newEndTime = newEvents.get(0).getEnd().toLocalTime();

    return EventSeries.builder(
        newEvents.get(0).getSubject(),
        newStartTime,
        newEndTime,
        oldSeries.getRepeatDays()
    ).build();
  }

  /**
   * Applies the changes to calendar and updates series associations.
   */
  private void applyChanges(List<Event> oldEvents, List<Event> newEvents,
                            List<EventKey> oldKeys, EventSeries oldSeries,
                            EventSeries newSeries, SeriesHandling seriesHandling) {
    for (int i = 0; i < oldEvents.size(); i++) {
      Event oldEvent = oldEvents.get(i);
      Event newEvent = newEvents.get(i);

      calendar.removeEvent(oldEvent);

      Event finalEvent = newEvent(oldEvent, newEvent, oldSeries, newSeries,
          seriesHandling);

      calendar.addEvent(finalEvent);
    }
  }

  /**
   * Creates the new event with series association.
   */
  private Event newEvent(Event oldEvent, Event newEvent, EventSeries oldSeries,
                         EventSeries newSeries, SeriesHandling seriesHandling) {
    Event finalEvent;

    if (seriesHandling == SeriesHandling.KEEP_SERIES && oldSeries != null) {
      oldSeries.removeOccurrence(oldEvent);
      finalEvent = newEvent;
      oldSeries.addOccurrence(finalEvent);

    } else if (newSeries != null) {
      if (oldSeries != null) {
        oldSeries.removeOccurrence(oldEvent);
      }

      finalEvent = newEvent.toBuilder()
          .series(newSeries)
          .build();

      newSeries.addOccurrence(finalEvent);

    } else {
      if (oldSeries != null) {
        oldSeries.removeOccurrence(oldEvent);
      }
      finalEvent = newEvent;
    }

    return finalEvent;
  }
}