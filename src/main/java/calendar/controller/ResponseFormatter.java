package calendar.controller;

import calendar.model.Ievent;
import java.time.format.DateTimeFormatter;


/**
 * Formats events for display.
 * Package-private utility class for controller use only.
 */
class ResponseFormatter {

  private static final DateTimeFormatter DISPLAY_DATE_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final DateTimeFormatter DISPLAY_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("HH:mm");

  /**
   * Private constructor to prevent instantiation.
   * This is a utility class with only static methods.
   */
  private ResponseFormatter() {
    throw new AssertionError("Utility class should not be instantiated");
  }

  /**
   * Formats a single event into a display string.
   *
   * @param event the event to format
   * @return formatted event string
   */
  static String formatEvent(Ievent event) {
    String startDate = event.getStart().format(DISPLAY_DATE_FORMATTER);
    String startTime = event.getStart().format(DISPLAY_TIME_FORMATTER);
    String endDate = event.getEnd().format(DISPLAY_DATE_FORMATTER);
    String endTime = event.getEnd().format(DISPLAY_TIME_FORMATTER);

    StringBuilder output = new StringBuilder();
    output.append("* ").append(event.getSubject())
        .append(" starting on ").append(startDate)
        .append(" at ").append(startTime)
        .append(", ending on ").append(endDate)
        .append(" at ").append(endTime);

    if (event.getLocation() != null && !event.getLocation().isEmpty()) {
      output.append(", location: ").append(event.getLocation());
    }

    if (event.getDescription() != null && !event.getDescription().isEmpty()) {
      output.append(", description: ").append(event.getDescription());
    }

    if (event.getStatus() != null && !event.getStatus().equals("public")) {
      output.append(", status: ").append(event.getStatus());
    }

    return output.toString();
  }
}
