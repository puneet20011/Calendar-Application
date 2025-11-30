package calendar.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import calendar.model.Event;
import calendar.model.Ievent;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for IcalExporter class functionality.
 * Tests iCalendar format export operations.
 */
public class IcalExporterTest {

  private List<Ievent> events;
  private String timezone;

  /**
   * Sets up the test fixture.
   */
  @Before
  public void setUp() {
    events = new ArrayList<>();
    timezone = "America/New_York";
  }

  /**
   * Resets captured logs after each test.
   */
  @After
  public void tearDown() {
    events = null;
    timezone = null;
  }

  private Event createEvent(String subject, LocalDateTime start, LocalDateTime end,
                            String description, String location, String status) throws Exception {
    Class<?> eventSeriesClass = Class.forName("calendar.model.EventSeries");
    Constructor<Event> constructor = Event.class.getDeclaredConstructor(
        String.class, LocalDateTime.class, LocalDateTime.class,
        eventSeriesClass, String.class, String.class, String.class);
    constructor.setAccessible(true);
    return constructor.newInstance(subject, start, end, null, description, location, status);
  }

  @Test
  public void testExportEmptyCalendar() throws Exception {
    IcalExporter exporter = new IcalExporter(events, timezone);
    String filename = "empty_calendar.ics";
    String filepath = exporter.exportToIcal(filename);

    assertTrue("File should exist", Files.exists(Paths.get(filepath)));

    String content = Files.readString(Paths.get(filepath));
    assertTrue("Should have calendar header",
        content.contains("BEGIN:VCALENDAR"));
    assertTrue("Should have version", content.contains("VERSION:2.0"));
    assertTrue("Should have calendar footer",
        content.contains("END:VCALENDAR"));
    assertTrue("Should have timezone info",
        content.contains("BEGIN:VTIMEZONE"));

    Files.deleteIfExists(Paths.get(filepath));
  }

  @Test
  public void testExportSingleEvent() throws Exception {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    events.add(createEvent("Test Event", start, end, null, null, "public"));

    IcalExporter exporter = new IcalExporter(events, timezone);
    String filename = "single_event.ics";
    String filepath = exporter.exportToIcal(filename);

    String content = Files.readString(Paths.get(filepath));

    assertTrue("Should have event", content.contains("BEGIN:VEVENT"));
    assertTrue("Should have event end", content.contains("END:VEVENT"));
    assertTrue("Should have summary", content.contains("SUMMARY:Test Event"));
    assertTrue("Should have start time", content.contains("DTSTART"));
    assertTrue("Should have end time", content.contains("DTEND"));
    assertTrue("Should have UID", content.contains("UID:"));
    assertTrue("Should have timestamp", content.contains("DTSTAMP:"));

    Files.deleteIfExists(Paths.get(filepath));
  }

  @Test
  public void testExportMultipleEvents() throws Exception {
    LocalDateTime start1 = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end1 = LocalDateTime.of(2025, 1, 15, 11, 0);
    LocalDateTime start2 = LocalDateTime.of(2025, 1, 16, 14, 0);
    LocalDateTime end2 = LocalDateTime.of(2025, 1, 16, 15, 0);

    events.add(createEvent("Event 1", start1, end1, null, null, "public"));
    events.add(createEvent("Event 2", start2, end2, null, null, "public"));

    IcalExporter exporter = new IcalExporter(events, timezone);
    String filename = "multiple_events.ics";
    String filepath = exporter.exportToIcal(filename);

    String content = Files.readString(Paths.get(filepath));

    int eventCount = content.split("BEGIN:VEVENT").length - 1;
    assertEquals("Should have 2 events", 2, eventCount);

    assertTrue("Should have Event 1", content.contains("SUMMARY:Event 1"));
    assertTrue("Should have Event 2", content.contains("SUMMARY:Event 2"));

    Files.deleteIfExists(Paths.get(filepath));
  }

  @Test
  public void testExportEventWithDescription() throws Exception {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    events.add(createEvent("Test Event", start, end,
        "This is a test description", null, "public"));

    IcalExporter exporter = new IcalExporter(events, timezone);
    String filename = "event_with_description.ics";
    String filepath = exporter.exportToIcal(filename);

    String content = Files.readString(Paths.get(filepath));
    assertTrue("Should have description",
        content.contains("DESCRIPTION:This is a test description"));

    Files.deleteIfExists(Paths.get(filepath));
  }

  @Test
  public void testExportPrivateEvent() throws Exception {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    events.add(createEvent("Private Event", start, end, null, null, "private"));

    IcalExporter exporter = new IcalExporter(events, timezone);
    String filename = "private_event.ics";
    String filepath = exporter.exportToIcal(filename);

    String content = Files.readString(Paths.get(filepath));
    assertTrue("Should have private class",
        content.contains("CLASS:PRIVATE"));

    Files.deleteIfExists(Paths.get(filepath));
  }

  @Test
  public void testExportContainsRequiredIcalHeaders() throws Exception {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    events.add(createEvent("Test", start, end, null, null, "public"));

    IcalExporter exporter = new IcalExporter(events, timezone);
    String filename = "test_headers.ics";
    String filepath = exporter.exportToIcal(filename);

    String content = Files.readString(Paths.get(filepath));

    assertTrue("Should contain PRODID", content.contains("PRODID:-//Calendar Application//EN"));
    assertTrue("Should contain CALSCALE", content.contains("CALSCALE:GREGORIAN"));
    assertTrue("Should contain METHOD", content.contains("METHOD:PUBLISH"));

    Files.deleteIfExists(Paths.get(filepath));
  }

  @Test
  public void testExportEventWithNullDescription() throws Exception {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    events.add(createEvent("Test Event", start, end, null, "Location", "public"));

    IcalExporter exporter = new IcalExporter(events, timezone);
    String filename = "event_null_description.ics";
    String filepath = exporter.exportToIcal(filename);

    String content = Files.readString(Paths.get(filepath));
    assertFalse("Should not have DESCRIPTION field", content.contains("DESCRIPTION:"));
    assertTrue("Should still have location", content.contains("LOCATION:Location"));

    Files.deleteIfExists(Paths.get(filepath));
  }

  @Test
  public void testExportEventWithEmptyDescription() throws Exception {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    events.add(createEvent("Test Event", start, end, "", "Location", "public"));

    IcalExporter exporter = new IcalExporter(events, timezone);
    String filename = "event_empty_description.ics";
    String filepath = exporter.exportToIcal(filename);

    String content = Files.readString(Paths.get(filepath));
    assertFalse("Should not have DESCRIPTION field for empty string",
        content.contains("DESCRIPTION:"));

    Files.deleteIfExists(Paths.get(filepath));
  }

  @Test
  public void testExportEventWithNullLocation() throws Exception {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    events.add(createEvent("Test Event", start, end, "Description", null, "public"));

    IcalExporter exporter = new IcalExporter(events, timezone);
    String filename = "event_null_location.ics";
    String filepath = exporter.exportToIcal(filename);

    String content = Files.readString(Paths.get(filepath));
    assertFalse("Should not have LOCATION field", content.contains("LOCATION:"));
    assertTrue("Should still have description", content.contains("DESCRIPTION:Description"));

    Files.deleteIfExists(Paths.get(filepath));
  }

  @Test
  public void testExportEventWithEmptyLocation() throws Exception {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    events.add(createEvent("Test Event", start, end, "Description", "", "public"));

    IcalExporter exporter = new IcalExporter(events, timezone);
    String filename = "event_empty_location.ics";
    String filepath = exporter.exportToIcal(filename);

    String content = Files.readString(Paths.get(filepath));
    assertFalse("Should not have LOCATION field for empty string",
        content.contains("LOCATION:"));

    Files.deleteIfExists(Paths.get(filepath));
  }

  @Test
  public void testExportTimezoneBlockExactFormat() throws Exception {
    events = new ArrayList<>();
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    events.add(createEvent("Test", start, end, null, null, "public"));

    String testTimezone = "America/Los_Angeles";
    IcalExporter exporter = new IcalExporter(events, testTimezone);
    String filename = "test_tz_exact.ics";
    String filepath = exporter.exportToIcal(filename);

    String content = Files.readString(Paths.get(filepath));

    assertTrue("Must contain BEGIN:VTIMEZONE", content.contains("BEGIN:VTIMEZONE\r\n"));
    assertTrue("Must contain TZID with exact timezone",
        content.contains("TZID:" + testTimezone + "\r\n"));
    assertTrue("Must contain END:VTIMEZONE", content.contains("END:VTIMEZONE\r\n"));

    int beginVcalIndex = content.indexOf("BEGIN:VCALENDAR");
    int beginTzIndex = content.indexOf("BEGIN:VTIMEZONE");
    int tzIdIndex = content.indexOf("TZID:" + testTimezone);
    int endTzIndex = content.indexOf("END:VTIMEZONE");

    assertTrue("Timezone block should be after calendar start", beginTzIndex > beginVcalIndex);
    assertTrue("TZID should be after timezone start", tzIdIndex > beginTzIndex);
    assertTrue("Timezone end should be after TZID", endTzIndex > tzIdIndex);
    int beginEventIndex = content.indexOf("BEGIN:VEVENT");
    assertTrue("Event should be after timezone block", beginEventIndex > endTzIndex);

    String tzLine = content.substring(
        content.indexOf("TZID:"),
        content.indexOf("\r\n", content.indexOf("TZID:"))
    );
    assertEquals("TZID line must be exact", "TZID:" + testTimezone, tzLine);

    Files.deleteIfExists(Paths.get(filepath));
  }

  @Test
  public void testExportMultipleTimezonesHaveSameFormat() throws Exception {
    String[] timezones = {"UTC", "Asia/Tokyo", "Europe/Berlin"};

    for (String tz : timezones) {
      events = new ArrayList<>();
      LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
      LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
      events.add(createEvent("Test", start, end, null, null, "public"));

      IcalExporter exporter = new IcalExporter(events, tz);
      String filename = "test_tz_" + tz.replace("/", "_") + ".ics";
      String filepath = exporter.exportToIcal(filename);

      String content = Files.readString(Paths.get(filepath));

      String expectedTzId = "TZID:" + tz + "\r\n";
      assertTrue("Must contain exact TZID format for " + tz,
          content.contains(expectedTzId));

      assertTrue("Must have proper BEGIN for " + tz,
          content.contains("BEGIN:VTIMEZONE\r\n"));
      assertTrue("Must have proper END for " + tz,
          content.contains("END:VTIMEZONE\r\n"));

      Files.deleteIfExists(Paths.get(filepath));
    }
  }

  @Test
  public void testExportEventContainsStatusAndTransparency() throws Exception {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    events.add(createEvent("Test Event", start, end, null, null, "public"));

    IcalExporter exporter = new IcalExporter(events, timezone);
    String filename = "event_status_transp.ics";
    String filepath = exporter.exportToIcal(filename);

    String content = Files.readString(Paths.get(filepath));

    assertTrue("Must contain STATUS:CONFIRMED",
        content.contains("STATUS:CONFIRMED\r\n"));
    assertTrue("Must contain TRANSP:OPAQUE",
        content.contains("TRANSP:OPAQUE\r\n"));

    int eventStart = content.indexOf("BEGIN:VEVENT");
    int eventEnd = content.indexOf("END:VEVENT");
    int statusIndex = content.indexOf("STATUS:CONFIRMED");
    int transpIndex = content.indexOf("TRANSP:OPAQUE");

    assertTrue("STATUS must be inside event block",
        statusIndex > eventStart && statusIndex < eventEnd);
    assertTrue("TRANSP must be inside event block",
        transpIndex > eventStart && transpIndex < eventEnd);

    int statusCount = content.split("STATUS:CONFIRMED").length - 1;
    int transpCount = content.split("TRANSP:OPAQUE").length - 1;
    assertEquals("Should have exactly one STATUS", 1, statusCount);
    assertEquals("Should have exactly one TRANSP", 1, transpCount);

    Files.deleteIfExists(Paths.get(filepath));
  }

  @Test
  public void testExportMultipleEventsHaveStatusAndTransp() throws Exception {
    LocalDateTime start1 = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end1 = LocalDateTime.of(2025, 1, 15, 11, 0);
    LocalDateTime start2 = LocalDateTime.of(2025, 1, 16, 14, 0);
    LocalDateTime end2 = LocalDateTime.of(2025, 1, 16, 15, 0);

    events.add(createEvent("Event 1", start1, end1, null, null, "public"));
    events.add(createEvent("Event 2", start2, end2, null, null, "private"));

    IcalExporter exporter = new IcalExporter(events, timezone);
    String filename = "multiple_status_transp.ics";
    String filepath = exporter.exportToIcal(filename);

    String content = Files.readString(Paths.get(filepath));

    int statusCount = content.split("STATUS:CONFIRMED").length - 1;
    int transpCount = content.split("TRANSP:OPAQUE").length - 1;

    assertEquals("Should have STATUS for each event", 2, statusCount);
    assertEquals("Should have TRANSP for each event", 2, transpCount);

    Files.deleteIfExists(Paths.get(filepath));
  }

  @Test
  public void testExportPrivateEventHasStatusAndTransp() throws Exception {
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    events.add(createEvent("Private Event", start, end, null, null, "private"));

    IcalExporter exporter = new IcalExporter(events, timezone);
    String filename = "private_status_transp.ics";
    String filepath = exporter.exportToIcal(filename);

    String content = Files.readString(Paths.get(filepath));

    assertTrue("Private event must have STATUS:CONFIRMED",
        content.contains("STATUS:CONFIRMED\r\n"));
    assertTrue("Private event must have TRANSP:OPAQUE",
        content.contains("TRANSP:OPAQUE\r\n"));
    assertTrue("Private event should also have CLASS:PRIVATE",
        content.contains("CLASS:PRIVATE"));

    Files.deleteIfExists(Paths.get(filepath));
  }
}