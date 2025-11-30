package calendar.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents Calendar System as whole.
 * Contains all operation for creation, edition, copy on Calendars.
 */
public interface CalendarSystemModel extends IcalendarModel {


  /**
   * Creates a new calendar with a name and timezone.
   *
   * @param name calendar name
   * @param timezone timezone in IANA format.
   * @throws IllegalArgumentException if name is duplicate or timezone is invalid
   */
  void createCalendar(String name, String timezone);

  /**
   * Sets the current calendar context for subsequent operations on that calendar.
   *
   * @param name name of calendar to use
   * @throws IllegalArgumentException if calendar not found
   */
  void useCalendar(String name);

  /**
   * Edits calendar properties of name or timezone.
   *
   * @param calendarName name of calendar to edit
   * @param property can be "name" or "timezone"
   * @param newValue new value for the property
   * @throws IllegalArgumentException if calendar not found or property invalid
   */
  void editCalendar(String calendarName, String property, String newValue);


  /**
   * Copies a single event to target calendar.
   *
   * @param eventName Subject of event to copy
   * @param eventStart start datetime of event in current calendar
   * @param targetCalendarName target calendar name
   * @param targetStart target start datetime in target calendar's timezone.
   * @throws IllegalStateException if no calendar is in use
   * @throws IllegalArgumentException if event or target calendar not found
   */
  void copyEvent(String eventName, LocalDateTime eventStart,
                 String targetCalendarName, LocalDateTime targetStart);

  /**
   * Copies all events on a specific date to target calendar.
   *
   * @param sourceDate date in current calendar
   * @param targetCalendarName target calendar name
   * @param targetDate date in target calendar
   * @throws IllegalStateException if no calendar is in use
   * @throws IllegalArgumentException if target calendar not found
   */
  void copyEventsOnDate(LocalDate sourceDate, String targetCalendarName,
                        LocalDate targetDate);

  /**
   * Copies all events in a date range to target calendar.
   * Maintains relative time offsets.
   *
   * @param startDate start of range included
   * @param endDate end of range included
   * @param targetCalendarName target calendar name
   * @param targetStartDate where to start copying in target
   * @throws IllegalStateException if no calendar is in use
   * @throws IllegalArgumentException if target calendar not found
   */
  void copyEventsInRange(LocalDate startDate, LocalDate endDate,
                         String targetCalendarName, LocalDate targetStartDate);


  /**
   * Gets the timezone of the currently active calendar.
   * used when exporting that cal as ical.
   *
   * @return the timezone ID string
   * @throws IllegalStateException if no calendar is in use
   */
  String getCurrentCalendarTimezone();
}