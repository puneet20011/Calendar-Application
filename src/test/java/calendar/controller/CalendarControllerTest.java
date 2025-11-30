package calendar.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import calendar.mocks.MockCalendarSystemModel;
import calendar.mocks.MockCalendarView;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Integration-style tests for {@link CalendarController} using mocks.
 */
public class CalendarControllerTest {

  private MockCalendarSystemModel mockModel;
  private MockCalendarView mockView;
  private StringBuilder modelLog;
  private StringBuilder viewLog;

  /**
   * Sets up test fixtures for each test.
   */
  @Before
  public void setUp() {
    modelLog = new StringBuilder();
    viewLog = new StringBuilder();
    mockModel = new MockCalendarSystemModel(modelLog);
    mockView = new MockCalendarView(viewLog, 1);
  }

  /**
   * Cleans up logs after each test.
   */
  @After
  public void tearDown() {
    modelLog.setLength(0);
    viewLog.setLength(0);
  }

  @Test
  public void testRunWithoutExitReportsError() {
    CalendarController controller = new CalendarController(
        mockModel, mockView, new StringReader(""));
    controller.run();

    assertTrue(viewLog.toString().contains("Calendar Application Started"));
    assertTrue(viewLog.toString().contains("File should have Exit command at the end"));
  }

  @Test
  public void testPromptDisplayedBetweenCommands() {
    String input = String.join("\n",
        "create event Sync from 2025-01-15T10:00 to 2025-01-15T11:00",
        "exit");

    CalendarController controller = new CalendarController(
        mockModel, mockView, new StringReader(input));
    controller.run();

    String output = viewLog.toString();
    int firstPromptIndex = output.indexOf("Input: Prompt displayed");
    int secondPromptIndex = output.indexOf("Input: Prompt displayed", firstPromptIndex + 1);

    assertTrue("Prompt should appear before each command and before exit", firstPromptIndex >= 0
        && secondPromptIndex > firstPromptIndex);
  }

  private calendar.model.Event createEvent(String subject, LocalDateTime start,
                                           LocalDateTime end) throws Exception {
    Class<?> eventSeriesClass = Class.forName("calendar.model.EventSeries");
    Constructor<calendar.model.Event> constructor =
        calendar.model.Event.class.getDeclaredConstructor(
            String.class, LocalDateTime.class, LocalDateTime.class,
            eventSeriesClass, String.class, String.class, String.class);
    constructor.setAccessible(true);
    return constructor.newInstance(subject, start, end, null, null, null, "public");
  }

  @Test
  public void testRunDisplaysInitialInstructions() {
    String input = "exit\n";
    CalendarController controller = new CalendarController(
        mockModel, mockView, new StringReader(input));
    controller.run();

    String output = viewLog.toString();
    assertTrue("Should display startup message",
        output.contains("Calendar Application Started"));
    assertTrue("Should display instructions",
        output.contains("Enter commands (type 'exit' to quit):"));
    assertTrue("Should display goodbye message",
        output.contains("Goodbye!"));
  }

  @Test
  public void testRunDisplaysInstructionsBeforeFirstPrompt() {
    String input = "exit\n";
    CalendarController controller = new CalendarController(
        mockModel, mockView, new StringReader(input));
    controller.run();

    String output = viewLog.toString();

    int instructionsIndex = output.indexOf("Enter commands (type 'exit' to quit):");
    int firstPromptIndex = output.indexOf("Input: Prompt displayed");

    assertTrue("Instructions should be displayed", instructionsIndex >= 0);
    assertTrue("First prompt should be displayed", firstPromptIndex >= 0);
    assertTrue("Instructions should come before first prompt",
        instructionsIndex < firstPromptIndex);
  }

  @Test
  public void testRunDisplaysInstructionsExactly() {
    String input = "exit\n";
    CalendarController controller = new CalendarController(
        mockModel, mockView, new StringReader(input));
    controller.run();

    String output = viewLog.toString();

    assertTrue("Should contain exact instruction text",
        output.contains("Enter commands (type 'exit' to quit):"));
    assertTrue("Should mention 'exit'",
        output.contains("exit"));
    assertTrue("Should mention 'quit'",
        output.contains("quit"));
  }

  @Test
  public void testRunDisplaysInstructionsOnce() {
    String input = "create calendar --name test --timezone UTC\n"
        + "use calendar --name test\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        mockModel, mockView, new StringReader(input));
    controller.run();

    String output = viewLog.toString();

    int count = 0;
    int index = 0;
    String searchString = "Enter commands (type 'exit' to quit):";

    while ((index = output.indexOf(searchString, index)) != -1) {
      count++;
      index += searchString.length();
    }

    assertEquals("Instructions should be displayed exactly once", 1, count);
  }

  @Test
  public void testRunDisplaysAllStartupMessages() {
    String input = "exit\n";
    CalendarController controller = new CalendarController(
        mockModel, mockView, new StringReader(input));
    controller.run();

    String output = viewLog.toString();

    assertTrue("Should display 'Calendar Application Started'",
        output.contains("Calendar Application Started"));
    assertTrue("Should display 'Enter commands'",
        output.contains("Enter commands"));
    assertTrue("Should display instruction to type 'exit'",
        output.contains("type 'exit' to quit"));
  }

  @Test
  public void testRunInstructionsFormat() {
    String input = "exit\n";
    CalendarController controller = new CalendarController(
        mockModel, mockView, new StringReader(input));
    controller.run();

    String output = viewLog.toString();

    assertTrue("Instructions should have correct format with parentheses",
        output.contains("(type 'exit' to quit)"));
    assertTrue("Instructions should end with colon",
        output.contains("Enter commands (type 'exit' to quit):"));
  }

  @Test
  public void testRunInstructionsNotDisplayedAfterExit() {
    String input = "exit\n";
    CalendarController controller = new CalendarController(
        mockModel, mockView, new StringReader(input));
    controller.run();

    String output = viewLog.toString();

    int instructionsIndex = output.indexOf("Enter commands (type 'exit' to quit):");
    int goodbyeIndex = output.indexOf("Goodbye!");

    assertTrue("Goodbye message should be displayed", goodbyeIndex >= 0);
    assertTrue("Instructions should come before goodbye",
        instructionsIndex < goodbyeIndex);
  }

  @Test
  public void testRunInstructionsCaseMatters() {
    String input = "exit\n";
    CalendarController controller = new CalendarController(
        mockModel, mockView, new StringReader(input));
    controller.run();

    String output = viewLog.toString();

    assertTrue("'Enter' should be capitalized",
        output.contains("Enter commands"));
    assertTrue("'exit' should be lowercase in instructions",
        output.contains("type 'exit'"));
  }

  @Test
  public void testRunInstructionsWithQuotes() {
    String input = "exit\n";
    CalendarController controller = new CalendarController(
        mockModel, mockView, new StringReader(input));
    controller.run();

    String output = viewLog.toString();

    assertTrue("Should have single quotes around exit",
        output.contains("'exit'"));
    assertFalse("Should not have double quotes around exit",
        output.contains("\"exit\""));
  }

  @Test
  public void testRunStartupMessageOrder() {
    String input = "exit\n";
    CalendarController controller = new CalendarController(
        mockModel, mockView, new StringReader(input));
    controller.run();

    String output = viewLog.toString();

    int startedIndex = output.indexOf("Calendar Application Started");
    int instructionsIndex = output.indexOf("Enter commands (type 'exit' to quit):");
    int firstPromptIndex = output.indexOf("Input: Prompt displayed");

    assertTrue("All messages should be displayed",
        startedIndex >= 0 && instructionsIndex >= 0 && firstPromptIndex >= 0);
    assertTrue("Messages should be in correct order",
        startedIndex < instructionsIndex && instructionsIndex < firstPromptIndex);
  }

  @Test
  public void testRunCatchesExceptionAndDisplaysError() {
    String input = "create calendar --name test --timezone UTC\n"
        + "use calendar --name test\n"
        + "create event Test from 2025-01-15T10:00 to 2025-01-15T11:00\n"
        + "exit\n";

    mockModel = new MockCalendarSystemModel(modelLog) {
      @Override
      public void createEvent(String subject, LocalDateTime start, LocalDateTime end) {
        throw new IllegalArgumentException("Duplicate event exists");
      }
    };

    CalendarController controller = new CalendarController(
        mockModel, mockView, new StringReader(input));
    controller.run();

    String errorOutput = viewLog.toString();
    assertTrue("Should display error with message",
        errorOutput.contains("Error: Duplicate event exists"));
  }

  @Test
  public void testRunCatchesExceptionWithNullMessage() {
    String input = "create calendar --name test --timezone UTC\n"
        + "use calendar --name test\n"
        + "create event Test from 2025-01-15T10:00 to 2025-01-15T11:00\n"
        + "exit\n";

    mockModel = new MockCalendarSystemModel(modelLog) {
      @Override
      public void createEvent(String subject, LocalDateTime start, LocalDateTime end) {
        throw new IllegalArgumentException(); // null message
      }
    };

    CalendarController controller = new CalendarController(
        mockModel, mockView, new StringReader(input));
    controller.run();

    String errorOutput = viewLog.toString();
    assertTrue("Should handle null message",
        errorOutput.contains("Error:"));
  }

  @Test
  public void testRunCatchesGenericException() {
    String input = "create calendar --name test --timezone UTC\n"
        + "exit\n";

    mockModel = new MockCalendarSystemModel(modelLog) {
      @Override
      public void createCalendar(String name, String timezone) {
        throw new RuntimeException("System error");
      }
    };

    CalendarController controller = new CalendarController(
        mockModel, mockView, new StringReader(input));
    controller.run();

    String errorOutput = viewLog.toString();
    assertTrue("Should catch Exception type",
        errorOutput.contains("Error: System error"));
  }
}