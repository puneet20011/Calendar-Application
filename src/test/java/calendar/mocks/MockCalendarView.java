package calendar.mocks;

import calendar.view.IcalendarView;
import java.util.Objects;

/**
 * A Java class representing a mock implementation of the IcalendarView interface.
 */
public class MockCalendarView implements IcalendarView {

  private final StringBuilder log;
  private final int uniqueCode;

  /**
   * Constructs a MockCalendarView object with a new log.
   *
   * @param log        StringBuilder object
   * @param uniqueCode unique int value
   */
  public MockCalendarView(StringBuilder log, int uniqueCode) {
    this.log = Objects.requireNonNull(log, "log cannot be null");
    this.uniqueCode = uniqueCode;
  }

  /**
   * Constructs a MockCalendarView object with just a log.
   *
   * @param log StringBuilder object
   */
  public MockCalendarView(StringBuilder log) {
    this(log, 0);
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
  public void displayMessage(String message) {
    log.append("Input: ").append(message).append(System.lineSeparator());
  }

  @Override
  public void displayError(String error) {
    log.append("Input: ").append(error).append(System.lineSeparator());
  }

  @Override
  public void displayPrompt() {
    log.append("Input: Prompt displayed").append(System.lineSeparator());
  }
}