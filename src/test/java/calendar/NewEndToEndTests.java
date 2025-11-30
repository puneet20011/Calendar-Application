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
 * End-to-end tests for new calendar functionality.
 * Tests cover multi-calendar management, copying events, and calendar editing.
 */
public class NewEndToEndTests {

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
  public void testCreateCalendar() {
    String commands = String.join("\n",
        "create calendar --name work --timezone America/New_York",
        "use calendar --name work",
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String expected = "* Meeting starting on 2025-05-01 at 10:00";
    String actual = output.toString();
    assertEquals(true, actual.contains("Calendar created: work"));
    assertEquals(true, actual.contains("Now using calendar: work"));
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testCreateMultipleCalendars() {
    String commands = String.join("\n",
        "create calendar --name work --timezone America/New_York",
        "create calendar --name personal --timezone America/Los_Angeles",
        "create calendar --name shared --timezone UTC",
        "use calendar --name work",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("Calendar created: work"));
    assertEquals(true, actual.contains("Calendar created: personal"));
    assertEquals(true, actual.contains("Calendar created: shared"));
    assertEquals(true, actual.contains("Now using calendar: work"));
  }

  @Test
  public void testCreateCalendarWithDuplicateNameFails() {
    String commands = String.join("\n",
        "create calendar --name work --timezone UTC",
        "create calendar --name work --timezone America/New_York",
        "exit"
    );
    runCommands(commands);

    String expected = "already exists";
    String actual = errorOutput.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testCreateCalendarWithInvalidTimezoneFails() {
    String commands = String.join("\n",
        "create calendar --name work --timezone InvalidTimezone",
        "exit"
    );
    runCommands(commands);

    String expected = "Invalid timezone";
    String actual = errorOutput.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testCreateCalendarWithEmptyNameFails() {
    String commands = String.join("\n",
        "create calendar --name --timezone UTC",
        "exit"
    );
    runCommands(commands);

    String expected = "Invalid command";
    String actual = errorOutput.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testUseCalendar() {
    String commands = String.join("\n",
        "create calendar --name work --timezone UTC",
        "create calendar --name personal --timezone UTC",
        "use calendar --name work",
        "create event WorkMeeting from 2025-05-01T10:00 to 2025-05-01T11:00",
        "use calendar --name personal",
        "create event PersonalAppointment from 2025-05-01T14:00 to 2025-05-01T15:00",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("Now using calendar: work"));
    assertEquals(true, actual.contains("Now using calendar: personal"));
    assertEquals(true, actual.contains("* PersonalAppointment"));
    assertEquals(false, actual.contains("* WorkMeeting"));
  }

  @Test
  public void testUseNonExistentCalendarFails() {
    String commands = String.join("\n",
        "use calendar --name nonexistent",
        "exit"
    );
    runCommands(commands);

    String expected = "not found";
    String actual = errorOutput.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testSwitchBetweenCalendars() {
    String commands = String.join("\n",
        "create calendar --name work --timezone UTC",
        "create calendar --name personal --timezone UTC",
        "use calendar --name work",
        "create event WorkEvent from 2025-05-01T10:00 to 2025-05-01T11:00",
        "use calendar --name personal",
        "create event PersonalEvent from 2025-05-01T14:00 to 2025-05-01T15:00",
        "use calendar --name work",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("* WorkEvent"));
    assertEquals(false, actual.contains("* PersonalEvent"));
  }

  @Test
  public void testCalendarsAreIsolated() {
    String commands = String.join("\n",
        "create calendar --name cal1 --timezone UTC",
        "create calendar --name cal2 --timezone UTC",
        "use calendar --name cal1",
        "create event Event1 from 2025-05-01T10:00 to 2025-05-01T11:00",
        "use calendar --name cal2",
        "create event Event2 from 2025-05-01T10:00 to 2025-05-01T11:00",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("* Event2"));
    assertEquals(false, actual.contains("* Event1"));
  }

  @Test
  public void testCreateEventWithoutCalendarFails() {
    String commands = String.join("\n",
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00",
        "exit"
    );
    runCommands(commands);

    String expected = "No calendar in use";
    String actual = errorOutput.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testEditCalendarName() {
    String commands = String.join("\n",
        "create calendar --name oldname --timezone UTC",
        "edit calendar --name oldname --property name newname",
        "use calendar --name newname",
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("Calendar property edited successfully"));
    assertEquals(true, actual.contains("Now using calendar: newname"));
    assertEquals(true, actual.contains("* Meeting"));
  }

  @Test
  public void testEditCalendarNameToExistingNameFails() {
    String commands = String.join("\n",
        "create calendar --name work --timezone UTC",
        "create calendar --name personal --timezone UTC",
        "edit calendar --name work --property name personal",
        "exit"
    );
    runCommands(commands);

    String expected = "already exists";
    String actual = errorOutput.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testEditCalendarNamePreservesEvents() {
    String commands = String.join("\n",
        "create calendar --name work --timezone UTC",
        "use calendar --name work",
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00",
        "edit calendar --name work --property name office",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("Calendar property edited successfully"));
    assertEquals(true, actual.contains("* Meeting"));
  }

  @Test
  public void testEditCalendarTimezone() {
    String commands = String.join("\n",
        "create calendar --name work --timezone America/New_York",
        "use calendar --name work",
        "create event Meeting from 2025-05-01T14:00 to 2025-05-01T15:00",
        "edit calendar --name work --property timezone America/Los_Angeles",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("Calendar property edited successfully"));
    assertEquals(true, actual.contains("starting on 2025-05-01 at 11:00"));
    assertEquals(true, actual.contains("ending on 2025-05-01 at 12:00"));
  }

  @Test
  public void testEditCalendarTimezoneConvertsAllEvents() {
    String commands = String.join("\n",
        "create calendar --name work --timezone America/New_York",
        "use calendar --name work",
        "create event Morning from 2025-05-01T09:00 to 2025-05-01T10:00",
        "create event Afternoon from 2025-05-01T14:00 to 2025-05-01T15:00",
        "edit calendar --name work --property timezone America/Los_Angeles",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("starting on 2025-05-01 at 06:00"));
    assertEquals(true, actual.contains("starting on 2025-05-01 at 11:00"));
  }

  @Test
  public void testEditCalendarInvalidTimezoneFails() {
    String commands = String.join("\n",
        "create calendar --name work --timezone UTC",
        "edit calendar --name work --property timezone Invalid/Timezone",
        "exit"
    );
    runCommands(commands);

    String expected = "Invalid timezone";
    String actual = errorOutput.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testEditNonExistentCalendarFails() {
    String commands = String.join("\n",
        "edit calendar --name nonexistent --property name newname",
        "exit"
    );
    runCommands(commands);

    String expected = "not found";
    String actual = errorOutput.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testEditCalendarInvalidPropertyFails() {
    String commands = String.join("\n",
        "create calendar --name work --timezone UTC",
        "edit calendar --name work --property invalid value",
        "exit"
    );
    runCommands(commands);

    String expected = "Invalid property";
    String actual = errorOutput.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testCopySingleEventToAnotherCalendar() {
    String commands = String.join("\n",
        "create calendar --name source --timezone UTC",
        "create calendar --name target --timezone UTC",
        "use calendar --name source",
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00",
        "copy event Meeting on 2025-05-01T10:00 --target target "
            + "to 2025-05-05T14:00",
        "use calendar --name target",
        "print events on 2025-05-05",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("Event copied successfully"));
    assertEquals(true, actual.contains("* Meeting starting on 2025-05-05 at 14:00"));
    assertEquals(true, actual.contains("ending on 2025-05-05 at 15:00"));
  }

  @Test
  public void testCopySingleEventPreservesDuration() {
    String commands = String.join("\n",
        "create calendar --name source --timezone UTC",
        "create calendar --name target --timezone UTC",
        "use calendar --name source",
        "create event LongMeeting from 2025-05-01T10:00 to 2025-05-01T13:30",
        "copy event LongMeeting on 2025-05-01T10:00 --target target "
            + "to 2025-05-10T09:00",
        "use calendar --name target",
        "print events on 2025-05-10",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("* LongMeeting starting on 2025-05-10 at 09:00"));
    assertEquals(true, actual.contains("ending on 2025-05-10 at 12:30"));
  }

  @Test
  public void testCopySingleEventWithOptionalProperties() {
    String commands = String.join("\n",
        "create calendar --name source --timezone UTC",
        "create calendar --name target --timezone UTC",
        "use calendar --name source",
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00",
        "edit event location Meeting from 2025-05-01T10:00 to 2025-05-01T11:00 "
            + "with \"Room 101\"",
        "edit event description Meeting from 2025-05-01T10:00 to 2025-05-01T11:00 "
            + "with \"Important\"",
        "edit event status Meeting from 2025-05-01T10:00 to 2025-05-01T11:00 "
            + "with private",
        "copy event Meeting on 2025-05-01T10:00 --target target "
            + "to 2025-05-05T10:00",
        "use calendar --name target",
        "print events on 2025-05-05",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("location: Room 101"));
    assertEquals(true, actual.contains("description: Important"));
    assertEquals(true, actual.contains("status: private"));
  }

  @Test
  public void testCopyEventNotFoundFails() {
    String commands = String.join("\n",
        "create calendar --name source --timezone UTC",
        "create calendar --name target --timezone UTC",
        "use calendar --name source",
        "copy event NonExistent on 2025-05-01T10:00 --target target "
            + "to 2025-05-05T10:00",
        "exit"
    );
    runCommands(commands);

    String expected = "Event not found";
    String actual = errorOutput.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testCopyEventToNonExistentCalendarFails() {
    String commands = String.join("\n",
        "create calendar --name source --timezone UTC",
        "use calendar --name source",
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00",
        "copy event Meeting on 2025-05-01T10:00 --target nonexistent "
            + "to 2025-05-05T10:00",
        "exit"
    );
    runCommands(commands);

    String expected = "not found";
    String actual = errorOutput.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testCopyEventWithoutUsingCalendarFails() {
    String commands = String.join("\n",
        "create calendar --name target --timezone UTC",
        "copy event Meeting on 2025-05-01T10:00 --target target "
            + "to 2025-05-05T10:00",
        "exit"
    );
    runCommands(commands);

    String expected = "No calendar in use";
    String actual = errorOutput.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testCopyEventWithTimezoneConversion() {
    String commands = String.join("\n",
        "create calendar --name est --timezone America/New_York",
        "create calendar --name pst --timezone America/Los_Angeles",
        "use calendar --name est",
        "create event Meeting from 2025-05-01T14:00 to 2025-05-01T15:00",
        "copy event Meeting on 2025-05-01T14:00 --target pst "
            + "to 2025-05-05T14:00",
        "use calendar --name pst",
        "print events on 2025-05-05",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("starting on 2025-05-05 at 14:00"));
    assertEquals(true, actual.contains("ending on 2025-05-05 at 15:00"));
  }

  @Test
  public void testCopyEventsOnDate() {
    String commands = String.join("\n",
        "create calendar --name source --timezone UTC",
        "create calendar --name target --timezone UTC",
        "use calendar --name source",
        "create event Morning from 2025-05-01T09:00 to 2025-05-01T10:00",
        "create event Afternoon from 2025-05-01T14:00 to 2025-05-01T15:00",
        "copy events on 2025-05-01 --target target to 2025-05-10",
        "use calendar --name target",
        "print events on 2025-05-10",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("Events copied successfully"));
    assertEquals(true, actual.contains("* Morning starting on 2025-05-10 at 09:00"));
    assertEquals(true, actual.contains("* Afternoon starting on 2025-05-10 at 14:00"));
  }

  @Test
  public void testCopyEventsOnDateWithTimezoneConversion() {
    String commands = String.join("\n",
        "create calendar --name est --timezone America/New_York",
        "create calendar --name pst --timezone America/Los_Angeles",
        "use calendar --name est",
        "create event Meeting1 from 2025-05-01T10:00 to 2025-05-01T11:00",
        "create event Meeting2 from 2025-05-01T14:00 to 2025-05-01T15:00",
        "copy events on 2025-05-01 --target pst to 2025-05-10",
        "use calendar --name pst",
        "print events on 2025-05-10",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("starting on 2025-05-10 at 07:00"));
    assertEquals(true, actual.contains("starting on 2025-05-10 at 11:00"));
  }

  @Test
  public void testCopyEventsOnDateEmpty() {
    String commands = String.join("\n",
        "create calendar --name source --timezone UTC",
        "create calendar --name target --timezone UTC",
        "use calendar --name source",
        "copy events on 2025-05-01 --target target to 2025-05-10",
        "use calendar --name target",
        "print events on 2025-05-10",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("Events copied successfully"));
    assertEquals(true, actual.contains("No events found"));
  }

  @Test
  public void testCopyEventsOnDatePreservesOptionalProperties() {
    String commands = String.join("\n",
        "create calendar --name source --timezone UTC",
        "create calendar --name target --timezone UTC",
        "use calendar --name source",
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00",
        "edit event location Meeting from 2025-05-01T10:00 to 2025-05-01T11:00 "
            + "with \"Room A\"",
        "edit event description Meeting from 2025-05-01T10:00 "
            + "to 2025-05-01T11:00 with \"Team sync\"",
        "copy events on 2025-05-01 --target target to 2025-05-10",
        "use calendar --name target",
        "print events on 2025-05-10",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("location: Room A"));
    assertEquals(true, actual.contains("description: Team sync"));
  }

  @Test
  public void testCopyEventsOnDateWithMultipleDayEvent() {
    String commands = String.join("\n",
        "create calendar --name source --timezone UTC",
        "create calendar --name target --timezone UTC",
        "use calendar --name source",
        "create event Conference from 2025-05-01T09:00 to 2025-05-03T17:00",
        "copy events on 2025-05-02 --target target to 2025-05-10",
        "use calendar --name target",
        "print events on 2025-05-10",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("* Conference"));
  }

  @Test
  public void testCopyEventsInRange() {
    String commands = String.join("\n",
        "create calendar --name source --timezone UTC",
        "create calendar --name target --timezone UTC",
        "use calendar --name source",
        "create event Day1 from 2025-05-01T10:00 to 2025-05-01T11:00",
        "create event Day2 from 2025-05-02T10:00 to 2025-05-02T11:00",
        "create event Day3 from 2025-05-03T10:00 to 2025-05-03T11:00",
        "copy events between 2025-05-01 and 2025-05-03 --target target to 2025-06-01",
        "use calendar --name target",
        "print events from 2025-06-01T00:00 to 2025-06-10T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("Events copied successfully"));
    assertEquals(true, actual.contains("* Day1 starting on 2025-06-01 at 10:00"));
    assertEquals(true, actual.contains("* Day2 starting on 2025-06-02 at 10:00"));
    assertEquals(true, actual.contains("* Day3 starting on 2025-06-03 at 10:00"));
  }

  @Test
  public void testCopyEventsInRangeMaintainsOffset() {
    String commands = String.join("\n",
        "create calendar --name source --timezone UTC",
        "create calendar --name target --timezone UTC",
        "use calendar --name source",
        "create event Event1 from 2025-05-15T10:00 to 2025-05-15T11:00",
        "create event Event2 from 2025-05-17T14:00 to 2025-05-17T15:00",
        "copy events between 2025-05-15 and 2025-05-17 --target target to 2025-06-01",
        "use calendar --name target",
        "print events from 2025-06-01T00:00 to 2025-06-10T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("* Event1 starting on 2025-06-01 at 10:00"));
    assertEquals(true, actual.contains("* Event2 starting on 2025-06-03 at 14:00"));
  }

  @Test
  public void testCopyEventsInRangeWithSeries() {
    String commands = String.join("\n",
        "create calendar --name source --timezone UTC",
        "create calendar --name target --timezone UTC",
        "use calendar --name source",
        "create event Weekly from 2025-05-05T10:00 to 2025-05-05T11:00 repeats MW for 4 times",
        "copy events between 2025-05-05 and 2025-05-14 --target target to 2025-06-02",
        "use calendar --name target",
        "print events from 2025-06-01T00:00 to 2025-06-30T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    int count = 0;
    int index = 0;
    while ((index = actual.indexOf("* Weekly", index)) != -1) {
      count++;
      index += "* Weekly".length();
    }
    assertEquals("Should copy all 4 occurrences", 4, count);
  }

  @Test
  public void testCopyEventsInRangePartialSeries() {
    String commands = String.join("\n",
        "create calendar --name source --timezone UTC",
        "create calendar --name target --timezone UTC",
        "use calendar --name source",
        "create event Daily from 2025-05-05T10:00 to 2025-05-05T11:00 repeats MTWRF for 10 times",
        "copy events between 2025-05-05 and 2025-05-08 --target target to 2025-06-01",
        "use calendar --name target",
        "print events from 2025-06-01T00:00 to 2025-06-30T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    int count = 0;
    int index = 0;
    while ((index = actual.indexOf("* Daily", index)) != -1) {
      count++;
      index += "* Daily".length();
    }
    assertEquals(true, count >= 3 && count <= 5);
  }

  @Test
  public void testCopyEventsInRangeWithTimezoneConversion() {
    String commands = String.join("\n",
        "create calendar --name est --timezone America/New_York",
        "create calendar --name pst --timezone America/Los_Angeles",
        "use calendar --name est",
        "create event Event1 from 2025-05-01T10:00 to 2025-05-01T11:00",
        "create event Event2 from 2025-05-02T14:00 to 2025-05-02T15:00",
        "copy events between 2025-05-01 and 2025-05-02 --target pst to 2025-06-01",
        "use calendar --name pst",
        "print events from 2025-06-01T00:00 to 2025-06-10T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("starting on 2025-06-01 at 07:00"));
    assertEquals(true, actual.contains("starting on 2025-06-02 at 11:00"));
  }

  @Test
  public void testCopyEventsInRangeEmpty() {
    String commands = String.join("\n",
        "create calendar --name source --timezone UTC",
        "create calendar --name target --timezone UTC",
        "use calendar --name source",
        "copy events between 2025-05-01 and 2025-05-05 --target target to 2025-06-01",
        "use calendar --name target",
        "print events from 2025-06-01T00:00 to 2025-06-10T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("Events copied successfully"));
    assertEquals(true, actual.contains("No events found"));
  }

  @Test
  public void testCopyEventsInRangeToSameCalendar() {
    String commands = String.join("\n",
        "create calendar --name work --timezone UTC",
        "use calendar --name work",
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00",
        "copy events between 2025-05-01 and 2025-05-01 --target work to 2025-05-15",
        "print events on 2025-05-15",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("* Meeting starting on 2025-05-15 at 10:00"));
  }

  @Test
  public void testCopyEventsInRangeSkipsDuplicates() {
    String commands = String.join("\n",
        "create calendar --name source --timezone UTC",
        "create calendar --name target --timezone UTC",
        "use calendar --name source",
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00",
        "use calendar --name target",
        "create event Meeting from 2025-06-01T10:00 to 2025-06-01T11:00",
        "use calendar --name source",
        "copy events between 2025-05-01 and 2025-05-01 --target target to 2025-06-01",
        "use calendar --name target",
        "print events on 2025-06-01",
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
    assertEquals("Should only have 1 event (duplicate skipped)", 1, count);
  }

  @Test
  public void testCompleteMultiCalendarWorkflow() {
    String commands = String.join("\n",
        "create calendar --name work --timezone America/New_York",
        "create calendar --name personal --timezone America/New_York",
        "",
        "use calendar --name work",
        "create event Standup from 2025-05-05T09:00 to 2025-05-05T09:30 repeats MTWRF for 5 times",
        "create event \"Project Meeting\" from 2025-05-06T14:00 to 2025-05-06T15:00",
        "",
        "use calendar --name personal",
        "create event \"Dentist\" from 2025-05-05T16:00 to 2025-05-05T17:00",
        "create event \"Gym\" on 2025-05-06",
        "",
        "use calendar --name work",
        "copy event \"Project Meeting\" on 2025-05-06T14:00 --target personal to 2025-05-13T14:00",
        "",
        "use calendar --name work",
        "print events from 2025-05-05T00:00 to 2025-05-10T23:59",
        "",
        "use calendar --name personal",
        "print events from 2025-05-05T00:00 to 2025-05-15T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("* Standup"));
    assertEquals(true, actual.contains("* Project Meeting"));
    assertEquals(true, actual.contains("* Dentist"));
    assertEquals(true, actual.contains("* Gym"));
  }

  @Test
  public void testCopyEventsBetweenDifferentTimezones() {
    String commands = String.join("\n",
        "create calendar --name tokyo --timezone Asia/Tokyo",
        "create calendar --name newyork --timezone America/New_York",
        "use calendar --name tokyo",
        "create event Meeting from 2025-05-01T15:00 to 2025-05-01T16:00",
        "copy events on 2025-05-01 --target newyork to 2025-05-01",
        "use calendar --name newyork",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("starting on 2025-05-01 at 02:00"));
  }

  @Test
  public void testEditCalendarTimezoneWithSeriesEvents() {
    String commands = String.join("\n",
        "create calendar --name work --timezone America/New_York",
        "use calendar --name work",
        "create event Weekly from 2025-05-05T10:00 to 2025-05-05T11:00 repeats MW for 3 times",
        "edit calendar --name work --property timezone America/Los_Angeles",
        "print events from 2025-05-05T00:00 to 2025-05-15T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    int count = 0;
    int index = 0;
    while ((index = actual.indexOf("at 07:00", index)) != -1) {
      count++;
      index += "at 07:00".length();
    }
    assertEquals("All 3 occurrences should be at 7:00 AM PST", 3, count);
  }

  @Test
  public void testCopySeriesCreatesNewSeriesInTarget() {
    String commands = String.join("\n",
        "create calendar --name source --timezone UTC",
        "create calendar --name target --timezone UTC",
        "use calendar --name source",
        "create event Weekly from 2025-05-05T10:00 to 2025-05-05T11:00 repeats MW for 4 times",
        "copy events between 2025-05-05 and 2025-05-14 --target target to 2025-06-02",
        "use calendar --name target",
        "edit series location Weekly from 2025-06-02T10:00 with \"Conference Room\"",
        "print events from 2025-06-01T00:00 to 2025-06-30T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    int locationCount = 0;
    int index = 0;
    while ((index = actual.indexOf("location: Conference Room", index)) != -1) {
      locationCount++;
      index += "location: Conference Room".length();
    }
    assertEquals("All copied events should have the location", 4, locationCount);
  }

  @Test
  public void testCopyStandaloneEventFromSeriesCalendar() {
    String commands = String.join("\n",
        "create calendar --name source --timezone UTC",
        "create calendar --name target --timezone UTC",
        "use calendar --name source",
        "create event Series from 2025-05-05T10:00 to 2025-05-05T11:00 repeats MW for 3 times",
        "create event Standalone from 2025-05-06T14:00 to 2025-05-06T15:00",
        "copy event Standalone on 2025-05-06T14:00 --target target to 2025-06-01T14:00",
        "use calendar --name target",
        "print events on 2025-06-01",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("* Standalone starting on 2025-06-01 at 14:00"));
    assertEquals(false, actual.contains("* Series"));
  }

  @Test
  public void testCopyEventsAcrossInternationalDateLine() {
    String commands = String.join("\n",
        "create calendar --name newyork --timezone America/New_York",
        "create calendar --name tokyo --timezone Asia/Tokyo",
        "use calendar --name newyork",
        "create event LateNight from 2025-05-01T23:00 to 2025-05-02T00:30",
        "copy events on 2025-05-01 --target tokyo to 2025-05-01",
        "use calendar --name tokyo",
        "print events on 2025-05-01",
        "print events on 2025-05-02",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("* LateNight"));
  }

  @Test
  public void testExportFromSpecificCalendar() {
    String commands = String.join("\n",
        "create calendar --name work --timezone UTC",
        "create calendar --name personal --timezone UTC",
        "use calendar --name work",
        "create event WorkMeeting from 2025-05-01T10:00 to 2025-05-01T11:00",
        "use calendar --name personal",
        "create event PersonalEvent from 2025-05-01T14:00 to 2025-05-01T15:00",
        "export cal personal_calendar.csv",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("Calendar exported successfully"));
    assertEquals(true, actual.contains("File location:"));
  }

  @Test
  public void testExportWithTimezoneInIcal() {
    String commands = String.join("\n",
        "create calendar --name work --timezone America/New_York",
        "use calendar --name work",
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00",
        "export cal calendar.ics",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("Calendar exported successfully"));
    assertEquals(true, actual.contains("File location:"));
  }

  @Test
  public void testThreeCalendarsWithCopying() {
    String commands = String.join("\n",
        "create calendar --name source --timezone UTC",
        "create calendar --name intermediate --timezone UTC",
        "create calendar --name final --timezone UTC",
        "use calendar --name source",
        "create event Original from 2025-05-01T10:00 to 2025-05-01T11:00",
        "copy event Original on 2025-05-01T10:00 --target intermediate to 2025-05-10T10:00",
        "use calendar --name intermediate",
        "copy event Original on 2025-05-10T10:00 --target final to 2025-05-20T10:00",
        "use calendar --name final",
        "print events on 2025-05-20",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("* Original starting on 2025-05-20 at 10:00"));
  }

  @Test
  public void testCopyEventsAndThenEditInTargetCalendar() {
    String commands = String.join("\n",
        "create calendar --name source --timezone UTC",
        "create calendar --name target --timezone UTC",
        "use calendar --name source",
        "create event Template from 2025-05-01T10:00 to 2025-05-01T11:00",
        "copy event Template on 2025-05-01T10:00 --target target "
            + "to 2025-05-05T10:00",
        "use calendar --name target",
        "edit event subject Template from 2025-05-05T10:00 to 2025-05-05T11:00 "
            + "with \"Customized Event\"",
        "edit event location \"Customized Event\" from 2025-05-05T10:00 "
            + "to 2025-05-05T11:00 with \"Room 202\"",
        "print events on 2025-05-05",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("* Customized Event"));
    assertEquals(true, actual.contains("location: Room 202"));
  }

  @Test
  public void testCopyAllDayEvents() {
    String commands = String.join("\n",
        "create calendar --name source --timezone UTC",
        "create calendar --name target --timezone UTC",
        "use calendar --name source",
        "create event Holiday on 2025-05-01",
        "create event Vacation on 2025-05-02",
        "copy events between 2025-05-01 and 2025-05-02 --target target to 2025-06-01",
        "use calendar --name target",
        "print events from 2025-06-01T00:00 to 2025-06-05T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("* Holiday starting on 2025-06-01 at 08:00"));
    assertEquals(true, actual.contains("* Vacation starting on 2025-06-02 at 08:00"));
  }

  @Test
  public void testCopyMixedEventTypes() {
    String commands = String.join("\n",
        "create calendar --name source --timezone UTC",
        "create calendar --name target --timezone UTC",
        "use calendar --name source",
        "create event AllDay on 2025-05-01",
        "create event Timed from 2025-05-01T10:00 to 2025-05-01T11:00",
        "create event Series from 2025-05-05T14:00 to 2025-05-05T15:00 repeats MW for 2 times",
        "copy events between 2025-05-01 and 2025-05-10 --target target to 2025-06-01",
        "use calendar --name target",
        "print events from 2025-06-01T00:00 to 2025-06-15T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("* AllDay"));
    assertEquals(true, actual.contains("* Timed"));
    assertEquals(true, actual.contains("* Series"));
  }

  @Test
  public void testEditCalendarNameAndContinueUsing() {
    String commands = String.join("\n",
        "create calendar --name temp --timezone UTC",
        "use calendar --name temp",
        "create event Event1 from 2025-05-01T10:00 to 2025-05-01T11:00",
        "edit calendar --name temp --property name permanent",
        "create event Event2 from 2025-05-02T10:00 to 2025-05-02T11:00",
        "print events from 2025-05-01T00:00 to 2025-05-05T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("* Event1"));
    assertEquals(true, actual.contains("* Event2"));
  }

  @Test
  public void testEditCalendarTimezoneAffectsAllDayEvents() {
    String commands = String.join("\n",
        "create calendar --name work --timezone America/New_York",
        "use calendar --name work",
        "create event AllDay on 2025-05-01",
        "edit calendar --name work --property timezone America/Los_Angeles",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("starting on 2025-05-01 at 05:00"));
    assertEquals(true, actual.contains("ending on 2025-05-01 at 14:00"));
  }

  @Test
  public void testMultipleCalendarsWithDifferentTimezones() {
    String commands = String.join("\n",
        "create calendar --name tokyo --timezone Asia/Tokyo",
        "create calendar --name london --timezone Europe/London",
        "create calendar --name newyork --timezone America/New_York",
        "use calendar --name tokyo",
        "create event TokyoMeeting from 2025-05-01T15:00 to 2025-05-01T16:00",
        "use calendar --name london",
        "create event LondonMeeting from 2025-05-01T10:00 to 2025-05-01T11:00",
        "use calendar --name newyork",
        "create event NYMeeting from 2025-05-01T14:00 to 2025-05-01T15:00",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("* NYMeeting"));
    assertEquals(false, actual.contains("* TokyoMeeting"));
    assertEquals(false, actual.contains("* LondonMeeting"));
  }

  @Test
  public void testCopyEventToSameCalendarDifferentTime() {
    String commands = String.join("\n",
        "create calendar --name work --timezone UTC",
        "use calendar --name work",
        "create event Template from 2025-05-01T10:00 to 2025-05-01T11:00",
        "copy event Template on 2025-05-01T10:00 --target work to 2025-05-01T14:00",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    int count = 0;
    int index = 0;
    while ((index = actual.indexOf("* Template", index)) != -1) {
      count++;
      index += "* Template".length();
    }
    assertEquals("Should have 2 events (original + copy)", 2, count);
  }

  @Test
  public void testCopyEventToDifferentDay() {
    String commands = String.join("\n",
        "create calendar --name source --timezone UTC",
        "create calendar --name target --timezone UTC",
        "use calendar --name source",
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00",
        "copy event Meeting on 2025-05-01T10:00 --target target to 2025-12-25T10:00",
        "use calendar --name target",
        "print events on 2025-12-25",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("* Meeting starting on 2025-12-25 at 10:00"));
  }

  @Test
  public void testCopyMultipleDayEventPreservesDuration() {
    String commands = String.join("\n",
        "create calendar --name source --timezone UTC",
        "create calendar --name target --timezone UTC",
        "use calendar --name source",
        "create event Conference from 2025-05-01T09:00 to 2025-05-03T17:00",
        "copy event Conference on 2025-05-01T09:00 --target target to 2025-06-01T09:00",
        "use calendar --name target",
        "print events on 2025-06-01",
        "print events on 2025-06-03",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("* Conference starting on 2025-06-01 at 09:00"));
    assertEquals(true, actual.contains("ending on 2025-06-03 at 17:00"));
  }

  @Test
  public void testCopyEventCreatingDuplicateInTargetFails() {
    String commands = String.join("\n",
        "create calendar --name source --timezone UTC",
        "create calendar --name target --timezone UTC",
        "use calendar --name source",
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00",
        "use calendar --name target",
        "create event Meeting from 2025-05-05T10:00 to 2025-05-05T11:00",
        "use calendar --name source",
        "copy event Meeting on 2025-05-01T10:00 --target target to 2025-05-05T10:00",
        "exit"
    );
    runCommands(commands);

    String expected = "already exist";
    String actual = errorOutput.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testCopyEventsRangeWithGaps() {
    String commands = String.join("\n",
        "create calendar --name source --timezone UTC",
        "create calendar --name target --timezone UTC",
        "use calendar --name source",
        "create event Mon from 2025-05-05T10:00 to 2025-05-05T11:00",
        "create event Wed from 2025-05-07T10:00 to 2025-05-07T11:00",
        "create event Fri from 2025-05-09T10:00 to 2025-05-09T11:00",
        "copy events between 2025-05-05 and 2025-05-09 --target target to 2025-06-01",
        "use calendar --name target",
        "print events from 2025-06-01T00:00 to 2025-06-10T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("* Mon starting on 2025-06-01"));
    assertEquals(true, actual.contains("* Wed starting on 2025-06-03"));
    assertEquals(true, actual.contains("* Fri starting on 2025-06-05"));
  }

  @Test
  public void testCopyEventsRangeExcludesEventsOutsideRange() {
    String commands = String.join("\n",
        "create calendar --name source --timezone UTC",
        "create calendar --name target --timezone UTC",
        "use calendar --name source",
        "create event Before from 2025-04-30T10:00 to 2025-04-30T11:00",
        "create event During1 from 2025-05-01T10:00 to 2025-05-01T11:00",
        "create event During2 from 2025-05-03T10:00 to 2025-05-03T11:00",
        "create event After from 2025-05-06T10:00 to 2025-05-06T11:00",
        "copy events between 2025-05-01 and 2025-05-05 --target target to 2025-06-01",
        "use calendar --name target",
        "print events from 2025-06-01T00:00 to 2025-06-10T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(false, actual.contains("* Before"));
    assertEquals(true, actual.contains("* During1"));
    assertEquals(true, actual.contains("* During2"));
    assertEquals(false, actual.contains("* After"));
  }

  @Test
  public void testCopyEventsRangeWithOverlappingMultiDayEvent() {
    String commands = String.join("\n",
        "create calendar --name source --timezone UTC",
        "create calendar --name target --timezone UTC",
        "use calendar --name source",
        "create event LongEvent from 2025-04-30T10:00 to 2025-05-05T16:00",
        "copy events between 2025-05-01 and 2025-05-03 --target target to 2025-06-01",
        "use calendar --name target",
        "print events on 2025-06-01",
        "print events on 2025-06-05",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("* LongEvent"));
  }

  @Test
  public void testCopySeriesWithDifferentWeekdayPatterns() {
    String commands = String.join("\n",
        "create calendar --name source --timezone UTC",
        "create calendar --name target --timezone UTC",
        "use calendar --name source",
        "create event Weekdays from 2025-05-05T09:00 to 2025-05-05T10:00 repeats MTWRF for 5 times",
        "create event Weekends from 2025-05-03T11:00 to 2025-05-03T12:00 repeats SU for 2 times",
        "copy events between 2025-05-03 and 2025-05-09 --target target to 2025-06-01",
        "use calendar --name target",
        "print events from 2025-06-01T00:00 to 2025-06-15T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("* Weekdays"));
    assertEquals(true, actual.contains("* Weekends"));
  }

  @Test
  public void testPrintEventsRequiresActiveCalendar() {
    String commands = String.join("\n",
        "create calendar --name work --timezone UTC",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String expected = "No calendar in use";
    String actual = errorOutput.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testShowStatusRequiresActiveCalendar() {
    String commands = String.join("\n",
        "create calendar --name work --timezone UTC",
        "show status on 2025-05-01T10:00",
        "exit"
    );
    runCommands(commands);

    String expected = "No calendar in use";
    String actual = errorOutput.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testExportRequiresActiveCalendar() {
    String commands = String.join("\n",
        "create calendar --name work --timezone UTC",
        "export cal test.csv",
        "exit"
    );
    runCommands(commands);

    String expected = "No calendar in use";
    String actual = errorOutput.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testCalendarContextPreservedThroughOperations() {
    String commands = String.join("\n",
        "create calendar --name work --timezone UTC",
        "use calendar --name work",
        "create event Event1 from 2025-05-01T10:00 to 2025-05-01T11:00",
        "create event Event2 from 2025-05-02T10:00 to 2025-05-02T11:00",
        "edit event subject Event1 from 2025-05-01T10:00 to 2025-05-01T11:00 with UpdatedEvent",
        "print events from 2025-05-01T00:00 to 2025-05-05T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("* UpdatedEvent"));
    assertEquals(true, actual.contains("* Event2"));
  }

  @Test
  public void testEditTimezoneUtcToEst() {
    String commands = String.join("\n",
        "create calendar --name work --timezone UTC",
        "use calendar --name work",
        "create event Meeting from 2025-05-01T15:00 to 2025-05-01T16:00",
        "edit calendar --name work --property timezone America/New_York",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("starting on 2025-05-01 at 11:00"));
  }

  @Test
  public void testEditTimezoneEstToUtc() {
    String commands = String.join("\n",
        "create calendar --name work --timezone America/New_York",
        "use calendar --name work",
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00",
        "edit calendar --name work --property timezone UTC",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("starting on 2025-05-01 at 14:00"));
  }

  @Test
  public void testEditTimezoneAcrossPacific() {
    String commands = String.join("\n",
        "create calendar --name work --timezone America/Los_Angeles",
        "use calendar --name work",
        "create event Meeting from 2025-05-01T23:00 to 2025-05-02T00:30",
        "edit calendar --name work --property timezone Asia/Tokyo",
        "print events on 2025-05-02",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("starting on 2025-05-02 at 15:00"));
  }

  @Test
  public void testCopyEventsWithWeekdaySeriesAllDays() {
    String commands = String.join("\n",
        "create calendar --name source --timezone UTC",
        "create calendar --name target --timezone UTC",
        "use calendar --name source",
        "create event Daily from 2025-05-05T10:00 to 2025-05-05T11:00 repeats MTWRFSU for 7 times",
        "copy events between 2025-05-05 and 2025-05-11 --target target to 2025-06-02",
        "use calendar --name target",
        "print events from 2025-06-02T00:00 to 2025-06-10T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    int count = 0;
    int index = 0;
    while ((index = actual.indexOf("* Daily", index)) != -1) {
      count++;
      index += "* Daily".length();
    }
    assertEquals("Should copy all 7 occurrences", 7, count);
  }

  @Test
  public void testCopyEventsRangeWithSeriesOnSpecificDays() {
    String commands = String.join("\n",
        "create calendar --name source --timezone UTC",
        "create calendar --name target --timezone UTC",
        "use calendar --name source",
        "create event MWF from 2025-05-05T10:00 to 2025-05-05T11:00 repeats MWF for 6 times",
        "copy events between 2025-05-05 and 2025-05-16 --target target to 2025-06-02",
        "use calendar --name target",
        "print events from 2025-06-02T00:00 to 2025-06-20T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    int count = 0;
    int index = 0;
    while ((index = actual.indexOf("* MWF", index)) != -1) {
      count++;
      index += "* MWF".length();
    }
    assertEquals("Should copy all 6 MWF occurrences", 6, count);
  }

  @Test
  public void testCopyEventsToMultipleTargetCalendars() {
    String commands = String.join("\n",
        "create calendar --name source --timezone UTC",
        "create calendar --name target1 --timezone UTC",
        "create calendar --name target2 --timezone UTC",
        "use calendar --name source",
        "create event Template from 2025-05-01T10:00 to 2025-05-01T11:00",
        "copy event Template on 2025-05-01T10:00 --target target1 to 2025-06-01T10:00",
        "copy event Template on 2025-05-01T10:00 --target target2 to 2025-07-01T10:00",
        "use calendar --name target1",
        "print events on 2025-06-01",
        "use calendar --name target2",
        "print events on 2025-07-01",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    int count = 0;
    int index = 0;
    while ((index = actual.indexOf("* Template", index)) != -1) {
      count++;
      index += "* Template".length();
    }
    assertEquals("Should have 2 copies in different calendars", 2, count);
  }

  @Test
  public void testCopyThenEditInSourceDoesNotAffectTarget() {
    String commands = String.join("\n",
        "create calendar --name source --timezone UTC",
        "create calendar --name target --timezone UTC",
        "use calendar --name source",
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00",
        "copy event Meeting on 2025-05-01T10:00 --target target "
            + "to 2025-06-01T10:00",
        "edit event subject Meeting from 2025-05-01T10:00 to 2025-05-01T11:00 "
            + "with \"Updated Source\"",
        "use calendar --name target",
        "print events on 2025-06-01",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("* Meeting starting on 2025-06-01"));
    assertEquals(false, actual.contains("* Updated Source"));
  }

  @Test
  public void testCopyThenEditInTargetDoesNotAffectSource() {
    String commands = String.join("\n",
        "create calendar --name source --timezone UTC",
        "create calendar --name target --timezone UTC",
        "use calendar --name source",
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00",
        "copy event Meeting on 2025-05-01T10:00 --target target "
            + "to 2025-06-01T10:00",
        "use calendar --name target",
        "edit event subject Meeting from 2025-06-01T10:00 to 2025-06-01T11:00 "
            + "with \"Updated Target\"",
        "use calendar --name source",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("* Meeting starting on 2025-05-01"));
    assertEquals(false, actual.contains("* Updated Target"));
  }

  @Test
  public void testCopySeriesThenSplitInTarget() {
    String commands = String.join("\n",
        "create calendar --name source --timezone UTC",
        "create calendar --name target --timezone UTC",
        "use calendar --name source",
        "create event Series from 2025-05-05T10:00 to 2025-05-05T11:00 repeats MW for 4 times",
        "copy events between 2025-05-05 and 2025-05-14 --target target to 2025-06-02",
        "use calendar --name target",
        "edit events start Series from 2025-06-09T10:00 with 2025-06-09T14:00",
        "print events from 2025-06-02T00:00 to 2025-06-20T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("starting on 2025-06-02 at 10:00"));
    assertEquals(true, actual.contains("starting on 2025-06-09 at 14:00"));
  }

  @Test
  public void testCopyAllDayEventSeries() {
    String commands = String.join("\n",
        "create calendar --name source --timezone UTC",
        "create calendar --name target --timezone UTC",
        "use calendar --name source",
        "create event Holidays on 2025-05-05 repeats MW for 3 times",
        "copy events between 2025-05-05 and 2025-05-12 --target target to 2025-06-02",
        "use calendar --name target",
        "print events from 2025-06-02T00:00 to 2025-06-15T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    int count = 0;
    int index = 0;
    while ((index = actual.indexOf("* Holidays", index)) != -1) {
      count++;
      index += "* Holidays".length();
    }
    assertEquals("Should copy all 3 all-day occurrences", 3, count);
  }

  @Test
  public void testCopyAllDayEventWithTimezoneConversion() {
    String commands = String.join("\n",
        "create calendar --name est --timezone America/New_York",
        "create calendar --name pst --timezone America/Los_Angeles",
        "use calendar --name est",
        "create event Holiday on 2025-05-01",
        "copy events on 2025-05-01 --target pst to 2025-06-01",
        "use calendar --name pst",
        "print events on 2025-06-01",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("starting on 2025-06-01 at 05:00"));
    assertEquals(true, actual.contains("ending on 2025-06-01 at 14:00"));
  }

  @Test
  public void testComplexMultiCalendarScenario() {
    String commands = String.join("\n",
        "create calendar --name work --timezone America/New_York",
        "create calendar --name personal --timezone America/New_York",
        "create calendar --name shared --timezone UTC",
        "",
        "use calendar --name work",
        "create event Standup from 2025-05-05T09:00 to 2025-05-05T09:30 repeats MTWRF for 5 times",
        "create event \"Team Meeting\" from 2025-05-06T14:00 to 2025-05-06T15:00",
        "",
        "use calendar --name personal",
        "create event Workout on 2025-05-05 repeats MWF for 6 times",
        "create event \"Doctor Appointment\" from 2025-05-07T16:00 to 2025-05-07T17:00",
        "",
        "use calendar --name work",
        "copy events between 2025-05-05 and 2025-05-09 --target shared to 2025-06-02",
        "",
        "use calendar --name personal",
        "copy events between 2025-05-05 and 2025-05-09 --target shared to 2025-06-02",
        "",
        "use calendar --name shared",
        "print events from 2025-06-02T00:00 to 2025-06-10T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("* Standup"));
    assertEquals(true, actual.contains("* Team Meeting"));
    assertEquals(true, actual.contains("* Workout"));
    assertEquals(true, actual.contains("* Doctor Appointment"));
  }

  @Test
  public void testRenameCalendarThenCopyToIt() {
    String commands = String.join("\n",
        "create calendar --name source --timezone UTC",
        "create calendar --name oldname --timezone UTC",
        "edit calendar --name oldname --property name newname",
        "use calendar --name source",
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00",
        "copy event Meeting on 2025-05-01T10:00 --target newname to 2025-06-01T10:00",
        "use calendar --name newname",
        "print events on 2025-06-01",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("* Meeting starting on 2025-06-01"));
  }

  @Test
  public void testChangeTimezoneAfterCopying() {
    String commands = String.join("\n",
        "create calendar --name source --timezone UTC",
        "create calendar --name target --timezone UTC",
        "use calendar --name source",
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00",
        "copy event Meeting on 2025-05-01T10:00 --target target to 2025-06-01T10:00",
        "edit calendar --name target --property timezone America/New_York",
        "use calendar --name target",
        "print events on 2025-06-01",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("starting on 2025-06-01 at 06:00"));
  }

  @Test
  public void testCopyEventAllDuplicatesDoesNotCrash() {
    String commands = String.join("\n",
        "create calendar --name source --timezone UTC",
        "create calendar --name target --timezone UTC",
        "use calendar --name source",
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00",
        "use calendar --name target",
        "create event Meeting from 2025-06-01T10:00 to 2025-06-01T11:00",
        "use calendar --name source",
        "copy event Meeting on 2025-05-01T10:00 --target target to 2025-06-01T10:00",
        "exit"
    );
    runCommands(commands);

    String expected = "already exist";
    String actual = errorOutput.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testCopyEventsOnDateWithAllDuplicatesSkipsThem() {
    String commands = String.join("\n",
        "create calendar --name source --timezone UTC",
        "create calendar --name target --timezone UTC",
        "use calendar --name source",
        "create event Event1 from 2025-05-01T10:00 to 2025-05-01T11:00",
        "create event Event2 from 2025-05-01T14:00 to 2025-05-01T15:00",
        "use calendar --name target",
        "create event Event1 from 2025-06-01T10:00 to 2025-06-01T11:00",
        "create event Event2 from 2025-06-01T14:00 to 2025-06-01T15:00",
        "use calendar --name source",
        "copy events on 2025-05-01 --target target to 2025-06-01",
        "use calendar --name target",
        "print events on 2025-06-01",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    int count = 0;
    int index = 0;
    while ((index = actual.indexOf("starting on 2025-06-01", index)) != -1) {
      count++;
      index += "starting on 2025-06-01".length();
    }
    assertEquals("Should only have 2 events (no duplicates created)", 2, count);
  }

  @Test
  public void testEditCalendarPropertyCaseSensitive() {
    String commands = String.join("\n",
        "create calendar --name work --timezone UTC",
        "edit calendar --name work --property NAME newwork",
        "use calendar --name newwork",
        "create event Test from 2025-05-01T10:00 to 2025-05-01T11:00",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("* Test"));
  }

  @Test
  public void testEditCalendarPropertyTimezoneCaseSensitive() {
    String commands = String.join("\n",
        "create calendar --name work --timezone UTC",
        "edit calendar --name work --property TIMEZONE America/New_York",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("Calendar property edited successfully"));
  }

  @Test
  public void testCopyPartialSeriesRange() {
    String commands = String.join("\n",
        "create calendar --name source --timezone UTC",
        "create calendar --name target --timezone UTC",
        "use calendar --name source",
        "create event Weekly from 2025-05-05T10:00 to 2025-05-05T11:00 repeats MW for 10 times",
        "copy events between 2025-05-12 and 2025-05-16 --target target to 2025-06-01",
        "use calendar --name target",
        "print events from 2025-06-01T00:00 to 2025-06-30T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    int count = 0;
    int index = 0;
    while ((index = actual.indexOf("* Weekly", index)) != -1) {
      count++;
      index += "* Weekly".length();
    }
    assertEquals(true, count >= 2 && count <= 3);
  }

  @Test
  public void testCopySeriesWithEditedOccurrence() {
    String commands = String.join("\n",
        "create calendar --name source --timezone UTC",
        "create calendar --name target --timezone UTC",
        "use calendar --name source",
        "create event Weekly from 2025-05-05T10:00 to 2025-05-05T11:00 "
            + "repeats MW for 4 times",
        "edit event location Weekly from 2025-05-07T10:00 to 2025-05-07T11:00 "
            + "with \"Special Room\"",
        "copy events between 2025-05-05 and 2025-05-14 --target target "
            + "to 2025-06-02",
        "use calendar --name target",
        "print events from 2025-06-02T00:00 to 2025-06-15T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    int count = 0;
    int index = 0;
    while ((index = actual.indexOf("* Weekly", index)) != -1) {
      count++;
      index += "* Weekly".length();
    }
    assertEquals("Should copy all 4 occurrences", 4, count);
  }

  @Test
  public void testStatusCheckInDifferentCalendars() {
    String commands = String.join("\n",
        "create calendar --name work --timezone UTC",
        "create calendar --name personal --timezone UTC",
        "use calendar --name work",
        "create event WorkMeeting from 2025-05-01T10:00 to 2025-05-01T11:00",
        "show status on 2025-05-01T10:30",
        "use calendar --name personal",
        "show status on 2025-05-01T10:30",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    int busyCount = 0;
    int availableCount = 0;
    int index = 0;

    while ((index = actual.indexOf("busy", index)) != -1) {
      busyCount++;
      index += "busy".length();
    }

    index = 0;
    while ((index = actual.indexOf("available", index)) != -1) {
      availableCount++;
      index += "available".length();
    }

    assertEquals("Should be busy in work calendar", 1, busyCount);
    assertEquals("Should be available in personal calendar", 1, availableCount);
  }

  @Test
  public void testExportMultipleCalendarsSeparately() {
    String commands = String.join("\n",
        "create calendar --name work --timezone UTC",
        "create calendar --name personal --timezone UTC",
        "use calendar --name work",
        "create event WorkEvent from 2025-05-01T10:00 to 2025-05-01T11:00",
        "export cal work_calendar.csv",
        "use calendar --name personal",
        "create event PersonalEvent from 2025-05-01T14:00 to 2025-05-01T15:00",
        "export cal personal_calendar.csv",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    int exportCount = 0;
    int index = 0;
    while ((index = actual.indexOf("Calendar exported successfully", index)) != -1) {
      exportCount++;
      index += "Calendar exported successfully".length();
    }
    assertEquals("Should have 2 successful exports", 2, exportCount);
  }

  @Test
  public void testCopyEventsRangeWithMultipleSeriesAndStandalone() {
    String commands = String.join("\n",
        "create calendar --name source --timezone UTC",
        "create calendar --name target --timezone UTC",
        "use calendar --name source",
        "create event Series1 from 2025-05-05T09:00 to 2025-05-05T10:00 repeats MW for 3 times",
        "create event Standalone from 2025-05-06T11:00 to 2025-05-06T12:00",
        "create event Series2 from 2025-05-05T14:00 to 2025-05-05T15:00 repeats TF for 3 times",
        "copy events between 2025-05-05 and 2025-05-13 --target target to 2025-06-02",
        "use calendar --name target",
        "print events from 2025-06-02T00:00 to 2025-06-15T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("* Series1"));
    assertEquals(true, actual.contains("* Standalone"));
    assertEquals(true, actual.contains("* Series2"));
  }

  @Test
  public void testCopyEventsPreservesSeriesStructure() {
    String commands = String.join("\n",
        "create calendar --name source --timezone UTC",
        "create calendar --name target --timezone UTC",
        "use calendar --name source",
        "create event Weekly from 2025-05-05T10:00 to 2025-05-05T11:00 repeats MW for 6 times",
        "copy events between 2025-05-05 and 2025-05-19 --target target to 2025-06-02",
        "use calendar --name target",
        "print events from 2025-06-02T00:00 to 2025-06-20T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    int count = 0;
    int index = 0;
    while ((index = actual.indexOf("* Weekly", index)) != -1) {
      count++;
      index += "* Weekly".length();
    }
    assertEquals(true, count >= 4 && count <= 6);
  }

  @Test
  public void testCopyEventWithSubjectContainingSpaces() {
    String commands = String.join("\n",
        "create calendar --name source --timezone UTC",
        "create calendar --name target --timezone UTC",
        "use calendar --name source",
        "create event \"Team Building Activity\" from 2025-05-01T10:00 "
            + "to 2025-05-01T11:00",
        "copy event \"Team Building Activity\" on 2025-05-01T10:00 "
            + "--target target to 2025-06-01T10:00",
        "use calendar --name target",
        "print events on 2025-06-01",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("* Team Building Activity"));
  }

  @Test
  public void testCannotUseCalendarBeforeCreating() {
    String commands = String.join("\n",
        "use calendar --name nonexistent",
        "exit"
    );
    runCommands(commands);

    String expected = "not found";
    String actual = errorOutput.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testCannotEditNonExistentCalendarProperty() {
    String commands = String.join("\n",
        "create calendar --name work --timezone UTC",
        "edit calendar --name work --property color blue",
        "exit"
    );
    runCommands(commands);

    String expected = "Invalid property";
    String actual = errorOutput.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testCopyEventRequiresActiveCalendar() {
    String commands = String.join("\n",
        "create calendar --name source --timezone UTC",
        "create calendar --name target --timezone UTC",
        "copy event Meeting on 2025-05-01T10:00 --target target to 2025-06-01T10:00",
        "exit"
    );
    runCommands(commands);

    String expected = "No calendar in use";
    String actual = errorOutput.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testCopyEventsOnDateRequiresActiveCalendar() {
    String commands = String.join("\n",
        "create calendar --name target --timezone UTC",
        "copy events on 2025-05-01 --target target to 2025-06-01",
        "exit"
    );
    runCommands(commands);

    String expected = "No calendar in use";
    String actual = errorOutput.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testCopyEventsInRangeRequiresActiveCalendar() {
    String commands = String.join("\n",
        "create calendar --name target --timezone UTC",
        "copy events between 2025-05-01 and 2025-05-05 --target target to 2025-06-01",
        "exit"
    );
    runCommands(commands);

    String expected = "No calendar in use";
    String actual = errorOutput.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testCopyEventEstToPstDifferentDay() {
    String commands = String.join("\n",
        "create calendar --name est --timezone America/New_York",
        "create calendar --name pst --timezone America/Los_Angeles",
        "use calendar --name est",
        "create event Morning from 2025-05-01T10:00 to 2025-05-01T11:00",
        "copy events on 2025-05-01 --target pst to 2025-05-10",
        "use calendar --name pst",
        "print events on 2025-05-10",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("Events copied successfully"));
    assertEquals(true,
        actual.contains("* Morning starting on 2025-05-10 at 07:00"));
  }

  @Test
  public void testEditCalendarTimezoneWithMultipleDayEvent() {
    String commands = String.join("\n",
        "create calendar --name work --timezone America/New_York",
        "use calendar --name work",
        "create event Conference from 2025-05-01T09:00 to 2025-05-03T17:00",
        "edit calendar --name work --property timezone America/Los_Angeles",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("starting on 2025-05-01 at 06:00"));
    assertEquals(true, actual.contains("ending on 2025-05-03 at 14:00"));
  }

  @Test
  public void testCopySeriesWeekdaysOnly() {
    String commands = String.join("\n",
        "create calendar --name source --timezone UTC",
        "create calendar --name target --timezone UTC",
        "use calendar --name source",
        "create event Weekdays from 2025-05-05T09:00 to 2025-05-05T10:00 "
            + "repeats MTWRF for 10 times",
        "copy events between 2025-05-05 and 2025-05-16 --target target "
            + "to 2025-06-02",
        "use calendar --name target",
        "print events from 2025-06-02T00:00 to 2025-06-20T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    int count = 0;
    int index = 0;
    while ((index = actual.indexOf("* Weekdays", index)) != -1) {
      count++;
      index += "* Weekdays".length();
    }
    assertEquals("Should copy all 10 weekday occurrences", 10, count);
  }

  @Test
  public void testCopySeriesWeekendsOnly() {
    String commands = String.join("\n",
        "create calendar --name source --timezone UTC",
        "create calendar --name target --timezone UTC",
        "use calendar --name source",
        "create event Weekends from 2025-05-03T10:00 to 2025-05-03T11:00 repeats SU for 4 times",
        "copy events between 2025-05-03 and 2025-05-24 --target target to 2025-06-01",
        "use calendar --name target",
        "print events from 2025-06-01T00:00 to 2025-06-30T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    int count = 0;
    int index = 0;
    while ((index = actual.indexOf("* Weekends", index)) != -1) {
      count++;
      index += "* Weekends".length();
    }
    assertEquals("Should copy all 4 weekend occurrences", 4, count);
  }

  @Test
  public void testCopySeriesSingleDayPattern() {
    String commands = String.join("\n",
        "create calendar --name source --timezone UTC",
        "create calendar --name target --timezone UTC",
        "use calendar --name source",
        "create event MondaysOnly from 2025-05-05T10:00 to 2025-05-05T11:00 repeats M for 4 times",
        "copy events between 2025-05-05 and 2025-05-26 --target target to 2025-06-02",
        "use calendar --name target",
        "print events from 2025-06-02T00:00 to 2025-06-30T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    int count = 0;
    int index = 0;
    while ((index = actual.indexOf("* MondaysOnly", index)) != -1) {
      count++;
      index += "* MondaysOnly".length();
    }
    assertEquals("Should copy all 4 Monday occurrences", 4, count);
  }

  @Test
  public void testMigrateMeetingsFromOldCalendarToNew() {
    String commands = String.join("\n",
        "create calendar --name old --timezone UTC",
        "create calendar --name new --timezone UTC",
        "use calendar --name old",
        "create event Meeting1 from 2025-05-01T10:00 to 2025-05-01T11:00",
        "create event Meeting2 from 2025-05-02T14:00 to 2025-05-02T15:00",
        "create event Series from 2025-05-05T09:00 to 2025-05-05T10:00 repeats MTWRF for 5 times",
        "copy events between 2025-05-01 and 2025-05-09 --target new to 2025-05-01",
        "use calendar --name new",
        "print events from 2025-05-01T00:00 to 2025-05-10T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("* Meeting1"));
    assertEquals(true, actual.contains("* Meeting2"));

    int seriesCount = 0;
    int index = 0;
    while ((index = actual.indexOf("* Series", index)) != -1) {
      seriesCount++;
      index += "* Series".length();
    }
    assertEquals("Should copy all 5 series occurrences", 5, seriesCount);
  }

  @Test
  public void testCreateTemplateAndCopyToMultipleDates() {
    String commands = String.join("\n",
        "create calendar --name templates --timezone UTC",
        "create calendar --name schedule --timezone UTC",
        "use calendar --name templates",
        "create event \"Standard Meeting\" from 2025-05-01T10:00 "
            + "to 2025-05-01T11:00",
        "edit event location \"Standard Meeting\" from 2025-05-01T10:00 "
            + "to 2025-05-01T11:00 with \"Conference Room A\"",
        "copy event \"Standard Meeting\" on 2025-05-01T10:00 --target schedule "
            + "to 2025-05-05T10:00",
        "copy event \"Standard Meeting\" on 2025-05-01T10:00 --target schedule "
            + "to 2025-05-12T10:00",
        "copy event \"Standard Meeting\" on 2025-05-01T10:00 --target schedule "
            + "to 2025-05-19T10:00",
        "use calendar --name schedule",
        "print events from 2025-05-05T00:00 to 2025-05-20T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    int count = 0;
    int index = 0;
    while ((index = actual.indexOf("* Standard Meeting", index)) != -1) {
      count++;
      index += "* Standard Meeting".length();
    }
    assertEquals("Should have 3 copies of the template", 3, count);

    int locationCount = 0;
    index = 0;
    while ((index = actual.indexOf("location: Conference Room A", index))
        != -1) {
      locationCount++;
      index += "location: Conference Room A".length();
    }
    assertEquals("All copies should have the location", 3, locationCount);
  }

  @Test
  public void testShareEventsBetweenTeamCalendars() {
    String commands = String.join("\n",
        "create calendar --name engineering --timezone UTC",
        "create calendar --name design --timezone UTC",
        "create calendar --name shared --timezone UTC",
        "use calendar --name engineering",
        "create event \"Sprint Planning\" from 2025-05-05T10:00 "
            + "to 2025-05-05T11:00 repeats MW for 4 times",
        "copy events between 2025-05-05 and 2025-05-14 --target shared "
            + "to 2025-05-05",
        "use calendar --name design",
        "create event \"Design Review\" from 2025-05-06T14:00 "
            + "to 2025-05-06T15:00 repeats T for 3 times",
        "copy events between 2025-05-06 and 2025-05-20 --target shared "
            + "to 2025-05-06",
        "use calendar --name shared",
        "print events from 2025-05-05T00:00 to 2025-05-21T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("* Sprint Planning"));
    assertEquals(true, actual.contains("* Design Review"));
  }

  @Test
  public void testCopyRangePartialDuplicatesSkipped() {
    String commands = String.join("\n",
        "create calendar --name source --timezone UTC",
        "create calendar --name target --timezone UTC",
        "use calendar --name source",
        "create event Event1 from 2025-05-01T10:00 to 2025-05-01T11:00",
        "create event Event2 from 2025-05-02T10:00 to 2025-05-02T11:00",
        "create event Event3 from 2025-05-03T10:00 to 2025-05-03T11:00",
        "use calendar --name target",
        "create event Event2 from 2025-06-02T10:00 to 2025-06-02T11:00",
        "use calendar --name source",
        "copy events between 2025-05-01 and 2025-05-03 --target target to 2025-06-01",
        "use calendar --name target",
        "print events from 2025-06-01T00:00 to 2025-06-05T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("* Event1 starting on 2025-06-01"));

    int event2Count = 0;
    int index = 0;
    while ((index = actual.indexOf("* Event2 starting on 2025-06-02", index)) != -1) {
      event2Count++;
      index += "* Event2 starting on 2025-06-02".length();
    }
    assertEquals("Should only have 1 Event2 (duplicate skipped)", 1, event2Count);
    assertEquals(true, actual.contains("* Event3 starting on 2025-06-03"));
  }

  @Test
  public void testCopyBetweenEuropeanTimezones() {
    String commands = String.join("\n",
        "create calendar --name london --timezone Europe/London",
        "create calendar --name paris --timezone Europe/Paris",
        "use calendar --name london",
        "create event Meeting from 2025-05-01T15:00 to 2025-05-01T16:00",
        "copy events on 2025-05-01 --target paris to 2025-05-01",
        "use calendar --name paris",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("starting on 2025-05-01 at 16:00"));
  }

  @Test
  public void testCopyBetweenAsianTimezones() {
    String commands = String.join("\n",
        "create calendar --name tokyo --timezone Asia/Tokyo",
        "create calendar --name seoul --timezone Asia/Seoul",
        "use calendar --name tokyo",
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00",
        "copy events on 2025-05-01 --target seoul to 2025-05-01",
        "use calendar --name seoul",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("starting on 2025-05-01 at 10:00"));
  }

  @Test
  public void testExportIcalWithDifferentTimezones() {
    String commands = String.join("\n",
        "create calendar --name est --timezone America/New_York",
        "use calendar --name est",
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00",
        "export cal est_calendar.ics",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("Calendar exported successfully"));
  }

  @Test
  public void testExportCsvWithDifferentTimezones() {
    String commands = String.join("\n",
        "create calendar --name pst --timezone America/Los_Angeles",
        "use calendar --name pst",
        "create event Meeting from 2025-05-01T14:00 to 2025-05-01T15:00",
        "export cal pst_calendar.csv",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("Calendar exported successfully"));
  }

  @Test
  public void testCalendarNameWithSpecialCharacters() {
    String commands = String.join("\n",
        "create calendar --name work-2025 --timezone UTC",
        "use calendar --name work-2025",
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("Calendar created: work-2025"));
    assertEquals(true, actual.contains("* Meeting"));
  }

  @Test
  public void testCalendarNameCaseSensitive() {
    String commands = String.join("\n",
        "create calendar --name Work --timezone UTC",
        "create calendar --name work --timezone UTC",
        "use calendar --name Work",
        "create event Event1 from 2025-05-01T10:00 to 2025-05-01T11:00",
        "use calendar --name work",
        "create event Event2 from 2025-05-01T14:00 to 2025-05-01T15:00",
        "use calendar --name Work",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("* Event1"));
    assertEquals(false, actual.contains("* Event2"));
  }

  @Test
  public void testCompleteCalendarManagementWorkflow() {
    String commands = String.join("\n",
        "create calendar --name dev --timezone America/New_York",
        "create calendar --name qa --timezone America/New_York",
        "create calendar --name prod --timezone UTC",
        "",
        "use calendar --name dev",
        "create event Standup from 2025-05-05T09:00 to 2025-05-05T09:30 repeats MTWRF for 10 times",
        "create event \"Sprint Planning\" from 2025-05-05T14:00 to 2025-05-05T16:00",
        "create event \"Sprint Retro\" from 2025-05-16T14:00 to 2025-05-16T15:00",
        "",
        "copy events between 2025-05-05 and 2025-05-16 --target qa to 2025-05-05",
        "",
        "use calendar --name qa",
        "create event \"QA Review\" from 2025-05-08T11:00 to 2025-05-08T12:00",
        "",
        "use calendar --name dev",
        "copy event \"Sprint Planning\" on 2025-05-05T14:00 --target prod to 2025-05-05T18:00",
        "copy event \"Sprint Retro\" on 2025-05-16T14:00 --target prod to 2025-05-16T18:00",
        "",
        "edit calendar --name dev --property timezone America/Los_Angeles",
        "",
        "use calendar --name prod",
        "print events from 2025-05-05T00:00 to 2025-05-17T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("* Sprint Planning"));
    assertEquals(true, actual.contains("* Sprint Retro"));
  }

  @Test
  public void testCopyEventsAcrossMonths() {
    String commands = String.join("\n",
        "create calendar --name source --timezone UTC",
        "create calendar --name target --timezone UTC",
        "use calendar --name source",
        "create event Event1 from 2025-04-30T10:00 to 2025-04-30T11:00",
        "create event Event2 from 2025-05-01T10:00 to 2025-05-01T11:00",
        "create event Event3 from 2025-05-02T10:00 to 2025-05-02T11:00",
        "copy events between 2025-04-30 and 2025-05-02 --target target to 2025-06-15",
        "use calendar --name target",
        "print events from 2025-06-15T00:00 to 2025-06-20T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("* Event1 starting on 2025-06-15"));
    assertEquals(true, actual.contains("* Event2 starting on 2025-06-16"));
    assertEquals(true, actual.contains("* Event3 starting on 2025-06-17"));
  }

  @Test
  public void testCopyEventsAcrossYears() {
    String commands = String.join("\n",
        "create calendar --name source --timezone UTC",
        "create calendar --name target --timezone UTC",
        "use calendar --name source",
        "create event Meeting from 2025-12-30T10:00 to 2025-12-30T11:00",
        "copy event Meeting on 2025-12-30T10:00 --target target to 2026-01-02T10:00",
        "use calendar --name target",
        "print events on 2026-01-02",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("* Meeting starting on 2026-01-02 at 10:00"));
  }

  @Test
  public void testCopyEventsWithinSameCalendar() {
    String commands = String.join("\n",
        "create calendar --name work --timezone UTC",
        "use calendar --name work",
        "create event Weekly from 2025-05-05T10:00 to 2025-05-05T11:00 repeats MW for 2 times",
        "copy events between 2025-05-05 and 2025-05-07 --target work to 2025-05-12",
        "print events from 2025-05-05T00:00 to 2025-05-20T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    int count = 0;
    int index = 0;
    while ((index = actual.indexOf("* Weekly", index)) != -1) {
      count++;
      index += "* Weekly".length();
    }
    assertEquals("Should have 4 total (2 original + 2 copied)", 4, count);
  }

  @Test
  public void testCopySingleEventMultipleTimes() {
    String commands = String.join("\n",
        "create calendar --name source --timezone UTC",
        "create calendar --name target --timezone UTC",
        "use calendar --name source",
        "create event Template from 2025-05-01T10:00 to 2025-05-01T11:00",
        "copy event Template on 2025-05-01T10:00 --target target to 2025-06-01T10:00",
        "copy event Template on 2025-05-01T10:00 --target target to 2025-06-08T10:00",
        "copy event Template on 2025-05-01T10:00 --target target to 2025-06-15T10:00",
        "use calendar --name target",
        "print events from 2025-06-01T00:00 to 2025-06-20T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    int count = 0;
    int index = 0;
    while ((index = actual.indexOf("* Template", index)) != -1) {
      count++;
      index += "* Template".length();
    }
    assertEquals("Should have 3 copies", 3, count);
  }

  @Test
  public void testAllMajorTimezones() {
    String commands = String.join("\n",
        "create calendar --name utc --timezone UTC",
        "create calendar --name est --timezone America/New_York",
        "create calendar --name pst --timezone America/Los_Angeles",
        "create calendar --name gmt --timezone Europe/London",
        "create calendar --name jst --timezone Asia/Tokyo",
        "use calendar --name utc",
        "create event UTCEvent from 2025-05-01T12:00 to 2025-05-01T13:00",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("Calendar created: utc"));
    assertEquals(true, actual.contains("Calendar created: est"));
    assertEquals(true, actual.contains("Calendar created: pst"));
    assertEquals(true, actual.contains("Calendar created: gmt"));
    assertEquals(true, actual.contains("Calendar created: jst"));
    assertEquals(true, actual.contains("* UTCEvent"));
  }

  @Test
  public void testEditCalendarNameWithMultipleEvents() {
    String commands = String.join("\n",
        "create calendar --name temp --timezone UTC",
        "use calendar --name temp",
        "create event Event1 from 2025-05-01T10:00 to 2025-05-01T11:00",
        "create event Event2 from 2025-05-02T10:00 to 2025-05-02T11:00",
        "create event Series from 2025-05-05T09:00 to 2025-05-05T10:00 repeats MW for 3 times",
        "edit calendar --name temp --property name permanent",
        "print events from 2025-05-01T00:00 to 2025-05-10T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("* Event1"));
    assertEquals(true, actual.contains("* Event2"));
    assertEquals(true, actual.contains("* Series"));
  }

  @Test
  public void testEditCalendarTimezoneWithMultipleEvents() {
    String commands = String.join("\n",
        "create calendar --name work --timezone UTC",
        "use calendar --name work",
        "create event Morning from 2025-05-01T08:00 to 2025-05-01T09:00",
        "create event Noon from 2025-05-01T12:00 to 2025-05-01T13:00",
        "create event Evening from 2025-05-01T18:00 to 2025-05-01T19:00",
        "edit calendar --name work --property timezone America/New_York",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("starting on 2025-05-01 at 04:00"));
    assertEquals(true, actual.contains("starting on 2025-05-01 at 08:00"));
    assertEquals(true, actual.contains("starting on 2025-05-01 at 14:00"));
  }

  @Test
  public void testCopyEventsRangePreservesEventOrder() {
    String commands = String.join("\n",
        "create calendar --name source --timezone UTC",
        "create calendar --name target --timezone UTC",
        "use calendar --name source",
        "create event Third from 2025-05-03T14:00 to 2025-05-03T15:00",
        "create event First from 2025-05-01T09:00 to 2025-05-01T10:00",
        "create event Second from 2025-05-02T11:00 to 2025-05-02T12:00",
        "copy events between 2025-05-01 and 2025-05-03 --target target to 2025-06-01",
        "use calendar --name target",
        "print events from 2025-06-01T00:00 to 2025-06-05T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    int firstIdx = actual.indexOf("* First");
    int secondIdx = actual.indexOf("* Second");
    int thirdIdx = actual.indexOf("* Third");

    assertEquals(true, firstIdx > 0 && secondIdx > firstIdx);
    assertEquals(true, secondIdx > 0 && thirdIdx > secondIdx);
  }

  @Test
  public void testCopyMixedDurationEvents() {
    String commands = String.join("\n",
        "create calendar --name source --timezone UTC",
        "create calendar --name target --timezone UTC",
        "use calendar --name source",
        "create event Short from 2025-05-01T10:00 to 2025-05-01T10:15",
        "create event Medium from 2025-05-01T11:00 to 2025-05-01T12:00",
        "create event Long from 2025-05-01T14:00 to 2025-05-01T18:00",
        "copy events on 2025-05-01 --target target to 2025-06-01",
        "use calendar --name target",
        "print events on 2025-06-01",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("ending on 2025-06-01 at 10:15"));
    assertEquals(true, actual.contains("ending on 2025-06-01 at 12:00"));
    assertEquals(true, actual.contains("ending on 2025-06-01 at 18:00"));
  }

  @Test
  public void testFullFeatureIntegration() {
    String commands = String.join("\n",
        "create calendar --name main --timezone America/New_York",
        "create calendar --name backup --timezone America/Los_Angeles",
        "create calendar --name archive --timezone UTC",
        "",
        "use calendar --name main",
        "create event Daily from 2025-05-05T09:00 to 2025-05-05T09:30 "
            + "repeats MTWRF for 5 times",
        "create event \"Important Meeting\" from 2025-05-06T14:00 "
            + "to 2025-05-06T15:00",
        "edit event location \"Important Meeting\" from 2025-05-06T14:00 "
            + "to 2025-05-06T15:00 with \"Boardroom\"",
        "",
        "copy events between 2025-05-05 and 2025-05-09 --target backup "
            + "to 2025-05-05",
        "",
        "edit calendar --name main --property timezone UTC",
        "",
        "copy events between 2025-05-05 and 2025-05-09 --target archive "
            + "to 2025-06-01",
        "",
        "edit calendar --name backup --property name secondary",
        "",
        "use calendar --name archive",
        "print events from 2025-06-01T00:00 to 2025-06-10T23:59",
        "",
        "export cal archive.ics",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("* Daily"));
    assertEquals(true, actual.contains("* Important Meeting"));
    assertEquals(true, actual.contains("location: Boardroom"));
    assertEquals(true, actual.contains("Calendar exported successfully"));
    assertEquals(true, actual.contains("Calendar property edited successfully"));
  }

  @Test
  public void testCopyEventsWithVeryLongRange() {
    String commands = String.join("\n",
        "create calendar --name source --timezone UTC",
        "create calendar --name target --timezone UTC",
        "use calendar --name source",
        "create event Start from 2025-01-01T10:00 to 2025-01-01T11:00",
        "create event Middle from 2025-06-15T10:00 to 2025-06-15T11:00",
        "create event End from 2025-12-31T10:00 to 2025-12-31T11:00",
        "copy events between 2025-01-01 and 2025-12-31 --target target to 2026-01-01",
        "use calendar --name target",
        "print events from 2026-01-01T00:00 to 2026-01-05T23:59",
        "print events from 2026-06-15T00:00 to 2026-06-20T23:59",
        "print events from 2026-12-30T00:00 to 2027-01-05T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("* Start starting on 2026-01-01"));
    assertEquals(true, actual.contains("* Middle starting on 2026-06-15"));
    assertEquals(true, actual.contains("* End starting on 2026-12-31"));
  }

  @Test
  public void testCopyEventsBetweenCalendarsWithSameTimezone() {
    String commands = String.join("\n",
        "create calendar --name cal1 --timezone America/New_York",
        "create calendar --name cal2 --timezone America/New_York",
        "use calendar --name cal1",
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00",
        "copy event Meeting on 2025-05-01T10:00 --target cal2 to 2025-05-01T10:00",
        "use calendar --name cal2",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("starting on 2025-05-01 at 10:00"));
    assertEquals(true, actual.contains("ending on 2025-05-01 at 11:00"));
  }

  @Test
  public void testCopyEventsRangeOneDay() {
    String commands = String.join("\n",
        "create calendar --name source --timezone UTC",
        "create calendar --name target --timezone UTC",
        "use calendar --name source",
        "create event AllDay from 2025-05-01T09:00 to 2025-05-01T17:00",
        "copy events between 2025-05-01 and 2025-05-01 --target target to 2025-06-01",
        "use calendar --name target",
        "print events on 2025-06-01",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("* AllDay starting on 2025-06-01"));
  }

  @Test
  public void testDetailedErrorMessages() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Test from 2025-05-01T10:00 to 2025-05-01T11:00",
        "copy event Test on 2025-05-01T10:00 --target nonexistent to 2025-06-01T10:00",
        "exit"
    );
    runCommands(commands);

    String actual = errorOutput.toString();
    assertEquals(true, actual.contains("not found") || actual.contains("Error"));
  }

  @Test
  public void testSuccessMessagesForAllOperations() {
    String commands = String.join("\n",
        "create calendar --name work --timezone UTC",
        "use calendar --name work",
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00",
        "create calendar --name backup --timezone UTC",
        "copy event Meeting on 2025-05-01T10:00 --target backup to 2025-06-01T10:00",
        "edit calendar --name work --property name main",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("Calendar created: work"));
    assertEquals(true, actual.contains("Now using calendar: work"));
    assertEquals(true, actual.contains("Event created: Meeting"));
    assertEquals(true, actual.contains("Calendar created: backup"));
    assertEquals(true, actual.contains("Event copied successfully"));
    assertEquals(true, actual.contains("Calendar property edited successfully"));
  }

  @Test
  public void testExportCsvFormatDetection() {
    String commands = String.join("\n",
        "create calendar --name work --timezone UTC",
        "use calendar --name work",
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00",
        "export cal test.CSV",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("Calendar exported successfully"));
  }

  @Test
  public void testExportIcsExtension() {
    String commands = String.join("\n",
        "create calendar --name work --timezone UTC",
        "use calendar --name work",
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00",
        "export cal test.ics",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("Calendar exported successfully"));
  }

  @Test
  public void testExportInvalidExtension() {
    String commands = String.join("\n",
        "create calendar --name work --timezone UTC",
        "use calendar --name work",
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00",
        "export cal test.txt",
        "exit"
    );
    runCommands(commands);

    String expected = "Unsupported file format";
    String actual = errorOutput.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testExportNoExtension() {
    String commands = String.join("\n",
        "create calendar --name work --timezone UTC",
        "use calendar --name work",
        "export cal testfile",
        "exit"
    );
    runCommands(commands);

    String expected = "extension";
    String actual = errorOutput.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testTimezoneConversionCrossingMidnight() {
    String commands = String.join("\n",
        "create calendar --name est --timezone America/New_York",
        "use calendar --name est",
        "create event LateNight from 2025-05-01T23:00 to 2025-05-02T01:00",
        "edit calendar --name est --property timezone Asia/Tokyo",
        "print events on 2025-05-02",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();

    assertEquals(true, actual.contains("starting on 2025-05-02 at 12:00"));
    assertEquals(true, actual.contains("ending on 2025-05-02 at 14:00"));
  }

  @Test
  public void testCopySeriesWithTimezoneConversionSpansMultipleDays() {
    String commands = String.join("\n",
        "create calendar --name est --timezone America/New_York",
        "create calendar --name tokyo --timezone Asia/Tokyo",
        "use calendar --name est",
        "create event Evening from 2025-05-05T22:00 to 2025-05-05T23:00 repeats MW for 3 times",
        "copy events between 2025-05-05 and 2025-05-12 --target tokyo to 2025-06-02",
        "use calendar --name tokyo",
        "print events from 2025-06-02T00:00 to 2025-06-15T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();

    assertEquals(true, actual.contains("* Evening"));
    assertEquals(true, actual.contains("at 11:00"));

    int count = 0;
    int index = 0;
    while ((index = actual.indexOf("* Evening", index)) != -1) {
      count++;
      index += "* Evening".length();
    }
    assertEquals("Should copy all 3 occurrences", 3, count);
  }

  @Test
  public void testCopyEventWithExtraSpaces() {
    String commands = String.join("\n",
        "create calendar --name source --timezone UTC",
        "create calendar --name target --timezone UTC",
        "use calendar --name source",
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00",
        "copy event   Meeting   on   2025-05-01T10:00   --target   target   to   2025-06-01T10:00",
        "use calendar --name target",
        "print events on 2025-06-01",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("* Meeting"));
  }

  @Test
  public void testCopyThenEditCalendarTimezone() {
    String commands = String.join("\n",
        "create calendar --name source --timezone UTC",
        "create calendar --name target --timezone UTC",
        "use calendar --name source",
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00",
        "copy event Meeting on 2025-05-01T10:00 --target target to 2025-06-01T10:00",
        "edit calendar --name source --property timezone America/New_York",
        "use calendar --name source",
        "print events on 2025-05-01",
        "use calendar --name target",
        "print events on 2025-06-01",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("starting on 2025-05-01 at 06:00"));
    assertEquals(true, actual.contains("starting on 2025-06-01 at 10:00"));
  }

  @Test
  public void testCopySingleOccurrenceFromSeries() {
    String commands = String.join("\n",
        "create calendar --name source --timezone UTC",
        "create calendar --name target --timezone UTC",
        "use calendar --name source",
        "create event Weekly from 2025-05-05T10:00 to 2025-05-05T11:00 repeats MW for 4 times",
        "copy event Weekly on 2025-05-07T10:00 --target target to 2025-06-01T10:00",
        "use calendar --name target",
        "print events on 2025-06-01",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("* Weekly starting on 2025-06-01 at 10:00"));

    int count = 0;
    int index = 0;
    while ((index = actual.indexOf("* Weekly", index)) != -1) {
      count++;
      index += "* Weekly".length();
    }
    assertEquals("Should copy only 1 occurrence", 1, count);
  }

  @Test
  public void testCopyEventsOnDateWithSeries() {
    String commands = String.join("\n",
        "create calendar --name source --timezone UTC",
        "create calendar --name target --timezone UTC",
        "use calendar --name source",
        "create event Series from 2025-05-05T10:00 to 2025-05-05T11:00 repeats MW for 4 times",
        "create event Standalone from 2025-05-05T14:00 to 2025-05-05T15:00",
        "copy events on 2025-05-05 --target target to 2025-06-01",
        "use calendar --name target",
        "print events on 2025-06-01",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("* Series"));
    assertEquals(true, actual.contains("* Standalone"));

    int count = 0;
    int index = 0;
    while ((index = actual.indexOf("starting on 2025-06-01", index)) != -1) {
      count++;
      index += "starting on 2025-06-01".length();
    }
    assertEquals("Should copy both events", 2, count);
  }

  @Test
  public void testCopyPartialSeriesStartsBeforeRange() {
    String commands = String.join("\n",
        "create calendar --name source --timezone UTC",
        "create calendar --name target --timezone UTC",
        "use calendar --name source",
        "create event Weekly from 2025-05-05T10:00 to 2025-05-05T11:00 repeats MW for 8 times",
        "copy events between 2025-05-12 and 2025-05-26 --target target to 2025-06-01",
        "use calendar --name target",
        "print events from 2025-06-01T00:00 to 2025-06-30T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    int count = 0;
    int index = 0;
    while ((index = actual.indexOf("* Weekly", index)) != -1) {
      count++;
      index += "* Weekly".length();
    }
    assertEquals(true, count >= 4 && count <= 5);
  }

  @Test
  public void testCopyPartialSeriesEndsAfterRange() {
    String commands = String.join("\n",
        "create calendar --name source --timezone UTC",
        "create calendar --name target --timezone UTC",
        "use calendar --name source",
        "create event Weekly from 2025-05-05T10:00 to 2025-05-05T11:00 repeats MW for 10 times",
        "copy events between 2025-05-05 and 2025-05-09 --target target to 2025-06-01",
        "use calendar --name target",
        "print events from 2025-06-01T00:00 to 2025-06-30T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    int count = 0;
    int index = 0;
    while ((index = actual.indexOf("* Weekly", index)) != -1) {
      count++;
      index += "* Weekly".length();
    }
    assertEquals("Should copy only 2 occurrences in range", 2, count);
  }

  @Test
  public void testCopySeriesWithUntilDate() {
    String commands = String.join("\n",
        "create calendar --name source --timezone UTC",
        "create calendar --name target --timezone UTC",
        "use calendar --name source",
        "create event Weekly from 2025-05-05T10:00 to 2025-05-05T11:00 repeats MW until 2025-05-30",
        "copy events between 2025-05-05 and 2025-05-30 --target target to 2025-06-02",
        "use calendar --name target",
        "print events from 2025-06-02T00:00 to 2025-06-30T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("* Weekly"));

    int count = 0;
    int index = 0;
    while ((index = actual.indexOf("* Weekly", index)) != -1) {
      count++;
      index += "* Weekly".length();
    }
    assertEquals(true, count >= 7 && count <= 9);
  }

  @Test
  public void testCopySeriesRangeWithNoOccurrences() {
    String commands = String.join("\n",
        "create calendar --name source --timezone UTC",
        "create calendar --name target --timezone UTC",
        "use calendar --name source",
        "create event Weekly from 2025-05-05T10:00 to 2025-05-05T11:00 repeats MW for 4 times",
        "copy events between 2025-05-06 and 2025-05-06 --target target to 2025-06-01",
        "use calendar --name target",
        "print events on 2025-06-01",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("No events found"));
  }

  @Test
  public void testCopyAllDaySeriesWithTimezoneConversion() {
    String commands = String.join("\n",
        "create calendar --name est --timezone America/New_York",
        "create calendar --name pst --timezone America/Los_Angeles",
        "use calendar --name est",
        "create event Holiday on 2025-05-05 repeats MW for 3 times",
        "copy events between 2025-05-05 and 2025-05-12 --target pst to 2025-06-02",
        "use calendar --name pst",
        "print events from 2025-06-02T00:00 to 2025-06-15T23:59",
        "exit"
    );
    runCommands(commands);

    String actual = output.toString();
    assertEquals(true, actual.contains("starting on 2025-06-02 at 05:00"));
    assertEquals(true, actual.contains("ending on 2025-06-02 at 14:00"));

    int count = 0;
    int index = 0;
    while ((index = actual.indexOf("* Holiday", index)) != -1) {
      count++;
      index += "* Holiday".length();
    }
    assertEquals("Should copy all 3 occurrences", 3, count);
  }

  @Test
  public void testSeriesExceedingMaxOccurrencesFails() {
    String commands = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Daily from 2025-05-01T10:00 to 2025-05-01T11:00 "
            + "repeats MTWRFSU for 400 times",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String expected = "Series too long";
    String actual = errorOutput.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testCopyEventCommandWithoutTarget() {
    String commands = String.join("\n",
        "create calendar --name work --timezone UTC",
        "use calendar --name work",
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00",
        "copy event Meeting on 2025-05-01T10:00 to 2025-06-01T10:00",
        "exit"
    );
    runCommands(commands);

    String expected = "Invalid command";
    String actual = errorOutput.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testCopyEventsToSameCalendarSameDate() {
    String commands = String.join("\n",
        "create calendar --name work --timezone UTC",
        "use calendar --name work",
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00",
        "copy event Meeting on 2025-05-01T10:00 --target work to 2025-05-01T10:00",
        "print events on 2025-05-01",
        "exit"
    );
    runCommands(commands);

    String expected = "already exist";
    String actual = errorOutput.toString();
    assertEquals(true, actual.contains(expected));
  }

  @Test
  public void testCreateCalendarWithOnlySpaces() {
    String commands = String.join("\n",
        "create calendar --name \"   \" --timezone UTC",
        "exit"
    );
    runCommands(commands);

    String actual = errorOutput.toString();
    assertEquals(true, actual.contains("Invalid command"));
  }
}