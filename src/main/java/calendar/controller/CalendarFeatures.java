package calendar.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents the features offered by the Calendar for the GUI based view
 * Provides methods for creating and managing calendars, events, and navigation.
 */
public interface CalendarFeatures {

  /**
   * Creates a new calendar with the specified name and timezone.
   *
   * @param name the name of the calendar
   * @param timezone the timezone for the calendar
   */
  void createCalendar(String name, String timezone);

  /**
   * Selects an existing calendar by name.
   *
   * @param name the name of the calendar to select
   */
  void selectCalendar(String name);

  /**
   * Edits a property of an existing calendar.
   *
   * @param calendarName the name of the calendar to edit
   * @param property the property to modify
   * @param newValue the new value for the property
   */
  void editCalendarProperty(String calendarName, String property, String newValue);

  /**
   * Creates a single, non-recurring event.
   *
   * @param subject the subject/title of the event
   * @param start the start date and time
   * @param end the end date and time
   * @param description the event description
   * @param location the event location
   * @param status the event status
   */
  void createSingleEvent(String subject, LocalDateTime start, LocalDateTime end,
                         String description, String location, String status);

  /**
   * Creates a recurring event with specified parameters.
   *
   * @param subject the subject of the event
   * @param start the start date and time
   * @param end the end date and time
   * @param weekdays the days of the week for recurrence
   * @param occurrences the number of occurrences
   * @param untilDate the end date for recurrence
   * @param description the event description
   * @param location the event location
   * @param status the event status
   */
  void createRecurringEvent(String subject, LocalDateTime start, LocalDateTime end,
                            String weekdays, int occurrences, LocalDate untilDate,
                            String description, String location, String status);

  /**
   * Edits a property of a single event occurrence.
   *
   * @param subject  the subject of the event to edit
   * @param start    the start date and time of the event
   * @param property the property to modify
   * @param newValue the new value for the property
   */
  void editSingleEvent(String subject, LocalDateTime start, String property, String newValue);

  /**
   * Edits a property across multiple event occurrences.
   *
   * @param subject  the subject of the events to edit
   * @param start    the start date and time of the event series
   * @param property the property to modify
   * @param newValue the new value for the property
   */
  void editMultipleEvents(String subject, LocalDateTime start, String property, String newValue);

  /**
   * Edits a property across all events in the series.
   *
   * @param subject  the subject of the events to edit
   * @param start    the start date and time of the event series
   * @param property the property to modify
   * @param newValue the new value for the property
   */
  void editEntireSeries(String subject, LocalDateTime start, String property, String newValue);

  /**
   * Copies a single event to a target calendar.
   *
   * @param eventSubject   the subject of the event to copy
   * @param eventStart     the start date and time of the event
   * @param targetCalendar the name of the target calendar
   * @param targetStart    the start date and time in the target calendar
   */
  void copySingleEvent(String eventSubject, LocalDateTime eventStart,
                       String targetCalendar, LocalDateTime targetStart);

  /**
   * Copies all events on a specific date to a target calendar.
   *
   * @param sourceDate     the date to copy events from
   * @param targetCalendar the name of the target calendar
   * @param targetDate     the target date in the destination calendar
   */
  void copyEventsOnDate(LocalDate sourceDate, String targetCalendar, LocalDate targetDate);

  /**
   * Copies all events within a date range to a target calendar.
   *
   * @param startDate       the start of the date range
   * @param endDate         the end of the date range
   * @param targetCalendar  the name of the target calendar
   * @param targetStartDate the start date in the target calendar
   */
  void copyEventsInRange(LocalDate startDate, LocalDate endDate,
                         String targetCalendar, LocalDate targetStartDate);

  /**
   * Navigates to a specific month in the calendar view.
   *
   * @param year  the year to navigate to
   * @param month the month to navigate to
   */
  void navigateToMonth(int year, int month);

  /**
   * Selects a specific date in the calendar.
   *
   * @param date the date to select
   */
  void selectDate(LocalDate date);

  /**
   * Retrieves the list of available calendar names.
   *
   * @return an array of available calendar names
   */
  String[] getAvailableCalendars();
}