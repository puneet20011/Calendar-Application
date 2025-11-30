package calendar.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Facade for managing multiple calendars and delegating operations.
 * Implements CalendarSystemModel which unifies all calendar operations.
 * This is the main entry point for the controller.
 */
public class CalendarSystem implements CalendarSystemModel {

  private final Map<String, CalendarContext> calendars;
  private CalendarContext currentCalendar;
  private final EventCopier eventCopier;
  private final CalendarTimezoneConverter timezoneConverter;
  private final SystemValidator validator;

  /**
   * Facade for managing multiple calendars and delegating operations.
   */
  public CalendarSystem() {
    this.calendars = new HashMap<>();
    this.currentCalendar = null;
    this.eventCopier = new EventCopier();
    this.timezoneConverter = new CalendarTimezoneConverter();
    this.validator = new SystemValidator(null);
    this.validator.setCalendars(calendars);
  }


  @Override
  public void createCalendar(String name, String timezone) {
    validator.validateCalendarName(name);
    validator.validateCalendarDoesNotExist(name);
    ZoneId zoneId = validator.validateAndParseTimezone(timezone);

    CalendarContext context = new CalendarContext(name, zoneId);
    calendars.put(name, context);
  }

  @Override
  public void useCalendar(String name) {
    validator.validateCalendarExists(name);
    this.currentCalendar = calendars.get(name);
  }

  @Override
  public void editCalendar(String calendarName, String property, String newValue) {
    validator.validateCalendarExists(calendarName);
    validator.validateCalendarProperty(property);

    CalendarContext context = calendars.get(calendarName);

    switch (property.toLowerCase()) {
      case "name":
        handleNameChange(context, newValue);
        break;

      case "timezone":
        handleTimezoneChange(context, newValue);
        break;

      default:
        //pass
    }

  }


  @Override
  public void createEvent(String subject, LocalDateTime start, LocalDateTime end) {
    validator.validateCalendarInUse(currentCalendar);
    currentCalendar.getModel().createEvent(subject, start, end);
  }

  @Override
  public void createEventSeries(String subject, LocalDateTime start, LocalDateTime end,
                                String weekdays, int occurrences,
                                LocalDateTime untilDate) {
    validator.validateCalendarInUse(currentCalendar);
    currentCalendar.getModel().createEventSeries(subject, start, end,
        weekdays, occurrences, untilDate);
  }

  @Override
  public void createAllDayEvent(String subject, LocalDateTime date) {
    validator.validateCalendarInUse(currentCalendar);
    currentCalendar.getModel().createAllDayEvent(subject, date);
  }

  @Override
  public List<Ievent> getByDate(LocalDate date) {
    validator.validateCalendarInUse(currentCalendar);
    return currentCalendar.getModel().getByDate(date);
  }

  @Override
  public List<Ievent> getByRange(LocalDateTime start, LocalDateTime end) {
    validator.validateCalendarInUse(currentCalendar);
    return currentCalendar.getModel().getByRange(start, end);
  }

  @Override
  public boolean isBusy(LocalDateTime dateTime) {
    validator.validateCalendarInUse(currentCalendar);
    return currentCalendar.getModel().isBusy(dateTime);
  }

  @Override
  public List<Ievent> getAllEvents() {
    validator.validateCalendarInUse(currentCalendar);
    return currentCalendar.getModel().getAllEvents();
  }

  @Override
  public String getCurrentCalendarTimezone() {
    validator.validateCalendarInUse(currentCalendar);
    return currentCalendar.getTimezone().getId();
  }

  @Override
  public void editEvent(String subject, LocalDateTime start, String property,
                        String newValue) {
    validator.validateCalendarInUse(currentCalendar);
    currentCalendar.getModel().editEvent(subject, start, property, newValue);
  }

  @Override
  public void editEvents(String subject, LocalDateTime start, String property,
                         String newValue) {
    validator.validateCalendarInUse(currentCalendar);
    currentCalendar.getModel().editEvents(subject, start, property, newValue);
  }

  @Override
  public void editSeries(String subject, LocalDateTime start, String property,
                         String newValue) {
    validator.validateCalendarInUse(currentCalendar);
    currentCalendar.getModel().editSeries(subject, start, property, newValue);
  }

  @Override
  public void copyEvent(String eventName, LocalDateTime eventStart,
                        String targetCalendarName, LocalDateTime targetStart) {
    validator.validateCalendarInUse(currentCalendar);
    validator.validateCalendarExists(targetCalendarName);

    CalendarContext targetContext = calendars.get(targetCalendarName);
    eventCopier.copySingleEvent(currentCalendar, targetContext,
        eventName, eventStart, targetStart);
  }

  @Override
  public void copyEventsOnDate(LocalDate sourceDate, String targetCalendarName,
                               LocalDate targetDate) {
    validator.validateCalendarInUse(currentCalendar);
    validator.validateCalendarExists(targetCalendarName);

    CalendarContext targetContext = calendars.get(targetCalendarName);
    eventCopier.copyEventsOnDate(currentCalendar, targetContext,
        sourceDate, targetDate);
  }

  @Override
  public void copyEventsInRange(LocalDate startDate, LocalDate endDate,
                                String targetCalendarName, LocalDate targetStartDate) {
    validator.validateCalendarInUse(currentCalendar);
    validator.validateCalendarExists(targetCalendarName);

    CalendarContext targetContext = calendars.get(targetCalendarName);
    eventCopier.copyEventsInRange(currentCalendar, targetContext,
        startDate, endDate, targetStartDate);
  }


  /**
   * Gets the name of currently active calendar.
   *
   */
  String getCurrentCalendarName() {
    return currentCalendar != null ? currentCalendar.getName() : null;
  }

  /**
   * Checks if a calendar context is currently active.
   */
  boolean hasActiveCalendar() {
    return currentCalendar != null;
  }

  /**
   * Handles calendar name change operation.
   */
  private void handleNameChange(CalendarContext context, String newValue) {
    validator.validateCalendarDoesNotExist(newValue);

    calendars.remove(context.getName());
    context.setName(newValue);
    calendars.put(newValue, context);

    if (currentCalendar == context) {
      currentCalendar = context;
    }
  }

  /**
   * Handles calendar timezone change operation.
   */
  private void handleTimezoneChange(CalendarContext context, String newValue) {
    ZoneId oldTimezone = context.getTimezone();
    ZoneId newTimezone = validator.validateAndParseTimezone(newValue);

    timezoneConverter.convertAllEvents(
        context.getModel(),
        oldTimezone,
        newTimezone
    );

    context.setTimezone(newTimezone);
  }
}