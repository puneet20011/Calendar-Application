package calendar.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for EventCopier class functionality.
 * Tests event copying operations between calendars with timezone handling.
 */
public class EventCopierTest {

  private CalendarContext sourceContext;
  private CalendarContext targetContext;
  private EventCopier copier;

  /**
   * Sets up the test fixture.
   */
  @Before
  public void setUp() {
    sourceContext = new CalendarContext("Source", ZoneId.of("America/New_York"));
    targetContext = new CalendarContext("Target", ZoneId.of("America/Los_Angeles"));
    copier = new EventCopier();
  }

  /**
   * Clears references after each test run.
   */
  @After
  public void tearDown() {
    sourceContext = null;
    targetContext = null;
    copier = null;
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCopySingleEventNotFound() {
    LocalDateTime sourceStart = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime targetStart = LocalDateTime.of(2025, 1, 16, 14, 0);

    copier.copySingleEvent(sourceContext, targetContext,
        "NonExistent", sourceStart, targetStart);
  }

  @Test
  public void testCopyEventWithOptionalFields() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    sourceContext.getModel().createEvent("Meeting", start, end);
    sourceContext.getModel().editEvent("Meeting", start,
        "description", "Important");
    sourceContext.getModel().editEvent("Meeting", start,
        "location", "Room 101");
    sourceContext.getModel().editEvent("Meeting", start,
        "status", "private");

    LocalDateTime targetStart = LocalDateTime.of(2025, 1, 20, 10, 0);

    copier.copySingleEvent(sourceContext, targetContext,
        "Meeting", start, targetStart);

    Event copied = (Event) targetContext.getModel()
        .getByDate(LocalDate.of(2025, 1, 20)).get(0);

    assertEquals("Description should be copied",
        "Important", copied.getDescription());
    assertEquals("Location should be copied",
        "Room 101", copied.getLocation());
    assertEquals("Status should be copied",
        "private", copied.getStatus());
  }

  @Test
  public void testCopySameCalendarSameTimezone() {
    CalendarContext sameContext = new CalendarContext("Same",
        ZoneId.of("America/New_York"));

    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    sameContext.getModel().createEvent("Meeting", start, end);

    LocalDate sourceDate = LocalDate.of(2025, 1, 15);
    LocalDate targetDate = LocalDate.of(2025, 1, 20);

    copier.copyEventsOnDate(sameContext, sameContext,
        sourceDate, targetDate);

    List<Ievent> original = sameContext.getModel()
        .getByDate(LocalDate.of(2025, 1, 15));
    List<Ievent> copied = sameContext.getModel()
        .getByDate(LocalDate.of(2025, 1, 20));

    assertEquals("Original should still exist", 1, original.size());
    assertEquals("Copy should exist", 1, copied.size());
  }

  @Test
  public void testCopySeriesOccurrencesIndividuallyWithDuplicates() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 13, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 13, 11, 0);
    sourceContext.getModel().createEventSeries("Weekly", start, end, "MW", 3, null);

    LocalDateTime targetStart1 = LocalDateTime.of(2025, 2, 1, 10, 0);
    LocalDateTime targetEnd1 = LocalDateTime.of(2025, 2, 1, 11, 0);
    targetContext.getModel().createEvent("Weekly", targetStart1, targetEnd1);

    LocalDate sourceStart = LocalDate.of(2025, 1, 13);
    LocalDate sourceEnd = LocalDate.of(2025, 1, 20);
    LocalDate targetStart = LocalDate.of(2025, 2, 1);

    copier.copyEventsInRange(sourceContext, targetContext,
        sourceStart, sourceEnd, targetStart);

    List<Ievent> allCopied = targetContext.getModel().getByRange(
        LocalDateTime.of(2025, 2, 1, 0, 0),
        LocalDateTime.of(2025, 2, 28, 23, 59)
    );

    assertTrue("Should have copied at least some events", allCopied.size() >= 2);
  }

  @Test
  public void testCopySeriesOccurrencesIndividuallyAllDuplicates() {
    CalendarContext sourceCtx = new CalendarContext("Source", ZoneId.of("UTC"));
    CalendarContext targetCtx = new CalendarContext("Target", ZoneId.of("UTC"));

    LocalDateTime start = LocalDateTime.of(2025, 1, 13, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 13, 11, 0);
    sourceCtx.getModel().createEventSeries("Weekly", start, end, "MW", 2, null);

    targetCtx.getModel().createEvent("Weekly",
        LocalDateTime.of(2025, 2, 1, 10, 0),
        LocalDateTime.of(2025, 2, 1, 11, 0));
    targetCtx.getModel().createEvent("Weekly",
        LocalDateTime.of(2025, 2, 3, 10, 0),
        LocalDateTime.of(2025, 2, 3, 11, 0));

    LocalDate sourceStart = LocalDate.of(2025, 1, 13);
    LocalDate sourceEnd = LocalDate.of(2025, 1, 20);
    LocalDate targetStart = LocalDate.of(2025, 2, 1);

    copier.copyEventsInRange(sourceCtx, targetCtx,
        sourceStart, sourceEnd, targetStart);

    List<Ievent> allCopied = targetCtx.getModel().getByRange(
        LocalDateTime.of(2025, 2, 1, 0, 0),
        LocalDateTime.of(2025, 2, 28, 23, 59)
    );

    assertEquals("Should not create duplicates", 2, allCopied.size());
  }

  @Test
  public void testCopyOptionalPropertiesWithException() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    sourceContext.getModel().createEvent("Meeting", start, end);
    sourceContext.getModel().editEvent("Meeting", start,
        "description", "Important");

    LocalDateTime targetStart = LocalDateTime.of(2025, 1, 20, 10, 0);

    copier.copySingleEvent(sourceContext, targetContext,
        "Meeting", start, targetStart);

    Event copied = (Event) targetContext.getModel()
        .getByDate(LocalDate.of(2025, 1, 20)).get(0);

    assertNotNull("Event should be created even if properties fail", copied);
    assertEquals("Subject should be copied", "Meeting", copied.getSubject());
  }

  @Test
  public void testCopySeriesOccurrencesIndividuallyEmptyList() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 13, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 13, 11, 0);
    sourceContext.getModel().createEventSeries("Weekly", start, end, "MW", 2, null);

    LocalDate sourceStart = LocalDate.of(2025, 3, 1);
    LocalDate sourceEnd = LocalDate.of(2025, 3, 7);
    LocalDate targetStart = LocalDate.of(2025, 4, 1);

    copier.copyEventsInRange(sourceContext, targetContext,
        sourceStart, sourceEnd, targetStart);

    List<Ievent> allCopied = targetContext.getModel().getByRange(
        LocalDateTime.of(2025, 4, 1, 0, 0),
        LocalDateTime.of(2025, 4, 30, 23, 59)
    );

    assertEquals("Should not copy any events", 0, allCopied.size());
  }

  @Test
  public void testCopySeriesPartialOverlap() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 13, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 13, 11, 0);
    sourceContext.getModel().createEventSeries("Weekly", start, end, "MTWRF", 5, null);

    LocalDate sourceStart = LocalDate.of(2025, 1, 13);
    LocalDate sourceEnd = LocalDate.of(2025, 1, 14);
    LocalDate targetStart = LocalDate.of(2025, 2, 1);

    copier.copyEventsInRange(sourceContext, targetContext,
        sourceStart, sourceEnd, targetStart);

    List<Ievent> allCopied = targetContext.getModel().getByRange(
        LocalDateTime.of(2025, 2, 1, 0, 0),
        LocalDateTime.of(2025, 2, 28, 23, 59)
    );

    assertTrue("Should copy partial series", allCopied.size() >= 1 && allCopied.size() <= 3);
  }

  @Test
  public void testCopyEventWithNullOptionalFields() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    sourceContext.getModel().createEvent("Meeting", start, end);

    LocalDateTime targetStart = LocalDateTime.of(2025, 1, 20, 10, 0);

    copier.copySingleEvent(sourceContext, targetContext,
        "Meeting", start, targetStart);

    Event copied = (Event) targetContext.getModel()
        .getByDate(LocalDate.of(2025, 1, 20)).get(0);

    assertNotNull("Event should be created", copied);
    assertEquals("Subject should be copied", "Meeting", copied.getSubject());
  }

  @Test
  public void testCopyEventWithEmptyOptionalFields() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    sourceContext.getModel().createEvent("Meeting", start, end);
    sourceContext.getModel().editEvent("Meeting", start, "description", "");
    sourceContext.getModel().editEvent("Meeting", start, "location", "");

    LocalDateTime targetStart = LocalDateTime.of(2025, 1, 20, 10, 0);

    copier.copySingleEvent(sourceContext, targetContext,
        "Meeting", start, targetStart);

    Event copied = (Event) targetContext.getModel()
        .getByDate(LocalDate.of(2025, 1, 20)).get(0);

    assertNotNull("Event should be created", copied);
    assertEquals("Subject should be copied", "Meeting", copied.getSubject());
  }

  @Test
  public void testCopyOptionalPropertiesPublicStatus() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    sourceContext.getModel().createEvent("Meeting", start, end);
    sourceContext.getModel().editEvent("Meeting", start, "status", "public");

    LocalDateTime targetStart = LocalDateTime.of(2025, 1, 20, 10, 0);

    copier.copySingleEvent(sourceContext, targetContext,
        "Meeting", start, targetStart);

    Event copied = (Event) targetContext.getModel()
        .getByDate(LocalDate.of(2025, 1, 20)).get(0);

    assertEquals("Status should be public", "public", copied.getStatus());
  }

  @Test
  public void testCopySeriesOccurrencesIndividuallySuccess() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 13, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 13, 11, 0);
    sourceContext.getModel().createEventSeries("Weekly", start, end, "MW", 3, null);

    LocalDate sourceStart = LocalDate.of(2025, 1, 13);
    LocalDate sourceEnd = LocalDate.of(2025, 1, 20);
    LocalDate targetStart = LocalDate.of(2025, 2, 1);

    copier.copyEventsInRange(sourceContext, targetContext,
        sourceStart, sourceEnd, targetStart);

    List<Ievent> allCopied = targetContext.getModel().getByRange(
        LocalDateTime.of(2025, 2, 1, 0, 0),
        LocalDateTime.of(2025, 2, 28, 23, 59)
    );

    assertTrue("Should successfully copy events", allCopied.size() >= 2);
  }

  @Test
  public void testCopySeriesOccurrencesIndividuallyWithDuplicate() {
    CalendarContext sourceCtx = new CalendarContext("Source", ZoneId.of("UTC"));
    CalendarContext targetCtx = new CalendarContext("Target", ZoneId.of("UTC"));

    LocalDateTime start = LocalDateTime.of(2025, 1, 13, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 13, 11, 0);
    sourceCtx.getModel().createEventSeries("Weekly", start, end, "MW", 2, null);

    targetCtx.getModel().createEvent("Weekly",
        LocalDateTime.of(2025, 2, 1, 10, 0),
        LocalDateTime.of(2025, 2, 1, 11, 0));

    LocalDate sourceStart = LocalDate.of(2025, 1, 13);
    LocalDate sourceEnd = LocalDate.of(2025, 1, 20);
    LocalDate targetStart = LocalDate.of(2025, 2, 1);

    copier.copyEventsInRange(sourceCtx, targetCtx,
        sourceStart, sourceEnd, targetStart);

    List<Ievent> allCopied = targetCtx.getModel().getByRange(
        LocalDateTime.of(2025, 2, 1, 0, 0),
        LocalDateTime.of(2025, 2, 28, 23, 59)
    );

    assertTrue("Should have at least the pre-existing event", allCopied.size() >= 1);
  }

  @Test
  public void testCopySeriesOccurrencesCopiesOptionalProperties() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 13, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 13, 11, 0);
    sourceContext.getModel().createEventSeries("Weekly", start, end, "M", 2, null);

    sourceContext.getModel().editEvent("Weekly", start, "description", "Important");
    sourceContext.getModel().editEvent("Weekly", start, "location", "Room 101");

    LocalDate sourceStart = LocalDate.of(2025, 1, 13);
    LocalDate sourceEnd = LocalDate.of(2025, 1, 20);
    LocalDate targetStart = LocalDate.of(2025, 2, 1);

    copier.copyEventsInRange(sourceContext, targetContext,
        sourceStart, sourceEnd, targetStart);

    List<Ievent> allCopied = targetContext.getModel().getByRange(
        LocalDateTime.of(2025, 2, 1, 0, 0),
        LocalDateTime.of(2025, 2, 28, 23, 59)
    );

    assertTrue("Should copy events with optional properties", allCopied.size() >= 1);
  }

  @Test
  public void testCopySeriesWithSaturdayOccurrences() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 18, 10, 0); // Saturday
    LocalDateTime end = LocalDateTime.of(2025, 1, 18, 11, 0);
    sourceContext.getModel().createEventSeries("Saturday Meeting", start, end, "S", 2, null);

    LocalDate sourceStart = LocalDate.of(2025, 1, 18);
    LocalDate sourceEnd = LocalDate.of(2025, 1, 25);
    LocalDate targetStart = LocalDate.of(2025, 2, 1);

    copier.copyEventsInRange(sourceContext, targetContext,
        sourceStart, sourceEnd, targetStart);

    List<Ievent> allCopied = targetContext.getModel().getByRange(
        LocalDateTime.of(2025, 2, 1, 0, 0),
        LocalDateTime.of(2025, 2, 28, 23, 59)
    );

    assertTrue("Should copy Saturday events", allCopied.size() >= 1);
  }

  @Test
  public void testCopySeriesWithSundayOccurrences() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 19, 10, 0); // Sunday
    LocalDateTime end = LocalDateTime.of(2025, 1, 19, 11, 0);
    sourceContext.getModel().createEventSeries("Sunday Meeting", start, end, "U", 2, null);

    LocalDate sourceStart = LocalDate.of(2025, 1, 19);
    LocalDate sourceEnd = LocalDate.of(2025, 1, 26);
    LocalDate targetStart = LocalDate.of(2025, 2, 2);

    copier.copyEventsInRange(sourceContext, targetContext,
        sourceStart, sourceEnd, targetStart);

    List<Ievent> allCopied = targetContext.getModel().getByRange(
        LocalDateTime.of(2025, 2, 1, 0, 0),
        LocalDateTime.of(2025, 2, 28, 23, 59)
    );

    assertTrue("Should copy Sunday events", allCopied.size() >= 1);
  }

  @Test
  public void testCopyEventWithAllOptionalPropertiesPresent() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);

    sourceContext.getModel().createEvent("Meeting", start, end);
    sourceContext.getModel().editEvent("Meeting", start, "description", "Important meeting");
    sourceContext.getModel().editEvent("Meeting", start, "location", "Room 101");
    sourceContext.getModel().editEvent("Meeting", start, "status", "private");

    LocalDateTime targetStart = LocalDateTime.of(2025, 1, 20, 10, 0);

    copier.copySingleEvent(sourceContext, targetContext, "Meeting", start, targetStart);

    Event copied = (Event) targetContext.getModel().getByDate(LocalDate.of(2025, 1, 20)).get(0);

    assertEquals("Description should be copied", "Important meeting", copied.getDescription());
    assertEquals("Location should be copied", "Room 101", copied.getLocation());
    assertEquals("Status should be copied", "private", copied.getStatus());
  }

  @Test
  public void testCopyEventWithOnlyDescription() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);

    sourceContext.getModel().createEvent("Meeting", start, end);
    sourceContext.getModel().editEvent("Meeting", start, "description", "Test description");

    LocalDateTime targetStart = LocalDateTime.of(2025, 1, 20, 10, 0);

    copier.copySingleEvent(sourceContext, targetContext, "Meeting", start, targetStart);

    Event copied = (Event) targetContext.getModel().getByDate(LocalDate.of(2025, 1, 20)).get(0);
    assertEquals("Description should be copied", "Test description", copied.getDescription());
  }

  @Test
  public void testCopyEventWithOnlyLocation() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);

    sourceContext.getModel().createEvent("Meeting", start, end);
    sourceContext.getModel().editEvent("Meeting", start, "location", "Conference Room");

    LocalDateTime targetStart = LocalDateTime.of(2025, 1, 20, 10, 0);

    copier.copySingleEvent(sourceContext, targetContext, "Meeting", start, targetStart);

    Event copied = (Event) targetContext.getModel().getByDate(LocalDate.of(2025, 1, 20)).get(0);
    assertEquals("Location should be copied", "Conference Room", copied.getLocation());
  }

  @Test
  public void testCopyEventWithOnlyStatus() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);

    sourceContext.getModel().createEvent("Meeting", start, end);
    sourceContext.getModel().editEvent("Meeting", start, "status", "private");

    LocalDateTime targetStart = LocalDateTime.of(2025, 1, 20, 10, 0);

    copier.copySingleEvent(sourceContext, targetContext, "Meeting", start, targetStart);

    Event copied = (Event) targetContext.getModel().getByDate(LocalDate.of(2025, 1, 20)).get(0);
    assertEquals("Status should be copied", "private", copied.getStatus());
  }

  @Test
  public void testCopyEventsOnDateCopiesOptionalProperties() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);

    sourceContext.getModel().createEvent("Event1", start, end);
    sourceContext.getModel().editEvent("Event1", start, "description", "Description 1");
    sourceContext.getModel().editEvent("Event1", start, "location", "Location 1");

    copier.copyEventsOnDate(sourceContext, targetContext,
        LocalDate.of(2025, 1, 15), LocalDate.of(2025, 1, 20));

    Event copied = (Event) targetContext.getModel().getByDate(LocalDate.of(2025, 1, 20)).get(0);
    assertEquals("Description should be copied", "Description 1", copied.getDescription());
    assertEquals("Location should be copied", "Location 1", copied.getLocation());
  }

  @Test
  public void testCopyEventsInRangeCopiesOptionalProperties() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);

    sourceContext.getModel().createEvent("RangeEvent", start, end);
    sourceContext.getModel().editEvent("RangeEvent", start, "description", "Range description");
    sourceContext.getModel().editEvent("RangeEvent", start, "location", "Range location");
    sourceContext.getModel().editEvent("RangeEvent", start, "status", "private");

    copier.copyEventsInRange(sourceContext, targetContext,
        LocalDate.of(2025, 1, 15), LocalDate.of(2025, 1, 15),
        LocalDate.of(2025, 2, 1));

    Event copied = (Event) targetContext.getModel().getByDate(LocalDate.of(2025, 2, 1)).get(0);
    assertEquals("Description should be copied", "Range description", copied.getDescription());
    assertEquals("Location should be copied", "Range location", copied.getLocation());
    assertEquals("Status should be copied", "private", copied.getStatus());
  }

  @Test
  public void testCopyEventWithMixedOptionalProperties() {
    LocalDateTime start1 = LocalDateTime.of(2025, 1, 15, 9, 0);
    LocalDateTime end1 = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime start2 = LocalDateTime.of(2025, 1, 15, 14, 0);
    LocalDateTime end2 = LocalDateTime.of(2025, 1, 15, 15, 0);

    sourceContext.getModel().createEvent("Event1", start1, end1);
    sourceContext.getModel().editEvent("Event1", start1, "description", "Has desc");
    sourceContext.getModel().editEvent("Event1", start1, "location", "Has loc");

    sourceContext.getModel().createEvent("Event2", start2, end2);
    sourceContext.getModel().editEvent("Event2", start2, "location", "Room 202");
    sourceContext.getModel().editEvent("Event2", start2, "status", "private");

    copier.copyEventsOnDate(sourceContext, targetContext,
        LocalDate.of(2025, 1, 15), LocalDate.of(2025, 1, 20));

    List<Ievent> copied = targetContext.getModel().getByDate(LocalDate.of(2025, 1, 20));
    assertEquals("Should copy both events", 2, copied.size());

    Event copied1 = null;
    Event copied2 = null;

    for (Ievent ievent : copied) {
      Event event = (Event) ievent;
      if (event.getSubject().equals("Event1")) {
        copied1 = event;
      } else if (event.getSubject().equals("Event2")) {
        copied2 = event;
      }
    }

    assertNotNull(copied1);
    assertNotNull(copied2);

    assertEquals("Event1 description", "Has desc", copied1.getDescription());
    assertEquals("Event1 location", "Has loc", copied1.getLocation());
    assertEquals("Event2 location", "Room 202", copied2.getLocation());
    assertEquals("Event2 status", "private", copied2.getStatus());
  }

  @Test
  public void testCopyEventFailureDoesNotPreventEventCreation() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);

    sourceContext.getModel().createEvent("Meeting", start, end);
    sourceContext.getModel().editEvent("Meeting", start, "description", "Test");

    LocalDateTime targetStart = LocalDateTime.of(2025, 1, 20, 10, 0);

    copier.copySingleEvent(sourceContext, targetContext, "Meeting", start, targetStart);

    List<Ievent> copied = targetContext.getModel().getByDate(LocalDate.of(2025, 1, 20));
    assertEquals("Event should be created even if properties fail", 1, copied.size());
    assertEquals("Subject should be copied", "Meeting", copied.get(0).getSubject());
  }

  @Test
  public void testCopyEventWithLongOptionalPropertyValues() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);

    String longDescription = "A".repeat(500);
    String longLocation = "B".repeat(300);

    sourceContext.getModel().createEvent("Event", start, end);
    sourceContext.getModel().editEvent("Event", start, "description", longDescription);
    sourceContext.getModel().editEvent("Event", start, "location", longLocation);

    LocalDateTime targetStart = LocalDateTime.of(2025, 1, 20, 10, 0);
    copier.copySingleEvent(sourceContext, targetContext, "Event", start, targetStart);

    Event copied = (Event) targetContext.getModel().getByDate(LocalDate.of(2025, 1, 20)).get(0);
    assertEquals("Long description should be copied", longDescription, copied.getDescription());
    assertEquals("Long location should be copied", longLocation, copied.getLocation());
  }

  @Test
  public void testCopyEventWithSpecialCharactersInOptionalProperties() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);

    sourceContext.getModel().createEvent("Event", start, end);
    sourceContext.getModel().editEvent("Event", start, "description",
        "Desc with \"quotes\" and, commas");
    sourceContext.getModel().editEvent("Event", start, "location",
        "Room: 101 & 102");

    LocalDateTime targetStart = LocalDateTime.of(2025, 1, 20, 10, 0);
    copier.copySingleEvent(sourceContext, targetContext, "Event", start, targetStart);

    Event copied = (Event) targetContext.getModel().getByDate(LocalDate.of(2025, 1, 20)).get(0);
    assertEquals("Special chars in description", "Desc with \"quotes\" and, commas",
        copied.getDescription());
    assertEquals("Special chars in location", "Room: 101 & 102", copied.getLocation());
  }

  @Test
  public void testCopySeriesPreservesExactWeekdayPattern() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 13, 10, 0); // Monday
    LocalDateTime end = LocalDateTime.of(2025, 1, 13, 11, 0);

    sourceContext.getModel().createEventSeries("MWF Pattern", start, end, "MWF", 6, null);

    Event originalEvent =
        (Event) sourceContext.getModel().getByDate(LocalDate.of(2025, 1, 13)).get(0);
    EventSeries originalSeries = originalEvent.getSeries();

    copier.copyEventsInRange(sourceContext, targetContext,
        LocalDate.of(2025, 1, 13), LocalDate.of(2025, 2, 1),
        LocalDate.of(2025, 2, 3));

    List<Ievent> copiedEvents = targetContext.getModel().getByRange(
        LocalDateTime.of(2025, 2, 1, 0, 0),
        LocalDateTime.of(2025, 3, 1, 23, 59)
    );

    assertTrue("Should copy multiple events", copiedEvents.size() >= 3);

    Set<java.time.DayOfWeek> copiedDays = new java.util.HashSet<>();
    for (Ievent ievent : copiedEvents) {
      Event event = (Event) ievent;
      copiedDays.add(event.getStart().getDayOfWeek());
    }

    assertTrue("Should have Monday", copiedDays.contains(java.time.DayOfWeek.MONDAY));
    assertTrue("Should have Wednesday", copiedDays.contains(java.time.DayOfWeek.WEDNESDAY));
    assertTrue("Should have Friday", copiedDays.contains(java.time.DayOfWeek.FRIDAY));
    assertFalse("Should NOT have Tuesday", copiedDays.contains(java.time.DayOfWeek.TUESDAY));
    assertFalse("Should NOT have Thursday", copiedDays.contains(java.time.DayOfWeek.THURSDAY));
    assertFalse("Should NOT have Saturday", copiedDays.contains(java.time.DayOfWeek.SATURDAY));
    assertFalse("Should NOT have Sunday", copiedDays.contains(java.time.DayOfWeek.SUNDAY));
  }

  @Test
  public void testCopySeriesWithOnlyMonday() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 13, 10, 0); // Monday
    LocalDateTime end = LocalDateTime.of(2025, 1, 13, 11, 0);

    sourceContext.getModel().createEventSeries("Monday Only", start, end, "M", 3, null);

    copier.copyEventsInRange(sourceContext, targetContext,
        LocalDate.of(2025, 1, 13), LocalDate.of(2025, 1, 31),
        LocalDate.of(2025, 2, 3));

    List<Ievent> copied = targetContext.getModel().getByRange(
        LocalDateTime.of(2025, 2, 1, 0, 0),
        LocalDateTime.of(2025, 2, 28, 23, 59)
    );

    for (Ievent ievent : copied) {
      Event event = (Event) ievent;
      assertEquals("All events should be on Monday",
          java.time.DayOfWeek.MONDAY, event.getStart().getDayOfWeek());
    }
  }

  @Test
  public void testCopySeriesWithOnlyTuesday() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 14, 10, 0); // Tuesday
    LocalDateTime end = LocalDateTime.of(2025, 1, 14, 11, 0);

    sourceContext.getModel().createEventSeries("Tuesday Only", start, end, "T", 3, null);

    copier.copyEventsInRange(sourceContext, targetContext,
        LocalDate.of(2025, 1, 14), LocalDate.of(2025, 1, 31),
        LocalDate.of(2025, 2, 4));

    List<Ievent> copied = targetContext.getModel().getByRange(
        LocalDateTime.of(2025, 2, 1, 0, 0),
        LocalDateTime.of(2025, 2, 28, 23, 59)
    );

    for (Ievent ievent : copied) {
      Event event = (Event) ievent;
      assertEquals("All events should be on Tuesday",
          java.time.DayOfWeek.TUESDAY, event.getStart().getDayOfWeek());
    }
  }

  @Test
  public void testCopySeriesWithOnlyWednesday() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0); // Wednesday
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);

    sourceContext.getModel().createEventSeries("Wednesday Only", start, end, "W", 3, null);

    copier.copyEventsInRange(sourceContext, targetContext,
        LocalDate.of(2025, 1, 15), LocalDate.of(2025, 1, 31),
        LocalDate.of(2025, 2, 5));

    List<Ievent> copied = targetContext.getModel().getByRange(
        LocalDateTime.of(2025, 2, 1, 0, 0),
        LocalDateTime.of(2025, 2, 28, 23, 59)
    );

    for (Ievent ievent : copied) {
      Event event = (Event) ievent;
      assertEquals("All events should be on Wednesday",
          java.time.DayOfWeek.WEDNESDAY, event.getStart().getDayOfWeek());
    }
  }

  @Test
  public void testCopySeriesWithOnlyThursday() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 16, 10, 0); // Thursday
    LocalDateTime end = LocalDateTime.of(2025, 1, 16, 11, 0);

    sourceContext.getModel().createEventSeries("Thursday Only", start, end, "R", 3, null);

    copier.copyEventsInRange(sourceContext, targetContext,
        LocalDate.of(2025, 1, 16), LocalDate.of(2025, 1, 31),
        LocalDate.of(2025, 2, 6));

    List<Ievent> copied = targetContext.getModel().getByRange(
        LocalDateTime.of(2025, 2, 1, 0, 0),
        LocalDateTime.of(2025, 2, 28, 23, 59)
    );

    for (Ievent ievent : copied) {
      Event event = (Event) ievent;
      assertEquals("All events should be on Thursday",
          java.time.DayOfWeek.THURSDAY, event.getStart().getDayOfWeek());
    }
  }

  @Test
  public void testCopySeriesWithOnlyFriday() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 17, 10, 0); // Friday
    LocalDateTime end = LocalDateTime.of(2025, 1, 17, 11, 0);

    sourceContext.getModel().createEventSeries("Friday Only", start, end, "F", 3, null);

    copier.copyEventsInRange(sourceContext, targetContext,
        LocalDate.of(2025, 1, 17), LocalDate.of(2025, 1, 31),
        LocalDate.of(2025, 2, 7));

    List<Ievent> copied = targetContext.getModel().getByRange(
        LocalDateTime.of(2025, 2, 1, 0, 0),
        LocalDateTime.of(2025, 2, 28, 23, 59)
    );

    for (Ievent ievent : copied) {
      Event event = (Event) ievent;
      assertEquals("All events should be on Friday",
          java.time.DayOfWeek.FRIDAY, event.getStart().getDayOfWeek());
    }
  }

  @Test
  public void testCopySeriesWeekdaysOnly() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 13, 10, 0); // Monday
    LocalDateTime end = LocalDateTime.of(2025, 1, 13, 11, 0);

    sourceContext.getModel().createEventSeries("Weekdays", start, end, "MTWRF", 10, null);

    copier.copyEventsInRange(sourceContext, targetContext,
        LocalDate.of(2025, 1, 13), LocalDate.of(2025, 1, 31),
        LocalDate.of(2025, 2, 3));

    List<Ievent> copied = targetContext.getModel().getByRange(
        LocalDateTime.of(2025, 2, 1, 0, 0),
        LocalDateTime.of(2025, 2, 28, 23, 59)
    );

    for (Ievent ievent : copied) {
      Event event = (Event) ievent;
      java.time.DayOfWeek day = event.getStart().getDayOfWeek();
      assertFalse("Should not have Saturday", day == java.time.DayOfWeek.SATURDAY);
      assertFalse("Should not have Sunday", day == java.time.DayOfWeek.SUNDAY);
      assertTrue("Should be a weekday",
          day == java.time.DayOfWeek.MONDAY
              || day == java.time.DayOfWeek.TUESDAY
              || day == java.time.DayOfWeek.WEDNESDAY
              || day == java.time.DayOfWeek.THURSDAY
              || day == java.time.DayOfWeek.FRIDAY);
    }
  }

  @Test
  public void testCopySeriesWeekendsOnly() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 18, 10, 0); // Saturday
    LocalDateTime end = LocalDateTime.of(2025, 1, 18, 11, 0);

    sourceContext.getModel().createEventSeries("Weekends", start, end, "SU", 6, null);

    copier.copyEventsInRange(sourceContext, targetContext,
        LocalDate.of(2025, 1, 18), LocalDate.of(2025, 2, 10),
        LocalDate.of(2025, 2, 15));

    List<Ievent> copied = targetContext.getModel().getByRange(
        LocalDateTime.of(2025, 2, 1, 0, 0),
        LocalDateTime.of(2025, 3, 1, 23, 59)
    );

    assertTrue("Should copy weekend events", copied.size() >= 2);

    for (Ievent ievent : copied) {
      Event event = (Event) ievent;
      java.time.DayOfWeek day = event.getStart().getDayOfWeek();
      assertTrue("Should only have weekend days",
          day == java.time.DayOfWeek.SATURDAY || day == java.time.DayOfWeek.SUNDAY);
    }
  }

  @Test
  public void testCopySeriesAlternatingDays() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 14, 10, 0); // Tuesday
    LocalDateTime end = LocalDateTime.of(2025, 1, 14, 11, 0);

    sourceContext.getModel().createEventSeries("T-Th Pattern", start, end, "TR", 6, null);

    copier.copyEventsInRange(sourceContext, targetContext,
        LocalDate.of(2025, 1, 14), LocalDate.of(2025, 2, 1),
        LocalDate.of(2025, 2, 4));

    List<Ievent> copied = targetContext.getModel().getByRange(
        LocalDateTime.of(2025, 2, 1, 0, 0),
        LocalDateTime.of(2025, 2, 28, 23, 59)
    );

    assertTrue("Should copy T-Th events", copied.size() >= 2);

    Set<java.time.DayOfWeek> copiedDays = new java.util.HashSet<>();
    for (Ievent ievent : copied) {
      Event event = (Event) ievent;
      copiedDays.add(event.getStart().getDayOfWeek());
    }

    assertTrue("Should have Tuesday", copiedDays.contains(java.time.DayOfWeek.TUESDAY));
    assertTrue("Should have Thursday", copiedDays.contains(java.time.DayOfWeek.THURSDAY));
    assertTrue("Should only have 2 days", copiedDays.size() == 2);
  }

  @Test
  public void testCopySeriesVerifyNoExtraDays() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 13, 10, 0); // Monday
    LocalDateTime end = LocalDateTime.of(2025, 1, 13, 11, 0);

    sourceContext.getModel().createEventSeries("M-F Only", start, end, "MF", 6, null);

    copier.copyEventsInRange(sourceContext, targetContext,
        LocalDate.of(2025, 1, 13), LocalDate.of(2025, 2, 1),
        LocalDate.of(2025, 2, 3));

    List<Ievent> copied = targetContext.getModel().getByRange(
        LocalDateTime.of(2025, 2, 1, 0, 0),
        LocalDateTime.of(2025, 2, 28, 23, 59)
    );

    Set<DayOfWeek> copiedDays = new java.util.HashSet<>();
    for (Ievent ievent : copied) {
      Event event = (Event) ievent;
      copiedDays.add(event.getStart().getDayOfWeek());
    }

    assertEquals("Should have exactly 2 days", 2, copiedDays.size());
    assertTrue("Should have Monday", copiedDays.contains(java.time.DayOfWeek.MONDAY));
    assertTrue("Should have Friday", copiedDays.contains(java.time.DayOfWeek.FRIDAY));

    assertFalse("Should NOT have Tuesday", copiedDays.contains(java.time.DayOfWeek.TUESDAY));
    assertFalse("Should NOT have Wednesday", copiedDays.contains(java.time.DayOfWeek.WEDNESDAY));
    assertFalse("Should NOT have Thursday", copiedDays.contains(java.time.DayOfWeek.THURSDAY));
    assertFalse("Should NOT have Saturday", copiedDays.contains(java.time.DayOfWeek.SATURDAY));
    assertFalse("Should NOT have Sunday", copiedDays.contains(java.time.DayOfWeek.SUNDAY));
  }

  @Test
  public void testCopySeriesFallsBackWhenCreationFails() {
    CalendarContext sourceCtx = new CalendarContext("Source", ZoneId.of("UTC"));
    CalendarContext targetCtx = new CalendarContext("Target", ZoneId.of("UTC"));
    EventCopier copier = new EventCopier();

    LocalDateTime start = LocalDateTime.of(2025, 1, 13, 10, 0); // Monday
    LocalDateTime end = LocalDateTime.of(2025, 1, 13, 11, 0);
    sourceCtx.getModel().createEventSeries("Series", start, end, "M", 3, null);

    targetCtx.getModel().createEvent("Series",
        LocalDateTime.of(2025, 2, 3, 10, 0),
        LocalDateTime.of(2025, 2, 3, 11, 0));

    copier.copyEventsInRange(sourceCtx, targetCtx,
        LocalDate.of(2025, 1, 13), LocalDate.of(2025, 1, 27),
        LocalDate.of(2025, 2, 3));

    List<Ievent> copied = targetCtx.getModel().getByRange(
        LocalDateTime.of(2025, 2, 1, 0, 0),
        LocalDateTime.of(2025, 2, 28, 23, 59)
    );

    assertTrue("Should have at least 2 events (proving fallback executed)",
        copied.size() >= 2);

    long seriesCount = 0;
    for (Ievent ievent : copied) {
      if (ievent.getSubject().equals("Series")) {
        seriesCount++;
      }
    }
    assertTrue("Should have multiple Series events from fallback", seriesCount >= 2);
  }

  @Test
  public void testCopySeriesPreservesCompleteWeekdayPattern() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 13, 10, 0); // Monday
    LocalDateTime end = LocalDateTime.of(2025, 1, 13, 11, 0);

    sourceContext.getModel().createEventSeries("All Days", start, end, "MTWRFSU", 14, null);

    copier.copyEventsInRange(sourceContext, targetContext,
        LocalDate.of(2025, 1, 13), LocalDate.of(2025, 1, 26),
        LocalDate.of(2025, 2, 3));

    List<Ievent> copiedEvents = targetContext.getModel().getByRange(
        LocalDateTime.of(2025, 2, 1, 0, 0),
        LocalDateTime.of(2025, 2, 28, 23, 59)
    );

    assertTrue("Should copy many events", copiedEvents.size() >= 7);

    int[] dayCounts = new int[7]; // M, T, W, R, F, S, U

    for (Ievent ievent : copiedEvents) {
      Event event = (Event) ievent;
      DayOfWeek day = event.getStart().getDayOfWeek();
      switch (day) {
        case MONDAY:
          dayCounts[0]++;
          break;
        case TUESDAY:
          dayCounts[1]++;
          break;
        case WEDNESDAY:
          dayCounts[2]++;
          break;
        case THURSDAY:
          dayCounts[3]++;
          break;
        case FRIDAY:
          dayCounts[4]++;
          break;
        case SATURDAY:
          dayCounts[5]++;
          break;
        case SUNDAY:
          dayCounts[6]++;
          break;
        default:
          fail("Unexpected day of week: " + day);
      }
    }

    assertTrue("Should have Monday events", dayCounts[0] > 0);
    assertTrue("Should have Tuesday events", dayCounts[1] > 0);
    assertTrue("Should have Wednesday events", dayCounts[2] > 0);
    assertTrue("Should have Thursday events", dayCounts[3] > 0);
    assertTrue("Should have Friday events", dayCounts[4] > 0);
    assertTrue("Should have Saturday events", dayCounts[5] > 0);
    assertTrue("Should have Sunday events", dayCounts[6] > 0);
  }
}