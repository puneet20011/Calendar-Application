package calendar.model;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Handles timezone conversions for all events in a calendar.
 * When a calendar's timezone changes, all events must be converted
 * to maintain the same absolute time (UTC moment).
 * Package-private utility class for model.
 */
class CalendarTimezoneConverter {

  /**
   * Converts all events in a calendar from old timezone to new timezone.
   * Maintains the same absolute moment in time (same UTC time).
   * Example: Event at 2pm EST (7pm UTC) becomes 11am PST (still 7pm UTC)
   *
   * @param model       the calendar model containing events to convert
   * @param oldTimezone the previous timezone
   * @param newTimezone the new timezone
   */
  void convertAllEvents(IcalendarModel model, ZoneId oldTimezone, ZoneId newTimezone) {
    List<Ievent> allEvents = model.getAllEvents();

    for (Ievent ievent : allEvents) {
      Event event = (Event) ievent;
      convertSingleEvent(model, event, oldTimezone, newTimezone);
    }
  }

  /**
   * Converts a single event's times from old timezone to new timezone.
   * Process:
   * 1. Interpret current times in OLD timezone -> get UTC moment
   * 2. Convert UTC moment to NEW timezone -> get new local times
   * 3. Update event with new local times
   */
  private void convertSingleEvent(IcalendarModel model, Event event,
                                  ZoneId oldTimezone, ZoneId newTimezone) {


    ZonedDateTime oldStart = event.getStart().atZone(oldTimezone);
    ZonedDateTime oldEnd = event.getEnd().atZone(oldTimezone);


    ZonedDateTime newStart = oldStart.withZoneSameInstant(newTimezone);
    ZonedDateTime newEnd = oldEnd.withZoneSameInstant(newTimezone);

    LocalDateTime newLocalStart = newStart.toLocalDateTime();
    LocalDateTime newLocalEnd = newEnd.toLocalDateTime();


    if (!newLocalStart.equals(event.getStart())) {
      String newStartStr = newLocalStart.format(CalConstrains.DATE_TIME_FORMATTER);
      model.editEvent(event.getSubject(), event.getStart(), "start", newStartStr);

      String newEndStr = newLocalEnd.format(CalConstrains.DATE_TIME_FORMATTER);
      model.editEvent(event.getSubject(), newLocalStart, "end", newEndStr);
    }
  }
}