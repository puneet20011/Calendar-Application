package calendar.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for SeriesEditor class functionality.
 * Tests series editing operations with different handling strategies.
 */
public class SeriesEditorTest {

  private EventCalendar calendar;
  private SystemValidator validator;
  private EventUpdater propertyUpdater;
  private SeriesEditor seriesEditor;

  /**
   * Sets up the test fixture.
   */
  @Before
  public void setUp() {
    calendar = new EventCalendar();
    validator = new SystemValidator(calendar);
    propertyUpdater = new EventUpdater(validator);
    seriesEditor = new SeriesEditor(calendar, validator, propertyUpdater);
  }

  /**
   * Tears down the test fixture.
   */
  @After
  public void tearDown() {
    calendar = null;
    validator = null;
    propertyUpdater = null;
    seriesEditor = null;
  }

  @Test
  public void testEditEmptyEventList() {
    List<Event> emptyList = new ArrayList<>();

    seriesEditor.editMultiple(emptyList, "subject", "New Subject", false);
    seriesEditor.editAll(emptyList, "subject", "New Subject", false);
  }

  @Test
  public void testAllThreePaths() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);

    CalendarModel model = new CalendarModel();

    model.createEventSeries("Path1", start, end, "W", 2, null);
    Event event1 = (Event) model.getByDate(LocalDate.of(2025, 1, 15)).get(0);
    EventSeries series1 = event1.getSeries();

    model.editEvent("Path1", start, "location", "Location1");

    Event edited1 = (Event) model.getByDate(LocalDate.of(2025, 1, 15)).get(0);
    assertEquals("Path1: should keep series", series1, edited1.getSeries());
    assertEquals("Path1: location updated", "Location1", edited1.getLocation());

    model.editEvents("Path1", start, "start", "2025-01-15T11:00");

    Event edited2 = (Event) model.getByDate(LocalDate.of(2025, 1, 15)).get(0);
    assertNotNull("Path2: should have new series", edited2.getSeries());
    assertFalse("Path2: should be different series", edited2.getSeries() == series1);

    model.createEventSeries("Path3",
        LocalDateTime.of(2025, 1, 16, 10, 0),
        LocalDateTime.of(2025, 1, 16, 11, 0), "W", 2, null);

    model.editEvent("Path3", LocalDateTime.of(2025, 1, 16, 10, 0),
        "start", "2025-01-16T14:00");

    Event edited3 = (Event) model.getByDate(LocalDate.of(2025, 1, 16)).get(0);
    assertNull("Path3: should have no series", edited3.getSeries());
  }

  @Test
  public void testKeepSeriesVerifyAddOccurrenceCalled() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);

    CalendarModel model = new CalendarModel();
    model.createEventSeries("Test", start, end, "W", 2, null);

    Event original = (Event) model.getByDate(LocalDate.of(2025, 1, 15)).get(0);
    EventSeries series = original.getSeries();

    int sizeBefore = series.getAllOccurrences().size();

    model.editEvent("Test", start, "status", "private");

    int sizeAfter = series.getAllOccurrences().size();

    assertEquals("Size should remain same", sizeBefore, sizeAfter);

    Event newEvent = (Event) model.getByDate(LocalDate.of(2025, 1, 15)).get(0);
    assertTrue("New event must be in series",
        series.getAllOccurrences().contains(newEvent));
  }

  @Test
  public void testNewSeriesVerifyAddOccurrenceCalled() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);

    CalendarModel model = new CalendarModel();
    model.createEventSeries("Test", start, end, "W", 3, null);

    model.editEvents("Test", start, "start", "2025-01-15T14:00");

    Event newEvent = (Event) model.getByDate(LocalDate.of(2025, 1, 15)).get(0);
    EventSeries newSeries = newEvent.getSeries();

    assertNotNull("Must have new series", newSeries);
    assertTrue("New event must be in new series occurrences",
        newSeries.getAllOccurrences().contains(newEvent));
    assertTrue("New series should have occurrences",
        newSeries.getAllOccurrences().size() > 0);
  }

  @Test
  public void testElsePathRemovesFromOldSeriesOnly() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);

    CalendarModel model = new CalendarModel();
    model.createEventSeries("Test", start, end, "W", 3, null);

    Event original = (Event) model.getByDate(LocalDate.of(2025, 1, 15)).get(0);
    EventSeries oldSeries = original.getSeries();
    int oldSize = oldSeries.getAllOccurrences().size();

    model.editEvent("Test", start, "start", "2025-01-15T14:00");

    assertEquals("Old series should lose one occurrence",
        oldSize - 1, oldSeries.getAllOccurrences().size());

    Event edited = (Event) model.getByDate(LocalDate.of(2025, 1, 15)).get(0);
    assertNull("Edited event should have no series", edited.getSeries());
  }

  @Test
  public void testKeepSeriesPathExecutesRemoveAndAdd() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);

    CalendarModel model = new CalendarModel();
    model.createEventSeries("Test", start, end, "W", 2, null);

    EventSeries series = ((Event) model.getByDate(LocalDate.of(2025, 1, 15)).get(0)).getSeries();
    int sizeBefore = series.getAllOccurrences().size();

    model.editEvent("Test", start, "description", "Updated");

    int sizeAfter = series.getAllOccurrences().size();

    assertEquals("Size proves remove and add both executed", sizeBefore, sizeAfter);

    Event newEvent = (Event) model.getByDate(LocalDate.of(2025, 1, 15)).get(0);
    assertEquals("Should have updated description", "Updated", newEvent.getDescription());
    assertTrue("New event should be in series", series.getAllOccurrences().contains(newEvent));
  }

  @Test
  public void testNewSeriesPathRemovesFromOldSeries() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);

    CalendarModel model = new CalendarModel();
    model.createEventSeries("Series", start, end, "W", 3, null);

    EventSeries oldSeries = ((Event) model.getByDate(LocalDate.of(2025, 1, 15)).get(0)).getSeries();
    int oldSize = oldSeries.getAllOccurrences().size();

    model.editSeries("Series", start, "start", "2025-01-15T14:00");

    int newOldSize = oldSeries.getAllOccurrences().size();
    assertTrue("Old series should have fewer or no occurrences", newOldSize < oldSize);
  }

  @Test
  public void testElsePathRemovesFromOldSeriesWhenPresent() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);

    CalendarModel model = new CalendarModel();
    model.createEventSeries("Test", start, end, "W", 3, null);

    EventSeries series = ((Event) model.getByDate(LocalDate.of(2025, 1, 15)).get(0)).getSeries();
    int sizeBefore = series.getAllOccurrences().size();

    model.editEvent("Test", start, "start", "2025-01-15T14:00");

    int sizeAfter = series.getAllOccurrences().size();

    assertEquals("Should have removed one occurrence", sizeBefore - 1, sizeAfter);

    Event edited = (Event) model.getByDate(LocalDate.of(2025, 1, 15)).get(0);
    assertNull("Edited event should not be in series", edited.getSeries());
  }

  @Test
  public void testEditMultipleEventsKeepSeriesVerifySeriesAssociation() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);

    CalendarModel model = new CalendarModel();
    model.createEventSeries("Meeting", start, end, "W", 2, null);

    Event originalEvent = (Event) model.getByDate(LocalDate.of(2025, 1, 15)).get(0);
    EventSeries originalSeries = originalEvent.getSeries();
    assertNotNull("Original event should have series", originalSeries);

    model.editEvent("Meeting", start, "location", "Room 101");

    Event editedEvent = (Event) model.getByDate(LocalDate.of(2025, 1, 15)).get(0);
    assertNotNull("Edited event should still have series", editedEvent.getSeries());
    assertEquals("Should be the same series", originalSeries, editedEvent.getSeries());
    assertEquals("Location should be updated", "Room 101", editedEvent.getLocation());
  }

  @Test
  public void testEditMultipleEventsKeepSeriesWithMultipleOccurrences() {
    LocalDateTime start1 = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime start2 = LocalDateTime.of(2025, 1, 22, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);

    CalendarModel model = new CalendarModel();
    model.createEventSeries("Weekly", start1, end, "W", 3, null);

    Event firstEvent = (Event) model.getByDate(LocalDate.of(2025, 1, 15)).get(0);
    Event secondEvent = (Event) model.getByDate(LocalDate.of(2025, 1, 22)).get(0);
    EventSeries series = firstEvent.getSeries();

    assertEquals("Series should have 3 occurrences initially", 3,
        series.getAllOccurrences().size());

    model.editEvent("Weekly", start1, "status", "private");

    assertEquals("Series should still have 3 occurrences", 3,
        series.getAllOccurrences().size());

    Event editedFirst = (Event) model.getByDate(LocalDate.of(2025, 1, 15)).get(0);
    assertEquals("First occurrence status should be private", "private",
        editedFirst.getStatus());
    assertNotNull("First occurrence should still be in series",
        editedFirst.getSeries());

    Event unchangedSecond = (Event) model.getByDate(LocalDate.of(2025, 1, 22)).get(0);
    assertEquals("Second occurrence status should be public", "public",
        unchangedSecond.getStatus());
    assertNotNull("Second occurrence should still be in series",
        unchangedSecond.getSeries());
  }

  @Test
  public void testEditAllEventsKeepSeriesPath() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);

    CalendarModel model = new CalendarModel();
    model.createEventSeries("Daily", start, end, "W", 3, null);

    Event firstEvent = (Event) model.getByDate(LocalDate.of(2025, 1, 15)).get(0);
    EventSeries originalSeries = firstEvent.getSeries();

    model.editSeries("Daily", start, "description", "All updated");

    List<Ievent> allEvents = model.getByRange(
        LocalDateTime.of(2025, 1, 1, 0, 0),
        LocalDateTime.of(2025, 2, 1, 0, 0)
    );

    assertEquals("Should still have 3 events", 3, allEvents.size());

    for (Ievent ievent : allEvents) {
      Event event = (Event) ievent;
      assertEquals("All should have updated description", "All updated",
          event.getDescription());
      assertNotNull("All should still be in series", event.getSeries());
      assertEquals("All should be in same series", originalSeries,
          event.getSeries());
    }
  }

  @Test
  public void testKeepSeriesWithOldSeriesNotNull() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);

    CalendarModel model = new CalendarModel();
    model.createEventSeries("Test", start, end, "W", 2, null);

    Event event = (Event) model.getByDate(LocalDate.of(2025, 1, 15)).get(0);
    EventSeries oldSeries = event.getSeries();

    assertNotNull("Old series should not be null", oldSeries);

    int countBefore = oldSeries.getAllOccurrences().size();
    assertEquals("Should have 2 occurrences before", 2, countBefore);

    model.editEvent("Test", start, "location", "New Location");

    int countAfter = oldSeries.getAllOccurrences().size();
    assertEquals("Should still have 2 occurrences after", 2, countAfter);

    Event edited = (Event) model.getByDate(LocalDate.of(2025, 1, 15)).get(0);
    assertTrue("Edited event should be in series occurrences",
        oldSeries.getAllOccurrences().contains(edited));
  }

  @Test
  public void testNewEventKeepSeriesPathFinalEventIsNewEvent() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);

    CalendarModel model = new CalendarModel();
    model.createEventSeries("Test", start, end, "W", 2, null);

    Event before = (Event) model.getByDate(LocalDate.of(2025, 1, 15)).get(0);
    String originalDescription = before.getDescription();

    model.editEvent("Test", start, "description", "Updated");

    Event after = (Event) model.getByDate(LocalDate.of(2025, 1, 15)).get(0);

    assertFalse("Should be different event objects", before == after);
    assertEquals("Should have updated description", "Updated", after.getDescription());
    assertNotNull("Should still have series", after.getSeries());
  }

  @Test
  public void testNewEventNewSeriesPathWithOldSeries() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);

    CalendarModel model = new CalendarModel();
    model.createEventSeries("Original", start, end, "W", 3, null);

    Event originalEvent = (Event) model.getByDate(LocalDate.of(2025, 1, 15)).get(0);
    EventSeries oldSeries = originalEvent.getSeries();
    assertNotNull("Must have old series", oldSeries);
    assertEquals("Old series should have 3", 3, oldSeries.getAllOccurrences().size());

    model.editSeries("Original", start, "start", "2025-01-15T14:00");

    Event newEvent = (Event) model.getByDate(LocalDate.of(2025, 1, 15)).get(0);
    assertNotNull("New event should have series", newEvent.getSeries());

    EventSeries newSeries = newEvent.getSeries();

    assertTrue("Should have new series with occurrences",
        newSeries.getAllOccurrences().size() > 0);
    assertTrue("New event should be in new series",
        newSeries.getAllOccurrences().contains(newEvent));
  }

  @Test
  public void testNewEventNewSeriesPathBuildsEventWithNewSeries() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);

    CalendarModel model = new CalendarModel();
    model.createEventSeries("Series", start, end, "W", 3, null);

    Event before = (Event) model.getByDate(LocalDate.of(2025, 1, 15)).get(0);
    EventSeries oldSeries = before.getSeries();

    model.editEvents("Series", start, "start", "2025-01-15T11:00");

    Event after = (Event) model.getByDate(LocalDate.of(2025, 1, 15)).get(0);
    EventSeries newSeries = after.getSeries();

    assertNotNull("Should have new series", newSeries);
    assertFalse("Should be different series object", oldSeries == newSeries);
    assertTrue("New event should be in new series",
        newSeries.getAllOccurrences().contains(after));
  }

  @Test
  public void testNewEventNewSeriesPathWithNullOldSeries() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);

    Event standalone = Event.builder("Standalone", start, end).build();
    calendar.addEvent(standalone);

    assertNull("Should have no series initially", standalone.getSeries());

    List<Event> eventsToEdit = new ArrayList<>();
    eventsToEdit.add(standalone);

    seriesEditor.editMultiple(eventsToEdit, "start", "2025-01-15T14:00", true);

    Event edited = calendar.findEvent("Standalone", LocalDateTime.of(2025, 1, 15, 14, 0));
    assertNotNull("Event should exist at new time", edited);
  }
}