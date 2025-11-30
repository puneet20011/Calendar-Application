package calendar.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for EventCalendar class functionality.
 * Tests event storage, retrieval, and calendar operations.
 */
public class EventCalendarTest {

  private EventCalendar calendar;

  /**
   * Sets up the test fixture.
   */
  @Before
  public void setUp() {
    calendar = new EventCalendar();
  }

  /**
   * TClears references after each test run.
   */
  @After
  public void tearDown() {
    calendar = null;
  }

  @Test
  public void testAddEvent() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    Event event = Event.builder("Test Event", start, end).build();

    calendar.addEvent(event);

    List<Ievent> events = calendar.getEventsByDate(LocalDate.of(2025, 1, 15));
    assertEquals("Should have one event", 1, events.size());
    assertEquals("Event should match", event, events.get(0));
  }

  @Test
  public void testAddMultiDayEvent() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 17, 11, 0);
    Event event = Event.builder("Multi Day Event", start, end).build();

    calendar.addEvent(event);

    List<Ievent> eventsDay1 = calendar.getEventsByDate(LocalDate.of(2025, 1, 15));
    List<Ievent> eventsDay2 = calendar.getEventsByDate(LocalDate.of(2025, 1, 16));
    List<Ievent> eventsDay3 = calendar.getEventsByDate(LocalDate.of(2025, 1, 17));

    assertEquals("Should have event on day 1", 1, eventsDay1.size());
    assertEquals("Should have event on day 2", 1, eventsDay2.size());
    assertEquals("Should have event on day 3", 1, eventsDay3.size());
    assertEquals("All events should be the same", event, eventsDay1.get(0));
    assertEquals("All events should be the same", event, eventsDay2.get(0));
    assertEquals("All events should be the same", event, eventsDay3.get(0));
  }

  @Test
  public void testRemoveEvent() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    Event event = Event.builder("Test Event", start, end).build();

    calendar.addEvent(event);
    calendar.removeEvent(event);

    List<Ievent> events = calendar.getEventsByDate(LocalDate.of(2025, 1, 15));
    assertTrue("Should have no events after removal", events.isEmpty());
  }

  @Test
  public void testRemoveMultiDayEvent() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 17, 11, 0);
    Event event = Event.builder("Multi Day Event", start, end).build();

    calendar.addEvent(event);
    calendar.removeEvent(event);

    List<Ievent> eventsDay1 = calendar.getEventsByDate(LocalDate.of(2025, 1, 15));
    List<Ievent> eventsDay2 = calendar.getEventsByDate(LocalDate.of(2025, 1, 16));
    List<Ievent> eventsDay3 = calendar.getEventsByDate(LocalDate.of(2025, 1, 17));

    assertTrue("Should have no events on day 1", eventsDay1.isEmpty());
    assertTrue("Should have no events on day 2", eventsDay2.isEmpty());
    assertTrue("Should have no events on day 3", eventsDay3.isEmpty());
  }

  @Test
  public void testGetEventsByDateNoEvents() {
    List<Ievent> events = calendar.getEventsByDate(LocalDate.of(2025, 1, 15));
    assertTrue("Should return empty list for no events", events.isEmpty());
  }

  @Test
  public void testGetEventsByRangeNoEvents() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 17, 0);

    List<Ievent> events = calendar.getEventsByRange(start, end);
    assertTrue("Should return empty list for no events", events.isEmpty());
  }

  @Test
  public void testGetEventsByRangeSameStartAndEnd() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    Event event = Event.builder("Event", start, end).build();

    calendar.addEvent(event);

    List<Ievent> events = calendar.getEventsByRange(start, start);
    assertEquals("Should find event with same start and end", 1, events.size());
  }

  @Test
  public void testGetEventsByRangeEventEndsBeforeRangeStarts() {
    LocalDateTime eventStart = LocalDateTime.of(2025, 1, 15, 9, 0);
    LocalDateTime eventEnd = LocalDateTime.of(2025, 1, 15, 10, 0);
    Event event = Event.builder("Early Event", eventStart, eventEnd).build();
    calendar.addEvent(event);

    LocalDateTime rangeStart = LocalDateTime.of(2025, 1, 15, 11, 0);
    LocalDateTime rangeEnd = LocalDateTime.of(2025, 1, 15, 12, 0);

    List<Ievent> events = calendar.getEventsByRange(rangeStart, rangeEnd);
    assertTrue("Range should not include event that ends before range starts", events.isEmpty());
  }

  @Test
  public void testGetEventsByRangeEventStartsAfterRangeEnds() {
    LocalDateTime eventStart = LocalDateTime.of(2025, 1, 15, 14, 0);
    LocalDateTime eventEnd = LocalDateTime.of(2025, 1, 15, 15, 0);
    Event event = Event.builder("Late Event", eventStart, eventEnd).build();
    calendar.addEvent(event);

    LocalDateTime rangeStart = LocalDateTime.of(2025, 1, 15, 12, 0);
    LocalDateTime rangeEnd = LocalDateTime.of(2025, 1, 15, 13, 0);

    List<Ievent> events = calendar.getEventsByRange(rangeStart, rangeEnd);
    assertTrue("Range should not include event that starts after range ends", events.isEmpty());
  }

  @Test
  public void testGetEventsByRangeMultiDayRange() {
    LocalDateTime eventStart = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime eventEnd = LocalDateTime.of(2025, 1, 15, 11, 0);
    Event event1 = Event.builder("Event 1", eventStart, eventEnd).build();

    LocalDateTime eventStart2 = LocalDateTime.of(2025, 1, 16, 14, 0);
    LocalDateTime eventEnd2 = LocalDateTime.of(2025, 1, 16, 15, 0);
    Event event2 = Event.builder("Event 2", eventStart2, eventEnd2).build();

    calendar.addEvent(event1);
    calendar.addEvent(event2);

    LocalDateTime rangeStart = LocalDateTime.of(2025, 1, 15, 9, 0);
    LocalDateTime rangeEnd = LocalDateTime.of(2025, 1, 16, 16, 0);

    List<Ievent> events = calendar.getEventsByRange(rangeStart, rangeEnd);
    assertEquals("Should find both events in multi-day range", 2, events.size());
  }

  @Test
  public void testGetAllEventsEmpty() {
    List<Ievent> events = calendar.getAllEvents();
    assertTrue("Should return empty list when no events", events.isEmpty());
  }

  @Test
  public void testGetAllEventsSortedByStartTime() {
    LocalDateTime start1 = LocalDateTime.of(2025, 1, 16, 10, 0);
    LocalDateTime end1 = LocalDateTime.of(2025, 1, 16, 11, 0);
    LocalDateTime start2 = LocalDateTime.of(2025, 1, 15, 9, 0);
    LocalDateTime end2 = LocalDateTime.of(2025, 1, 15, 10, 0);

    Event event1 = Event.builder("Event 1", start2, end2).build();
    Event event2 = Event.builder("Event 2", start1, end1).build();

    calendar.addEvent(event1);
    calendar.addEvent(event2);

    List<Ievent> events = calendar.getAllEvents();
    assertEquals("Should have 2 events", 2, events.size());
    assertEquals("First event should be Event 1", "Event 1", events.get(0).getSubject());
    assertEquals("Second event should be Event 2", "Event 2", events.get(1).getSubject());
  }

  @Test
  public void testFindEventNotFound() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    Event found = calendar.findEvent("Non-existent", start);
    assertNull("Should return null for non-existent event", found);
  }

  @Test
  public void testFindEventFound() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    Event event = Event.builder("Test Event", start, end).build();

    calendar.addEvent(event);

    Event found = calendar.findEvent("Test Event", start);
    assertNotNull("Should find the event", found);
    assertEquals("Found event should match", event, found);
  }

  @Test
  public void testFindEventWrongSubject() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    Event event = Event.builder("Test Event", start, end).build();

    calendar.addEvent(event);

    Event found = calendar.findEvent("Wrong Subject", start);
    assertNull("Should not find event with wrong subject", found);
  }

  @Test
  public void testFindEventWrongTime() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    Event event = Event.builder("Test Event", start, end).build();

    calendar.addEvent(event);

    LocalDateTime wrongTime = LocalDateTime.of(2025, 1, 15, 12, 0);
    Event found = calendar.findEvent("Test Event", wrongTime);
    assertNull("Should not find event with wrong time", found);
  }

  @Test
  public void testIsBusyNoEvents() {
    LocalDateTime dateTime = LocalDateTime.of(2025, 1, 15, 10, 0);
    assertFalse("Should not be busy when no events", calendar.isBusy(dateTime));
  }

  @Test
  public void testIsBusyAtEventStart() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    Event event = Event.builder("Meeting", start, end).build();

    calendar.addEvent(event);

    assertTrue("Should be busy at event start", calendar.isBusy(start));
  }

  @Test
  public void testIsBusyAtEventEnd() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    Event event = Event.builder("Meeting", start, end).build();

    calendar.addEvent(event);

    assertFalse("Should not be busy at event end", calendar.isBusy(end));
  }

  @Test
  public void testIsBusyJustBeforeEvent() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    Event event = Event.builder("Meeting", start, end).build();

    calendar.addEvent(event);

    LocalDateTime justBefore = LocalDateTime.of(2025, 1, 15, 9, 59, 59);
    assertFalse("Should not be busy just before event", calendar.isBusy(justBefore));
  }

  @Test
  public void testIsBusyJustAfterEvent() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    Event event = Event.builder("Meeting", start, end).build();

    calendar.addEvent(event);

    LocalDateTime justAfter = LocalDateTime.of(2025, 1, 15, 11, 0, 1);
    assertFalse("Should not be busy just after event", calendar.isBusy(justAfter));
  }

  @Test
  public void testIsBusyWithOverlappingEvents() {
    LocalDateTime start1 = LocalDateTime.of(2025, 1, 15, 9, 0);
    LocalDateTime end1 = LocalDateTime.of(2025, 1, 15, 11, 0);
    LocalDateTime start2 = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end2 = LocalDateTime.of(2025, 1, 15, 12, 0);

    Event event1 = Event.builder("Meeting 1", start1, end1).build();
    Event event2 = Event.builder("Meeting 2", start2, end2).build();

    calendar.addEvent(event1);
    calendar.addEvent(event2);

    assertTrue("Should be busy during overlap",
        calendar.isBusy(LocalDateTime.of(2025, 1, 15, 10, 30)));
  }

  @Test
  public void testContainsKeyFalse() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    EventKey key = new EventKey("Test Event", start, end);

    assertFalse("Should not contain key for non-existent event", calendar.containsKey(key));
  }

  @Test
  public void testContainsKeyTrue() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    Event event = Event.builder("Test Event", start, end).build();
    EventKey key = new EventKey("Test Event", start, end);

    calendar.addEvent(event);

    assertTrue("Should contain key for existing event", calendar.containsKey(key));
  }

  @Test
  public void testContainsKeyAfterRemoval() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    Event event = Event.builder("Test Event", start, end).build();
    EventKey key = new EventKey("Test Event", start, end);

    calendar.addEvent(event);
    calendar.removeEvent(event);

    assertFalse("Should not contain key after event removal", calendar.containsKey(key));
  }

  @Test
  public void testGetEventsByDateReturnsModifiableList() {
    List<Ievent> events = calendar.getEventsByDate(LocalDate.of(2025, 1, 15));

    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    Event event = Event.builder("Test", start, end).build();

    events.add(event);

    assertEquals("Should be able to modify returned list", 1, events.size());
  }

  @Test
  public void testGetEventsByRangeSortsByStartTime() {
    LocalDateTime start1 = LocalDateTime.of(2025, 1, 15, 14, 0);
    LocalDateTime end1 = LocalDateTime.of(2025, 1, 15, 15, 0);
    LocalDateTime start2 = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end2 = LocalDateTime.of(2025, 1, 15, 11, 0);

    Event event1 = Event.builder("Late Event", start1, end1).build();
    Event event2 = Event.builder("Early Event", start2, end2).build();

    calendar.addEvent(event1);
    calendar.addEvent(event2);

    LocalDateTime rangeStart = LocalDateTime.of(2025, 1, 15, 9, 0);
    LocalDateTime rangeEnd = LocalDateTime.of(2025, 1, 15, 16, 0);

    List<Ievent> events = calendar.getEventsByRange(rangeStart, rangeEnd);

    assertEquals("Should have 2 events", 2, events.size());
    assertEquals("First event should be Early Event", "Early Event", events.get(0).getSubject());
    assertEquals("Second event should be Late Event", "Late Event", events.get(1).getSubject());
  }
}