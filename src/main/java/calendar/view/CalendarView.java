package calendar.view;

import java.io.IOException;

/**
 * View implementation that handles output display through Appendable objects.
 * Uses Appendable for maximum abstraction and testability.
 */
public class CalendarView implements IcalendarView {

  private final Appendable out;
  private final Appendable err;

  /**
   * Creates a view with Appendables.
   * Easy to test by passing own PrintStreams when testing
   *
   * @param out appendable for normal output
   * @param err appendable for error output
   */
  public CalendarView(Appendable out, Appendable err) {
    this.out = out;
    this.err = err;
  }

  /**
   * Creates a view using standard system streams.
   * Convenience constructor for typical usage.
   */
  public CalendarView() {
    this(System.out, System.err);
  }

  @Override
  public void displayMessage(String message) {
    try {
      out.append(message).append("\n");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void displayError(String error) {
    try {
      err.append("ERROR: ").append(error).append("\n");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void displayPrompt() {
    try {
      out.append("> ");
    } catch (IOException e) {
      // pass
    }
  }
}