package calendar.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for EventUpdater class functionality.
 * Tests event editing, updating, and series manipulation operations.
 */
public class EventUpdaterTest {

  private CalendarModel model;

  /**
   * Sets up the test fixture.
   */
  @Before
  public void setUp() {
    model = new CalendarModel();
  }

  /**
   * Tears down the test fixture.
   */
  @After
  public void tearDown() {
    model = null;
  }

  @Test
  public void testEditEventSingleCausesConflict() {
    LocalDateTime start1 = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end1 = LocalDateTime.of(2025, 1, 15, 11, 0);
    LocalDateTime start2 = LocalDateTime.of(2025, 1, 15, 12, 0);
    LocalDateTime end2 = LocalDateTime.of(2025, 1, 15, 13, 0);

    model.createEvent("Event 1", start1, end1);
    model.createEvent("Event 2", start2, end2);

    model.editEvent("Event 2", start2, "start", "2025-01-15T10:00");
    model.editEvent("Event 2", LocalDateTime.of(2025, 1, 15, 10, 0), "end", "2025-01-15T11:00");

    List<Ievent> events = model.getByDate(LocalDate.of(2025, 1, 15));
    assertEquals(2, events.size());

    Event event1 = null;
    Event event2 = null;

    for (Ievent ievent : events) {
      Event event = (Event) ievent;
      if (event.getSubject().equals("Event 1")) {
        event1 = event;
      } else if (event.getSubject().equals("Event 2")) {
        event2 = event;
      }
    }

    assertNotNull(event1);
    assertNotNull(event2);
    assertEquals(LocalDateTime.of(2025, 1, 15, 10, 0), event1.getStart());
    assertEquals(LocalDateTime.of(2025, 1, 15, 11, 0), event1.getEnd());
    assertEquals(LocalDateTime.of(2025, 1, 15, 10, 0), event2.getStart());
    assertEquals(LocalDateTime.of(2025, 1, 15, 11, 0), event2.getEnd());
  }

  @Test
  public void testEditEventStartAfterOriginalEndPreservesDuration() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    model.createEvent("Event", start, end);
    model.editEvent("Event", start, "start", "2025-01-15T14:00");

    Event event = (Event) model.getByDate(LocalDate.of(2025, 1, 15)).get(0);
    assertEquals(LocalTime.of(14, 0), event.getStart().toLocalTime());
    assertEquals("End time should preserve duration", LocalTime.of(15, 0),
        event.getEnd().toLocalTime());
  }

  @Test
  public void testChangePropEditsStatus() {
    LocalDateTime start = LocalDateTime.of(2026, 4, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2026, 4, 1, 11, 0);
    model.createEvent("Status Test Event", start, end);

    model.editEvent("Status Test Event", start, "status", "private");

    Event event = (Event) model.getByDate(start.toLocalDate()).get(0);
    assertEquals("Status should be updated to 'private'",
        "private", event.getStatus());
  }

  @Test
  public void testEditSeriesNonKeyPropertyDoesNotCauseConflict() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    model.createEventSeries("Safe Edit Series", start, end, "W", 2, null);

    model.editSeries("Safe Edit Series", start, "description", "New Description for all");

    List<Ievent> events = model.getByDate(start.toLocalDate());
    assertEquals("Should still have 1 event on this date", 1, events.size());
    assertEquals("New Description for all", events.get(0).getDescription());
  }

  @Test
  public void testEditEventEndTimeInSeriesUpdatesTimeOnly() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    model.createEventSeries("Weekly", start, end, "W", 3, null);

    model.editEvent("Weekly", start, "end", "2025-01-15T11:30");

    Event editedEvent = (Event) model.getByDate(LocalDate.of(2025, 1, 15)).get(0);

    assertEquals("Date should remain Jan 15",
        LocalDate.of(2025, 1, 15), editedEvent.getEnd().toLocalDate());
    assertEquals("Time should update to 11:30",
        LocalTime.of(11, 30), editedEvent.getEnd().toLocalTime());
  }

  @Test
  public void testEditEventEndTimeInSeriesIgnoresDatePortion() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    model.createEventSeries("Weekly", start, end, "W", 3, null);

    model.editEvent("Weekly", start, "end", "2025-01-20T12:00");

    Event editedEvent = (Event) model.getByDate(LocalDate.of(2025, 1, 15)).get(0);

    assertEquals("Date should stay Jan 15, not Jan 20",
        LocalDate.of(2025, 1, 15), editedEvent.getEnd().toLocalDate());
    assertEquals("Time should be 12:00 from input",
        LocalTime.of(12, 0), editedEvent.getEnd().toLocalTime());
  }

  @Test
  public void testEditEventEndTimeStandaloneUpdatesFullDatetime() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    model.createEvent("Single Event", start, end);

    model.editEvent("Single Event", start, "end", "2025-01-20T14:00");

    Event editedEvent = (Event) model.getByDate(LocalDate.of(2025, 1, 15)).get(0);

    assertEquals("Date should update to Jan 20",
        LocalDate.of(2025, 1, 20), editedEvent.getEnd().toLocalDate());
    assertEquals("Time should update to 14:00",
        LocalTime.of(14, 0), editedEvent.getEnd().toLocalTime());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditEventEndTimeBeforeStartThrowsException() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    model.createEvent("Event", start, end);

    model.editEvent("Event", start, "end", "2025-01-15T09:00");
  }
}