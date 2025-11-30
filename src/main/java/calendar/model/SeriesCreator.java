package calendar.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Set;

/**
 * Creates event series with recurrence patterns.
 * package-private utility class for model.
 */
class SeriesCreator {

  private final EventCalendar calendar;
  private final SystemValidator validator;

  SeriesCreator(EventCalendar calendar, SystemValidator validator) {
    this.calendar = calendar;
    this.validator = validator;
  }

  /**
   * Creates an event series with recurrence pattern.
   */
  void createSeries(String subject, LocalDateTime start, LocalDateTime end,
                    String weekdays, int occurrences, LocalDateTime untilDate) {
    validator.validateTimeOrder(start.toLocalTime(), end.toLocalTime());

    Set<DayOfWeek> repeatDays = parseWeekdays(weekdays);

    EventSeries series = EventSeries.builder(
        subject,
        start.toLocalTime(),
        end.toLocalTime(),
        repeatDays
    ).build();

    createOccurances(series, subject, start, end, repeatDays, occurrences, untilDate);
  }

  /**
   * Generates individual occurrences for the series.
   */
  private void createOccurances(EventSeries series, String subject,
                                         LocalDateTime start, LocalDateTime end,
                                         Set<DayOfWeek> repeatDays, int occurrences,
                                         LocalDateTime untilDate) {
    LocalDate currentDate = start.toLocalDate();
    int count = 0;
    boolean isFirstEvent = true;

    while (true) {
      if (isFirstEvent || repeatDays.contains(currentDate.getDayOfWeek())) {
        LocalDateTime eventStart = LocalDateTime.of(currentDate, start.toLocalTime());
        LocalDateTime eventEnd = LocalDateTime.of(currentDate, end.toLocalTime());

        EventKey key = new EventKey(subject, eventStart, eventEnd);
        validator.validateCreateSeries(key, eventStart);

        Event event = Event.builder(subject, eventStart, eventEnd)
            .series(series)
            .build();

        series.addOccurrence(event);
        calendar.addEvent(event);

        count++;
        isFirstEvent = false;

        if (occurrences > 0 && count >= occurrences) {
          break;
        }
      }

      currentDate = currentDate.plusDays(1);

      if (untilDate != null && currentDate.isAfter(untilDate.toLocalDate())) {
        break;
      }

      if (count > CalConstrains.MAX_SERIES_OCCURRENCES) {
        throw new IllegalArgumentException("Series too long");
      }
    }
  }

  /**
   * Parses weekday string into set of DayOfWeek.
   */
  private Set<DayOfWeek> parseWeekdays(String weekdays) {
    Set<DayOfWeek> days = EnumSet.noneOf(DayOfWeek.class);
    for (char c : weekdays.toUpperCase().toCharArray()) {
      switch (c) {
        case 'M':
          days.add(DayOfWeek.MONDAY);
          break;
        case 'T':
          days.add(DayOfWeek.TUESDAY);
          break;
        case 'W':
          days.add(DayOfWeek.WEDNESDAY);
          break;
        case 'R':
          days.add(DayOfWeek.THURSDAY);
          break;
        case 'F':
          days.add(DayOfWeek.FRIDAY);
          break;
        case 'S':
          days.add(DayOfWeek.SATURDAY);
          break;
        case 'U':
          days.add(DayOfWeek.SUNDAY);
          break;
        default:
      }
    }
    return days;
  }
}