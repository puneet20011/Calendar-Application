package calendar.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

/**
 * Validates all system operations including events and calendar management.
 * Checks for duplication conflicts and throws exceptions.
 * Package-private utility class for model.
 */
class SystemValidator {

  private final EventCalendar calendar;
  private Map<String, CalendarContext> calendars;

  SystemValidator(EventCalendar calendar) {
    this.calendar = calendar;
    this.calendars = null;
  }

  /**
   * Sets the calendars map for calendar management validation.
   * Called by CalendarSystem after construction.
   *
   * @param calendars the calendars map
   */
  void setCalendars(Map<String, CalendarContext> calendars) {
    this.calendars = calendars;
  }

  /**
   * Validates that start time is before end time.
   */
  void validateTimeOrder(LocalDateTime start, LocalDateTime end) {
    if (start.isAfter(end) || start.equals(end)) {
      throw new IllegalArgumentException("Event start time must be before end time");
    }
  }

  /**
   * Validates that start time is before end time (for times only).
   */
  void validateTimeOrder(LocalTime start, LocalTime end) {
    if (start.isAfter(end) || start.equals(end)) {
      throw new IllegalArgumentException("Event start time must be before end time");
    }
  }

  /**
   * Validates that an event key doesn't already exist.
   */
  void validateUniqueEvent(EventKey key) {
    if (calendar.containsKey(key)) {
      throw new IllegalArgumentException(
          "Event with same subject, start, and end time already exists");
    }
  }

  /**
   * Validates that editing won't create a duplicate.
   */
  void validateEditNoDuplicate(EventKey newKey, EventKey oldKey) {
    if (calendar.containsKey(newKey) && !oldKey.equals(newKey)) {
      throw new IllegalArgumentException("Edit would create duplicate event");
    }
  }

  /**
   * Validates that editing a list of events won't create duplicates.
   */
  void validateEventsEdit(EventKey newKey, List<EventKey> oldKeys) {
    if (calendar.containsKey(newKey) && !oldKeys.contains(newKey)) {
      throw new IllegalArgumentException("Edit would create duplicate event");
    }
  }

  /**
   * Validates series edit constraints (date changes not allowed for series time changes).
   */
  void validateSeriesTimeEdit(Event event, LocalDateTime newStart) {
    if (!newStart.toLocalDate().equals(event.getStart().toLocalDate())) {
      throw new IllegalArgumentException(
          "Cannot change the date for series events. "
              + "Series events repeat based on their pattern - only the time component can be "
              + "edited. "
              + "To move an event to a different date, first use 'edit event start' to remove it "
              + "from the series.");
    }

    LocalTime newStartTime = newStart.toLocalTime();
    LocalDateTime originalDate =
        LocalDateTime.of(event.getStart().toLocalDate(), newStartTime);

    LocalDateTime newEnd;
    if (originalDate.isBefore(event.getEnd())) {
      newEnd = event.getEnd();
    } else {
      Duration duration = Duration.between(event.getStart(), event.getEnd());
      newEnd = originalDate.plus(duration);
    }

    if (!originalDate.toLocalDate().equals(newEnd.toLocalDate())) {
      throw new IllegalArgumentException(
          "Cannot edit: Series events must span a single day. "
              + "New start time would create an event ending on a different day. "
              + "To create a multi-day event, first use 'edit event start' to remove it from the "
              + "series, then edit the end time.");
    }
  }

  /**
   * Validates that a series doesn't create duplicate events.
   */
  void validateCreateSeries(EventKey key, LocalDateTime date) {
    if (calendar.containsKey(key)) {
      throw new IllegalArgumentException(
          "Series would create duplicate event on " + date.toLocalDate());
    }
  }

  /**
   * Validates calendar name is not null or empty.
   */
  void validateCalendarName(String name) {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Calendar name cannot be empty");
    }
  }

  /**
   * Validates that a calendar with the given name doesn't already exist.
   *
   * @param name the calendar name to check
   * @throws IllegalArgumentException if calendar already exists
   */
  void validateCalendarDoesNotExist(String name) {
    if (calendars != null && calendars.containsKey(name)) {
      throw new IllegalArgumentException(
          "Calendar with name '" + name + "' already exists");
    }
  }

  /**
   * Validates that a calendar with the given name exists.
   */
  void validateCalendarExists(String name) {
    if (calendars != null && !calendars.containsKey(name)) {
      throw new IllegalArgumentException("Calendar '" + name + "' not found");
    }
  }

  /**
   * Validates and parses timezone string.
   */
  ZoneId validateAndParseTimezone(String timezone) {
    try {
      return ZoneId.of(timezone);
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid timezone: " + timezone);
    }
  }

  /**
   * Validates that a calendar is currently in use.
   */
  void validateCalendarInUse(CalendarContext currentCalendar) {
    if (currentCalendar == null) {
      throw new IllegalStateException(
          "No calendar in use. Use 'use calendar --name <n>' or "
              + "'create calendar --name <n> --timezone <tz>' first");
    }
  }

  /**
   * Validates calendar property name in case of change.
   */
  void validateCalendarProperty(String property) {
    String lowerProperty = property.toLowerCase();
    if (!lowerProperty.equals("name") && !lowerProperty.equals("timezone")) {
      throw new IllegalArgumentException(
          "Invalid property: " + property + ". Valid properties: name, timezone");
    }
  }

  /**
   * Validates that matching events exist for copying.
   */
  void validateEventExistsForCopy(List<Event> matchingEvents, String eventSubject,
                                  LocalDateTime eventStart) {
    if (matchingEvents.isEmpty()) {
      throw new IllegalArgumentException(
          "Event not found: " + eventSubject + " at " + eventStart);
    }
  }

  /**
   * Validates that at least one event was successfully copied.
   */
  void validateCopySuccessful(int copiedCount) {
    if (copiedCount == 0) {
      throw new IllegalArgumentException(
          "Cannot copy event: All matching events already exist in the target calendar");
    }
  }

  /**
   * Validates that a copied series event maintains single-day constraint.
   * Series events must not span multiple days even after timezone conversion.
   */
  void validateSeriesSingleDay(LocalDateTime targetStart, LocalDateTime targetEnd,
                               String eventSubject) {
    if (!targetStart.toLocalDate().equals(targetEnd.toLocalDate())) {
      throw new IllegalArgumentException(
          "Cannot copy series event '"
              + eventSubject
              + "': "
              + "Timezone conversion would create a multi-day event "
              + "(starts " + targetStart.toLocalDate() + ", ends " + targetEnd.toLocalDate() + "). "
              + "Series events must span only a single day.");
    }
  }
}