package calendar.controller;

import calendar.model.CalendarSystemModel;
import calendar.model.Ievent;
import calendar.view.IcalendarGuiView;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a Controller that handles GUI based interactions.
 * GUI controller handles the callbacks from the GUI view.
 */
public class CalendarGuiController implements CalendarFeatures {

  private final CalendarSystemModel model;
  private final IcalendarGuiView view;
  private final Set<String> calendarNames;

  /**
   * Constructs a CalendarGuiController with the model and view.
   *
   * @param model the calendar system model
   * @param view  the calendar GUI view
   */
  public CalendarGuiController(CalendarSystemModel model, IcalendarGuiView view) {
    this.model = model;
    this.view = view;
    this.calendarNames = new HashSet<>();
    view.setFeatures(this);
  }

  /**
   * Initializes the controller with a default calendar and display.
   */
  public void run() {
    String defaultTimezone = ZoneId.systemDefault().getId();
    try {
      model.createCalendar("Default", defaultTimezone);
      model.useCalendar("Default");
      calendarNames.add("Default");

      view.refreshCalendarList(calendarNames, "Default", defaultTimezone);

      LocalDate today = LocalDate.now();
      navigateToMonth(today.getYear(), today.getMonthValue());
      selectDate(today);

      view.display();
    } catch (Exception e) {
      view.showError("Failed to initialize: " + e.getMessage());
    }
  }

  @Override
  public void createCalendar(String name, String timezone) {
    try {
      model.createCalendar(name, timezone);
      calendarNames.add(name);
      view.refreshCalendarList(calendarNames, null, null);
      view.showSuccess("Calendar created: " + name);
    } catch (Exception e) {
      view.showError(e.getMessage());
    }
  }

  @Override
  public void selectCalendar(String name) {
    try {
      model.useCalendar(name);
      String timezone = model.getCurrentCalendarTimezone();
      view.refreshCalendarList(calendarNames, name, timezone);

      YearMonth currentMonth = view.getCurrentMonth();
      navigateToMonth(currentMonth.getYear(), currentMonth.getMonthValue());
      selectDate(view.getSelectedDate());
    } catch (Exception e) {
      view.showError(e.getMessage());
    }
  }

  @Override
  public void createSingleEvent(String subject, LocalDateTime start, LocalDateTime end,
                                String description, String location, String status) {
    try {
      model.createEvent(subject, start, end);
      updateOptionalFields(subject, start, description, location, status);
      refreshViews(start.toLocalDate());
      view.showSuccess("Event created: " + subject);
    } catch (Exception e) {
      view.showError(e.getMessage());
    }
  }

  @Override
  public void createRecurringEvent(String subject, LocalDateTime start, LocalDateTime end,
                                   String weekdays, int occurrences, LocalDate untilDate,
                                   String description, String location, String status) {
    try {
      LocalDateTime until = untilDate != null ? untilDate.atTime(23, 59) : null;
      model.createEventSeries(subject, start, end, weekdays, occurrences, until);
      updateOptionalFields(subject, start, description, location, status);
      refreshViews(start.toLocalDate());
      view.showSuccess("Event series created: " + subject);
    } catch (Exception e) {
      view.showError(e.getMessage());
    }
  }

  @Override
  public void editSingleEvent(String subject, LocalDateTime start,
                              String property, String newValue) {
    try {
      model.editEvent(subject, start, property, newValue);
      refreshViews(start.toLocalDate());
      view.showSuccess("Event updated");
    } catch (Exception e) {
      view.showError(e.getMessage());
    }
  }

  @Override
  public void editMultipleEvents(String subject, LocalDateTime start,
                                 String property, String newValue) {
    try {
      model.editEvents(subject, start, property, newValue);
      refreshViews(start.toLocalDate());
      view.showSuccess("Events updated");
    } catch (Exception e) {
      view.showError(e.getMessage());
    }
  }

  @Override
  public void editEntireSeries(String subject, LocalDateTime start,
                               String property, String newValue) {
    try {
      model.editSeries(subject, start, property, newValue);
      refreshViews(start.toLocalDate());
      view.showSuccess("Entire series updated");
    } catch (Exception e) {
      view.showError(e.getMessage());
    }
  }

  @Override
  public void navigateToMonth(int year, int month) {
    try {
      LocalDate firstDay = LocalDate.of(year, month, 1);
      LocalDate lastDay = firstDay.plusMonths(1).minusDays(1);

      LocalDateTime start = firstDay.atStartOfDay();
      LocalDateTime end = lastDay.atTime(23, 59, 59);

      List<Ievent> events = model.getByRange(start, end);
      view.refreshMonthView(year, month, events);
    } catch (Exception e) {
      view.showError(e.getMessage());
    }
  }

  @Override
  public void selectDate(LocalDate date) {
    try {
      List<Ievent> events = model.getByDate(date);
      view.refreshDayView(date, events);
    } catch (Exception e) {
      view.showError(e.getMessage());
    }
  }

  @Override
  public String[] getAvailableCalendars() {
    return calendarNames.toArray(new String[0]);
  }

  private void updateOptionalFields(String subject, LocalDateTime start,
                                    String description, String location, String status) {
    try {
      if (description != null && !description.trim().isEmpty()) {
        model.editEvent(subject, start, "description", description);
      }
      if (location != null && !location.trim().isEmpty()) {
        model.editEvent(subject, start, "location", location);
      }
      if (status != null && !status.equals("public")) {
        model.editEvent(subject, start, "status", status);
      }
    } catch (Exception e) {
      // pass
    }
  }

  @Override
  public void editCalendarProperty(String calendarName, String property, String newValue) {
    try {
      model.editCalendar(calendarName, property, newValue);

      if (property.equalsIgnoreCase("name")) {
        calendarNames.remove(calendarName);
        calendarNames.add(newValue);
        String timezone = model.getCurrentCalendarTimezone();
        view.refreshCalendarList(calendarNames, newValue, timezone);
      }

      view.showSuccess("Calendar updated");

      if (property.equalsIgnoreCase("timezone")) {
        String timezone = model.getCurrentCalendarTimezone();
        view.refreshCalendarList(calendarNames, calendarName, timezone);

        YearMonth currentMonth = view.getCurrentMonth();
        navigateToMonth(currentMonth.getYear(), currentMonth.getMonthValue());
      }
    } catch (Exception e) {
      view.showError(e.getMessage());
    }
  }

  @Override
  public void copySingleEvent(String eventSubject, LocalDateTime eventStart,
                              String targetCalendar, LocalDateTime targetStart) {
    try {
      model.copyEvent(eventSubject, eventStart, targetCalendar, targetStart);
      view.showSuccess("Event copied to " + targetCalendar);
    } catch (Exception e) {
      view.showError(e.getMessage());
    }
  }

  @Override
  public void copyEventsOnDate(LocalDate sourceDate, String targetCalendar, LocalDate targetDate) {
    try {
      model.copyEventsOnDate(sourceDate, targetCalendar, targetDate);
      view.showSuccess("Events copied to " + targetCalendar);
    } catch (Exception e) {
      view.showError(e.getMessage());
    }
  }

  @Override
  public void copyEventsInRange(LocalDate startDate, LocalDate endDate,
                                String targetCalendar, LocalDate targetStartDate) {
    try {
      model.copyEventsInRange(startDate, endDate, targetCalendar, targetStartDate);
      view.showSuccess("Events copied to " + targetCalendar);
    } catch (Exception e) {
      view.showError(e.getMessage());
    }
  }

  private void refreshViews(LocalDate date) {
    navigateToMonth(date.getYear(), date.getMonthValue());
    selectDate(date);
  }
}