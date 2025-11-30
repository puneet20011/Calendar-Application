package calendar.mocks;

import calendar.controller.CalendarFeatures;
import calendar.model.Ievent;
import calendar.view.IcalendarGuiView;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Mock implementation of IcalendarGuiView for testing CalendarGuiController.
 * Records all method calls and allows verification of controller behavior.
 */
public class MockCalendarGuiView implements IcalendarGuiView {

  private final StringBuilder log;
  private CalendarFeatures features;
  private LocalDate selectedDate;
  private YearMonth currentMonth;
  private Set<String> calendarNames;
  private String currentCalendar;
  private String currentTimezone;
  private List<Ievent> monthEvents;
  private List<Ievent> dayEvents;

  /**
   * Constructs a MockCalendarGuiView with a shared log.
   *
   * @param log StringBuilder to record interactions
   */
  public MockCalendarGuiView(StringBuilder log) {
    this.log = Objects.requireNonNull(log, "log cannot be null");
    this.selectedDate = LocalDate.now();
    this.currentMonth = YearMonth.now();
    this.calendarNames = new HashSet<>();
    this.monthEvents = new ArrayList<>();
    this.dayEvents = new ArrayList<>();
  }

  /**
   * Constructs a MockCalendarGuiView with a new log.
   */
  public MockCalendarGuiView() {
    this(new StringBuilder());
  }

  /**
   * Gets the recorded log of all view interactions.
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

  @Override
  public void setFeatures(CalendarFeatures features) {
    this.features = features;
    log.append("setFeatures called").append(System.lineSeparator());
  }

  @Override
  public void display() {
    log.append("display called").append(System.lineSeparator());
  }

  @Override
  public LocalDate getSelectedDate() {
    log.append("getSelectedDate:").append(selectedDate).append(System.lineSeparator());
    return selectedDate;
  }

  @Override
  public YearMonth getCurrentMonth() {
    log.append("getCurrentMonth:").append(currentMonth).append(System.lineSeparator());
    return currentMonth;
  }

  @Override
  public void refreshCalendarList(Set<String> calendarNames, String currentCalendar,
                                  String timezone) {
    this.calendarNames = new HashSet<>(calendarNames);
    this.currentCalendar = currentCalendar;
    this.currentTimezone = timezone;
    log.append("refreshCalendarList:")
        .append("names=").append(calendarNames)
        .append(",current=").append(currentCalendar)
        .append(",timezone=").append(timezone)
        .append(System.lineSeparator());
  }

  @Override
  public void refreshMonthView(int year, int month, List<Ievent> events) {
    this.currentMonth = YearMonth.of(year, month);
    this.monthEvents = new ArrayList<>(events);
    log.append("refreshMonthView:")
        .append(year).append('-').append(month)
        .append(",events=").append(events.size())
        .append(System.lineSeparator());
  }

  @Override
  public void refreshDayView(LocalDate date, List<Ievent> events) {
    this.selectedDate = date;
    this.dayEvents = new ArrayList<>(events);
    log.append("refreshDayView:")
        .append(date)
        .append(",events=").append(events.size())
        .append(System.lineSeparator());
  }

  @Override
  public void showSuccess(String message) {
    log.append("showSuccess:").append(message).append(System.lineSeparator());
  }

  @Override
  public void showError(String errorMessage) {
    log.append("showError:").append(errorMessage).append(System.lineSeparator());
  }

  @Override
  public boolean showConfirmation(String message) {
    log.append("showConfirmation:").append(message).append(System.lineSeparator());
    return true; // Default to confirming
  }

  // Additional helper methods for testing

  /**
   * Gets the features controller that was set.
   *
   * @return the features controller
   */
  public CalendarFeatures getFeatures() {
    return features;
  }

  /**
   * Gets the current calendar names.
   *
   * @return set of calendar names
   */
  public Set<String> getCalendarNames() {
    return new HashSet<>(calendarNames);
  }

  /**
   * Gets the current active calendar name.
   *
   * @return current calendar name
   */
  public String getCurrentCalendar() {
    return currentCalendar;
  }

  /**
   * Gets the current calendar timezone.
   *
   * @return current timezone
   */
  public String getCurrentTimezone() {
    return currentTimezone;
  }

  /**
   * Gets the events shown in the month view.
   *
   * @return list of month events
   */
  public List<Ievent> getMonthEvents() {
    return new ArrayList<>(monthEvents);
  }

  /**
   * Gets the events shown in the day view.
   *
   * @return list of day events
   */
  public List<Ievent> getDayEvents() {
    return new ArrayList<>(dayEvents);
  }

  /**
   * Sets the selected date for testing.
   *
   * @param date the date to select
   */
  public void setSelectedDate(LocalDate date) {
    this.selectedDate = date;
  }

  /**
   * Sets the current month for testing.
   *
   * @param month the month to set
   */
  public void setCurrentMonth(YearMonth month) {
    this.currentMonth = month;
  }

  /**
   * Simulates user selecting a calendar from the dropdown.
   *
   * @param calendarName the calendar to select
   */
  public void simulateSelectCalendar(String calendarName) {
    if (features != null) {
      features.selectCalendar(calendarName);
    }
  }

  /**
   * Simulates user navigating to a month.
   *
   * @param year the year
   * @param month the month
   */
  public void simulateNavigateToMonth(int year, int month) {
    if (features != null) {
      features.navigateToMonth(year, month);
    }
  }

  /**
   * Simulates user selecting a date.
   *
   * @param date the date to select
   */
  public void simulateSelectDate(LocalDate date) {
    if (features != null) {
      features.selectDate(date);
    }
  }
}