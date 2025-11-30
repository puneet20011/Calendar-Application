package calendar.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for CalendarTimezoneConverter class functionality.
 * Tests timezone conversion operations for calendar events.
 */
public class CalendarTimezoneConverterTest {

  private CalendarModel model;
  private CalendarTimezoneConverter converter;

  /**
   * Sets up the test fixture.
   */
  @Before
  public void setUp() {
    model = new CalendarModel();
    converter = new CalendarTimezoneConverter();
  }

  /**
   * Clears references after each test run.
   */
  @After
  public void tearDown() {
    model = null;
    converter = null;
  }

  @Test
  public void testConvertAllEventsEmptyCalendar() {
    ZoneId oldTz = ZoneId.of("America/New_York");
    ZoneId newTz = ZoneId.of("America/Los_Angeles");

    converter.convertAllEvents(model, oldTz, newTz);

    List<Ievent> events = model.getByRange(
        LocalDateTime.of(1900, 1, 1, 0, 0),
        LocalDateTime.of(2100, 12, 31, 23, 59)
    );
    assertEquals("Should have no events", 0, events.size());
  }

  @Test
  public void testConvertSingleEventEstToPst() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 14, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 15, 0);
    model.createEvent("Meeting", start, end);

    ZoneId oldTz = ZoneId.of("America/New_York");
    ZoneId newTz = ZoneId.of("America/Los_Angeles");

    converter.convertAllEvents(model, oldTz, newTz);

    List<Ievent> events = model.getByDate(LocalDate.of(2025, 1, 15));
    assertEquals("Should have one event", 1, events.size());

    Event converted = (Event) events.get(0);
    assertEquals("Start time should be 11:00 AM PST",
        LocalDateTime.of(2025, 1, 15, 11, 0), converted.getStart());
    assertEquals("End time should be 12:00 PM PST",
        LocalDateTime.of(2025, 1, 15, 12, 0), converted.getEnd());
  }

  @Test
  public void testConvertSingleEventPstToEst() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    model.createEvent("Meeting", start, end);

    ZoneId oldTz = ZoneId.of("America/Los_Angeles");
    ZoneId newTz = ZoneId.of("America/New_York");

    converter.convertAllEvents(model, oldTz, newTz);

    List<Ievent> events = model.getByDate(LocalDate.of(2025, 1, 15));
    assertEquals("Should have one event", 1, events.size());

    Event converted = (Event) events.get(0);
    assertEquals("Start time should be 1:00 PM EST",
        LocalDateTime.of(2025, 1, 15, 13, 0), converted.getStart());
    assertEquals("End time should be 2:00 PM EST",
        LocalDateTime.of(2025, 1, 15, 14, 0), converted.getEnd());
  }

  @Test
  public void testConvertMultipleEvents() {
    LocalDateTime start1 = LocalDateTime.of(2025, 1, 15, 9, 0);
    LocalDateTime end1 = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime start2 = LocalDateTime.of(2025, 1, 15, 14, 0);
    LocalDateTime end2 = LocalDateTime.of(2025, 1, 15, 15, 0);

    model.createEvent("Morning Meeting", start1, end1);
    model.createEvent("Afternoon Meeting", start2, end2);

    ZoneId oldTz = ZoneId.of("America/New_York");
    ZoneId newTz = ZoneId.of("America/Chicago");

    converter.convertAllEvents(model, oldTz, newTz);

    List<Ievent> events = model.getByDate(LocalDate.of(2025, 1, 15));
    assertEquals("Should have two events", 2, events.size());

    Event morning = null;
    Event afternoon = null;

    for (Ievent ievent : events) {
      Event event = (Event) ievent;
      if (event.getSubject().equals("Morning Meeting")) {
        morning = event;
      } else if (event.getSubject().equals("Afternoon Meeting")) {
        afternoon = event;
      }
    }

    assertNotNull("Morning meeting should exist", morning);
    assertEquals("Morning meeting should be at 8:00 AM CST",
        LocalDateTime.of(2025, 1, 15, 8, 0), morning.getStart());

    assertNotNull("Afternoon meeting should exist", afternoon);
    assertEquals("Afternoon meeting should be at 1:00 PM CST",
        LocalDateTime.of(2025, 1, 15, 13, 0), afternoon.getStart());
  }

  @Test
  public void testConvertEventSeries() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 13, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 13, 11, 0);
    model.createEventSeries("Weekly Standup", start, end, "MWF", 3, null);

    ZoneId oldTz = ZoneId.of("UTC");
    ZoneId newTz = ZoneId.of("America/New_York");

    converter.convertAllEvents(model, oldTz, newTz);

    List<Ievent> allEvents = model.getByRange(
        LocalDateTime.of(2025, 1, 1, 0, 0),
        LocalDateTime.of(2025, 1, 31, 23, 59)
    );

    assertEquals("Should have 3 events in series", 3, allEvents.size());

    for (Ievent ievent : allEvents) {
      Event event = (Event) ievent;
      assertEquals("All events should start at 5:00 AM",
          5, event.getStart().getHour());
      assertEquals("All events should end at 6:00 AM",
          6, event.getEnd().getHour());
    }
  }

  @Test
  public void testConvertEventAcrossInternationalDateLine() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 23, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 16, 0, 0);
    model.createEvent("Late Night Meeting", start, end);

    ZoneId oldTz = ZoneId.of("America/New_York");
    ZoneId newTz = ZoneId.of("Asia/Tokyo");

    converter.convertAllEvents(model, oldTz, newTz);

    List<Ievent> events = model.getByDate(LocalDate.of(2025, 1, 16));
    assertEquals("Should have one event", 1, events.size());

    Event converted = (Event) events.get(0);
    assertEquals("Should be on Jan 16",
        LocalDate.of(2025, 1, 16), converted.getStart().toLocalDate());
    assertEquals("Should be at 1:00 PM JST",
        13, converted.getStart().getHour());
  }

  @Test
  public void testConvertAllDayEvent() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 8, 0);
    model.createAllDayEvent("All Day Event", start);

    ZoneId oldTz = ZoneId.of("America/New_York");
    ZoneId newTz = ZoneId.of("America/Los_Angeles");

    converter.convertAllEvents(model, oldTz, newTz);

    List<Ievent> events = model.getByDate(LocalDate.of(2025, 1, 15));
    assertEquals("Should have one event", 1, events.size());

    Event converted = (Event) events.get(0);
    assertEquals("Start should be 5:00 AM PST",
        LocalDateTime.of(2025, 1, 15, 5, 0), converted.getStart());
    assertEquals("End should be 2:00 PM PST",
        LocalDateTime.of(2025, 1, 15, 14, 0), converted.getEnd());
  }

  @Test
  public void testConvertSameTimezone() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    model.createEvent("Meeting", start, end);

    ZoneId sameTz = ZoneId.of("America/New_York");

    converter.convertAllEvents(model, sameTz, sameTz);

    List<Ievent> events = model.getByDate(LocalDate.of(2025, 1, 15));
    assertEquals("Should have one event", 1, events.size());

    Event event = (Event) events.get(0);
    assertEquals("Start time should remain unchanged", start, event.getStart());
    assertEquals("End time should remain unchanged", end, event.getEnd());
  }

  @Test
  public void testConvertEuropeanTimezones() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 15, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 16, 0);
    model.createEvent("Meeting", start, end);

    ZoneId oldTz = ZoneId.of("Europe/London");
    ZoneId newTz = ZoneId.of("Europe/Paris");

    converter.convertAllEvents(model, oldTz, newTz);

    List<Ievent> events = model.getByDate(LocalDate.of(2025, 1, 15));
    assertEquals("Should have one event", 1, events.size());

    Event converted = (Event) events.get(0);
    assertEquals("Start time should be 4:00 PM Paris",
        LocalDateTime.of(2025, 1, 15, 16, 0), converted.getStart());
    assertEquals("End time should be 5:00 PM Paris",
        LocalDateTime.of(2025, 1, 15, 17, 0), converted.getEnd());
  }

  @Test
  public void testConvertEventWithOptionalFields() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    model.createEvent("Meeting", start, end);
    model.editEvent("Meeting", start, "description", "Important meeting");
    model.editEvent("Meeting", start, "location", "Conference Room A");
    model.editEvent("Meeting", start, "status", "private");

    ZoneId oldTz = ZoneId.of("America/New_York");
    ZoneId newTz = ZoneId.of("America/Los_Angeles");

    converter.convertAllEvents(model, oldTz, newTz);

    List<Ievent> events = model.getByDate(LocalDate.of(2025, 1, 15));
    Event converted = (Event) events.get(0);

    assertEquals("Description should be preserved",
        "Important meeting", converted.getDescription());
    assertEquals("Location should be preserved",
        "Conference Room A", converted.getLocation());
    assertEquals("Status should be preserved",
        "private", converted.getStatus());
  }
}