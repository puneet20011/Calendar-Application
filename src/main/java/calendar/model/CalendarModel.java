package calendar.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Main calendar model that orchestrates calendar operations.
 * Uses other utility classes to perform operations.
 */
public class CalendarModel implements IcalendarModel {

  private final EventCalendar calendar;
  private final SystemValidator validator;
  private final EventUpdater propertyUpdater;
  private final SeriesEditor seriesEditor;
  private final SeriesCreator seriesCreator;

  /**
   * Creates a new calendar model with all required components.
   */
  public CalendarModel() {
    this.calendar = new EventCalendar();
    this.validator = new SystemValidator(calendar);
    this.propertyUpdater = new EventUpdater(validator);
    this.seriesEditor = new SeriesEditor(calendar, validator, propertyUpdater);
    this.seriesCreator = new SeriesCreator(calendar, validator);
  }

  @Override
  public void createEvent(String subject, LocalDateTime start, LocalDateTime end) {
    validator.validateTimeOrder(start, end);

    EventKey key = new EventKey(subject, start, end);
    validator.validateUniqueEvent(key);

    Event event = Event.builder(subject, start, end).build();
    calendar.addEvent(event);
  }

  @Override
  public void createEventSeries(String subject, LocalDateTime start, LocalDateTime end,
                                String weekdays, int occurrences, LocalDateTime untilDate) {
    seriesCreator.createSeries(subject, start, end, weekdays, occurrences, untilDate);
  }

  @Override
  public void createAllDayEvent(String subject, LocalDateTime date) {
    LocalDateTime start = LocalDateTime.of(date.toLocalDate(),
        CalConstrains.ALL_DAY_START_TIME);
    LocalDateTime end = LocalDateTime.of(date.toLocalDate(),
        CalConstrains.ALL_DAY_END_TIME);
    createEvent(subject, start, end);
  }

  @Override
  public List<Ievent> getByDate(LocalDate date) {
    return calendar.getEventsByDate(date);
  }

  @Override
  public List<Ievent> getByRange(LocalDateTime start, LocalDateTime end) {
    return calendar.getEventsByRange(start, end);
  }

  @Override
  public List<Ievent> getAllEvents() {
    return calendar.getAllEvents();
  }

  @Override
  public boolean isBusy(LocalDateTime dateTime) {
    return calendar.isBusy(dateTime);
  }

  @Override
  public void editEvent(String subject, LocalDateTime start, String property, String newValue) {
    Event event = findEvent(subject, start);

    if (event.getSeries() != null && !property.equals("start")) {
      validateSeriesPropertyEdit(event, property, newValue);
    }

    Event eventForEditing = event;
    if (event.getSeries() != null && property.equals("start")) {
      eventForEditing = event.toBuilder()
          .series(null)
          .build();
    }

    Event newEvent = propertyUpdater.updateProperty(eventForEditing, property, newValue, true);

    EventKey oldKey = new EventKey(event.getSubject(), event.getStart(), event.getEnd());
    EventKey newKey = new EventKey(newEvent.getSubject(), newEvent.getStart(), newEvent.getEnd());

    validator.validateEditNoDuplicate(newKey, oldKey);

    calendar.removeEvent(event);

    if (event.getSeries() != null) {
      event.getSeries().removeOccurrence(event);

      if (!property.equals("start")) {
        event.getSeries().addOccurrence(newEvent);
      }
    }

    calendar.addEvent(newEvent);
  }

  @Override
  public void editEvents(String subject, LocalDateTime start, String property, String newValue) {
    Event event = findEvent(subject, start);

    if (event.getSeries() == null) {
      editEvent(subject, start, property, newValue);
      return;
    }

    validateSeriesPropertyEdit(event, property, newValue);

    List<Event> eventsToEdit = event.getSeries().getOccurrencesFromDate(start.toLocalDate());
    boolean isStartChange = property.equals("start");

    seriesEditor.editMultiple(eventsToEdit, property, newValue, isStartChange);
  }

  @Override
  public void editSeries(String subject, LocalDateTime start, String property, String newValue) {
    Event event = findEvent(subject, start);

    if (event.getSeries() == null) {
      editEvent(subject, start, property, newValue);
      return;
    }

    validateSeriesPropertyEdit(event, property, newValue);

    List<Event> eventsToEdit = event.getSeries().getAllOccurrences();
    boolean isStartChange = property.equals("start");

    seriesEditor.editAll(eventsToEdit, property, newValue, isStartChange);
  }

  /**
   * Finds a specific event.
   */
  private Event findEvent(String subject, LocalDateTime start) {
    Event event = calendar.findEvent(subject, start);
    if (event == null) {
      throw new IllegalArgumentException("Event not found");
    }
    return event;
  }

  /**
   * Validates series property edits based on property.
   */
  private void validateSeriesPropertyEdit(Event event, String property, String newValue) {
    if (property.equals("start")) {
      LocalDateTime parsedStart = LocalDateTime.parse(newValue,
          CalConstrains.DATE_TIME_FORMATTER);
      validator.validateSeriesTimeEdit(event, parsedStart);
    }
  }
}