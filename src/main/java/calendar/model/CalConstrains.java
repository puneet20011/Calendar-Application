package calendar.model;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Constants used throughout the calendar application.
 * private utility class for model holding constrains.
 */
class CalConstrains {

  static final LocalTime ALL_DAY_START_TIME = LocalTime.of(8, 0);
  static final LocalTime ALL_DAY_END_TIME = LocalTime.of(17, 0);

  static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

  static final int MAX_SERIES_OCCURRENCES = 365;

  private CalConstrains() {
    throw new AssertionError("Constants class should not be instantiated");
  }
}
