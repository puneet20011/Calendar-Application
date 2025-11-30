package calendar.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents Single Calendar's Model and defines all the operations on the calendar.
 * Defines all the operations based on user commands on a Calendar.
 */
public interface IcalendarModel {
  /**
   * Creates a single event in the calendar with specified Start and end date time..
   *
   * @param subject the event subject
   * @param start the start date and time
   * @param end the end date and time
   * @throws IllegalArgumentException if an event with the same subject, start, and end exists,
   *                                  or if start time is not before end time
   */
  void createEvent(String subject, LocalDateTime start, LocalDateTime end);

  /**
   * Creates a series of recurring events on specific days of the week.
   * Events repeat either for a specified number of occurrences or until a specific date.
   * All events in the series have the same start time and must span only a single day.
   *
   * @param subject the event subject for all occurrences
   * @param start the start date and time for the first occurrence
   * @param end the end date and time for the first occurrence
   * @param weekdays string of weekday characters
   * @param occurrences the number of times to repeats
   * @param untilDate the date to repeat until (inclusive), or null if using occurrences count
   * @throws IllegalArgumentException if the series would create duplicate events,
   *                                  or if start time is not before end time
   */
  void createEventSeries(String subject, LocalDateTime start, LocalDateTime end,
                         String weekdays, int occurrences, LocalDateTime untilDate);

  /**
   * Creates a single all-day event from 8am to 5pm.
   *
   * @param subject the event subject
   * @param date the date for the all-day event
   * @throws IllegalArgumentException if an event with the same subject, start, and end exists
   */
  void createAllDayEvent(String subject, LocalDateTime date);

  /**
   * Retrieves list of all evens on a specific date.
   *
   * @param date the date to query
   * @return a list of events on the specified date, sorted by start time
   */
  List<Ievent> getByDate(LocalDate date);

  /**
   * Retrieves list of all events that partly or completely  within a date/time range.
   *
   * @param start the start of the range
   * @param end the end of the range
   * @return a list of events within the range, sorted by start time
   */
  List<Ievent> getByRange(LocalDateTime start, LocalDateTime end);

  /**
   * Checks if the user is busy at a specific date and time.
   *
   * @param dateTime the date and time to check
   * @return true if the user has an event scheduled at that time, false otherwise
   */
  boolean isBusy(LocalDateTime dateTime);

  /**
   * Edits a single event instance by modifying one of its properties.
   * If the event is part of a series, only this specific instance is modified.
   *
   * @param subject the subject of the event to edit
   * @param start the start date/time to uniquely identify the event
   * @param property the property to edit (subject, start, end, description, location, status)
   * @param newValue the new value for the property
   * @throws IllegalArgumentException if event is not found, property is invalid,
   *                                  or edit would create a duplicate event
   */
  void editEvent(String subject, LocalDateTime start, String property, String newValue);

  /**
   * Edits an event and all subsequent events in its series.
   * If the event is not part of a series, behaves the same as editEvent.
   *
   * @param subject the subject of the event to edit
   * @param start the start date/time of the event
   * @param property the property to edit
   * @param newValue the new value for the property
   * @throws IllegalArgumentException if event is not found, property is invalid,
   *                                  or edit would create duplicate events
   */
  void editEvents(String subject, LocalDateTime start, String property, String newValue);

  /**
   * Edits all events in a series.
   * If the event is not part of a series, behaves the same as editEvent.
   *
   * @param subject the subject of the event to edit
   * @param start the start date/time of the event
   * @param property the property to edit.
   * @param newValue the new value for the property
   * @throws IllegalArgumentException if event is not found, property is invalid,
   *                                  or edit would create duplicate events
   */
  void editSeries(String subject, LocalDateTime start, String property, String newValue);

  /**
   * Retrieves all events in the calendar.
   *
   * @return a list of all events, sorted by start time
   */
  List<Ievent> getAllEvents();
}
