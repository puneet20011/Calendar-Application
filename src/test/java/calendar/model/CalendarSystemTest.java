package calendar.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for CalendarSystem class functionality.
 * Tests multi-calendar management, event operations, and calendar context switching.
 */
public class CalendarSystemTest {

  private CalendarSystem system;

  /**
   * Sets up the test fixture.
   */
  @Before
  public void setUp() {
    system = new CalendarSystem();
  }

  /**
   * Clears references after each test run.
   */
  @After
  public void tearDown() {
    system = null;
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateCalendarDuplicateName() {
    system.createCalendar("work", "America/New_York");
    system.createCalendar("work", "UTC");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateCalendarEmptyName() {
    system.createCalendar("", "UTC");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateCalendarNullName() {
    system.createCalendar(null, "UTC");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateCalendarWhitespaceName() {
    system.createCalendar("   ", "UTC");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateCalendarInvalidTimezone() {
    system.createCalendar("work", "InvalidTimezone");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUseNonExistentCalendar() {
    system.useCalendar("nonexistent");
  }

  @Test
  public void testEditCalendarName() {
    system.createCalendar("work", "UTC");
    system.editCalendar("work", "name", "office");
    system.useCalendar("office");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditCalendarNameToExisting() {
    system.createCalendar("work", "UTC");
    system.createCalendar("personal", "UTC");
    system.editCalendar("work", "name", "personal");
  }

  @Test
  public void testEditCalendarTimezone() {
    system.createCalendar("work", "America/New_York");
    system.useCalendar("work");

    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    system.createEvent("Meeting", start, end);

    system.editCalendar("work", "timezone", "America/Los_Angeles");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditCalendarInvalidTimezone() {
    system.createCalendar("work", "UTC");
    system.editCalendar("work", "timezone", "Invalid/Timezone");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditNonExistentCalendar() {
    system.editCalendar("nonexistent", "name", "newname");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditCalendarInvalidProperty() {
    system.createCalendar("work", "UTC");
    system.editCalendar("work", "invalid", "value");
  }

  @Test(expected = IllegalStateException.class)
  public void testIsBusyWithoutUsingCalendar() {
    system.isBusy(LocalDateTime.of(2025, 1, 15, 10, 0));
  }

  @Test(expected = IllegalStateException.class)
  public void testEditEventWithoutUsingCalendar() {
    system.editEvent("Meeting", LocalDateTime.of(2025, 1, 15, 10, 0), "subject", "New");
  }

  @Test(expected = IllegalStateException.class)
  public void testGetAllEventsWithoutUsingCalendar() {
    system.getAllEvents();
  }

  @Test(expected = IllegalStateException.class)
  public void testGetCurrentCalendarTimezoneWithoutUsingCalendar() {
    system.getCurrentCalendarTimezone();
  }

  @Test
  public void testCreateEventInCalendar() {
    system.createCalendar("work", "UTC");
    system.useCalendar("work");

    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    system.createEvent("Meeting", start, end);

    List<Ievent> events = system.getByDate(LocalDate.of(2025, 1, 15));
    assertEquals(1, events.size());
    assertEquals("Meeting", events.get(0).getSubject());
  }

  @Test
  public void testCreateEventInMultipleCalendars() {
    system.createCalendar("work", "UTC");
    system.createCalendar("personal", "UTC");

    system.useCalendar("work");
    system.createEvent("Work Meeting", LocalDateTime.of(2025, 1, 15, 10, 0),
        LocalDateTime.of(2025, 1, 15, 11, 0));

    system.useCalendar("personal");
    system.createEvent("Dentist", LocalDateTime.of(2025, 1, 15, 14, 0),
        LocalDateTime.of(2025, 1, 15, 15, 0));

    List<Ievent> personalEvents = system.getByDate(LocalDate.of(2025, 1, 15));
    assertEquals(1, personalEvents.size());
    assertEquals("Dentist", personalEvents.get(0).getSubject());

    system.useCalendar("work");
    List<Ievent> workEvents = system.getByDate(LocalDate.of(2025, 1, 15));
    assertEquals(1, workEvents.size());
    assertEquals("Work Meeting", workEvents.get(0).getSubject());
  }

  @Test
  public void testCreateEventSeriesInCalendar() {
    system.createCalendar("work", "UTC");
    system.useCalendar("work");

    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    system.createEventSeries("Daily Standup", start, end, "MTWRF", 5, null);

    List<Ievent> events = system.getByDate(LocalDate.of(2025, 1, 15));
    assertEquals(1, events.size());
    assertNotNull(((Event) events.get(0)).getSeries());

    EventSeries series = ((Event) events.get(0)).getSeries();
    assertEquals(5, series.getAllOccurrences().size());
  }

  @Test
  public void testCreateAllDayEventInCalendar() {
    system.createCalendar("work", "UTC");
    system.useCalendar("work");

    system.createAllDayEvent("Holiday", LocalDateTime.of(2025, 1, 15, 0, 0));

    List<Ievent> events = system.getByDate(LocalDate.of(2025, 1, 15));
    assertEquals(1, events.size());
    assertEquals("Holiday", events.get(0).getSubject());
  }

  @Test
  public void testGetByRangeInCalendar() {
    system.createCalendar("work", "UTC");
    system.useCalendar("work");

    system.createEvent("Event 1", LocalDateTime.of(2025, 1, 15, 10, 0),
        LocalDateTime.of(2025, 1, 15, 11, 0));
    system.createEvent("Event 2", LocalDateTime.of(2025, 1, 16, 14, 0),
        LocalDateTime.of(2025, 1, 16, 15, 0));

    List<Ievent> events = system.getByRange(
        LocalDateTime.of(2025, 1, 15, 0, 0),
        LocalDateTime.of(2025, 1, 17, 0, 0));

    assertEquals(2, events.size());
  }

  @Test
  public void testIsBusyInCalendar() {
    system.createCalendar("work", "UTC");
    system.useCalendar("work");

    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    system.createEvent("Meeting", start, end);

    assertTrue(system.isBusy(start));
    assertFalse(system.isBusy(end));
  }

  @Test
  public void testEditEventInCalendar() {
    system.createCalendar("work", "UTC");
    system.useCalendar("work");

    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    system.createEvent("Meeting", start, end);

    system.editEvent("Meeting", start, "subject", "Updated Meeting");

    List<Ievent> events = system.getByDate(LocalDate.of(2025, 1, 15));
    assertEquals("Updated Meeting", events.get(0).getSubject());
  }

  @Test
  public void testEditEventsInCalendar() {
    system.createCalendar("work", "UTC");
    system.useCalendar("work");

    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    system.createEventSeries("Weekly", start, end, "W", 3, null);

    system.editEvents("Weekly", start, "location", "Room 101");

    List<Ievent> events = system.getByDate(LocalDate.of(2025, 1, 15));
    assertEquals("Room 101", events.get(0).getLocation());
  }

  @Test
  public void testEditSeriesInCalendar() {
    system.createCalendar("work", "UTC");
    system.useCalendar("work");

    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    system.createEventSeries("Weekly", start, end, "W", 3, null);

    system.editSeries("Weekly", start, "description", "Updated for all");

    List<Ievent> events = system.getByDate(LocalDate.of(2025, 1, 15));
    assertEquals("Updated for all", events.get(0).getDescription());
  }

  @Test
  public void testGetAllEvents() {
    system.createCalendar("work", "UTC");
    system.useCalendar("work");

    LocalDateTime start1 = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end1 = LocalDateTime.of(2025, 1, 15, 11, 0);
    LocalDateTime start2 = LocalDateTime.of(2025, 1, 16, 14, 0);
    LocalDateTime end2 = LocalDateTime.of(2025, 1, 16, 15, 0);

    system.createEvent("Event 1", start1, end1);
    system.createEvent("Event 2", start2, end2);

    List<Ievent> allEvents = system.getAllEvents();
    assertEquals("Should have 2 events", 2, allEvents.size());
  }

  @Test
  public void testGetCurrentCalendarTimezone() {
    system.createCalendar("work", "America/New_York");
    system.useCalendar("work");

    String timezone = system.getCurrentCalendarTimezone();
    assertEquals("Should return calendar timezone", "America/New_York", timezone);
  }


  @Test
  public void testCopyEventBetweenCalendars() {
    system.createCalendar("source", "UTC");
    system.createCalendar("target", "UTC");

    system.useCalendar("source");
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    system.createEvent("Meeting", start, end);

    LocalDateTime targetStart = LocalDateTime.of(2025, 1, 20, 10, 0);
    system.copyEvent("Meeting", start, "target", targetStart);

    system.useCalendar("target");
    List<Ievent> events = system.getByDate(LocalDate.of(2025, 1, 20));
    assertEquals(1, events.size());
    assertEquals("Meeting", events.get(0).getSubject());
  }

  @Test(expected = IllegalStateException.class)
  public void testCopyEventWithoutUsingCalendar() {
    system.createCalendar("target", "UTC");
    system.copyEvent("Meeting", LocalDateTime.of(2025, 1, 15, 10, 0),
        "target", LocalDateTime.of(2025, 1, 20, 10, 0));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCopyEventToNonExistentCalendar() {
    system.createCalendar("source", "UTC");
    system.useCalendar("source");
    system.createEvent("Meeting", LocalDateTime.of(2025, 1, 15, 10, 0),
        LocalDateTime.of(2025, 1, 15, 11, 0));

    system.copyEvent("Meeting", LocalDateTime.of(2025, 1, 15, 10, 0),
        "nonexistent", LocalDateTime.of(2025, 1, 20, 10, 0));
  }

  @Test
  public void testCopyEventsOnDate() {
    system.createCalendar("source", "UTC");
    system.createCalendar("target", "UTC");

    system.useCalendar("source");
    system.createEvent("Event 1", LocalDateTime.of(2025, 1, 15, 10, 0),
        LocalDateTime.of(2025, 1, 15, 11, 0));
    system.createEvent("Event 2", LocalDateTime.of(2025, 1, 15, 14, 0),
        LocalDateTime.of(2025, 1, 15, 15, 0));

    system.copyEventsOnDate(LocalDate.of(2025, 1, 15), "target",
        LocalDate.of(2025, 1, 20));

    system.useCalendar("target");
    List<Ievent> events = system.getByDate(LocalDate.of(2025, 1, 20));
    assertEquals(2, events.size());
  }

  @Test
  public void testCopyEventsInRange() {
    system.createCalendar("source", "UTC");
    system.createCalendar("target", "UTC");

    system.useCalendar("source");
    system.createEvent("Event 1", LocalDateTime.of(2025, 1, 15, 10, 0),
        LocalDateTime.of(2025, 1, 15, 11, 0));
    system.createEvent("Event 2", LocalDateTime.of(2025, 1, 16, 14, 0),
        LocalDateTime.of(2025, 1, 16, 15, 0));
    system.createEvent("Event 3", LocalDateTime.of(2025, 1, 17, 9, 0),
        LocalDateTime.of(2025, 1, 17, 10, 0));

    system.copyEventsInRange(LocalDate.of(2025, 1, 15),
        LocalDate.of(2025, 1, 16), "target", LocalDate.of(2025, 2, 1));

    system.useCalendar("target");
    List<Ievent> events = system.getByRange(
        LocalDateTime.of(2025, 2, 1, 0, 0),
        LocalDateTime.of(2025, 2, 3, 0, 0));
    assertEquals(2, events.size());
  }

  @Test
  public void testEditCalendarNamePreservesCurrentContext() {
    system.createCalendar("work", "UTC");
    system.useCalendar("work");
    system.createEvent("Meeting", LocalDateTime.of(2025, 1, 15, 10, 0),
        LocalDateTime.of(2025, 1, 15, 11, 0));

    system.editCalendar("work", "name", "office");

    List<Ievent> events = system.getByDate(LocalDate.of(2025, 1, 15));
    assertEquals(1, events.size());
  }

  @Test
  public void testHasActiveCalendar() throws Exception {
    java.lang.reflect.Method method = CalendarSystem.class
        .getDeclaredMethod("hasActiveCalendar");
    method.setAccessible(true);

    assertFalse((Boolean) method.invoke(system));

    system.createCalendar("work", "UTC");
    system.useCalendar("work");

    assertTrue((Boolean) method.invoke(system));
  }

  @Test
  public void testGetCurrentCalendarName() throws Exception {
    java.lang.reflect.Method method = CalendarSystem.class
        .getDeclaredMethod("getCurrentCalendarName");
    method.setAccessible(true);

    assertEquals(null, method.invoke(system));

    system.createCalendar("work", "UTC");
    system.useCalendar("work");

    assertEquals("work", method.invoke(system));
  }

  @Test
  public void testEditCalendarTimezoneConvertsAllEvents() {
    system.createCalendar("work", "America/New_York");
    system.useCalendar("work");

    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 14, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 15, 0);
    system.createEvent("Meeting", start, end);

    system.editCalendar("work", "timezone", "America/Los_Angeles");

    Event converted = (Event) system.getByDate(LocalDate.of(2025, 1, 15)).get(0);
    assertEquals("Event should be converted to PST",
        LocalTime.of(11, 0), converted.getStart().toLocalTime());
    assertEquals("End should also be converted",
        LocalTime.of(12, 0), converted.getEnd().toLocalTime());
  }

  @Test
  public void testEditCalendarTimezoneActuallyUpdatesTimezone() {
    system.createCalendar("work", "America/New_York");
    system.useCalendar("work");

    system.editCalendar("work", "timezone", "Europe/London");

    String newTimezone = system.getCurrentCalendarTimezone();
    assertEquals("Timezone should be updated", "Europe/London", newTimezone);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCopyEventsOnDateToNonExistentCalendar() {
    system.createCalendar("source", "UTC");
    system.useCalendar("source");

    system.createEvent("Event", LocalDateTime.of(2025, 1, 15, 10, 0),
        LocalDateTime.of(2025, 1, 15, 11, 0));

    system.copyEventsOnDate(LocalDate.of(2025, 1, 15), "nonexistent",
        LocalDate.of(2025, 1, 20));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCopyEventsInRangeToNonExistentCalendar() {
    system.createCalendar("source", "UTC");
    system.useCalendar("source");

    system.createEvent("Event", LocalDateTime.of(2025, 1, 15, 10, 0),
        LocalDateTime.of(2025, 1, 15, 11, 0));

    system.copyEventsInRange(LocalDate.of(2025, 1, 15), LocalDate.of(2025, 1, 17),
        "nonexistent", LocalDate.of(2025, 2, 1));
  }

  @Test(expected = IllegalStateException.class)
  public void testCreateEventSeriesWithoutUsingCalendar() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    system.createEventSeries("Meeting", start, end, "MWF", 3, null);
  }

  @Test(expected = IllegalStateException.class)
  public void testCreateAllDayEventWithoutUsingCalendar() {
    LocalDateTime date = LocalDateTime.of(2025, 1, 15, 0, 0);
    system.createAllDayEvent("Holiday", date);
  }

  @Test(expected = IllegalStateException.class)
  public void testGetByRangeWithoutUsingCalendar() {
    system.getByRange(
        LocalDateTime.of(2025, 1, 15, 0, 0),
        LocalDateTime.of(2025, 1, 17, 0, 0));
  }

  @Test(expected = IllegalStateException.class)
  public void testEditEventsWithoutUsingCalendar() {
    system.editEvents("Meeting", LocalDateTime.of(2025, 1, 15, 10, 0),
        "location", "Room 101");
  }

  @Test(expected = IllegalStateException.class)
  public void testEditSeriesWithoutUsingCalendar() {
    system.editSeries("Meeting", LocalDateTime.of(2025, 1, 15, 10, 0),
        "location", "Room 101");
  }

  @Test(expected = IllegalStateException.class)
  public void testCopyEventsOnDateWithoutUsingCalendar() {
    system.createCalendar("target", "UTC");
    system.copyEventsOnDate(LocalDate.of(2025, 1, 15), "target",
        LocalDate.of(2025, 1, 20));
  }

  @Test(expected = IllegalStateException.class)
  public void testCopyEventsInRangeWithoutUsingCalendar() {
    system.createCalendar("target", "UTC");
    system.copyEventsInRange(LocalDate.of(2025, 1, 15),
        LocalDate.of(2025, 1, 17), "target",
        LocalDate.of(2025, 2, 1));
  }
}