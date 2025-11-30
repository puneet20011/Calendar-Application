package calendar.controller;

import calendar.model.Ievent;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Exports calendar events to CSV format compatible with Google Calendar.
 * Package-private utility class for controller.
 */
class CsvExporter {

  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("MM/dd/yyyy");
  private static final DateTimeFormatter TIME_FORMATTER =
      DateTimeFormatter.ofPattern("hh:mm a", Locale.US);
  private static final LocalTime ALL_DAY_START_TIME = LocalTime.of(8, 0);
  private static final LocalTime ALL_DAY_END_TIME = LocalTime.of(17, 0);

  /**
   * Exports all events to a CSV file.
   *
   * @param events the list of events to export
   * @param filename the name of the file to create
   * @return the absolute path of the created file
   */
  String exportToCsv(List<Ievent> events, String filename) {
    try {
      Path filePath = Paths.get(filename);

      try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
        writeHeader(writer);

        for (Ievent event : events) {
          writeEvent(writer, event);
        }
      }

      return filePath.toAbsolutePath().toString();

    } catch (IOException e) {
      throw new RuntimeException("Failed to export calendar: " + e.getMessage());
    }
  }

  /**
   * Writes CSV header row.
   */
  private void writeHeader(BufferedWriter writer) throws IOException {
    writer.write("Subject,Start Date,Start Time,End Date,End Time,"
        + "All Day Event,Description,Location,Private");
    writer.newLine();
  }

  /**
   * Writes a single events to CSV.
   */
  private void writeEvent(BufferedWriter writer, Ievent event) throws IOException {
    LocalDate startDate = event.getStart().toLocalDate();
    LocalTime startTime = event.getStart().toLocalTime();
    LocalDate endDate = event.getEnd().toLocalDate();
    LocalTime endTime = event.getEnd().toLocalTime();

    boolean isAllDay = isAllDayEvent(startTime, endTime, startDate, endDate);

    String subject = escapeCsv(event.getSubject());
    String startDateStr = startDate.format(DATE_FORMATTER);
    String startTimeStr = startTime.format(TIME_FORMATTER);
    String endDateStr = endDate.format(DATE_FORMATTER);
    String endTimeStr = endTime.format(TIME_FORMATTER);
    String allDayStr = isAllDay ? "True" : "False";

    String description = formatOptionalField(event.getDescription());
    String location = formatOptionalField(event.getLocation());
    String privateStr = "private".equalsIgnoreCase(event.getStatus()) ? "True" : "False";

    writer.write(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s",
        subject, startDateStr, startTimeStr, endDateStr, endTimeStr,
        allDayStr, description, location, privateStr));
    writer.newLine();
  }

  /**
   * Determines if an event is an all-day event.
   */
  private boolean isAllDayEvent(LocalTime startTime, LocalTime endTime,
                                LocalDate startDate, LocalDate endDate) {
    return startTime.equals(ALL_DAY_START_TIME)
        && endTime.equals(ALL_DAY_END_TIME)
        && startDate.equals(endDate);
  }

  /**
   * Formats an optional field for CSV output.
   */
  private String formatOptionalField(String value) {
    if (value == null || value.isEmpty()) {
      return "";
    }
    return escapeCsv(value);
  }

  /**
   * Handles the escape sequences.
   * Specially for comma in field vaules.
   */
  private String escapeCsv(String value) {
    if (value == null || value.isEmpty()) {
      return "";
    }

    boolean needsQuoting = value.contains(",")
        || value.contains("\"")
        || value.contains("\n")
        || value.contains("\r");

    if (needsQuoting) {
      String escaped = value.replace("\"", "\"\"");
      return "\"" + escaped + "\"";
    }

    return value;
  }
}
