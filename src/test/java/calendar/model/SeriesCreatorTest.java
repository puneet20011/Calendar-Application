package calendar.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for SeriesCreator class functionality.
 * Tests event series creation with recurrence patterns and weekday parsing.
 */
public class SeriesCreatorTest {

  private EventCalendar calendar;
  private SystemValidator validator;
  private SeriesCreator seriesCreator;

  /**
   * Sets up the test fixture.
   */
  @Before
  public void setUp() {
    calendar = new EventCalendar();
    validator = new SystemValidator(calendar);
    seriesCreator = new SeriesCreator(calendar, validator);
  }

  /**
   * Tears down the test fixture.
   */
  @After
  public void tearDown() {
    calendar = null;
    validator = null;
    seriesCreator = null;
  }

  @Test
  public void testCreateSeriesWithOccurrences() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);

    seriesCreator.createSeries("Weekly Meeting", start, end, "W", 3, null);

    List<Ievent> events = calendar.getEventsByDate(LocalDate.of(2025, 1, 15));
    assertEquals("Should create first occurrence", 1, events.size());
    assertEquals("Subject should match", "Weekly Meeting", events.get(0).getSubject());
    assertNotNull("Event should have series", ((Event) events.get(0)).getSeries());

    EventSeries series = ((Event) events.get(0)).getSeries();
    assertEquals("Series should have 3 occurrences", 3, series.getAllOccurrences().size());
  }

  @Test
  public void testCreateSeriesWithUntilDate() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    LocalDateTime until = LocalDateTime.of(2025, 1, 20, 23, 59);

    seriesCreator.createSeries("Daily Standup", start, end, "MTWRF", -1, until);

    List<Ievent> allEvents = calendar.getEventsByRange(
        LocalDate.of(2025, 1, 15).atStartOfDay(),
        LocalDate.of(2025, 1, 25).atStartOfDay()
    );
    assertEquals("Should create 4 events (15th, 16th, 17th, 20th)", 4, allEvents.size());
  }

  @Test
  public void testCreateSeriesMultipleWeekdays() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 13, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 13, 11, 0);

    seriesCreator.createSeries("MWF Meeting", start, end, "MWF", 3, null);

    List<Ievent> mondayEvents = calendar.getEventsByDate(LocalDate.of(2025, 1, 13));
    List<Ievent> wednesdayEvents = calendar.getEventsByDate(LocalDate.of(2025, 1, 15));
    List<Ievent> fridayEvents = calendar.getEventsByDate(LocalDate.of(2025, 1, 17));

    assertEquals("Should have event on Monday", 1, mondayEvents.size());
    assertEquals("Should have event on Wednesday", 1, wednesdayEvents.size());
    assertEquals("Should have event on Friday", 1, fridayEvents.size());

    EventSeries series = ((Event) mondayEvents.get(0)).getSeries();
    assertNotNull("Monday event should have series", series);
    assertEquals("Wednesday event should be in same series", series,
        ((Event) wednesdayEvents.get(0)).getSeries());
    assertEquals("Friday event should be in same series", series,
        ((Event) fridayEvents.get(0)).getSeries());
  }

  @Test
  public void testCreateSeriesAllWeekdays() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 13, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 13, 11, 0);

    seriesCreator.createSeries("Daily Meeting", start, end, "MTWRFSU", 7, null);

    for (int i = 0; i < 7; i++) {
      LocalDate date = LocalDate.of(2025, 1, 13).plusDays(i);
      List<Ievent> events = calendar.getEventsByDate(date);
      assertEquals("Should have event on " + date, 1, events.size());
      assertEquals("Subject should match", "Daily Meeting", events.get(0).getSubject());
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateSeriesStartAfterEnd() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 11, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 10, 0);

    seriesCreator.createSeries("Invalid Series", start, end, "W", 1, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateSeriesTooLong() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);

    seriesCreator.createSeries("Long Series", start, end, "MTWRFSU", 500, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateSeriesWithDuplicateEvent() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);

    Event event = Event.builder("Test", start, end).build();
    calendar.addEvent(event);

    seriesCreator.createSeries("Test", start, end, "W", 1, null);
  }

  @Test
  public void testCreateSeriesWithEmptyWeekdays() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);

    try {
      seriesCreator.createSeries("Test Series", start, end, "", 1, null);
      fail("Should throw exception for empty weekdays");
    } catch (IllegalArgumentException e) {
      // Expected - empty weekdays should not be allowed
    }
  }

  @Test
  public void testCreateSeriesWithInvalidWeekdays() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);

    try {
      seriesCreator.createSeries("Test Series", start, end, "INVALID", 1, null);
      fail("Should throw exception for invalid weekdays");
    } catch (IllegalArgumentException e) {
      // Expected - invalid weekdays should not be allowed
    }
  }

  @Test
  public void testCreateSeriesZeroOccurrences() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);

    try {
      seriesCreator.createSeries("Zero Series", start, end, "W", 0, null);
      List<Ievent> events = calendar.getEventsByDate(LocalDate.of(2025, 1, 15));
      assertEquals("Should create no events for zero occurrences", 0, events.size());
    } catch (IllegalArgumentException e) {
      assertTrue("Zero occurrences should either create no events or throw exception", true);
    }
  }

  @Test
  public void testCreateSeriesWithMixedValidInvalidWeekdays() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 13, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 13, 11, 0);

    seriesCreator.createSeries("Mixed Series", start, end, "MXF", 3, null);

    List<Ievent> mondayEvents = calendar.getEventsByDate(LocalDate.of(2025, 1, 13));
    List<Ievent> fridayEvents = calendar.getEventsByDate(LocalDate.of(2025, 1, 17));

    assertEquals("Should have event on Monday", 1, mondayEvents.size());
    assertEquals("Should have event on Friday", 1, fridayEvents.size());
  }
}