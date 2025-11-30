package calendar.view;

import calendar.controller.CalendarFeatures;
import calendar.model.Ievent;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;

/**
 * GUI view interface.
 */
public interface IcalendarGuiView {

  /**
   * Sets the features controller for this view.
   *
   * @param features the calendar features controller
   */
  void setFeatures(CalendarFeatures features);

  /**
   * Displays the view.
   */
  void display();

  /**
   * Gets the currently selected date.
   *
   * @return the selected date
   */
  LocalDate getSelectedDate();

  /**
   * Gets the current month being displayed.
   *
   * @return the current month
   */
  YearMonth getCurrentMonth();

  /**
   * Refreshes the calendar list display.
   *
   * @param calendarNames the set of all calendar names
   * @param currentCalendar the currently active calendar
   * @param timezone the timezone of the current calendar
   */
  void refreshCalendarList(Set<String> calendarNames, String currentCalendar, String timezone);

  /**
   * Refreshes the month view with events.
   *
   * @param year the year to display
   * @param month the month to display
   * @param events the list of events in this month
   */
  void refreshMonthView(int year, int month, List<Ievent> events);

  /**
   * Refreshes the day view with events for a specific date.
   *
   * @param date the date to display
   * @param events the list of events on this date
   */
  void refreshDayView(LocalDate date, List<Ievent> events);

  /**
   * Shows a success message to the user.
   *
   * @param message the success message
   */
  void showSuccess(String message);

  /**
   * Shows an error message to the user.
   *
   * @param errorMessage the error message
   */
  void showError(String errorMessage);

  /**
   * Shows a confirmation dialog to the user.
   *
   * @param message the confirmation message
   * @return true if the user confirmed, false otherwise
   */
  boolean showConfirmation(String message);
}