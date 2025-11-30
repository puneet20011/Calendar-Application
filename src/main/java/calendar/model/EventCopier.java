package calendar.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Handles copying events between calendars with timezone conversion.
 * Package-private utility class for system model.
 */
class EventCopier {

  private final SystemValidator validator;

  EventCopier() {
    this.validator = new SystemValidator(null);
  }

  /**
   * Copies a single event to target calendar at specified date/time.
   */
  void copySingleEvent(CalendarContext sourceContext, CalendarContext targetContext,
                       String eventSubject, LocalDateTime eventStart,
                       LocalDateTime targetStart) {

    IcalendarModel sourceModel = sourceContext.getModel();
    IcalendarModel targetModel = targetContext.getModel();

    List<Event> matchingEvents = findMatchingEvents(sourceModel, eventSubject, eventStart);

    int copiedCount = 0;

    for (Event sourceEvent : matchingEvents) {
      long durationMinutes = ChronoUnit.MINUTES.between(
          sourceEvent.getStart(), sourceEvent.getEnd());
      LocalDateTime targetEnd = targetStart.plusMinutes(durationMinutes);

      try {
        targetModel.createEvent(eventSubject, targetStart, targetEnd);
        copyOptionalProperties(targetModel, sourceEvent, targetStart);
        copiedCount++;
      } catch (IllegalArgumentException e) {
        //pass
      }
    }

    validator.validateCopySuccessful(copiedCount);
  }

  /**
   * Copies all events on a specific date to target calendar.
   * Times remain the same but are interpreted in target timezone.
   */
  void copyEventsOnDate(CalendarContext sourceContext, CalendarContext targetContext,
                        LocalDate sourceDate, LocalDate targetDate) {

    IcalendarModel sourceModel = sourceContext.getModel();
    IcalendarModel targetModel = targetContext.getModel();

    List<Ievent> eventsOnDate = sourceModel.getByDate(sourceDate);

    for (Ievent ievent : eventsOnDate) {
      Event event = (Event) ievent;
      copyEventTimeZoneChange(
          event, targetModel, sourceContext, targetContext, targetDate);
    }
  }

  /**
   * Copies all events in a date range to target calendar starting at target date.
   * Maintains relative offsets. For series events, only copies overlapping occurrences.
   */
  void copyEventsInRange(CalendarContext sourceContext, CalendarContext targetContext,
                         LocalDate sourceStart, LocalDate sourceEnd,
                         LocalDate targetStartDate) {

    IcalendarModel sourceModel = sourceContext.getModel();

    LocalDateTime rangeStart = sourceStart.atStartOfDay();
    LocalDateTime rangeEnd = sourceEnd.atTime(23, 59, 59);

    List<Ievent> eventsInRange = sourceModel.getByRange(rangeStart, rangeEnd);
    long dayOffset = ChronoUnit.DAYS.between(sourceStart, targetStartDate);

    Set<EventSeries> processedSeries = new HashSet<>();

    for (Ievent ievent : eventsInRange) {
      Event event = (Event) ievent;
      processEventForRangeCopy(event, sourceContext, targetContext,
          rangeStart, rangeEnd, dayOffset, processedSeries);
    }
  }


  /**
   * Finds all source events matching the subject and start time.
   * Returns a list of all matching events (handles ambiguous cases).
   */
  private List<Event> findMatchingEvents(IcalendarModel sourceModel, String eventSubject,
                                         LocalDateTime eventStart) {
    List<Ievent> eventsOnDate = sourceModel.getByDate(eventStart.toLocalDate());
    List<Event> matchingEvents = new ArrayList<>();

    for (Ievent ievent : eventsOnDate) {
      Event event = (Event) ievent;
      if (event.getSubject().equals(eventSubject)
          && event.getStart().equals(eventStart)) {
        matchingEvents.add(event);
      }
    }

    validator.validateEventExistsForCopy(matchingEvents, eventSubject, eventStart);

    return matchingEvents;
  }

  /**
   * Copies a single event with timezone conversion.
   */
  private void copyEventTimeZoneChange(Event event, IcalendarModel targetModel,
                                               CalendarContext sourceContext,
                                               CalendarContext targetContext,
                                               LocalDate targetDate) {
    LocalDateTime targetStart = convertTimezone(
        event.getStart(),
        sourceContext.getTimezone(),
        targetContext.getTimezone(),
        targetDate
    );

    long durationMinutes = ChronoUnit.MINUTES.between(
        event.getStart(),
        event.getEnd()
    );

    LocalDateTime targetEnd = targetStart.plusMinutes(durationMinutes);

    try {
      targetModel.createEvent(event.getSubject(), targetStart, targetEnd);
      copyOptionalProperties(targetModel, event, targetStart);
    } catch (IllegalArgumentException e) {
      // Skip duplicates
    }
  }

  /**
   * Processes a single event for range copy operation.
   */
  private void processEventForRangeCopy(Event event, CalendarContext sourceContext,
                                        CalendarContext targetContext,
                                        LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                        long dayOffset, Set<EventSeries> processedSeries) {
    EventSeries series = event.getSeries();

    if (series != null && !processedSeries.contains(series)) {
      copySeriesInRange(sourceContext, targetContext, series,
          rangeStart, rangeEnd, dayOffset);
      processedSeries.add(series);
    } else if (series == null) {
      copyEventSingleoffset(event, sourceContext, targetContext, dayOffset);
    }
  }

  /**
   * Copies a standalone event with date offset and timezone conversion.
   */
  private void copyEventSingleoffset(Event event, CalendarContext sourceContext,
                                             CalendarContext targetContext, long dayOffset) {
    IcalendarModel targetModel = targetContext.getModel();

    LocalDateTime offsetStart = event.getStart().plusDays(dayOffset);
    LocalDateTime offsetEnd = event.getEnd().plusDays(dayOffset);

    LocalDateTime targetStart = convertTimezone(
        event.getStart(),
        sourceContext.getTimezone(),
        targetContext.getTimezone(),
        offsetStart.toLocalDate()
    );

    LocalDateTime targetEnd = convertTimezone(
        event.getEnd(),
        sourceContext.getTimezone(),
        targetContext.getTimezone(),
        offsetEnd.toLocalDate()
    );

    try {
      targetModel.createEvent(event.getSubject(), targetStart, targetEnd);
      copyOptionalProperties(targetModel, event, targetStart);
    } catch (IllegalArgumentException e) {
      // Skip duplicates
    }
  }

  /**
   * Converts a LocalDateTime from source timezone to target timezone.
   */
  private LocalDateTime convertTimezone(LocalDateTime sourceTime,
                                        ZoneId sourceZone,
                                        ZoneId targetZone,
                                        LocalDate targetDate) {

    ZonedDateTime sourceZoned = sourceTime.atZone(sourceZone);
    ZonedDateTime targetZoned = sourceZoned.withZoneSameInstant(targetZone);

    LocalDateTime convertedTime = targetZoned.toLocalDateTime();

    long dayDiff = ChronoUnit.DAYS.between(sourceTime.toLocalDate(), convertedTime.toLocalDate());

    LocalDate adjustedDate = targetDate.plusDays(dayDiff);

    return LocalDateTime.of(adjustedDate, convertedTime.toLocalTime());
  }

  /**
   * Copies occurrences of a series that overlap with the specified range.
   */
  private void copySeriesInRange(CalendarContext sourceContext,
                                 CalendarContext targetContext,
                                 EventSeries series, LocalDateTime rangeStart,
                                 LocalDateTime rangeEnd, long dayOffset) {

    List<Event> occurrencesInRange = findOccurrencesInRange(series, rangeStart, rangeEnd);

    if (occurrencesInRange.isEmpty()) {
      return;
    }

    boolean success = createSeriesnew(
        occurrencesInRange, sourceContext, targetContext, series, dayOffset);

    if (!success) {
      copySeriesIndividaul(
          occurrencesInRange, sourceContext, targetContext, dayOffset);
    }
  }

  /**
   * Finds all occurrences of a series that fall within the specified range.
   */
  private List<Event> findOccurrencesInRange(EventSeries series,
                                             LocalDateTime rangeStart,
                                             LocalDateTime rangeEnd) {
    List<Event> allOccurrences = series.getAllOccurrences();
    List<Event> occurrencesInRange = new ArrayList<>();

    for (Event occurrence : allOccurrences) {
      if (!occurrence.getEnd().isBefore(rangeStart)
          && !occurrence.getStart().isAfter(rangeEnd)) {
        occurrencesInRange.add(occurrence);
      }
    }

    return occurrencesInRange;
  }

  /**
   * Attempts to create a series in the target calendar.
   * Returns true if successful, false otherwise.
   */
  private boolean createSeriesnew(List<Event> occurrencesInRange,
                                          CalendarContext sourceContext,
                                          CalendarContext targetContext,
                                          EventSeries series, long dayOffset) {
    IcalendarModel targetModel = targetContext.getModel();
    Event firstOccurrence = occurrencesInRange.get(0);

    LocalDateTime targetStart = targetStartTime(
        firstOccurrence, sourceContext, targetContext, dayOffset);

    LocalDateTime targetEnd = targetEndTime(
        firstOccurrence, sourceContext, targetContext, dayOffset);

    validator.validateSeriesSingleDay(targetStart, targetEnd,
        firstOccurrence.getSubject());

    String weekdays = buildWeekdaysString(series);

    try {
      targetModel.createEventSeries(
          firstOccurrence.getSubject(),
          targetStart,
          targetEnd,
          weekdays,
          occurrencesInRange.size(),
          null
      );

      copyOptionalProperties(targetModel, firstOccurrence, targetStart);
      return true;

    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  /**
   * Copies series occurrences individually when series creation fails.
   */
  private void copySeriesIndividaul(List<Event> occurrencesInRange,
                                                 CalendarContext sourceContext,
                                                 CalendarContext targetContext,
                                                 long dayOffset) {
    IcalendarModel targetModel = targetContext.getModel();

    for (Event occurrence : occurrencesInRange) {
      LocalDateTime targetStart = targetStartTime(
          occurrence, sourceContext, targetContext, dayOffset);

      LocalDateTime targetEnd = targetEndTime(
          occurrence, sourceContext, targetContext, dayOffset);

      validator.validateSeriesSingleDay(targetStart, targetEnd,
          occurrence.getSubject());

      try {
        targetModel.createEvent(occurrence.getSubject(), targetStart, targetEnd);
        copyOptionalProperties(targetModel, occurrence, targetStart);
      } catch (IllegalArgumentException ignored) {
        // Skip duplicates
      }
    }
  }

  /**
   * Calculates the target start time with offset and timezone conversion.
   */
  private LocalDateTime targetStartTime(Event event,
                                                 CalendarContext sourceContext,
                                                 CalendarContext targetContext,
                                                 long dayOffset) {
    LocalDateTime offsetStart = event.getStart().plusDays(dayOffset);

    return convertTimezone(
        event.getStart(),
        sourceContext.getTimezone(),
        targetContext.getTimezone(),
        offsetStart.toLocalDate()
    );
  }

  /**
   * Calculates the target end time with offset and timezone conversion.
   */
  private LocalDateTime targetEndTime(Event event,
                                               CalendarContext sourceContext,
                                               CalendarContext targetContext,
                                               long dayOffset) {
    LocalDateTime offsetEnd = event.getEnd().plusDays(dayOffset);

    return convertTimezone(
        event.getEnd(),
        sourceContext.getTimezone(),
        targetContext.getTimezone(),
        offsetEnd.toLocalDate()
    );
  }

  /**
   * Builds a weekdays string from a series' repeat days.
   */
  private String buildWeekdaysString(EventSeries series) {
    StringBuilder weekdays = new StringBuilder();
    for (java.time.DayOfWeek day : series.getRepeatDays()) {
      weekdays.append(daysToChar(day));
    }
    return weekdays.toString();
  }


  private void copyOptionalProperties(IcalendarModel targetModel, Event sourceEvent,
                                      LocalDateTime targetStart) {
    try {
      if (sourceEvent.getDescription() != null
          && !sourceEvent.getDescription().isEmpty()) {
        targetModel.editEvent(sourceEvent.getSubject(), targetStart,
            "description", sourceEvent.getDescription());
      }

      if (sourceEvent.getLocation() != null
          && !sourceEvent.getLocation().isEmpty()) {
        targetModel.editEvent(sourceEvent.getSubject(), targetStart,
            "location", sourceEvent.getLocation());
      }

      if (!sourceEvent.getStatus().equals("public")) {
        targetModel.editEvent(sourceEvent.getSubject(), targetStart,
            "status", sourceEvent.getStatus());
      }
    } catch (Exception e) {
      // Properties are optional, event still created
    }
  }


  private char daysToChar(java.time.DayOfWeek day) {
    switch (day) {
      case MONDAY: return 'M';
      case TUESDAY: return 'T';
      case WEDNESDAY: return 'W';
      case THURSDAY: return 'R';
      case FRIDAY: return 'F';
      case SATURDAY: return 'S';
      case SUNDAY: return 'U';
      default: return 'M';
    }
  }
}