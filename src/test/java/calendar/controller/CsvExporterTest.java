package calendar.controller;

import static org.junit.Assert.assertEquals;
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
import org.junit.Test;

/**
 * Tests for CsvExporter class functionality.
 * Tests CSV export operations, formatting, and edge cases.
 */
public class CsvExporterTest {

  private List<Ievent> events;

  /**
   * Resets captured logs after each test.
   */
  @After
  public void tearDown() {
    events = null;
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
  public void testExportCsvEmptyCalendar() throws Exception {
    events = new ArrayList<>();
    CsvExporter exporter = new CsvExporter();

    String filename = "empty_calendar.csv";
    String filePath = exporter.exportToCsv(events, filename);

    assertTrue(Files.exists(Paths.get(filePath)));

    List<String> lines = Files.readAllLines(Paths.get(filePath));
    assertEquals("Should only have header", 1, lines.size());
    assertTrue("Header should be correct", lines.get(0).startsWith("Subject,Start Date"));

    Files.deleteIfExists(Paths.get(filePath));
  }

  @Test
  public void testWriteCsvWithAllDayEvent() throws Exception {
    events = new ArrayList<>();
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 8, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 17, 0);
    events.add(createEvent("All Day Event", start, end, null, null, "public"));

    CsvExporter exporter = new CsvExporter();
    String filename = "test_all_day.csv";
    String filePath = exporter.exportToCsv(events, filename);

    assertTrue(Files.exists(Paths.get(filePath)));

    String content = Files.readString(Paths.get(filePath));
    assertTrue(content.contains("All Day Event"));
    assertTrue(content.contains("True"));

    Files.deleteIfExists(Paths.get(filePath));
  }

  @Test
  public void testWriteCsvWithCrossDayEvent() throws Exception {
    events = new ArrayList<>();
    events.add(createEvent("Cross Day Event",
        LocalDateTime.of(2025, 1, 15, 22, 0),
        LocalDateTime.of(2025, 1, 16, 2, 0),
        null, null, "public"));

    CsvExporter exporter = new CsvExporter();
    String filename = "test_cross_day.csv";
    String filePath = exporter.exportToCsv(events, filename);

    assertTrue(Files.exists(Paths.get(filePath)));

    String content = Files.readString(Paths.get(filePath));
    assertTrue(content.contains("Cross Day Event"));
    assertTrue(content.contains("False"));
    assertTrue(content.contains("01/15/2025,10:00 PM,01/16/2025,02:00 AM"));

    Files.deleteIfExists(Paths.get(filePath));
  }

  @Test
  public void testWriteCsvWithAllOptionalFields() throws Exception {
    events = new ArrayList<>();
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);

    events.add(createEvent("Full Event", start, end,
        "Test Description, with comma",
        "Test Location \"with quotes\"",
        "private"));

    CsvExporter exporter = new CsvExporter();
    String filename = "test_optional_fields.csv";
    String filePath = exporter.exportToCsv(events, filename);

    assertTrue(Files.exists(Paths.get(filePath)));

    String content = Files.readString(Paths.get(filePath));
    assertTrue(content.contains("Full Event"));
    assertTrue(content.contains("\"Test Description, with comma\""));
    assertTrue(content.contains("\"Test Location \"\"with quotes\"\"\""));
    assertTrue(content.trim().endsWith("True"));

    Files.deleteIfExists(Paths.get(filePath));
  }

  @Test
  public void testEscapeCsvWithNullValue() throws Exception {
    events = new ArrayList<>();
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);
    events.add(createEvent("Event", start, end, null, null, "public"));

    CsvExporter exporter = new CsvExporter();
    String filename = "test_null_escape.csv";
    String filePath = exporter.exportToCsv(events, filename);

    String content = Files.readString(Paths.get(filePath));
    assertTrue("Null fields should be empty", content.contains("False,,"));

    Files.deleteIfExists(Paths.get(filePath));
  }

  @Test
  public void testEscapeCsvWithEmptyValue() throws Exception {
    events = new ArrayList<>();
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);

    events.add(createEvent("Empty Test", start, end, "", "", "public"));

    CsvExporter exporter = new CsvExporter();
    String filename = "test_empty_escape.csv";
    String filePath = exporter.exportToCsv(events, filename);

    String content = Files.readString(Paths.get(filePath));
    assertTrue(content.contains("False,,"));

    Files.deleteIfExists(Paths.get(filePath));
  }

  @Test
  public void testEscapeCsvWithQuotes() throws Exception {
    events = new ArrayList<>();
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);

    events.add(createEvent("Event", start, end,
        "Value with \"quotes\"", null, "public"));

    CsvExporter exporter = new CsvExporter();
    String filename = "test_quotes_escape.csv";
    String filePath = exporter.exportToCsv(events, filename);

    String content = Files.readString(Paths.get(filePath));
    assertTrue("Quotes should be escaped",
        content.contains("\"Value with \"\"quotes\"\"\""));

    Files.deleteIfExists(Paths.get(filePath));
  }

  @Test
  public void testEscapeCsvWithNewline() throws Exception {
    events = new ArrayList<>();
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);

    events.add(createEvent("Event", start, end,
        "Value\nwith newline", null, "public"));

    CsvExporter exporter = new CsvExporter();
    String filename = "test_newline_escape.csv";
    String filePath = exporter.exportToCsv(events, filename);

    String content = Files.readString(Paths.get(filePath));
    assertTrue("Newline should cause quoting",
        content.contains("\"Value\nwith newline\""));

    Files.deleteIfExists(Paths.get(filePath));
  }

  @Test
  public void testEscapeCsvSimpleValue() throws Exception {
    events = new ArrayList<>();
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);

    events.add(createEvent("SimpleValue", start, end, null, null, "public"));

    CsvExporter exporter = new CsvExporter();
    String filename = "test_simple_escape.csv";
    String filePath = exporter.exportToCsv(events, filename);

    String content = Files.readString(Paths.get(filePath));
    assertTrue("Simple value should appear unquoted",
        content.contains("SimpleValue,"));

    Files.deleteIfExists(Paths.get(filePath));
  }

  @Test
  public void testWriteCsvAllDayFalseBranches() throws Exception {
    events = new ArrayList<>();
    LocalDateTime start1 = LocalDateTime.of(2025, 1, 15, 8, 0);
    LocalDateTime end1 = LocalDateTime.of(2025, 1, 15, 16, 0);
    events.add(createEvent("False End Time Event", start1, end1, null, null, "public"));

    LocalDateTime start2 = LocalDateTime.of(2025, 1, 16, 1, 0);
    LocalDateTime end2 = LocalDateTime.of(2025, 1, 17, 1, 0);
    events.add(createEvent("False Multi Day Event", start2, end2, null, null, "public"));

    CsvExporter exporter = new CsvExporter();
    String filename = "test_all_day_false.csv";
    String filePath = exporter.exportToCsv(events, filename);

    assertTrue(Files.exists(Paths.get(filePath)));

    String content = Files.readString(Paths.get(filePath));

    assertTrue("Event 1 must be False (Wrong End Time)",
        content.contains("False End Time Event,01/15/2025,08:00 AM,01/15/2025,04:00 PM,False,"));

    assertTrue("Event 2 must be False (Cross-Day)",
        content.contains("False Multi Day Event,01/16/2025,01:00 AM,01/17/2025,01:00 AM,False,"));

    Files.deleteIfExists(Paths.get(filePath));
  }

  @Test
  public void testWriteCsvWithEmptyStringDescription() throws Exception {
    events = new ArrayList<>();
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);

    events.add(createEvent("Empty Desc Test", start, end, "", "Location", "public"));

    CsvExporter exporter = new CsvExporter();
    String filename = "test_empty_description.csv";
    String filePath = exporter.exportToCsv(events, filename);

    assertTrue(Files.exists(Paths.get(filePath)));

    String content = Files.readString(Paths.get(filePath));
    assertTrue("Empty string description should result in an empty field (,,) in the CSV.",
        content.contains("False,,Location"));

    Files.deleteIfExists(Paths.get(filePath));
  }

  @Test
  public void testWriteCsvWithEmptyStringLocation() throws Exception {
    events = new ArrayList<>();
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);

    events.add(createEvent("Empty Loc Test", start, end, "Description", "", "public"));

    CsvExporter exporter = new CsvExporter();
    String filename = "test_empty_location.csv";
    String filePath = exporter.exportToCsv(events, filename);

    assertTrue(Files.exists(Paths.get(filePath)));

    String content = Files.readString(Paths.get(filePath));
    assertTrue("Empty string location should result in an empty field (,,) in the CSV.",
        content.contains("Description,,False"));

    Files.deleteIfExists(Paths.get(filePath));
  }
}