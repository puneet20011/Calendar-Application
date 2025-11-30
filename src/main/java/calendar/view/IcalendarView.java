package calendar.view;

/**
 * Represents calendar's view operations.
 * The view is responsible only for displaying information.
 */
public interface IcalendarView {

  /**
   * Displays a message to the user.
   */
  void displayMessage(String message);

  /**
   * Displays an error message to the user.
   */
  void displayError(String error);

  /**
   * Displays the command prompt.
   */
  void displayPrompt();
}