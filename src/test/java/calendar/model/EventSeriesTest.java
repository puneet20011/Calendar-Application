package calendar.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for EventSeries class functionality.
 * Tests series creation and occurrence.
 */
public class EventSeriesTest {

  private EventSeries series;
  private LocalTime startTime;
  private LocalTime endTime;
  private Set<DayOfWeek> repeatDays;

  /**
   * Sets up the test fixture.
   */
  @Before
  public void setUp() {
    startTime = LocalTime.of(10, 0);
    endTime = LocalTime.of(11, 0);
    repeatDays = EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY);
    series = new EventSeries("Weekly Meeting", startTime, endTime, repeatDays);
  }

  @Test
  public void testConstructor() {
    assertNotNull("Series should not be null", series);
    assertEquals("Base subject should match", "Weekly Meeting",
        getBaseSubject(series));
    assertEquals("Start time should match", startTime, getStartTime(series));
    assertEquals("End time should match", endTime, getEndTime(series));
    assertEquals("Repeat days should match", repeatDays, series.getRepeatDays());
    assertTrue("Occurrences should be empty initially",
        series.getAllOccurrences().isEmpty());
  }

  @Test
  public void testAddOccurrence() {
    LocalDateTime eventStart = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime eventEnd = LocalDateTime.of(2025, 1, 15, 11, 0);
    Event event = new Event("Weekly Meeting", eventStart, eventEnd, series,
        "Description", "Location", "status");

    series.addOccurrence(event);

    List<Event> occurrences = series.getAllOccurrences();
    assertEquals("Should have one occurrence", 1, occurrences.size());
    assertTrue("Should contain the added event", occurrences.contains(event));
  }

  @Test
  public void testAddMultipleOccurrences() {
    LocalDateTime event1Start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime event1End = LocalDateTime.of(2025, 1, 15, 11, 0);
    Event event1 = new Event("Weekly Meeting", event1Start, event1End, series,
        "Description", "Location", "status");

    LocalDateTime event2Start = LocalDateTime.of(2025, 1, 17, 10, 0);
    LocalDateTime event2End = LocalDateTime.of(2025, 1, 17, 11, 0);
    Event event2 = new Event("Weekly Meeting", event2Start, event2End, series,
        "Description", "Location", "status");

    series.addOccurrence(event1);
    series.addOccurrence(event2);

    List<Event> occurrences = series.getAllOccurrences();
    assertEquals("Should have two occurrences", 2, occurrences.size());
    assertTrue("Should contain event1", occurrences.contains(event1));
    assertTrue("Should contain event2", occurrences.contains(event2));
  }

  @Test
  public void testRemoveOccurrence() {
    LocalDateTime eventStart = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime eventEnd = LocalDateTime.of(2025, 1, 15, 11, 0);
    Event event = new Event("Weekly Meeting", eventStart, eventEnd, series,
        "Description", "Location", "status");

    series.addOccurrence(event);
    assertEquals("Should have one occurrence", 1, series.getAllOccurrences().size());

    series.removeOccurrence(event);
    assertTrue("Occurrences should be empty after removal",
        series.getAllOccurrences().isEmpty());
  }

  @Test
  public void testRemoveNonExistentOccurrence() {
    LocalDateTime eventStart = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime eventEnd = LocalDateTime.of(2025, 1, 15, 11, 0);
    Event event = new Event("Weekly Meeting", eventStart, eventEnd, series,
        "Description", "Location", "status");

    series.removeOccurrence(event);
    assertTrue("Occurrences should still be empty",
        series.getAllOccurrences().isEmpty());
  }

  @Test
  public void testGetAllOccurrences() {
    LocalDateTime event1Start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime event1End = LocalDateTime.of(2025, 1, 15, 11, 0);
    Event event1 = new Event("Weekly Meeting", event1Start, event1End, series,
        "Description", "Location", "status");

    LocalDateTime event2Start = LocalDateTime.of(2025, 1, 17, 10, 0);
    LocalDateTime event2End = LocalDateTime.of(2025, 1, 17, 11, 0);
    Event event2 = new Event("Weekly Meeting", event2Start, event2End, series,
        "Description", "Location", "status");

    series.addOccurrence(event1);
    series.addOccurrence(event2);

    List<Event> occurrences = series.getAllOccurrences();
    assertNotNull("Occurrences should not be null", occurrences);
    assertEquals("Should have two occurrences", 2, occurrences.size());

    occurrences.clear();
    assertEquals("Original occurrences should not be affected", 2,
        series.getAllOccurrences().size());
  }

  @Test
  public void testGetOccurrencesFromDateExactMatch() {
    LocalDateTime eventStart = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime eventEnd = LocalDateTime.of(2025, 1, 15, 11, 0);
    Event event = new Event("Weekly Meeting", eventStart, eventEnd, series,
        "Description", "Location", "status");

    series.addOccurrence(event);

    LocalDate fromDate = LocalDate.of(2025, 1, 15);
    List<Event> filteredOccurrences = series.getOccurrencesFromDate(fromDate);

    assertEquals("Should have one occurrence from exact date", 1,
        filteredOccurrences.size());
    assertTrue("Should contain the event", filteredOccurrences.contains(event));
  }

  @Test
  public void testGetOccurrencesFromDateNoMatches() {
    LocalDateTime eventStart = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime eventEnd = LocalDateTime.of(2025, 1, 15, 11, 0);
    Event event = new Event("Weekly Meeting", eventStart, eventEnd, series,
        "Description", "Location", "status");

    series.addOccurrence(event);

    LocalDate fromDate = LocalDate.of(2025, 1, 20);
    List<Event> filteredOccurrences = series.getOccurrencesFromDate(fromDate);

    assertTrue("Should have no occurrences from future date",
        filteredOccurrences.isEmpty());
  }

  @Test
  public void testGetRepeatDays() {
    Set<DayOfWeek> days = series.getRepeatDays();
    assertNotNull("Repeat days should not be null", days);
    assertEquals("Should have three repeat days", 3, days.size());
    assertTrue("Should contain Monday", days.contains(DayOfWeek.MONDAY));
    assertTrue("Should contain Wednesday", days.contains(DayOfWeek.WEDNESDAY));
    assertTrue("Should contain Friday", days.contains(DayOfWeek.FRIDAY));
  }

  @Test
  public void testEmptyRepeatDays() {
    EventSeries emptySeries = new EventSeries("Empty", startTime, endTime,
        EnumSet.noneOf(DayOfWeek.class));

    Set<DayOfWeek> days = emptySeries.getRepeatDays();
    assertNotNull("Repeat days should not be null", days);
    assertTrue("Should have no repeat days", days.isEmpty());
  }

  @Test
  public void testSingleRepeatDay() {
    EventSeries singleDaySeries = new EventSeries("Daily", startTime, endTime,
        EnumSet.of(DayOfWeek.MONDAY));

    Set<DayOfWeek> days = singleDaySeries.getRepeatDays();
    assertEquals("Should have one repeat day", 1, days.size());
    assertTrue("Should contain Monday", days.contains(DayOfWeek.MONDAY));
  }

  @Test
  public void testAllWeekdays() {
    EventSeries weekdaysSeries = new EventSeries("Weekdays", startTime, endTime,
        EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY));

    Set<DayOfWeek> days = weekdaysSeries.getRepeatDays();
    assertEquals("Should have five repeat days", 5, days.size());
    assertTrue("Should contain all weekdays",
        days.containsAll(EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY)));
  }

  private String getBaseSubject(EventSeries series) {
    try {
      java.lang.reflect.Field field = EventSeries.class.getDeclaredField("baseSubject");
      field.setAccessible(true);
      return (String) field.get(series);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private LocalTime getStartTime(EventSeries series) {
    try {
      java.lang.reflect.Field field = EventSeries.class.getDeclaredField("startTime");
      field.setAccessible(true);
      return (LocalTime) field.get(series);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private LocalTime getEndTime(EventSeries series) {
    try {
      java.lang.reflect.Field field = EventSeries.class.getDeclaredField("endTime");
      field.setAccessible(true);
      return (LocalTime) field.get(series);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBuilderThrowsIfSubjectIsNull() {
    EventSeries.builder()
        .startTime(startTime)
        .endTime(endTime)
        .repeatDays(repeatDays)
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBuilderThrowsIfSubjectIsEmpty() {
    EventSeries.builder()
        .subject("")
        .startTime(startTime)
        .endTime(endTime)
        .repeatDays(repeatDays)
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBuilderThrowsIfStartTimeIsNull() {
    EventSeries.builder()
        .subject("Test")
        .endTime(endTime)
        .repeatDays(repeatDays)
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBuilderThrowsIfEndTimeIsNull() {
    EventSeries.builder()
        .subject("Test")
        .startTime(startTime)
        .repeatDays(repeatDays)
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBuilderThrowsIfRepeatDaysIsNull() {
    EventSeries.builder()
        .subject("Test")
        .startTime(startTime)
        .endTime(endTime)
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBuilderThrowsIfRepeatDaysIsEmpty() {
    EventSeries.builder()
        .subject("Test")
        .startTime(startTime)
        .endTime(endTime)
        .repeatDays(java.util.EnumSet.noneOf(java.time.DayOfWeek.class))
        .build();
  }
}
