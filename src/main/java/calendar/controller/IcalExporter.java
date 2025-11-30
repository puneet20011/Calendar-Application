package calendar.controller;

import calendar.model.Ievent;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Exports calendar events to iCalendar (iCal/ics) format per RFC 5545.
 * Package-private utility class for controller.
 */
class IcalExporter {

  private static final DateTimeFormatter ICAL_DATETIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

  private final List<Ievent> events;
  private final ZoneId timezone;

  IcalExporter(List<Ievent> events, String timezoneId) {
    this.events = events;
    this.timezone = ZoneId.of(timezoneId);
  }

  /**
   * Exports all events to an iCal file.
   *
   * @param filename the name of the file to create
   * @return the absolute path of the created file
   */
  String exportToIcal(String filename) {
    try {
      Path filePath = Paths.get(filename);

      try (BufferedWriter writer = Files.newBufferedWriter(filePath,
          StandardCharsets.UTF_8)) {
        writeCalendarHeader(writer);

        for (Ievent event : events) {
          writeEvent(writer, event);
        }

        writeCalendarFooter(writer);
      }

      return filePath.toAbsolutePath().toString();

    } catch (IOException e) {
      throw new RuntimeException("Failed to export calendar: " + e.getMessage());
    }
  }

  /**
   * Writes iCal header.
   */
  private void writeCalendarHeader(BufferedWriter writer) throws IOException {
    writer.write("BEGIN:VCALENDAR\r\n");
    writer.write("VERSION:2.0\r\n");
    writer.write("PRODID:-//Calendar Application//EN\r\n");
    writer.write("CALSCALE:GREGORIAN\r\n");
    writer.write("METHOD:PUBLISH\r\n");

    writer.write("BEGIN:VTIMEZONE\r\n");
    writer.write("TZID:" + timezone.getId() + "\r\n");
    writer.write("END:VTIMEZONE\r\n");
  }

  /**
   * Writes iCal footer.
   */
  private void writeCalendarFooter(BufferedWriter writer) throws IOException {
    writer.write("END:VCALENDAR\r\n");
  }

  /**
   * Writes a single event in iCal format.
   */
  private void writeEvent(BufferedWriter writer, Ievent event) throws IOException {
    writer.write("BEGIN:VEVENT\r\n");

    String uid = generateUid(event);
    writer.write("UID:" + uid + "\r\n");

    String dtstamp = ZonedDateTime.now(timezone)
        .format(ICAL_DATETIME_FORMATTER);
    writer.write("DTSTAMP:" + dtstamp + "Z\r\n");

    String dtstart = formatDateTime(event.getStart());
    writer.write("DTSTART;TZID=" + timezone.getId() + ":" + dtstart + "\r\n");

    String dtend = formatDateTime(event.getEnd());
    writer.write("DTEND;TZID=" + timezone.getId() + ":" + dtend + "\r\n");

    writer.write("SUMMARY:" + escapeIcalText(event.getSubject()) + "\r\n");

    if (event.getDescription() != null && !event.getDescription().isEmpty()) {
      writer.write("DESCRIPTION:" + escapeIcalText(event.getDescription()) + "\r\n");
    }

    if (event.getLocation() != null && !event.getLocation().isEmpty()) {
      writer.write("LOCATION:" + escapeIcalText(event.getLocation()) + "\r\n");
    }

    String classification = "private".equalsIgnoreCase(event.getStatus())
        ? "PRIVATE" : "PUBLIC";
    writer.write("CLASS:" + classification + "\r\n");

    writer.write("STATUS:CONFIRMED\r\n");

    writer.write("TRANSP:OPAQUE\r\n");

    writer.write("END:VEVENT\r\n");
  }

  /**
   * Formats LocalDateTime to iCal format.
   */
  private String formatDateTime(LocalDateTime dateTime) {
    return dateTime.format(ICAL_DATETIME_FORMATTER);
  }

  /**
   * Generates a unique identifier for an event.
   */
  private String generateUid(Ievent event) {
    String base = event.getSubject() + event.getStart().toString()
        + event.getEnd().toString();
    String uuid = UUID.nameUUIDFromBytes(base.getBytes()).toString();
    return uuid + "@calendar.app";
  }

  /**
   * Escapes special characters for iCal text fields per RFC 5545.
   * Special characters: backslash, semicolon, comma, newline
   */
  private String escapeIcalText(String text) {
    if (text == null || text.isEmpty()) {
      return "";
    }

    return text
        .replace("\\", "\\\\")
        .replace(";", "\\;")
        .replace(",", "\\,")
        .replace("\n", "\\n")
        .replace("\r", "");
  }
}