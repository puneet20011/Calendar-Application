package calendar.controller;

import calendar.model.CalendarSystemModel;
import calendar.model.Ievent;
import calendar.view.IcalendarView;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;

/**
 * Executes parsed commands by delegating to the model and view.
 * Pattern matching is handled by CommandPatterns class.
 * Package-private utility class for controller use only.
 */
class CommandExecutor {

  private final CalendarSystemModel model;
  private final IcalendarView view;

  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd");

  private final List<CommandPatterns.CommandPattern> patterns;

  CommandExecutor(CalendarSystemModel model, IcalendarView view) {
    this.model = model;
    this.view = view;
    this.patterns = CommandPatterns.getAllPatterns();
  }

  /**
   * Preprocesses command by removing quotes and normalizing whitespace.
   */
  String preprocessCommand(String command) {
    command = command.replaceAll("\"([^\"]+)\"", "$1");
    command = command.replaceAll("\\s+", " ");
    return command.trim();
  }

  /**
   * Parses and executes a command.
   * Returns true if command was successfully parsed and executed.
   */
  boolean parseAndExecute(String command) {
    command = preprocessCommand(command);

    for (CommandPatterns.CommandPattern commandPattern : patterns) {
      Matcher matcher = commandPattern.getPattern().matcher(command);
      if (matcher.matches()) {
        executeCommand(commandPattern.getName(), matcher);
        return true;
      }
    }

    return false;
  }

  /**
   * Executes the command based on the pattern name and matched groups.
   */
  private void executeCommand(String commandName, Matcher matcher) {
    switch (commandName) {
      case "CREATE_CALENDAR":
        handleCreateCalendar(matcher);
        break;
      case "USE_CALENDAR":
        handleUseCalendar(matcher);
        break;
      case "EDIT_CALENDAR":
        handleEditCalendar(matcher);
        break;
      case "COPY_EVENT":
        handleCopyEvent(matcher);
        break;
      case "COPY_EVENTS_ON_DATE":
        handleCopyEventsOnDate(matcher);
        break;
      case "COPY_EVENTS_RANGE":
        handleCopyEventsRange(matcher);
        break;
      case "CREATE_EVENT":
        handleCreateEvent(matcher);
        break;
      case "CREATE_EVENT_SERIES_COUNT":
        handleCreateEventSeries(matcher);
        break;
      case "CREATE_EVENT_SERIES_UNTIL":
        handleCreateEventSeriesUntil(matcher);
        break;
      case "CREATE_ALL_DAY_EVENT":
        handleCreateAllDayEvent(matcher);
        break;
      case "CREATE_ALL_DAY_SERIES_COUNT":
        handleCreateAllDaySeriesCount(matcher);
        break;
      case "CREATE_ALL_DAY_SERIES_UNTIL":
        handleCreateAllDaySeriesUntil(matcher);
        break;
      case "EDIT_EVENT":
        handleEditEvent(matcher);
        break;
      case "EDIT_EVENTS":
        handleEditEvents(matcher);
        break;
      case "EDIT_SERIES":
        handleEditSeries(matcher);
        break;
      case "PRINT_EVENTS_ON_DATE":
        handlePrintEventsOnDate(matcher);
        break;
      case "PRINT_EVENTS_IN_RANGE":
        handlePrintEventsInRange(matcher);
        break;
      case "SHOW_STATUS":
        handleShowStatus(matcher);
        break;
      case "EXPORT":
        handleExport(matcher);
        break;
      default:
        view.displayError("Unknown command type: " + commandName);
    }
  }

  private void handleCreateCalendar(Matcher matcher) {
    String name = matcher.group(1).trim();
    String timezone = matcher.group(2).trim();

    model.createCalendar(name, timezone);
    view.displayMessage("Calendar created: " + name);
  }

  private void handleUseCalendar(Matcher matcher) {
    String name = matcher.group(1).trim();

    model.useCalendar(name);
    view.displayMessage("Now using calendar: " + name);
  }

  private void handleEditCalendar(Matcher matcher) {
    String calendarName = matcher.group(1).trim();
    String property = matcher.group(2).trim();
    String newValue = matcher.group(3).trim();

    model.editCalendar(calendarName, property, newValue);
    view.displayMessage("Calendar property edited successfully");
  }

  private void handleCopyEvent(Matcher matcher) {
    String eventName = matcher.group(1).trim();
    LocalDateTime eventStart = LocalDateTime.parse(matcher.group(2), DATE_TIME_FORMATTER);
    String targetCalendar = matcher.group(3).trim();
    LocalDateTime targetStart = LocalDateTime.parse(matcher.group(4), DATE_TIME_FORMATTER);

    model.copyEvent(eventName, eventStart, targetCalendar, targetStart);
    view.displayMessage("Event copied successfully");
  }

  private void handleCopyEventsOnDate(Matcher matcher) {
    LocalDate sourceDate = LocalDate.parse(matcher.group(1), DATE_FORMATTER);
    String targetCalendar = matcher.group(2).trim();
    LocalDate targetDate = LocalDate.parse(matcher.group(3), DATE_FORMATTER);

    model.copyEventsOnDate(sourceDate, targetCalendar, targetDate);
    view.displayMessage("Events copied successfully");
  }

  private void handleCopyEventsRange(Matcher matcher) {
    LocalDate startDate = LocalDate.parse(matcher.group(1), DATE_FORMATTER);
    LocalDate endDate = LocalDate.parse(matcher.group(2), DATE_FORMATTER);
    String targetCalendar = matcher.group(3).trim();
    LocalDate targetStartDate = LocalDate.parse(matcher.group(4), DATE_FORMATTER);

    model.copyEventsInRange(startDate, endDate, targetCalendar, targetStartDate);
    view.displayMessage("Events copied successfully");
  }


  private void handleCreateEvent(Matcher matcher) {
    String subject = matcher.group(1).trim();
    LocalDateTime start = LocalDateTime.parse(matcher.group(2), DATE_TIME_FORMATTER);
    LocalDateTime end = LocalDateTime.parse(matcher.group(3), DATE_TIME_FORMATTER);

    model.createEvent(subject, start, end);
    view.displayMessage("Event created: " + subject);
  }

  private void handleCreateEventSeries(Matcher matcher) {
    String subject = matcher.group(1).trim();
    LocalDateTime start = LocalDateTime.parse(matcher.group(2), DATE_TIME_FORMATTER);
    LocalDateTime end = LocalDateTime.parse(matcher.group(3), DATE_TIME_FORMATTER);
    String weekdays = matcher.group(4);
    int occurrences = Integer.parseInt(matcher.group(5));

    model.createEventSeries(subject, start, end, weekdays, occurrences, null);
    view.displayMessage("Event series created: " + subject);
  }

  private void handleCreateEventSeriesUntil(Matcher matcher) {
    String subject = matcher.group(1).trim();
    LocalDateTime start = LocalDateTime.parse(matcher.group(2), DATE_TIME_FORMATTER);
    LocalDateTime end = LocalDateTime.parse(matcher.group(3), DATE_TIME_FORMATTER);
    String weekdays = matcher.group(4);
    LocalDate untilDate = LocalDate.parse(matcher.group(5), DATE_FORMATTER);
    LocalDateTime until = LocalDateTime.of(untilDate, LocalTime.of(23, 59));

    model.createEventSeries(subject, start, end, weekdays, -1, until);
    view.displayMessage("Event series created: " + subject);
  }

  private void handleCreateAllDayEvent(Matcher matcher) {
    String subject = matcher.group(1).trim();
    LocalDate date = LocalDate.parse(matcher.group(2), DATE_FORMATTER);

    model.createAllDayEvent(subject, date.atStartOfDay());
    view.displayMessage("All-day event created: " + subject);
  }

  private void handleCreateAllDaySeriesCount(Matcher matcher) {
    String subject = matcher.group(1).trim();
    LocalDate startDate = LocalDate.parse(matcher.group(2), DATE_FORMATTER);
    String weekdays = matcher.group(3);
    int occurrences = Integer.parseInt(matcher.group(4));
    LocalDateTime start = LocalDateTime.of(startDate, LocalTime.of(8, 0));
    LocalDateTime end = LocalDateTime.of(startDate, LocalTime.of(17, 0));

    model.createEventSeries(subject, start, end, weekdays, occurrences, null);
    view.displayMessage("All-day event series created: " + subject);
  }

  private void handleCreateAllDaySeriesUntil(Matcher matcher) {
    String subject = matcher.group(1).trim();
    LocalDate startDate = LocalDate.parse(matcher.group(2), DATE_FORMATTER);
    String weekdays = matcher.group(3);
    LocalDate untilDate = LocalDate.parse(matcher.group(4), DATE_FORMATTER);
    LocalDateTime start = LocalDateTime.of(startDate, LocalTime.of(8, 0));
    LocalDateTime end = LocalDateTime.of(startDate, LocalTime.of(17, 0));
    LocalDateTime until = LocalDateTime.of(untilDate, LocalTime.of(23, 59));

    model.createEventSeries(subject, start, end, weekdays, -1, until);
    view.displayMessage("All-day event series created: " + subject);
  }

  private void handleEditEvent(Matcher matcher) {
    String property = matcher.group(1).trim();
    String subject = matcher.group(2).trim();
    LocalDateTime start = LocalDateTime.parse(matcher.group(3), DATE_TIME_FORMATTER);
    String newValue = matcher.group(5).trim();

    model.editEvent(subject, start, property, newValue);
    view.displayMessage("Event edited successfully");
  }

  private void handleEditEvents(Matcher matcher) {
    String property = matcher.group(1).trim();
    String subject = matcher.group(2).trim();
    LocalDateTime start = LocalDateTime.parse(matcher.group(3), DATE_TIME_FORMATTER);
    String newValue = matcher.group(4).trim();

    model.editEvents(subject, start, property, newValue);
    view.displayMessage("Events edited successfully");
  }

  private void handleEditSeries(Matcher matcher) {
    String property = matcher.group(1).trim();
    String subject = matcher.group(2).trim();
    LocalDateTime start = LocalDateTime.parse(matcher.group(3), DATE_TIME_FORMATTER);
    String newValue = matcher.group(4).trim();

    model.editSeries(subject, start, property, newValue);
    view.displayMessage("Series edited successfully");
  }

  private void handlePrintEventsOnDate(Matcher matcher) {
    LocalDate date = LocalDate.parse(matcher.group(1), DATE_FORMATTER);
    List<Ievent> events = model.getByDate(date);
    displayFormattedEvents(events);
  }

  private void handlePrintEventsInRange(Matcher matcher) {
    LocalDateTime start = LocalDateTime.parse(matcher.group(1), DATE_TIME_FORMATTER);
    LocalDateTime end = LocalDateTime.parse(matcher.group(2), DATE_TIME_FORMATTER);
    List<Ievent> events = model.getByRange(start, end);
    displayFormattedEvents(events);
  }

  private void handleShowStatus(Matcher matcher) {
    LocalDateTime dateTime = LocalDateTime.parse(matcher.group(1), DATE_TIME_FORMATTER);
    boolean busy = model.isBusy(dateTime);
    view.displayMessage(busy ? "busy" : "available");
  }

  private void handleExport(Matcher matcher) {
    String filename = matcher.group(1).trim();
    String extension = getFileExtension(filename);

    List<Ievent> allEvents = model.getAllEvents();
    String timezone = model.getCurrentCalendarTimezone();

    String absolutePath;
    switch (extension.toLowerCase()) {
      case "csv":
        CsvExporter csvExporter = new CsvExporter();
        absolutePath = csvExporter.exportToCsv(allEvents, filename);
        break;

      case "ical":
      case "ics":
        IcalExporter icalExporter = new IcalExporter(allEvents, timezone);
        absolutePath = icalExporter.exportToIcal(filename);
        break;

      default:
        throw new IllegalArgumentException(
            "Unsupported file format: " + extension + ". Supported formats: csv, ical, ics");
    }

    view.displayMessage("Calendar exported successfully");
    view.displayMessage("File location: " + absolutePath);
  }

  /**
   * Extracts file extension from filename.
   */
  private String getFileExtension(String filename) {
    int lastDotIndex = filename.lastIndexOf('.');

    if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
      throw new IllegalArgumentException(
          "Filename must have an extension (e.g., .csv or .ical)");
    }

    return filename.substring(lastDotIndex + 1);
  }

  /**
   * Formats events into strings and displays them through the view.
   */
  private void displayFormattedEvents(List<Ievent> events) {
    if (events.isEmpty()) {
      view.displayMessage("No events found.");
      return;
    }

    for (Ievent event : events) {
      String formattedEvent = ResponseFormatter.formatEvent(event);
      view.displayMessage(formattedEvent);
    }
  }
}