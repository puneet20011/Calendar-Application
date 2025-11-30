package calendar.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import calendar.mocks.MockCalendarSystemModel;
import calendar.mocks.MockCalendarView;
import calendar.model.Event;
import java.lang.reflect.Constructor;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for CommandExecutor with the unified calendar system model.
 */
public class CommandExecutorTest {

  private MockCalendarSystemModel mockModel;
  private MockCalendarView mockView;
  private CommandExecutor executor;
  private StringBuilder modelLog;
  private StringBuilder viewLog;

  /**
   * Sets up mocks and executor before each test.
   */
  @Before
  public void setUp() {
    modelLog = new StringBuilder();
    viewLog = new StringBuilder();
    mockModel = new MockCalendarSystemModel(modelLog);
    mockView = new MockCalendarView(viewLog, 1);
    executor = new CommandExecutor(mockModel, mockView);
  }

  /**
   * Resets captured logs after each test.
   */
  @After
  public void tearDown() {
    modelLog.setLength(0);
    viewLog.setLength(0);
  }

  @Test
  public void testPreprocessCommandRemovesQuotesAndNormalizesWhitespace() {
    String raw = "  create   event  \"Team Sync\"   from   2025-01-15T10:00   to   "
        + "2025-01-15T11:00  ";
    String expected = "create event Team Sync from 2025-01-15T10:00 to 2025-01-15T11:00";
    assertEquals(expected, executor.preprocessCommand(raw));
  }

  @Test
  public void testParseAndExecuteExportIcal() {
    mockModel.setAllEvents(Collections.emptyList());
    mockModel.setCurrentCalendarTimezone("UTC");

    boolean result = executor.parseAndExecute("export cal calendar.ics");

    assertTrue(result);
    assertTrue(modelLog.toString().contains("getAllEvents"));
    assertTrue(modelLog.toString().contains("getCurrentCalendarTimezone"));
    assertTrue(viewLog.toString().contains("Calendar exported successfully"));
    assertTrue(viewLog.toString().contains("File location:"));
  }

  @Test
  public void testParseAndExecuteExportCsv() {
    mockModel.setAllEvents(Collections.emptyList());

    boolean result = executor.parseAndExecute("export cal calendar.csv");

    assertTrue(result);
    assertTrue(modelLog.toString().contains("getAllEvents"));
    assertTrue(viewLog.toString().contains("Calendar exported successfully"));
    assertTrue(viewLog.toString().contains("File location:"));
  }

  @Test
  public void testParseAndExecuteExportWithEvents() throws Exception {
    Event event = createEvent("Meeting",
        LocalDateTime.of(2025, 1, 15, 10, 0),
        LocalDateTime.of(2025, 1, 15, 11, 0));
    mockModel.setAllEvents(List.of(event));
    mockModel.setCurrentCalendarTimezone("America/New_York");

    boolean result = executor.parseAndExecute("export cal my-calendar.ics");

    assertTrue(result);
    assertTrue(modelLog.toString().contains("getAllEvents"));
    assertTrue(modelLog.toString().contains("getCurrentCalendarTimezone"));
  }

  @Test
  public void testParseAndExecuteInvalidCommandReturnsFalse() {
    boolean result = executor.parseAndExecute("not a real command");

    assertFalse(result);
    assertEquals("", modelLog.toString());
    assertEquals("", viewLog.toString());
  }

  @Test
  public void testCopyEventsOnDate() {
    boolean result = executor.parseAndExecute(
        "copy events on 2025-02-01 --target shared to 2025-02-05");

    assertTrue(result);
    assertTrue(modelLog.toString().contains("copyEventsOnDate:2025-02-01,shared,2025-02-05"));
    assertTrue(viewLog.toString().contains("Events copied successfully"));
  }

  @Test
  public void testCopyEventsRange() {
    boolean result = executor.parseAndExecute(
        "copy events between 2025-02-01 and 2025-02-07 --target shared to 2025-03-01");

    assertTrue(result);
    assertTrue(modelLog.toString().contains(
        "copyEventsInRange:2025-02-01,2025-02-07,shared,2025-03-01"));
    assertTrue(viewLog.toString().contains("Events copied successfully"));
  }

  private Event createEvent(String subject, LocalDateTime start, LocalDateTime end)
      throws Exception {
    Class<?> eventSeriesClass = Class.forName("calendar.model.EventSeries");
    Constructor<Event> constructor = Event.class.getDeclaredConstructor(
        String.class, LocalDateTime.class, LocalDateTime.class,
        eventSeriesClass, String.class, String.class, String.class);
    constructor.setAccessible(true);
    return constructor.newInstance(subject, start, end, null, null, null, "public");
  }

  @Test
  public void testParseAndExecuteEditEvents() {
    boolean result = executor.parseAndExecute(
        "edit events location Meeting from 2025-01-15T10:00 with Room 201");

    assertTrue(result);
    assertTrue(modelLog.toString().contains(
        "editEvents:Meeting,2025-01-15T10:00,location,Room 201"));
    assertTrue(viewLog.toString().contains("Events edited successfully"));
  }

  @Test
  public void testParseAndExecuteEditEventsTrimsWhitespace() {
    boolean result = executor.parseAndExecute(
        "edit events   location   Meeting   from   2025-01-15T10:00   with   Room 301  ");

    assertTrue(result);
    String log = modelLog.toString();
    assertTrue("Should trim property", log.contains("location"));
    assertTrue("Should trim subject", log.contains("Meeting"));
    assertTrue("Should trim new value", log.contains("Room 301"));
    assertTrue(viewLog.toString().contains("Events edited successfully"));
  }

  @Test
  public void testParseAndExecuteEditSeries() {
    boolean result = executor.parseAndExecute(
        "edit series location Weekly from 2025-01-15T10:00 with Conference Room");

    assertTrue(result);
    assertTrue(modelLog.toString().contains(
        "editSeries:Weekly,2025-01-15T10:00,location,Conference Room"));
    assertTrue(viewLog.toString().contains("Series edited successfully"));
  }

  @Test
  public void testParseAndExecuteEditSeriesTrimsWhitespace() {
    boolean result = executor.parseAndExecute(
        "edit series   location   Meeting   from   2025-01-15T10:00   with   Room 401  ");

    assertTrue(result);
    String log = modelLog.toString();
    assertTrue("Should trim property", log.contains("location"));
    assertTrue("Should trim subject", log.contains("Meeting"));
    assertTrue("Should trim new value", log.contains("Room 401"));
    assertTrue(viewLog.toString().contains("Series edited successfully"));
  }

  @Test
  public void testParseAndExecuteEditEventsWithLongValue() {
    boolean result = executor.parseAndExecute(
        "edit events description Meeting from 2025-01-15T10:00 with "
            + "This is a very long description with multiple words and details");

    assertTrue(result);
    assertTrue(modelLog.toString().contains(
        "This is a very long description with multiple words and details"));
    assertTrue(viewLog.toString().contains("Events edited successfully"));
  }

  @Test
  public void testParseAndExecuteEditSeriesWithLongValue() {
    boolean result = executor.parseAndExecute(
        "edit series description Meeting from 2025-01-15T10:00 with "
            + "This is a very long description for the entire series");

    assertTrue(result);
    assertTrue(modelLog.toString().contains(
        "This is a very long description for the entire series"));
    assertTrue(viewLog.toString().contains("Series edited successfully"));
  }

  @Test
  public void testHandleEditCalendarWithNameProperty() {
    boolean result = executor.parseAndExecute(
        "edit calendar --name work --property name newwork");

    assertTrue(result);
    assertTrue(modelLog.toString().contains("editCalendar:work,name,newwork"));
    assertTrue(viewLog.toString().contains("Calendar property edited successfully"));
  }

  @Test
  public void testHandleEditCalendarWithTimezoneProperty() {
    boolean result = executor.parseAndExecute(
        "edit calendar --name work --property timezone America/Los_Angeles");

    assertTrue(result);
    assertTrue(modelLog.toString().contains("editCalendar:work,timezone,America/Los_Angeles"));
    assertTrue(viewLog.toString().contains("Calendar property edited successfully"));
  }

  @Test
  public void testHandleEditCalendarTrimsParameters() {
    boolean result = executor.parseAndExecute(
        "edit calendar --name   mycal   --property   timezone   UTC  ");

    assertTrue(result);
    String log = modelLog.toString();
    assertTrue("Should trim all parameters", log.contains("editCalendar:mycal,timezone,UTC"));
    assertTrue(viewLog.toString().contains("Calendar property edited successfully"));
  }

  @Test
  public void testExportWithValidCsvExtension() {
    mockModel.setAllEvents(Collections.emptyList());

    boolean result = executor.parseAndExecute("export cal calendar.csv");

    assertTrue("Command should be parsed", result);
    assertTrue("Should call getAllEvents", modelLog.toString().contains("getAllEvents"));
    assertTrue("Should export successfully",
        viewLog.toString().contains("Calendar exported successfully"));
  }

  @Test
  public void testExportWithValidIcalExtension() {
    mockModel.setAllEvents(Collections.emptyList());
    mockModel.setCurrentCalendarTimezone("UTC");

    boolean result = executor.parseAndExecute("export cal calendar.ical");

    assertTrue("Command should be parsed", result);
    assertTrue("Should call getAllEvents", modelLog.toString().contains("getAllEvents"));
    assertTrue("Should call getCurrentCalendarTimezone",
        modelLog.toString().contains("getCurrentCalendarTimezone"));
    assertTrue("Should export successfully",
        viewLog.toString().contains("Calendar exported successfully"));
  }

  @Test
  public void testExportWithValidIcsExtension() {
    mockModel.setAllEvents(Collections.emptyList());
    mockModel.setCurrentCalendarTimezone("UTC");

    boolean result = executor.parseAndExecute("export cal calendar.ics");

    assertTrue("Command should be parsed", result);
    assertTrue("Should export successfully",
        viewLog.toString().contains("Calendar exported successfully"));
  }
}