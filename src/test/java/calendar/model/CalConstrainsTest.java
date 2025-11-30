package calendar.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import org.junit.Test;

/**
 * Tests for CalConstrains constant definitions and utility values.
 */
public class CalConstrainsTest {

  @Test
  public void testAllDayStartTime() throws Exception {
    Field field = CalConstrains.class.getDeclaredField("ALL_DAY_START_TIME");
    assertNotNull(field);
    assertTrue(Modifier.isStatic(field.getModifiers()));
    assertTrue(Modifier.isFinal(field.getModifiers()));

    LocalTime allDayStart = (LocalTime) field.get(null);
    assertEquals(LocalTime.of(8, 0), allDayStart);
  }

  @Test
  public void testAllDayEndTime() throws Exception {
    Field field = CalConstrains.class.getDeclaredField("ALL_DAY_END_TIME");
    assertNotNull(field);
    assertTrue(Modifier.isStatic(field.getModifiers()));
    assertTrue(Modifier.isFinal(field.getModifiers()));

    LocalTime allDayEnd = (LocalTime) field.get(null);
    assertEquals(LocalTime.of(17, 0), allDayEnd);
  }

  @Test
  public void testDateTimeFormatter() throws Exception {
    Field field = CalConstrains.class.getDeclaredField("DATE_TIME_FORMATTER");
    assertNotNull(field);
    assertTrue(Modifier.isStatic(field.getModifiers()));
    assertTrue(Modifier.isFinal(field.getModifiers()));

    DateTimeFormatter formatter = (DateTimeFormatter) field.get(null);
    assertNotNull(formatter);

    LocalDateTime dateTime = LocalDateTime.of(2025, 1, 15, 10, 30);
    assertEquals("2025-01-15T10:30", dateTime.format(formatter));
  }

  @Test
  public void testMaxSeriesOccurrences() throws Exception {
    Field field = CalConstrains.class.getDeclaredField("MAX_SERIES_OCCURRENCES");
    assertNotNull(field);
    assertTrue(Modifier.isStatic(field.getModifiers()));
    assertTrue(Modifier.isFinal(field.getModifiers()));

    int maxOccurrences = (int) field.get(null);
    assertEquals(365, maxOccurrences);
  }
}
