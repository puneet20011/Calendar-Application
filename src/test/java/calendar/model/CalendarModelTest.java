package calendar.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for core CalendarModel functionality.
 * Validates event creation, editing, retrieval, and busy status.
 */
public class CalendarModelTest {

  private CalendarModel model;

  /**
   * Creates a fresh calendar model before each test.
   */
  @Before
  public void setUp() {
    model = new CalendarModel();
  }

  /**
   * Clears references after each test run.
   */
  @After
  public void tearDown() {
    model = null;
  }

  @Test
  public void testEditEventsDoesNotCallSeriesValidationForStandaloneEvent() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);

    model.createEvent("Standalone", start, end);

    model.editEvents("Standalone", start, "end", "2025-01-16T10:00");

    Event edited = (Event) model.getByDate(LocalDate.of(2025, 1, 15)).get(0);
    assertEquals("End date should change", LocalDate.of(2025, 1, 16),
        edited.getEnd().toLocalDate());
  }

  @Test
  public void testEditSeriesEventStartRemovesFromSeries() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);

    model.createEventSeries("Weekly Meeting", start, end, "W", 3, null);

    Event originalEvent = (Event) model.getByDate(LocalDate.of(2025, 1, 15)).get(0);
    EventSeries originalSeries = originalEvent.getSeries();
    assertNotNull("Event should be in a series", originalSeries);
    assertEquals("Series should have 3 occurrences", 3,
        originalSeries.getAllOccurrences().size());

    model.editEvent("Weekly Meeting", start, "start", "2025-01-15T14:00");

    Event editedEvent = (Event) model.getByDate(LocalDate.of(2025, 1, 15)).get(0);
    assertNull("Edited event should no longer be in series", editedEvent.getSeries());

    assertEquals("Series should now have 2 occurrences", 2,
        originalSeries.getAllOccurrences().size());
  }

  @Test
  public void testEditSeriesEventNonStartPropertyKeepsInSeries() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);

    model.createEventSeries("Weekly Meeting", start, end, "W", 3, null);

    Event originalEvent = (Event) model.getByDate(LocalDate.of(2025, 1, 15)).get(0);
    EventSeries originalSeries = originalEvent.getSeries();
    assertNotNull("Event should be in a series", originalSeries);

    model.editEvent("Weekly Meeting", start, "description", "Updated description");

    Event editedEvent = (Event) model.getByDate(LocalDate.of(2025, 1, 15)).get(0);
    assertNotNull("Edited event should still be in series", editedEvent.getSeries());
    assertEquals("Should be in same series", originalSeries, editedEvent.getSeries());
    assertEquals("Description should be updated", "Updated description",
        editedEvent.getDescription());

    assertEquals("Series should still have 3 occurrences", 3,
        originalSeries.getAllOccurrences().size());
  }

  @Test
  public void testEditSeriesEventLocationKeepsInSeries() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);

    model.createEventSeries("Meeting", start, end, "W", 2, null);

    Event originalEvent = (Event) model.getByDate(LocalDate.of(2025, 1, 15)).get(0);
    EventSeries originalSeries = originalEvent.getSeries();

    model.editEvent("Meeting", start, "location", "Room 101");

    Event editedEvent = (Event) model.getByDate(LocalDate.of(2025, 1, 15)).get(0);
    assertNotNull("Should still be in series", editedEvent.getSeries());
    assertEquals("Should be same series", originalSeries, editedEvent.getSeries());
    assertEquals("Location should be updated", "Room 101", editedEvent.getLocation());
    assertEquals("Series should still have 2 occurrences", 2,
        originalSeries.getAllOccurrences().size());
  }

  @Test
  public void testEditSeriesEventStatusKeepsInSeries() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);

    model.createEventSeries("Meeting", start, end, "W", 2, null);

    Event originalEvent = (Event) model.getByDate(LocalDate.of(2025, 1, 15)).get(0);
    EventSeries originalSeries = originalEvent.getSeries();

    model.editEvent("Meeting", start, "status", "private");

    Event editedEvent = (Event) model.getByDate(LocalDate.of(2025, 1, 15)).get(0);
    assertNotNull("Should still be in series", editedEvent.getSeries());
    assertEquals("Should be same series", originalSeries, editedEvent.getSeries());
    assertEquals("Status should be updated", "private", editedEvent.getStatus());
  }

  @Test
  public void testEditSeriesEventSubjectKeepsInSeries() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);

    model.createEventSeries("Old Subject", start, end, "W", 2, null);

    Event originalEvent = (Event) model.getByDate(LocalDate.of(2025, 1, 15)).get(0);
    EventSeries originalSeries = originalEvent.getSeries();

    model.editEvent("Old Subject", start, "subject", "New Subject");

    Event editedEvent = (Event) model.getByDate(LocalDate.of(2025, 1, 15)).get(0);
    assertNotNull("Should still be in series", editedEvent.getSeries());
    assertEquals("Should be same series", originalSeries, editedEvent.getSeries());
    assertEquals("Subject should be updated", "New Subject", editedEvent.getSubject());
  }

  @Test
  public void testEditSeriesEventEndTimeKeepsInSeries() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);

    model.createEventSeries("Meeting", start, end, "W", 2, null);

    Event originalEvent = (Event) model.getByDate(LocalDate.of(2025, 1, 15)).get(0);
    EventSeries originalSeries = originalEvent.getSeries();

    model.editEvent("Meeting", start, "end", "2025-01-15T12:00");

    Event editedEvent = (Event) model.getByDate(LocalDate.of(2025, 1, 15)).get(0);
    assertNotNull("Should still be in series", editedEvent.getSeries());
    assertEquals("Should be same series", originalSeries, editedEvent.getSeries());
    assertEquals("End time should be updated", LocalTime.of(12, 0),
        editedEvent.getEnd().toLocalTime());
  }

  @Test
  public void testEditSeriesEventMultipleNonStartProperties() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);

    model.createEventSeries("Meeting", start, end, "W", 2, null);

    Event originalEvent = (Event) model.getByDate(LocalDate.of(2025, 1, 15)).get(0);
    EventSeries originalSeries = originalEvent.getSeries();
    assertNotNull("Original event should be in series", originalSeries);

    model.editEvent("Meeting", start, "description", "Important");
    Event after1 = (Event) model.getByDate(LocalDate.of(2025, 1, 15)).get(0);
    assertNotNull("Should still be in series after description edit",
        after1.getSeries());

    model.editEvent("Meeting", start, "location", "Room 202");
    Event after2 = (Event) model.getByDate(LocalDate.of(2025, 1, 15)).get(0);
    assertNotNull("Should still be in series after location edit",
        after2.getSeries());

    model.editEvent("Meeting", start, "status", "private");
    Event after3 = (Event) model.getByDate(LocalDate.of(2025, 1, 15)).get(0);
    assertNotNull("Should still be in series after status edit",
        after3.getSeries());

    assertEquals("All edits should be in same series", originalSeries,
        after3.getSeries());
  }

  @Test
  public void testEditSeriesEventStartAfterOtherEdits() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);

    model.createEventSeries("Meeting", start, end, "W", 3, null);

    EventSeries originalSeries = ((Event) model.getByDate(LocalDate.of(2025, 1, 15))
        .get(0)).getSeries();
    assertEquals("Should start with 3 occurrences", 3,
        originalSeries.getAllOccurrences().size());

    model.editEvent("Meeting", start, "location", "Room 101");
    Event after1 = (Event) model.getByDate(LocalDate.of(2025, 1, 15)).get(0);
    assertNotNull("Should be in series", after1.getSeries());

    model.editEvent("Meeting", start, "start", "2025-01-15T14:00");
    Event after2 = (Event) model.getByDate(LocalDate.of(2025, 1, 15)).get(0);
    assertNull("Should no longer be in series", after2.getSeries());

    assertEquals("Series should now have 2 occurrences", 2,
        originalSeries.getAllOccurrences().size());
  }
}