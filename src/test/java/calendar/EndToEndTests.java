package calendar;

import static org.junit.Assert.assertEquals;

import calendar.controller.CalendarController;
import calendar.controller.IcalendarController;
import calendar.model.CalendarSystem;
import calendar.model.CalendarSystemModel;
import calendar.view.CalendarView;
import calendar.view.IcalendarView;
import java.io.StringReader;
import org.junit.Before;
import org.junit.Test;

/**
 * End-to-end tests for the calendar application.
 * Tests cover create, edit, query, and export functionality.
 * All tests use assertEquals and verify results via print queries.
 */
public class EndToEndTests {

  private CalendarSystemModel model;
  private StringBuilder output;
  private StringBuilder errorOutput;
  private IcalendarView view;

  /**
   * Sets up the test environment before each test.
   */
  @Before
  public void setUp() {
    model = new CalendarSystem();
    output = new StringBuilder();
    errorOutput = new StringBuilder();
    view = new CalendarView(output, errorOutput);
  }

  /**
   * Helper method to run commands and capture output.
   */
  private void runCommands(String commands) {
    output.setLength(0);
    errorOutput.setLength(0);
    StringReader input = new StringReader(commands);
    IcalendarController controller = new CalendarController(model, view, input);
    controller.run();
  }

  @Test
  public void testCreateSingleEvent() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String expected = "* Meeting starting on 2025-05-01 at 10:00, ending on 2025-05-01 at 11:00";
    String actual = output.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testCreateMultiDayEvent() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Conference from 2025-05-01T09:00 to 2025-05-03T17:00",
        "print events on 2025-05-02",
        "exit"
    );
    runCommands(commands);

    String expected = "* Conference starting on 2025-05-01 at 09:00, ending on 2025-05-03 at 17:00";
    String actual = output.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testCreateAllDayEvent() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Holiday on 2025-05-01",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String expected = "* Holiday starting on 2025-05-01 at 08:00, ending on 2025-05-01 at 17:00";
    String actual = output.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testCreateDuplicateEventFails() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00",
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String expected = "already exists";
    String actual = errorOutput.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testCreateEventWithInvalidTimeOrder() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Invalid from 2025-05-01T11:00 to 2025-05-01T10:00",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String expected = "must be before end time";
    String actual = errorOutput.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testCreateEventSeriesWithCount() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Standup from 2025-05-05T10:00 to 2025-05-05T10:30 repeats MWF for 6 times",
        "print events from 2025-05-05T00:00 to 2025-05-31T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    int count = 0;
    int index = 0;
    while ((index = actual.indexOf("* Standup", index)) != -1) {
      count++;
      index += "* Standup".length();
    }
    int expected = 6;
    assertEquals(expected, count);
  }

  @Test
  public void testCreateEventSeriesUntilDate() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event \"Weekly Review\" from 2025-05-02T14:00 "
            + "to 2025-05-02T15:00 repeats F until 2025-05-30",
        "print events from 2025-05-01T00:00 to 2025-05-31T23:59",
        "exit"
    );
    runCommands(commands);

    String expected = "* Weekly Review starting on 2025-05-02 at 14:00, "
        + "ending on 2025-05-02 at 15:00";
    String actual = output.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testCreateAllDaySeriesWithCount() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event \"Gym Day\" on 2025-05-05 repeats MWF for 10 times",
        "print events from 2025-05-05T00:00 to 2025-05-31T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    int count = 0;
    int index = 0;
    while ((index = actual.indexOf("* Gym Day", index)) != -1) {
      count++;
      index += "* Gym Day".length();
    }
    int expected = 10;
    assertEquals(expected, count);
  }

  @Test
  public void testCreateAllDaySeriesUntilDate() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event \"Weekend Fun\" on 2025-05-03 repeats SU until 2025-05-31",
        "print events from 2025-05-01T00:00 to 2025-05-31T23:59",
        "exit"
    );
    runCommands(commands);

    String expected = "* Weekend Fun starting on 2025-05-03 at 08:00, "
        + "ending on 2025-05-03 at 17:00";
    String actual = output.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testCreateSeriesOnMultipleDays() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Class from 2025-05-01T09:00 to 2025-05-01T10:30 repeats TR for 8 times",
        "print events from 2025-05-01T00:00 to 2025-05-31T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    int count = 0;
    int index = 0;
    while ((index = actual.indexOf("* Class", index)) != -1) {
      count++;
      index += "* Class".length();
    }
    int expected = 8;
    assertEquals(expected, count);
  }

  @Test
  public void testEditSingleEventSubject() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00",
        "edit event subject Meeting from 2025-05-01T10:00 "
            + "to 2025-05-01T11:00 with \"Important Meeting\"",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String expected = "* Important Meeting starting on 2025-05-01 at 10:00, "
        + "ending on 2025-05-01 at 11:00";
    String actual = output.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testEditSingleEventStartTime() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00",
        "edit event start Meeting from 2025-05-01T10:00 "
            + "to 2025-05-01T11:00 with 2025-05-01T10:30",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String expected = "* Meeting starting on 2025-05-01 at 10:30, ending on 2025-05-01 at 11:00";
    String actual = output.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testEditSingleEventEndTime() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00",
        "edit event end Meeting from 2025-05-01T10:00 to 2025-05-01T11:00 with 2025-05-01T11:30",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String expected = "* Meeting starting on 2025-05-01 at 10:00, ending on 2025-05-01 at 11:30";
    String actual = output.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testEditSingleEventLocation() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00",
        "edit event location Meeting from 2025-05-01T10:00 to 2025-05-01T11:00 with \"Room 101\"",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String expected = "* Meeting starting on 2025-05-01 at 10:00, "
        + "ending on 2025-05-01 at 11:00, location: Room 101";
    String actual = output.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testEditSingleEventDescription() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00",
        "edit event description Meeting from 2025-05-01T10:00 "
            + "to 2025-05-01T11:00 with \"Quarterly review meeting\"",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String expected = "* Meeting starting on 2025-05-01 at 10:00, "
        + "ending on 2025-05-01 at 11:00, description: Quarterly review meeting";
    String actual = output.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testEditSingleEventStatus() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00",
        "edit event status Meeting from 2025-05-01T10:00 to 2025-05-01T11:00 with private",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String expected = "* Meeting starting on 2025-05-01 at 10:00, "
        + "ending on 2025-05-01 at 11:00, status: private";
    String actual = output.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testEditEventNotFound() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "edit event subject Nonexistent from 2025-05-01T10:00 to 2025-05-01T11:00 with NewName",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String expected = "Event not found";
    String actual = errorOutput.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testEditSeriesSubject() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Standup from 2025-05-05T10:00 to 2025-05-05T10:30 repeats MWF for 6 times",
        "edit series subject Standup from 2025-05-05T10:00 with \"Daily Standup\"",
        "print events from 2025-05-05T00:00 to 2025-05-31T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    int count = 0;
    int index = 0;
    while ((index = actual.indexOf("* Daily Standup", index)) != -1) {
      count++;
      index += "* Daily Standup".length();
    }
    int expected = 6;
    assertEquals(expected, count);
  }

  @Test
  public void testEditSeriesStartTime() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Standup from 2025-05-05T10:00 to 2025-05-05T10:30 repeats MWF for 6 times",
        "edit series start Standup from 2025-05-05T10:00 with 2025-05-05T09:00",
        "print events from 2025-05-05T00:00 to 2025-05-31T23:59",
        "exit"
    );
    runCommands(commands);

    String expected = "* Standup starting on 2025-05-05 at 09:00, ending on 2025-05-05 at 10:30";
    String actual = output.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testEditSeriesLocation() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Standup from 2025-05-05T10:00 to 2025-05-05T10:30 repeats MWF for 6 times",
        "edit series location Standup from 2025-05-05T10:00 with \"Conference Room A\"",
        "print events from 2025-05-05T00:00 to 2025-05-31T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    int count = 0;
    int index = 0;
    while ((index = actual.indexOf("location: Conference Room A", index)) != -1) {
      count++;
      index += "location: Conference Room A".length();
    }
    int expected = 6;
    assertEquals(expected, count);
  }

  @Test
  public void testEditEventsFromDateForward() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Standup from 2025-05-04T10:00 to 2025-05-04T10:30 repeats MW for 6 times",
        "edit events subject Standup from 2025-05-12T10:00 with \"Updated Standup\"",
        "print events from 2025-05-04T00:00 to 2025-05-31T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("* Standup"));
    assertEquals(true, actual.contains("* Updated Standup"));
  }

  @Test
  public void testComplexSeriesEditingScenario() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event First from 2025-05-05T10:00 to 2025-05-05T11:00 repeats MW for 6 times",
        "edit events subject First from 2025-05-12T10:00 with Second",
        "edit series subject First from 2025-05-05T10:00 with Third",
        "edit events start Third from 2025-05-12T10:00 with 2025-05-12T10:30",
        "edit series subject Third from 2025-05-05T10:00 with Fourth",
        "edit series subject Third from 2025-05-12T10:30 with Fifth",
        "print events from 2025-05-05T00:00 to 2025-05-31T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    int fourthCount = 0;
    int fifthCount = 0;
    int index = 0;

    while ((index = actual.indexOf("* Fourth", index)) != -1) {
      fourthCount++;
      index += "* Fourth".length();
    }

    index = 0;
    while ((index = actual.indexOf("* Fifth", index)) != -1) {
      fifthCount++;
      index += "* Fifth".length();
    }

    int expectedFourth = 2;
    int expectedFifth = 4;
    assertEquals(expectedFourth, fourthCount);
    assertEquals(expectedFifth, fifthCount);
  }

  @Test
  public void testEditSeriesStartTimeChangeOnlySingleInstance() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Meeting from 2025-05-05T10:00 to 2025-05-05T11:00 repeats MW for 4 times",
        "edit event start Meeting from 2025-05-05T10:00 to 2025-05-05T11:00 with 2025-05-05T14:00",
        "print events from 2025-05-05T00:00 to 2025-05-31T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("at 14:00"));
    assertEquals(true, actual.contains("at 10:00"));
  }

  @Test
  public void testEditSeriesCannotChangeDateOnlyTime() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Meeting from 2025-05-05T10:00 to 2025-05-05T11:00 repeats MW for 4 times",
        "edit series start Meeting from 2025-05-05T10:00 with 2025-05-06T10:00",
        "print events from 2025-05-05T00:00 to 2025-05-31T23:59",
        "exit"
    );
    runCommands(commands);

    String expected = "Cannot change the date for series events";
    String actual = errorOutput.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testPrintEventsOnDate() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Morning from 2025-05-01T09:00 to 2025-05-01T10:00",
        "create event Afternoon from 2025-05-01T14:00 to 2025-05-01T15:00",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String expected1 = "* Morning starting on 2025-05-01 at 09:00, ending on 2025-05-01 at 10:00";
    String expected2 = "* Afternoon starting on 2025-05-01 at 14:00, ending on 2025-05-01 at 15:00";
    String actual = output.toString();
    assertEquals(true, actual.contains(expected1));
    assertEquals(true, actual.contains(expected2));
  }

  @Test
  public void testPrintEventsOnDateWithNoEvents() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String expected = "No events found.";
    String actual = output.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testPrintEventsInRange() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Event1 from 2025-05-01T10:00 to 2025-05-01T11:00",
        "create event Event2 from 2025-05-03T10:00 to 2025-05-03T11:00",
        "create event Event3 from 2025-05-05T10:00 to 2025-05-05T11:00",
        "print events from 2025-05-01T00:00 to 2025-05-03T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("* Event1"));
    assertEquals(true, actual.contains("* Event2"));
    assertEquals(false, actual.contains("* Event3"));
  }

  @Test
  public void testPrintEventsShowsLocation() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00",
        "edit event location Meeting from 2025-05-01T10:00 to 2025-05-01T11:00 with \"Room 101\"",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String expected = "* Meeting starting on 2025-05-01 at 10:00, "
        + "ending on 2025-05-01 at 11:00, location: Room 101";
    String actual = output.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testPrintEventsShowsDescription() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00",
        "edit event description Meeting from 2025-05-01T10:00 "
            + "to 2025-05-01T11:00 with \"Important discussion\"",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String expected = "* Meeting starting on 2025-05-01 at 10:00, "
        + "ending on 2025-05-01 at 11:00, description: Important discussion";
    String actual = output.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testPrintMultiDayEventInRange() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Conference from 2025-05-01T09:00 to 2025-05-03T17:00",
        "print events from 2025-05-02T00:00 to 2025-05-02T23:59",
        "exit"
    );
    runCommands(commands);

    String expected = "* Conference starting on 2025-05-01 at 09:00, ending on 2025-05-03 at 17:00";
    String actual = output.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testShowStatusBusy() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00",
        "show status on 2025-05-01T10:30",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String expected = "busy";
    String actual = output.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testShowStatusAvailable() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00",
        "show status on 2025-05-01T14:00",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String expected = "available";
    String actual = output.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testShowStatusAtStartTime() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00",
        "show status on 2025-05-01T10:00",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String expected = "busy";
    String actual = output.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testShowStatusAtEndTime() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00",
        "show status on 2025-05-01T11:00",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String expected = "available";
    String actual = output.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testShowStatusBusyWithMultipleEvents() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Meeting1 from 2025-05-01T10:00 to 2025-05-01T11:00",
        "create event Meeting2 from 2025-05-01T14:00 to 2025-05-01T15:00",
        "show status on 2025-05-01T14:30",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String expected = "busy";
    String actual = output.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testExportCalendar() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00",
        "export cal test_calendar.csv",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String expected = "Calendar exported successfully";
    String actual = output.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testExportEmptyCalendar() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "export cal empty_calendar.csv",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String expected = "Calendar exported successfully";
    String actual = output.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testInvalidCommand() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "invalid command here",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String expected = "Invalid command";
    String actual = errorOutput.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testInvalidProperty() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00",
        "edit event invalidprop Meeting from 2025-05-01T10:00 to 2025-05-01T11:00 with value",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String expected = "Invalid property";
    String actual = errorOutput.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testEmptyLinesAndComments() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "",
        "# This is a comment",
        "",
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00",
        "",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String expected = "* Meeting starting on 2025-05-01 at 10:00, ending on 2025-05-01 at 11:00";
    String actual = output.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testAllDayEventDefaultTimes() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Holiday on 2025-05-01",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String expected = "* Holiday starting on 2025-05-01 at 08:00, ending on 2025-05-01 at 17:00";
    String actual = output.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testAllDayEventBusyStatus() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Holiday on 2025-05-01",
        "show status on 2025-05-01T09:00",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String expected = "busy";
    String actual = output.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testAllDayEventAvailableBeforeStart() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Holiday on 2025-05-01",
        "show status on 2025-05-01T07:00",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String expected = "available";
    String actual = output.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testAllDayEventAvailableAfterEnd() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Holiday on 2025-05-01",
        "show status on 2025-05-01T18:00",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String expected = "available";
    String actual = output.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testCreateEventAtMidnight() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event \"Midnight Meeting\" from 2025-05-01T00:00 to 2025-05-01T01:00",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String expected = "* Midnight Meeting starting on 2025-05-01 at 00:00, "
        + "ending on 2025-05-01 at 01:00";
    String actual = output.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testCreateEventJustBeforeMidnight() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event \"Late Night\" from 2025-05-01T23:00 to 2025-05-01T23:59",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String expected = "* Late Night starting on 2025-05-01 at 23:00, ending on 2025-05-01 at 23:59";
    String actual = output.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testSeriesWithSingleOccurrence() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Solo from 2025-05-04T10:00 to 2025-05-04T11:00 repeats M for 1 times",
        "print events from 2025-05-04T00:00 to 2025-05-10T23:59",
        "exit"
    );
    runCommands(commands);

    String expected = "* Solo starting on 2025-05-04 at 10:00, ending on 2025-05-04 at 11:00";
    String actual = output.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testSeriesWithAllWeekdays() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Everyday from 2025-05-05T10:00 "
            + "to 2025-05-05T11:00 repeats MTWRFSU for 7 times",
        "print events from 2025-05-05T00:00 to 2025-05-15T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    int count = 0;
    int index = 0;
    while ((index = actual.indexOf("* Everyday", index)) != -1) {
      count++;
      index += "* Everyday".length();
    }
    int expected = 7;
    assertEquals(expected, count);
  }

  @Test
  public void testEditCreatesNoDuplicateCheck() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Meeting1 from 2025-05-01T10:00 to 2025-05-01T11:00",
        "create event Meeting2 from 2025-05-01T14:00 to 2025-05-01T15:00",
        "edit event subject Meeting2 from 2025-05-01T14:00 to 2025-05-01T15:00 with Meeting1",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("* Meeting1"));
  }

  @Test
  public void testEditWouldCreateDuplicateFails() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Meeting1 from 2025-05-01T10:00 to 2025-05-01T11:00",
        "create event Meeting2 from 2025-05-01T14:00 to 2025-05-01T15:00",
        "edit event subject Meeting2 from 2025-05-01T14:00 to 2025-05-01T15:00 with Meeting1",
        "edit event start Meeting1 from 2025-05-01T10:00 to 2025-05-01T11:00 with 2025-05-01T14:00",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String expected = "duplicate";
    String actual = errorOutput.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testSeriesSplitsWhenStartTimeChanges() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Meeting from 2025-05-05T10:00 to 2025-05-05T11:00 repeats MW for 6 times",
        "edit events start Meeting from 2025-05-12T10:00 with 2025-05-12T14:00",
        "edit series subject Meeting from 2025-05-05T10:00 with FirstBatch",
        "print events from 2025-05-05T00:00 to 2025-05-31T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("* FirstBatch"));
    assertEquals(true, actual.contains("* Meeting"));
  }

  @Test
  public void testEditSingleInstanceRemovesFromSeries() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Meeting from 2025-05-05T10:00 to 2025-05-05T11:00 repeats MW for 4 times",
        "edit event start Meeting from 2025-05-07T10:00 to 2025-05-07T11:00 with 2025-05-07T15:00",
        "edit series subject Meeting from 2025-05-05T10:00 with UpdatedSeries",
        "print events from 2025-05-05T00:00 to 2025-05-31T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("* UpdatedSeries"));
    assertEquals(true, actual.contains("* Meeting"));
  }

  @Test
  public void testPrintEventsAreSortedByStartTime() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Evening from 2025-05-01T18:00 to 2025-05-01T19:00",
        "create event Morning from 2025-05-01T08:00 to 2025-05-01T09:00",
        "create event Afternoon from 2025-05-01T14:00 to 2025-05-01T15:00",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    int morningIdx = actual.indexOf("* Morning");
    int afternoonIdx = actual.indexOf("* Afternoon");
    int eveningIdx = actual.indexOf("* Evening");

    assertEquals(true, morningIdx > 0 && afternoonIdx > morningIdx);
    assertEquals(true, afternoonIdx > 0 && eveningIdx > afternoonIdx);
  }

  @Test
  public void testPrintRangeIncludesPartialOverlap() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event LongMeeting from 2025-05-01T10:00 to 2025-05-01T16:00",
        "print events from 2025-05-01T14:00 to 2025-05-01T15:00",
        "exit"
    );
    runCommands(commands);

    String expected = "* LongMeeting starting on 2025-05-01 at 10:00, "
        + "ending on 2025-05-01 at 16:00";
    String actual = output.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testCompleteWorkflow() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "# Create some events",
        "create event \"Morning Standup\" from 2025-05-05T09:00 "
            + "to 2025-05-05T09:30 repeats MTWRF for 10 times",
        "create event Lunch on 2025-05-05",
        "create event \"Project Review\" from 2025-05-05T14:00 to 2025-05-05T15:00",
        "",
        "# Edit an event",
        "edit event location \"Project Review\" from 2025-05-05T14:00 "
            + "to 2025-05-05T15:00 with \"Conference Room B\"",
        "",
        "# Query events",
        "print events on 2025-05-05",
        "show status on 2025-05-05T09:15",
        "",
        "# Export",
        "export cal workflow_test.csv",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("busy"));
    assertEquals(true, actual.contains("Calendar exported successfully"));

    String expectedLocation = "* Project Review starting on 2025-05-05 at 14:00, "
        + "ending on 2025-05-05 at 15:00, location: Conference Room B";
    assertEquals(true, actual.contains(expectedLocation));
  }

  @Test
  public void testCannotCreateDuplicateEvent() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00",
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String expected = "already exists";
    String actual = errorOutput.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testCanCreateSameSubjectDifferentTimes() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00",
        "create event Meeting from 2025-05-01T14:00 to 2025-05-01T15:00",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    int count = 0;
    int index = 0;
    while ((index = actual.indexOf("* Meeting", index)) != -1) {
      count++;
      index += "* Meeting".length();
    }
    int expected = 2;
    assertEquals(expected, count);
  }

  @Test
  public void testEditSubjectCannotCreateDuplicate() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Meeting1 from 2025-05-01T10:00 to 2025-05-01T11:00",
        "create event Meeting2 from 2025-05-01T10:00 to 2025-05-01T11:00",
        "edit event subject Meeting1 from 2025-05-01T10:00 to 2025-05-01T11:00 with Meeting2",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String expected = "duplicate";
    String actual = errorOutput.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testSeriesCannotCreateDuplicateOccurrence() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Meeting from 2025-05-05T10:00 to 2025-05-05T11:00",
        "create event Meeting from 2025-05-05T10:00 to 2025-05-05T11:00 repeats MW for 3 times",
        "print events from 2025-05-05T00:00 to 2025-05-31T23:59",
        "exit"
    );
    runCommands(commands);

    String expected = "duplicate";
    String actual = errorOutput.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testCannotCreateEventWithStartAfterEnd() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Invalid from 2025-05-01T11:00 to 2025-05-01T10:00",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String expected = "must be before end time";
    String actual = errorOutput.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testCannotCreateEventWithStartEqualToEnd() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Invalid from 2025-05-01T10:00 to 2025-05-01T10:00",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String expected = "must be before end time";
    String actual = errorOutput.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testEditEndTimeCannotBeBeforeStart() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00",
        "edit event end Meeting from 2025-05-01T10:00 to 2025-05-01T11:00 with 2025-05-01T09:00",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String expected = "must be after the start time";
    String actual = errorOutput.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testCannotEditSeriesEventDate() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Meeting from 2025-05-05T10:00 to 2025-05-05T11:00 repeats MW for 3 times",
        "edit series start Meeting from 2025-05-05T10:00 with 2025-05-06T10:00",
        "print events from 2025-05-05T00:00 to 2025-05-31T23:59",
        "exit"
    );
    runCommands(commands);

    String expected = "Cannot change the date for series events";
    String actual = errorOutput.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testCannotEditSeriesStartTimeToCreateMultiDayEvent() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Meeting from 2025-05-05T10:00 to 2025-05-05T11:00 repeats MW for 3 times",
        "edit series start Meeting from 2025-05-05T10:00 with 2025-05-05T23:30",
        "print events from 2025-05-05T00:00 to 2025-05-31T23:59",
        "exit"
    );
    runCommands(commands);

    String expected = "Series events must span a single day";
    String actual = errorOutput.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testEditNonExistentEvent() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "edit event subject Nonexistent from 2025-05-01T10:00 to 2025-05-01T11:00 with NewName",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String expected = "Event not found";
    String actual = errorOutput.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testEditEventWithWrongStartTime() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00",
        "edit event subject Meeting from 2025-05-01T14:00 to 2025-05-01T11:00 with NewName",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String expected = "Event not found";
    String actual = errorOutput.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testOverlappingEventsAllowed() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Meeting1 from 2025-05-01T10:00 to 2025-05-01T11:00",
        "create event Meeting2 from 2025-05-01T10:30 to 2025-05-01T11:30",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String expected1 = "* Meeting1 starting on 2025-05-01 at 10:00, ending on 2025-05-01 at 11:00";
    String expected2 = "* Meeting2 starting on 2025-05-01 at 10:30, ending on 2025-05-01 at 11:30";
    String actual = output.toString();
    assertEquals(true, actual.contains(expected1));
    assertEquals(true, actual.contains(expected2));
  }

  @Test
  public void testEventSpanningMidnight() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event MidnightEvent from 2025-05-01T23:00 to 2025-05-02T01:00",
        "print events on 2025-05-01",
        "print events on 2025-05-02",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    int count = 0;
    int index = 0;
    while ((index = actual.indexOf("* MidnightEvent", index)) != -1) {
      count++;
      index += "* MidnightEvent".length();
    }
    int expected = 2;
    assertEquals(expected, count);
  }

  @Test
  public void testVeryLongEvent() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event LongConference from 2025-05-01T09:00 to 2025-05-10T17:00",
        "print events from 2025-05-05T00:00 to 2025-05-05T23:59",
        "exit"
    );
    runCommands(commands);

    String expected = "* LongConference starting on 2025-05-01 at 09:00, "
        + "ending on 2025-05-10 at 17:00";
    String actual = output.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testEventWithSpecialCharactersInSubject() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event \"Meeting@#$%\" from 2025-05-01T10:00 to 2025-05-01T11:00",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String expected = "* Meeting@#$% starting on 2025-05-01 at 10:00, "
        + "ending on 2025-05-01 at 11:00";
    String actual = output.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testMultipleSeriesWithSameSubjectDifferentTimes() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Standup from 2025-05-05T09:00 to 2025-05-05T09:30 repeats MW for 3 times",
        "create event Standup from 2025-05-05T14:00 to 2025-05-05T14:30 repeats MW for 3 times",
        "print events from 2025-05-05T00:00 to 2025-05-31T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    int count = 0;
    int index = 0;
    while ((index = actual.indexOf("* Standup", index)) != -1) {
      count++;
      index += "* Standup".length();
    }
    int expected = 6;
    assertEquals(expected, count);
  }

  @Test
  public void testEditSeriesThenEditSingleInstance() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Meeting from 2025-05-05T10:00 to 2025-05-05T11:00 repeats MW for 4 times",
        "edit series location Meeting from 2025-05-05T10:00 with \"Room A\"",
        "edit event location Meeting from 2025-05-07T10:00 to 2025-05-07T11:00 with \"Room B\"",
        "print events from 2025-05-05T00:00 to 2025-05-31T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("location: Room A"));
    assertEquals(true, actual.contains("location: Room B"));
  }

  @Test
  public void testCreateMultiDaySeriesShouldFail() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Invalid from 2025-05-15T22:00 to 2025-05-16T02:00 repeats MW for 3 times",
        "exit"
    );
    runCommands(commands);

    String actual = errorOutput.toString();
    assertEquals(true, actual.contains("single day") || actual.contains("before end"));
  }

  @Test
  public void testSpecificationExampleWorkflow() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event First from 2025-05-05T10:00 to 2025-05-05T11:00 repeats MW for 6 times",
        "edit events subject First from 2025-05-12T10:00 with Second",
        "edit series subject First from 2025-05-05T10:00 with Third",
        "edit events start Third from 2025-05-12T10:00 with 2025-05-12T10:30",
        "edit series subject Third from 2025-05-05T10:00 with Fourth",
        "edit series subject Third from 2025-05-12T10:30 with Fifth",
        "print events from 2025-05-05T00:00 to 2025-05-31T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();

    int fourthCount = 0;
    int fifthCount = 0;
    int index = 0;

    while ((index = actual.indexOf("* Fourth", index)) != -1) {
      fourthCount++;
      index += "* Fourth".length();
    }

    index = 0;
    while ((index = actual.indexOf("* Fifth", index)) != -1) {
      fifthCount++;
      index += "* Fifth".length();
    }

    assertEquals("Should have 2 events named Fourth (May 5 and 7)", 2, fourthCount);
    assertEquals("Should have 4 events named Fifth (May 12, 14, 19, 21)", 4, fifthCount);
  }

  @Test
  public void testEditEndTimeCausingMultiDayInSeriesShouldFail() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Meeting from 2025-05-05T22:00 to 2025-05-05T23:00 repeats MW for 3 times",
        "edit event end Meeting from 2025-05-05T22:00 to 2025-05-05T23:00 with 2025-05-06T01:00",
        "exit"
    );
    runCommands(commands);

    String actual = errorOutput.toString();
    assertEquals(true, actual.contains("single day") || actual.contains("after the start"));
  }

  @Test
  public void testEditStartTimeCausingMultiDayInSeriesShouldFail() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Meeting from 2025-05-05T23:00 to 2025-05-05T23:30 repeats MW for 3 times",
        "edit series start Meeting from 2025-05-05T23:00 with 2025-05-05T23:45",
        "print events from 2025-05-05T00:00 to 2025-05-31T23:59",
        "exit"
    );
    runCommands(commands);

    String expected = "must span a single day";
    String actual = errorOutput.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testInvalidWeekdayCode() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Meeting from 2025-05-05T10:00 to 2025-05-05T11:00 repeats XYZ for 3 times",
        "print events on 2025-05-05",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(false, actual.contains("* Meeting"));
  }

  @Test
  public void testEditEventsLocation() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Meeting from 2025-05-05T10:00 to 2025-05-05T11:00 repeats MW for 4 times",
        "edit events location Meeting from 2025-05-12T10:00 with \"Room B\"",
        "print events from 2025-05-05T00:00 to 2025-05-31T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    int locationCount = 0;
    int index = 0;
    while ((index = actual.indexOf("location: Room B", index)) != -1) {
      locationCount++;
      index += "location: Room B".length();
    }
    assertEquals("Should have location on 2 events (May 12, 14)", 2, locationCount);
  }

  @Test
  public void testEditEventsDescription() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Meeting from 2025-05-05T10:00 to 2025-05-05T11:00 repeats MW for 4 times",
        "edit events description Meeting from 2025-05-12T10:00 with \"Updated description\"",
        "print events from 2025-05-05T00:00 to 2025-05-31T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    int descCount = 0;
    int index = 0;
    while ((index = actual.indexOf("description: Updated description", index)) != -1) {
      descCount++;
      index += "description: Updated description".length();
    }
    assertEquals("Should have description on 2 events", 2, descCount);
  }

  @Test
  public void testEditEventsStatus() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Meeting from 2025-05-05T10:00 to 2025-05-05T11:00 repeats MW for 4 times",
        "edit events status Meeting from 2025-05-12T10:00 with private",
        "print events from 2025-05-05T00:00 to 2025-05-31T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    int statusCount = 0;
    int index = 0;
    while ((index = actual.indexOf("status: private", index)) != -1) {
      statusCount++;
      index += "status: private".length();
    }
    assertEquals("Should have private status on 2 events", 2, statusCount);
  }

  @Test
  public void testEditSeriesDescription() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Meeting from 2025-05-05T10:00 to 2025-05-05T11:00 repeats MW for 4 times",
        "edit series description Meeting from 2025-05-05T10:00 with \"Team sync meeting\"",
        "print events from 2025-05-05T00:00 to 2025-05-31T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    int descCount = 0;
    int index = 0;
    while ((index = actual.indexOf("description: Team sync meeting", index)) != -1) {
      descCount++;
      index += "description: Team sync meeting".length();
    }
    assertEquals("Should have description on all 4 events", 4, descCount);
  }

  @Test
  public void testEditSeriesStatus() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Meeting from 2025-05-05T10:00 to 2025-05-05T11:00 repeats MW for 4 times",
        "edit series status Meeting from 2025-05-05T10:00 with private",
        "print events from 2025-05-05T00:00 to 2025-05-31T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    int statusCount = 0;
    int index = 0;
    while ((index = actual.indexOf("status: private", index)) != -1) {
      statusCount++;
      index += "status: private".length();
    }
    assertEquals("Should have private status on all 4 events", 4, statusCount);
  }

  @Test
  public void testEditEventsCreatingDuplicateShouldFail() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Meeting from 2025-05-05T10:00 to 2025-05-05T11:00 repeats MW for 3 times",
        "create event Standup from 2025-05-05T14:00 to 2025-05-05T15:00",
        "edit events subject Meeting from 2025-05-05T10:00 with Standup",
        "edit events start Standup from 2025-05-05T10:00 with 2025-05-05T14:00",
        "exit"
    );
    runCommands(commands);

    String actual = errorOutput.toString();
    assertEquals(true, actual.contains("duplicate"));
  }

  @Test
  public void testEditSeriesCreatingDuplicateShouldFail() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Meeting from 2025-05-05T10:00 to 2025-05-05T11:00 repeats MW for 3 times",
        "create event Standup from 2025-05-05T14:00 to 2025-05-05T15:00",
        "edit series subject Meeting from 2025-05-05T10:00 with Standup",
        "edit series start Standup from 2025-05-05T10:00 with 2025-05-05T14:00",
        "exit"
    );
    runCommands(commands);

    String actual = errorOutput.toString();
    assertEquals(true, actual.contains("duplicate"));
  }

  @Test
  public void testShowStatusJustBeforeEventStart() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00",
        "show status on 2025-05-01T09:59",
        "exit"
    );
    runCommands(commands);

    String expected = "available";
    String actual = output.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testQueryEventsWithMultipleEventsOnSameDay() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Event1 from 2025-05-01T08:00 to 2025-05-01T09:00",
        "create event Event2 from 2025-05-01T10:00 to 2025-05-01T11:00",
        "create event Event3 from 2025-05-01T14:00 to 2025-05-01T15:00",
        "create event Event4 from 2025-05-01T16:00 to 2025-05-01T17:00",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("* Event1"));
    assertEquals(true, actual.contains("* Event2"));
    assertEquals(true, actual.contains("* Event3"));
    assertEquals(true, actual.contains("* Event4"));
  }

  @Test
  public void testCannotEditSeriesEventToChangeDate() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Meeting from 2025-05-05T10:00 to 2025-05-05T11:00 repeats MW for 3 times",
        "edit events start Meeting from 2025-05-05T10:00 with 2025-05-06T10:00",
        "print events from 2025-05-05T00:00 to 2025-05-31T23:59",
        "exit"
    );
    runCommands(commands);

    String expected = "Cannot change the date";
    String actual = errorOutput.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testEditSingleEventInSeriesOnlyAffectsThatInstance() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Meeting from 2025-05-05T10:00 to 2025-05-05T11:00 repeats MW for "
            + "5 times",
        "edit event subject Meeting from 2025-05-12T10:00 to 2025-05-12T11:00 with \"Special"
            + " Meeting\"",
        "print events from 2025-05-05T00:00 to 2025-05-31T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();

    int meetingCount = 0;
    int specialCount = 0;
    int index = 0;

    while ((index = actual.indexOf("* Meeting", index)) != -1) {
      meetingCount++;
      index += "* Meeting".length();
    }

    index = 0;
    while ((index = actual.indexOf("* Special Meeting", index)) != -1) {
      specialCount++;
      index += "* Special Meeting".length();
    }

    assertEquals("Should have 4 events still named Meeting", 4, meetingCount);
    assertEquals("Should have 1 event named Special Meeting", 1, specialCount);
  }

  @Test
  public void testEditEventsEnd() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Meeting from 2025-05-05T10:00 to 2025-05-05T11:00 repeats MW for 4 times",
        "edit events end Meeting from 2025-05-12T10:00 with 2025-05-12T11:30",
        "print events from 2025-05-05T00:00 to 2025-05-31T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("ending on 2025-05-05 at 11:00"));
    assertEquals(true, actual.contains("ending on 2025-05-12 at 11:30"));
    assertEquals(true, actual.contains("ending on 2025-05-14 at 11:30"));
  }

  @Test
  public void testEditSeriesEnd() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Meeting from 2025-05-05T10:00 to 2025-05-05T11:00 repeats MW for 4 times",
        "edit series end Meeting from 2025-05-05T10:00 with 2025-05-05T11:45",
        "print events from 2025-05-05T00:00 to 2025-05-31T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    int endTimeCount = 0;
    int index = 0;
    while ((index = actual.indexOf("ending on 2025-05", index)) != -1) {
      if (actual.substring(index).startsWith("ending on 2025-05-05 at 11:45")
          || actual.substring(index).startsWith("ending on 2025-05-07 at 11:45")
          || actual.substring(index).startsWith("ending on 2025-05-12 at 11:45")
          || actual.substring(index).startsWith("ending on 2025-05-14 at 11:45")) {
        endTimeCount++;
      }
      index++;
    }
    assertEquals("All 4 events should end at 11:45", 4, endTimeCount);
  }

  @Test
  public void testEditEventsStart() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Meeting from 2025-05-05T10:00 to 2025-05-05T11:00 repeats MW for 4 times",
        "edit events start Meeting from 2025-05-12T10:00 with 2025-05-12T10:30",
        "print events from 2025-05-05T00:00 to 2025-05-31T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("starting on 2025-05-05 at 10:00"));
    assertEquals(true, actual.contains("starting on 2025-05-12 at 10:30"));
    assertEquals(true, actual.contains("starting on 2025-05-14 at 10:30"));
  }

  @Test
  public void testSeriesSplitCreatesTwoIndependentSeries() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Meeting from 2025-05-05T10:00 to 2025-05-05T11:00 repeats MW for 6 times",
        "edit events start Meeting from 2025-05-12T10:00 with 2025-05-12T14:00",
        "edit series subject Meeting from 2025-05-05T10:00 with MorningGroup",
        "edit series subject Meeting from 2025-05-12T14:00 with AfternoonGroup",
        "print events from 2025-05-05T00:00 to 2025-05-31T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();

    int morningCount = 0;
    int afternoonCount = 0;
    int index = 0;

    while ((index = actual.indexOf("* MorningGroup", index)) != -1) {
      morningCount++;
      index += "* MorningGroup".length();
    }

    index = 0;
    while ((index = actual.indexOf("* AfternoonGroup", index)) != -1) {
      afternoonCount++;
      index += "* AfternoonGroup".length();
    }

    assertEquals("Should have 2 morning events", 2, morningCount);
    assertEquals("Should have 4 afternoon events", 4, afternoonCount);
  }

  @Test
  public void testEditNonExistentEventInSeries() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Meeting from 2025-05-05T10:00 to 2025-05-05T11:00 repeats MW for 3 times",
        "edit events subject Meeting from 2025-05-20T10:00 with NewName",
        "print events from 2025-05-05T00:00 to 2025-05-31T23:59",
        "exit"
    );
    runCommands(commands);

    String expected = "Event not found";
    String actual = errorOutput.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testEditSeriesNonExistent() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "edit series subject Ghost from 2025-05-05T10:00 with NewName",
        "print events on 2025-05-05",
        "exit"
    );
    runCommands(commands);

    String expected = "Event not found";
    String actual = errorOutput.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testPrintRangeExcludesEventsOutsideRange() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Before from 2025-05-01T10:00 to 2025-05-01T11:00",
        "create event During from 2025-05-05T10:00 to 2025-05-05T11:00",
        "create event After from 2025-05-10T10:00 to 2025-05-10T11:00",
        "print events from 2025-05-04T00:00 to 2025-05-06T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(false, actual.contains("* Before"));
    assertEquals(true, actual.contains("* During"));
    assertEquals(false, actual.contains("* After"));
  }

  @Test
  public void testAllWeekdayCodes() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Mon from 2025-05-05T10:00 to 2025-05-05T11:00 repeats M for 2 times",
        "create event Tue from 2025-05-06T10:00 to 2025-05-06T11:00 repeats T for 2 times",
        "create event Wed from 2025-05-07T10:00 to 2025-05-07T11:00 repeats W for 2 times",
        "create event Thu from 2025-05-01T10:00 to 2025-05-01T11:00 repeats R for 2 times",
        "create event Fri from 2025-05-02T10:00 to 2025-05-02T11:00 repeats F for 2 times",
        "create event Sat from 2025-05-03T10:00 to 2025-05-03T11:00 repeats S for 2 times",
        "create event Sun from 2025-05-04T10:00 to 2025-05-04T11:00 repeats U for 2 times",
        "print events from 2025-05-01T00:00 to 2025-05-31T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("* Mon"));
    assertEquals(true, actual.contains("* Tue"));
    assertEquals(true, actual.contains("* Wed"));
    assertEquals(true, actual.contains("* Thu"));
    assertEquals(true, actual.contains("* Fri"));
    assertEquals(true, actual.contains("* Sat"));
    assertEquals(true, actual.contains("* Sun"));
  }

  @Test
  public void testEditEventEndTimeBeforeStartShouldFail() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00",
        "edit event end Meeting from 2025-05-01T10:00 to 2025-05-01T11:00 with 2025-05-01T09:00",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String expected = "must be after the start time";
    String actual = errorOutput.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testEditEventEndTimeEqualToStartShouldFail() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00",
        "edit event end Meeting from 2025-05-01T10:00 to 2025-05-01T11:00 with 2025-05-01T10:00",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String expected = "must be after the start time";
    String actual = errorOutput.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testMultipleSeparateSeriesWithSameSubject() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Meeting from 2025-05-05T09:00 to 2025-05-05T10:00 repeats MW for 3 times",
        "create event Meeting from 2025-05-05T14:00 to 2025-05-05T15:00 repeats MW for 3 times",
        "print events from 2025-05-05T00:00 to 2025-05-31T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    int count = 0;
    int index = 0;
    while ((index = actual.indexOf("* Meeting", index)) != -1) {
      count++;
      index += "* Meeting".length();
    }
    assertEquals("Should have 6 events (3 morning + 3 afternoon)", 6, count);
  }

  @Test
  public void testEventWithAllOptionalProperties() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00",
        "edit event location Meeting from 2025-05-01T10:00 to 2025-05-01T11:00 with \"Room 101\"",
        "edit event description Meeting from 2025-05-01T10:00 to 2025-05-01T11:00 with \"Important "
            + "meeting\"",
        "edit event status Meeting from 2025-05-01T10:00 to 2025-05-01T11:00 with private",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String expected = "* Meeting starting on 2025-05-01 at 10:00, "
        + "ending on 2025-05-01 at 11:00, "
        + "location: Room 101, "
        + "description: Important meeting, "
        + "status: private";
    String actual = output.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testSeriesWithAllOptionalProperties() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Meeting from 2025-05-05T10:00 to 2025-05-05T11:00 repeats MW for 3 times",
        "edit series location Meeting from 2025-05-05T10:00 with \"Conference Room\"",
        "edit series description Meeting from 2025-05-05T10:00 with \"Weekly sync\"",
        "edit series status Meeting from 2025-05-05T10:00 with private",
        "print events from 2025-05-05T00:00 to 2025-05-31T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    int fullPropsCount = 0;
    int index = 0;
    while ((index = actual.indexOf("location: Conference Room", index)) != -1) {
      String segment = actual.substring(index, Math.min(index + 200, actual.length()));
      if (segment.contains("description: Weekly sync") && segment.contains("status: private")) {
        fullPropsCount++;
      }
      index++;
    }
    assertEquals("All 3 events should have all properties", 3, fullPropsCount);
  }

  @Test
  public void testPrintRangeWithEmptyResult() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00",
        "print events from 2025-06-01T00:00 to 2025-06-30T23:59",
        "exit"
    );
    runCommands(commands);

    String expected = "No events found.";
    String actual = output.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testEditEventStartPreservesEndTime() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00",
        "edit event start Meeting from 2025-05-01T10:00 to 2025-05-01T11:00 with 2025-05-01T10:15",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String expected = "* Meeting starting on 2025-05-01 at 10:15, ending on 2025-05-01 at 11:00";
    String actual = output.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testEditEventStartAfterOriginalEndExtendsDuration() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00",
        "edit event start Meeting from 2025-05-01T10:00 to 2025-05-01T11:00 with 2025-05-01T12:00",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String expected = "* Meeting starting on 2025-05-01 at 12:00, ending on 2025-05-01 at 13:00";
    String actual = output.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testStatusAtExactEndTimeIsAvailable() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00",
        "show status on 2025-05-01T11:00",
        "exit"
    );
    runCommands(commands);

    String expected = "available";
    String actual = output.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testStatusAtExactStartTimeIsBusy() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00",
        "show status on 2025-05-01T10:00",
        "exit"
    );
    runCommands(commands);

    String expected = "busy";
    String actual = output.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testQuotedSubjectPreserved() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event \"Team Meeting: Q1 Planning\" from 2025-05-01T10:00 to 2025-05-01T11:00",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String expected = "* Team Meeting: Q1 Planning starting on 2025-05-01 at 10:00";
    String actual = output.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testEditEventsOnNonSeriesEventBehavesLikeEditEvent() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00",
        "edit events subject Meeting from 2025-05-01T10:00 with UpdatedMeeting",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String expected = "* UpdatedMeeting starting on 2025-05-01 at 10:00";
    String actual = output.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testEditSeriesOnNonSeriesEventBehavesLikeEditEvent() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00",
        "edit series subject Meeting from 2025-05-01T10:00 with UpdatedMeeting",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String expected = "* UpdatedMeeting starting on 2025-05-01 at 10:00";
    String actual = output.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testMultiDayEventShowsOnAllDays() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Conference from 2025-05-01T09:00 to 2025-05-03T17:00",
        "print events on 2025-05-01",
        "print events on 2025-05-02",
        "print events on 2025-05-03",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    int count = 0;
    int index = 0;
    while ((index = actual.indexOf("* Conference", index)) != -1) {
      count++;
      index += "* Conference".length();
    }
    assertEquals("Should appear on 3 days", 3, count);
  }

  @Test
  public void testEditSingleInstanceThenSeriesEditDoesNotAffectIt() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Meeting from 2025-05-05T10:00 to 2025-05-05T11:00 repeats MW for 4 times",
        "edit event start Meeting from 2025-05-07T10:00 to 2025-05-07T11:00 with 2025-05-07T15:00",
        "edit series subject Meeting from 2025-05-05T10:00 with UpdatedSeries",
        "print events from 2025-05-05T00:00 to 2025-05-31T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    int updatedCount = 0;
    int originalCount = 0;
    int index = 0;

    while ((index = actual.indexOf("* UpdatedSeries", index)) != -1) {
      updatedCount++;
      index += "* UpdatedSeries".length();
    }

    index = 0;
    while ((index = actual.indexOf("* Meeting", index)) != -1) {
      originalCount++;
      index += "* Meeting".length();
    }

    assertEquals("Should have 3 events in updated series", 3, updatedCount);
    assertEquals("Should have 1 event removed from series", 1, originalCount);
  }

  @Test
  public void testSeriesUntilDateInclusive() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Meeting from 2025-05-05T10:00 to 2025-05-05T11:00 repeats M until 2025-05-26",
        "print events from 2025-05-01T00:00 to 2025-05-31T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    int count = 0;
    int index = 0;
    while ((index = actual.indexOf("* Meeting", index)) != -1) {
      count++;
      index += "* Meeting".length();
    }
    assertEquals("Should create events until May 26 (4 Mondays)", 4, count);
  }

  @Test
  public void testExportShowsAbsolutePath() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "export cal test.csv",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("File location:"));
    assertEquals(true, actual.contains("test.csv"));
  }

  @Test
  public void testCanCreateEventWithSameSubjectDifferentEnd() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00",
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:30",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    int count = 0;
    int index = 0;
    while ((index = actual.indexOf("* Meeting", index)) != -1) {
      count++;
      index += "* Meeting".length();
    }
    assertEquals("Should have 2 events", 2, count);
  }

  @Test
  public void testStatusBusyDuringMultiDayEvent() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Conference from 2025-05-01T09:00 to 2025-05-03T17:00",
        "show status on 2025-05-02T12:00",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("busy"));
  }

  @Test
  public void testInvalidDateFormat() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Meeting from 01/15/2025T10:00 to 01/15/2025T11:00",
        "exit"
    );
    runCommands(commands);

    String actual = errorOutput.toString();
    assertEquals(true, actual.contains("could not be parsed") || actual.contains("Error"));
  }

  @Test
  public void testPrintRangePartialOverlap() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event LongEvent from 2025-05-01T10:00 to 2025-05-05T16:00",
        "print events from 2025-05-03T00:00 to 2025-05-03T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("* LongEvent"));
  }

  @Test
  public void testEditNonExistentSeries() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "edit series subject Ghost from 2025-05-05T10:00 with NewName",
        "exit"
    );
    runCommands(commands);

    String actual = errorOutput.toString();
    assertEquals(true, actual.contains("Event not found"));
  }

  @Test
  public void testEditEventsNonExistent() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "edit events subject Ghost from 2025-05-05T10:00 with NewName",
        "exit"
    );
    runCommands(commands);

    String actual = errorOutput.toString();
    assertEquals(true, actual.contains("Event not found"));
  }

  @Test
  public void testAllDaySeriesOnWeekends() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event \"Weekend Fun\" on 2025-05-03 repeats SU for 4 times",
        "print events from 2025-05-01T00:00 to 2025-05-31T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    int count = 0;
    int index = 0;
    while ((index = actual.indexOf("* Weekend Fun", index)) != -1) {
      count++;
      index += "* Weekend Fun".length();
    }
    assertEquals("Should create 4 weekend events", 4, count);
  }

  @Test
  public void testSeriesWithDifferentDurations() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Short from 2025-05-05T10:00 to 2025-05-05T10:15 repeats MW for 3 times",
        "create event Long from 2025-05-05T14:00 to 2025-05-05T17:00 repeats MW for 3 times",
        "print events from 2025-05-05T00:00 to 2025-05-31T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("ending on 2025-05-05 at 10:15"));
    assertEquals(true, actual.contains("ending on 2025-05-05 at 17:00"));
  }

  @Test
  public void testPrintRangeWithNoEvents() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "print events from 2025-06-01T00:00 to 2025-06-30T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("No events found."));
  }
}