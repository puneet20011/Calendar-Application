package calendar.controller;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import calendar.model.Event;
import java.lang.reflect.Constructor;
import java.time.LocalDateTime;
import org.junit.Test;

/**
 * Tests for ResponseFormatter utility class.
 */
public class ResponseFormatterTest {

  @Test
  public void testFormatEventWithPrivateStatus() throws Exception {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    Event event = createEvent("Private Meeting", start, end, "private");

    String formatted = ResponseFormatter.formatEvent(event);

    assertTrue("Should include status", formatted.contains(", status: private"));
  }

  @Test
  public void testFormatEventWithPublicStatus() throws Exception {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    Event event = createEvent("Public Meeting", start, end, "public");

    String formatted = ResponseFormatter.formatEvent(event);

    assertFalse("Should not include status for public events",
        formatted.contains(", status:"));
  }

  @Test
  public void testFormatEventWithNullStatus() throws Exception {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    Event event = createEvent("Meeting", start, end, null);

    String formatted = ResponseFormatter.formatEvent(event);

    assertFalse("Should not include status for null status",
        formatted.contains(", status:"));
  }

  @Test
  public void testFormatEventWithConfidentialStatus() throws Exception {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    Event event = createEvent("Confidential Meeting", start, end, "confidential");

    String formatted = ResponseFormatter.formatEvent(event);

    assertTrue("Should include non-public status",
        formatted.contains(", status: confidential"));
  }

  private Event createEvent(String subject, LocalDateTime start,
                            LocalDateTime end, String status) throws Exception {
    Class<?> eventSeriesClass = Class.forName("calendar.model.EventSeries");
    Constructor<Event> constructor = Event.class.getDeclaredConstructor(
        String.class, LocalDateTime.class, LocalDateTime.class,
        eventSeriesClass, String.class, String.class, String.class);
    constructor.setAccessible(true);
    return constructor.newInstance(subject, start, end, null, null, null, status);
  }

  @Test
  public void testFormatEventWithDescriptionOnly() throws Exception {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    Event event = createEventWithOptionalFields("Meeting", start, end,
        "Important meeting", null, "public");

    String formatted = ResponseFormatter.formatEvent(event);

    assertTrue("Should include description",
        formatted.contains(", description: Important meeting"));
    assertFalse("Should not include location",
        formatted.contains(", location:"));
  }

  @Test
  public void testFormatEventWithLocationOnly() throws Exception {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    Event event = createEventWithOptionalFields("Meeting", start, end,
        null, "Room 101", "public");

    String formatted = ResponseFormatter.formatEvent(event);

    assertTrue("Should include location",
        formatted.contains(", location: Room 101"));
    assertFalse("Should not include description",
        formatted.contains(", description:"));
  }

  @Test
  public void testFormatEventWithEmptyDescription() throws Exception {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    Event event = createEventWithOptionalFields("Meeting", start, end,
        "", "Room 101", "public");

    String formatted = ResponseFormatter.formatEvent(event);

    assertFalse("Should not include empty description",
        formatted.contains(", description:"));
    assertTrue("Should include location",
        formatted.contains(", location: Room 101"));
  }

  @Test
  public void testFormatEventWithEmptyLocation() throws Exception {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    Event event = createEventWithOptionalFields("Meeting", start, end,
        "Important", "", "public");

    String formatted = ResponseFormatter.formatEvent(event);

    assertTrue("Should include description",
        formatted.contains(", description: Important"));
    assertFalse("Should not include empty location",
        formatted.contains(", location:"));
  }

  @Test
  public void testFormatEventWithNullDescriptionAndLocation() throws Exception {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    Event event = createEventWithOptionalFields("Meeting", start, end,
        null, null, "public");

    String formatted = ResponseFormatter.formatEvent(event);

    assertFalse("Should not include description",
        formatted.contains(", description:"));
    assertFalse("Should not include location",
        formatted.contains(", location:"));
  }

  @Test
  public void testFormatEventWithBothDescriptionAndLocation() throws Exception {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    Event event = createEventWithOptionalFields("Meeting", start, end,
        "Important meeting", "Room 101", "public");

    String formatted = ResponseFormatter.formatEvent(event);

    assertTrue("Should include description",
        formatted.contains(", description: Important meeting"));
    assertTrue("Should include location",
        formatted.contains(", location: Room 101"));
  }

  // Helper method to create events with optional fields
  private Event createEventWithOptionalFields(String subject, LocalDateTime start,
                                              LocalDateTime end, String description,
                                              String location, String status) throws Exception {
    Class<?> eventSeriesClass = Class.forName("calendar.model.EventSeries");
    Constructor<Event> constructor = Event.class.getDeclaredConstructor(
        String.class, LocalDateTime.class, LocalDateTime.class,
        eventSeriesClass, String.class, String.class, String.class);
    constructor.setAccessible(true);
    return constructor.newInstance(subject, start, end, null, description, location, status);
  }
}