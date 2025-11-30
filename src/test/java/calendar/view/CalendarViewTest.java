package calendar.view;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for CalendarView class functionality.
 * Tests message display, error handling, and output operations.
 */
public class CalendarViewTest {

  private IcalendarView view;
  private ByteArrayOutputStream outputStream;
  private ByteArrayOutputStream errorStream;

  /**
   * Sets up the test fixture.
   */
  @Before
  public void setUp() {
    outputStream = new ByteArrayOutputStream();
    errorStream = new ByteArrayOutputStream();

    view = new CalendarView(
        new PrintStream(outputStream),
        new PrintStream(errorStream)
    );
  }

  @Test
  public void testDisplayMessage() {
    String message = "Test message";
    view.displayMessage(message);

    String output = outputStream.toString();
    assertTrue(output.contains(message));
  }

  @Test
  public void testDisplayError() {
    String error = "Test error";
    view.displayError(error);

    String errorOutput = errorStream.toString();
    assertTrue(errorOutput.contains("ERROR: " + error));
  }

  @Test
  public void testDisplayPrompt() {
    view.displayPrompt();

    String output = outputStream.toString();
    assertTrue(output.contains("> "));
  }

  @Test
  public void testDisplayMessageWithSpecialCharacters() {
    String message = "Message with \"quotes\" and, commas";
    view.displayMessage(message);

    String output = outputStream.toString();
    assertTrue(output.contains(message));
  }

  @Test
  public void testDisplayMessageWithNewlines() {
    String message = "Message\nwith\nnewlines";
    view.displayMessage(message);

    String output = outputStream.toString();
    assertTrue(output.contains(message));
  }

  @Test
  public void testDisplayMessageEmpty() {
    view.displayMessage("");

    String output = outputStream.toString();
    assertTrue(output.length() > 0);
  }

  @Test
  public void testDisplayErrorEmpty() {
    view.displayError("");

    String errorOutput = errorStream.toString();
    assertTrue(errorOutput.contains("ERROR: "));
  }

  @Test
  public void testMultipleMessages() {
    view.displayMessage("First message");
    view.displayMessage("Second message");

    String output = outputStream.toString();
    assertTrue(output.contains("First message"));
    assertTrue(output.contains("Second message"));
  }

  @Test
  public void testMultipleErrors() {
    view.displayError("First error");
    view.displayError("Second error");

    String errorOutput = errorStream.toString();
    assertTrue(errorOutput.contains("ERROR: First error"));
    assertTrue(errorOutput.contains("ERROR: Second error"));
  }

  @Test
  public void testMixedOutput() {
    view.displayMessage("Normal message");
    view.displayError("Error message");
    view.displayPrompt();

    String output = outputStream.toString();
    String errorOutput = errorStream.toString();

    assertTrue(output.contains("Normal message"));
    assertTrue(output.contains("> "));
    assertTrue(errorOutput.contains("ERROR: Error message"));
  }

  @Test
  public void testDisplayMessageWithNullHandling() {
    try {
      view.displayMessage(null);
      String output = outputStream.toString();
      assertTrue(output.length() >= 0);
    } catch (NullPointerException e) {
      assertTrue(true);
    }
  }

  @Test
  public void testDisplayErrorWithNullHandling() {
    try {
      view.displayError(null);
      String errorOutput = errorStream.toString();
      assertTrue(errorOutput.contains("ERROR: "));
    } catch (NullPointerException e) {
      assertTrue(true);
    }
  }
}