package calendar.mocks;

import calendar.model.CalendarSystemModel;
import calendar.model.Ievent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Test double for {@link CalendarSystemModel} used by controller/view tests.
 * Records each interaction and lets tests stub return values.
 */
public class MockCalendarSystemModel implements CalendarSystemModel {

  private final StringBuilder log;

  private List<Ievent> eventsForDate = Collections.emptyList();
  private List<Ievent> eventsForRange = Collections.emptyList();
  private List<Ievent> allEvents = Collections.emptyList();
  private boolean busy;
  private String currentCalendarTimezone = "UTC";

  /**
   * Creates a mock model with a new log.
   */
  public MockCalendarSystemModel() {
    this(new StringBuilder());
  }

  /**
   * Creates a mock model with a shared log.
   *
   * @param log the StringBuilder to record interactions
   */
  public MockCalendarSystemModel(StringBuilder log) {
    this.log = Objects.requireNonNull(log, "log cannot be null");
  }

  /**
   * Gets the recorded log of all interactions.
   *
   * @return the log as a string
   */
  public String getLog() {
    return log.toString();
  }

  /**
   * Clears the recorded log.
   */
  public void clearLog() {
    log.setLength(0);
  }

  /**
   * Stubs the events to return for getByDate calls.
   *
   * @param events the events to return
   */
  public void setEventsForDate(List<? extends Ievent> events) {
    this.eventsForDate = events == null
        ? Collections.emptyList()
        : new ArrayList<>(events);
  }

  /**
   * Stubs the events to return for getByRange calls.
   *
   * @param events the events to return
   */
  public void setEventsForRange(List<? extends Ievent> events) {
    this.eventsForRange = events == null
        ? Collections.emptyList()
        : new ArrayList<>(events);
  }

  /**
   * Stubs the events to return for getAllEvents calls.
   *
   * @param events the events to return
   */
  public void setAllEvents(List<? extends Ievent> events) {
    this.allEvents = events == null
        ? Collections.emptyList()
        : new ArrayList<>(events);
  }

  /**
   * Stubs the busy status to return for isBusy calls.
   *
   * @param busy the busy status
   */
  public void setBusy(boolean busy) {
    this.busy = busy;
  }

  /**
   * Stubs the timezone to return for getCurrentCalendarTimezone calls.
   *
   * @param timezone the timezone string
   */
  public void setCurrentCalendarTimezone(String timezone) {
    this.currentCalendarTimezone = timezone;
  }

  @Override
  public void createCalendar(String name, String timezone) {
    log.append("createCalendar:")
        .append(name)
        .append(',')
        .append(timezone)
        .append(System.lineSeparator());
  }

  @Override
  public void useCalendar(String name) {
    log.append("useCalendar:")
        .append(name)
        .append(System.lineSeparator());
  }

  @Override
  public void editCalendar(String calendarName, String property, String newValue) {
    log.append("editCalendar:")
        .append(calendarName)
        .append(',')
        .append(property)
        .append(',')
        .append(newValue)
        .append(System.lineSeparator());
  }

  @Override
  public void copyEvent(String eventName, LocalDateTime eventStart,
                        String targetCalendarName, LocalDateTime targetStart) {
    log.append("copyEvent:")
        .append(eventName)
        .append(',')
        .append(eventStart)
        .append(',')
        .append(targetCalendarName)
        .append(',')
        .append(targetStart)
        .append(System.lineSeparator());
  }

  @Override
  public void copyEventsOnDate(LocalDate sourceDate, String targetCalendarName,
                               LocalDate targetDate) {
    log.append("copyEventsOnDate:")
        .append(sourceDate)
        .append(',')
        .append(targetCalendarName)
        .append(',')
        .append(targetDate)
        .append(System.lineSeparator());
  }

  @Override
  public void copyEventsInRange(LocalDate startDate, LocalDate endDate,
                                String targetCalendarName, LocalDate targetStartDate) {
    log.append("copyEventsInRange:")
        .append(startDate)
        .append(',')
        .append(endDate)
        .append(',')
        .append(targetCalendarName)
        .append(',')
        .append(targetStartDate)
        .append(System.lineSeparator());
  }

  @Override
  public String getCurrentCalendarTimezone() {
    log.append("getCurrentCalendarTimezone")
        .append(System.lineSeparator());
    return currentCalendarTimezone;
  }

  @Override
  public void createEvent(String subject, LocalDateTime start, LocalDateTime end) {
    log.append("createEvent:")
        .append(subject)
        .append(',')
        .append(start)
        .append(',')
        .append(end)
        .append(System.lineSeparator());
  }

  @Override
  public void createEventSeries(String subject, LocalDateTime start, LocalDateTime end,
                                String weekdays, int occurrences, LocalDateTime untilDate) {
    log.append("createEventSeries:")
        .append(subject)
        .append(',')
        .append(start)
        .append(',')
        .append(end)
        .append(',')
        .append(weekdays)
        .append(',')
        .append(occurrences)
        .append(',')
        .append(untilDate)
        .append(System.lineSeparator());
  }

  @Override
  public void createAllDayEvent(String subject, LocalDateTime date) {
    log.append("createAllDayEvent:")
        .append(subject)
        .append(',')
        .append(date)
        .append(System.lineSeparator());
  }

  @Override
  public List<Ievent> getByDate(LocalDate date) {
    log.append("getByDate:")
        .append(date)
        .append(System.lineSeparator());
    return new ArrayList<>(eventsForDate);
  }

  @Override
  public List<Ievent> getByRange(LocalDateTime start, LocalDateTime end) {
    log.append("getByRange:")
        .append(start)
        .append(',')
        .append(end)
        .append(System.lineSeparator());
    return new ArrayList<>(eventsForRange);
  }

  @Override
  public List<Ievent> getAllEvents() {
    log.append("getAllEvents")
        .append(System.lineSeparator());
    return new ArrayList<>(allEvents);
  }

  @Override
  public boolean isBusy(LocalDateTime dateTime) {
    log.append("isBusy:")
        .append(dateTime)
        .append(System.lineSeparator());
    return busy;
  }

  @Override
  public void editEvent(String subject, LocalDateTime start, String property, String newValue) {
    log.append("editEvent:")
        .append(subject)
        .append(',')
        .append(start)
        .append(',')
        .append(property)
        .append(',')
        .append(newValue)
        .append(System.lineSeparator());
  }

  @Override
  public void editEvents(String subject, LocalDateTime start, String property, String newValue) {
    log.append("editEvents:")
        .append(subject)
        .append(',')
        .append(start)
        .append(',')
        .append(property)
        .append(',')
        .append(newValue)
        .append(System.lineSeparator());
  }

  @Override
  public void editSeries(String subject, LocalDateTime start, String property, String newValue) {
    log.append("editSeries:")
        .append(subject)
        .append(',')
        .append(start)
        .append(',')
        .append(property)
        .append(',')
        .append(newValue)
        .append(System.lineSeparator());
  }
}