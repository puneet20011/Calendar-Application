package calendar.model;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for EventValidator class functionality.
 * Tests validation logic for events, series, and editing operations.
 */
public class SystemValidatorTest {

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

  @Test(expected = IllegalArgumentException.class)
  public void testCreateEventStartAfterEnd() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 11, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 10, 0);

    model.createEvent("Test Event", start, end);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateEventStartEqualsEnd() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 10, 0);

    model.createEvent("Test Event", start, end);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateEventDuplicateEvent() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);

    model.createEvent("Test Event", start, end);
    model.createEvent("Test Event", start, end);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateEventSeriesStartAfterEnd() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 11, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 10, 0);

    model.createEventSeries("Invalid Series", start, end, "MWF", 3, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateEventSeriesTooLong() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);

    model.createEventSeries("Long Series", start, end, "MTWRFSU", 500, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateEventSeriesWithDuplicateEventInSeries() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);

    model.createEvent("Test", start, end);

    model.createEventSeries("Test", start, end, "W", 1, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateEventSeriesStartToLocalTimeEqualsEndToLocalTime() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 16, 10, 0);

    model.createEventSeries("Invalid Time Series", start, end, "MWF", 3, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditEventEventNotFound() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);

    model.editEvent("Non-existent Event", start, "subject", "New Subject");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditEventInvalidProperty() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);

    model.createEvent("Event", start, end);
    model.editEvent("Event", start, "invalid", "value");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditEventsWithNonExistentEvent() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    model.editEvents("NonExistent", start, "subject", "NewSubject");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditSeriesWithNonExistentEvent() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    model.editSeries("NonExistent", start, "subject", "NewSubject");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditEventEndBeforeStartThrowsException() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    model.createEvent("Event", start, end);

    model.editEvent("Event", start, "end", "2025-01-15T09:00");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditEventEndEqualsStartThrowsException() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    model.createEvent("Event", start, end);

    model.editEvent("Event", start, "end", "2025-01-15T10:00");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditEventConflictOnSubjectChange() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    model.createEvent("Subject A", start, end);

    model.createEvent("Subject B", start, end);

    model.editEvent("Subject A", start, "subject", "Subject B");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateEventSeriesWithEmptyWeekdays() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    model.createEventSeries("Test Series", start, end, "", 5, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateEventSeriesWithInvalidWeekdays() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    model.createEventSeries("Test Series", start, end, "INVALID", 5, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditEventsCausesConflict() {
    LocalDateTime start1 = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end1 = LocalDateTime.of(2025, 1, 15, 11, 0);
    LocalDateTime start2 = LocalDateTime.of(2025, 1, 17, 10, 0);
    LocalDateTime end2 = LocalDateTime.of(2025, 1, 17, 11, 0);

    model.createEvent("Existing Conflict", start2, end2);

    model.createEventSeries("Series Event", start1, end1, "WF", 2, null);

    model.editEvents("Series Event", start1, "subject", "Existing Conflict");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditSeriesCausesConflict() {
    LocalDateTime start1 = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end1 = LocalDateTime.of(2025, 1, 15, 11, 0);
    LocalDateTime start2 = LocalDateTime.of(2025, 1, 17, 10, 0);
    LocalDateTime end2 = LocalDateTime.of(2025, 1, 17, 11, 0);

    model.createEvent("Existing Conflict", start2, end2);

    model.createEventSeries("Series Event", start1, end1, "WF", 2, null);

    model.editSeries("Series Event", start1, "subject", "Existing Conflict");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditSeriesStartChangesDateThrowsException() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    model.createEventSeries("Weekly", start, end, "W", 3, null);

    model.editSeries("Weekly", start, "start", "2025-01-20T10:00");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditSeriesStartCreatesMultiDayEventThrowsException() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    model.createEventSeries("Weekly", start, end, "W", 3, null);

    model.editSeries("Weekly", start, "start", "2025-01-15T23:30");
  }

  @Test
  public void testEditSeriesStartValidTimeChangeSucceeds() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    model.createEventSeries("Weekly", start, end, "W", 3, null);

    model.editSeries("Weekly", start, "start", "2025-01-15T14:00");

    Event edited = (Event) model.getByDate(LocalDate.of(2025, 1, 15)).get(0);
    assertEquals(LocalTime.of(14, 0), edited.getStart().toLocalTime());
  }
}