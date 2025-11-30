package calendar.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the Event class covering construction, equality, and builders.
 */
public class EventTest {

  private LocalDateTime start;
  private LocalDateTime end;
  private EventSeries series;

  /**
   * Initializes shared event fixtures before each test.
   */
  @Before
  public void setUp() {
    start = LocalDateTime.of(2025, 1, 15, 10, 0);
    end = LocalDateTime.of(2025, 1, 15, 11, 0);
    Set<java.time.DayOfWeek> days = EnumSet.of(java.time.DayOfWeek.MONDAY);
    series = new EventSeries("Test Series", LocalTime.of(10, 0),
        LocalTime.of(11, 0), days);
  }

  @Test
  public void testConstructorAllParameters() {
    Event event = Event.builder("Test Event", start, end)
        .series(series)
        .description("Test Description")
        .location("Test Location")
        .status("private")
        .build();

    assertEquals("Test Event", event.getSubject());
    assertEquals(start, event.getStart());
    assertEquals(end, event.getEnd());
    assertEquals(series, event.getSeries());
    assertEquals("Test Description", event.getDescription());
    assertEquals("Test Location", event.getLocation());
    assertEquals("private", event.getStatus());
  }

  @Test
  public void testConstructorNullSeries() {
    Event event = Event.builder("Test Event", start, end)
        .description("Test Description")
        .location("Test Location")
        .status("public")
        .build();

    assertEquals("Test Event", event.getSubject());
    assertEquals(start, event.getStart());
    assertEquals(end, event.getEnd());
    assertNull(event.getSeries());
    assertEquals("Test Description", event.getDescription());
    assertEquals("Test Location", event.getLocation());
    assertEquals("public", event.getStatus());
  }

  @Test
  public void testConstructorNullOptionalFields() {
    Event event = new Event("Test Event", start, end, series,
        null, null, null);

    assertEquals("Test Event", event.getSubject());
    assertEquals(start, event.getStart());
    assertEquals(end, event.getEnd());
    assertEquals(series, event.getSeries());
    assertNull(event.getDescription());
    assertNull(event.getLocation());
    assertNull(event.getStatus());
  }

  @Test
  public void testConstructorEmptyStrings() {
    Event event = new Event("Test Event", start, end, series,
        "", "", "");

    assertEquals("Test Event", event.getSubject());
    assertEquals(start, event.getStart());
    assertEquals(end, event.getEnd());
    assertEquals(series, event.getSeries());
    assertEquals("", event.getDescription());
    assertEquals("", event.getLocation());
    assertEquals("", event.getStatus());
  }

  @Test
  public void testGetters() {
    Event event = new Event("Subject", start, end, series,
        "Description", "Location", "status");

    assertEquals("Subject", event.getSubject());
    assertEquals(start, event.getStart());
    assertEquals(end, event.getEnd());
    assertEquals(series, event.getSeries());
    assertEquals("Description", event.getDescription());
    assertEquals("Location", event.getLocation());
    assertEquals("status", event.getStatus());
  }

  @Test
  public void testIsPartOfSeries() {
    Event eventWithSeries = new Event("Subject", start, end, series,
        "Description", "Location", "status");
    Event eventWithoutSeries = new Event("Subject", start, end, null,
        "Description", "Location", "status");

    assertTrue(eventWithSeries.getSeries() != null);
    assertFalse(eventWithoutSeries.getSeries() != null);
  }

  @Test
  public void testEqualsSameObject() {
    Event event = new Event("Subject", start, end, series,
        "Description", "Location", "status");
    assertTrue(event.equals(event));
  }

  @Test
  public void testEqualsEqualEvents() {
    Event event1 = new Event("Subject", start, end, series,
        "Description", "Location", "status");
    Event event2 = new Event("Subject", start, end, series,
        "Description", "Location", "status");
    assertTrue(event1.equals(event2));
  }

  @Test
  public void testEqualsDifferentSubjects() {
    Event event1 = new Event("Subject1", start, end, series,
        "Description", "Location", "status");
    Event event2 = new Event("Subject2", start, end, series,
        "Description", "Location", "status");
    assertFalse(event1.equals(event2));
  }

  @Test
  public void testEqualsDifferentStartTimes() {
    LocalDateTime differentStart = LocalDateTime.of(2025, 1, 15, 9, 0);
    Event event1 = new Event("Subject", start, end, series,
        "Description", "Location", "status");
    Event event2 = new Event("Subject", differentStart, end, series,
        "Description", "Location", "status");
    assertFalse(event1.equals(event2));
  }

  @Test
  public void testEqualsDifferentEndTimes() {
    LocalDateTime differentEnd = LocalDateTime.of(2025, 1, 15, 12, 0);
    Event event1 = new Event("Subject", start, end, series,
        "Description", "Location", "status");
    Event event2 = new Event("Subject", start, differentEnd, series,
        "Description", "Location", "status");
    assertFalse(event1.equals(event2));
  }

  @Test
  public void testEqualsNullObject() {
    Event event = new Event("Subject", start, end, series,
        "Description", "Location", "status");
    assertFalse(event.equals(null));
  }

  @Test
  public void testEqualsDifferentClass() {
    Event event = new Event("Subject", start, end, series,
        "Description", "Location", "status");
    assertFalse(event.equals("Not an Event"));
  }

  @Test
  public void testHashCodeConsistency() {
    Event event = new Event("Subject", start, end, series,
        "Description", "Location", "status");
    assertEquals(event.hashCode(), event.hashCode());
  }

  @Test
  public void testHashCodeDifferentSubjects() {
    Event event1 = new Event("Subject1", start, end, series,
        "Description", "Location", "status");
    Event event2 = new Event("Subject2", start, end, series,
        "Description", "Location", "status");
    assertFalse(event1.hashCode() == event2.hashCode());
  }

  @Test
  public void testHashCodeDifferentStartTimes() {
    LocalDateTime differentStart = LocalDateTime.of(2025, 1, 15, 9, 0);
    Event event1 = new Event("Subject", start, end, series,
        "Description", "Location", "status");
    Event event2 = new Event("Subject", differentStart, end, series,
        "Description", "Location", "status");
    assertFalse(event1.hashCode() == event2.hashCode());
  }

  @Test
  public void testHashCodeDifferentEndTimes() {
    LocalDateTime differentEnd = LocalDateTime.of(2025, 1, 15, 12, 0);
    Event event1 = new Event("Subject", start, end, series,
        "Description", "Location", "status");
    Event event2 = new Event("Subject", start, differentEnd, series,
        "Description", "Location", "status");
    assertFalse(event1.hashCode() == event2.hashCode());
  }

  @Test
  public void testEventWithSpecialCharacters() {
    String subjectWithSpecialChars = "Event with \"quotes\" and, commas";
    String descriptionWithNewlines = "Description\nwith\nnewlines";
    String locationWithCommas = "Location, with, commas";

    Event event = new Event(subjectWithSpecialChars, start, end, series,
        descriptionWithNewlines, locationWithCommas, "private");

    assertEquals(subjectWithSpecialChars, event.getSubject());
    assertEquals(descriptionWithNewlines, event.getDescription());
    assertEquals(locationWithCommas, event.getLocation());
  }

  @Test
  public void testEventWithEmptySubject() {
    Event event = new Event("", start, end, series,
        "Description", "Location", "status");
    assertEquals("", event.getSubject());
  }

  @Test
  public void testEventWithVeryLongStrings() {
    String longString = "A".repeat(1000);

    Event event = new Event(longString, start, end, series,
        longString, longString, longString);

    assertEquals(longString, event.getSubject());
    assertEquals(longString, event.getDescription());
    assertEquals(longString, event.getLocation());
    assertEquals(longString, event.getStatus());
  }

  @Test
  public void testEventCrossDayEvent() {
    LocalDateTime crossDayStart = LocalDateTime.of(2025, 1, 15, 23, 0);
    LocalDateTime crossDayEnd = LocalDateTime.of(2025, 1, 16, 1, 0);

    Event event = new Event("Cross Day Event", crossDayStart, crossDayEnd,
        series, "Description", "Location", "status");

    assertEquals(crossDayStart, event.getStart());
    assertEquals(crossDayEnd, event.getEnd());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBuilderThrowsIfSubjectIsNull() {
    Event.builder(null, start, end).build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBuilderThrowsIfSubjectIsWhitespace() {
    Event.builder("   ", start, end).build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBuilderThrowsIfStartIsNull() {
    Event.builder("Test", null, end).build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBuilderThrowsIfEndIsNull() {
    Event.builder("Test", start, null).build();
  }
}
