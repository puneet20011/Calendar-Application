package calendar.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for CalendarContext class functionality.
 * Tests calendar metadata management and model wrapping.
 */
public class CalendarContextTest {

  private CalendarContext context;
  private ZoneId timezone;

  /**
   * Sets up the test fixture.
   */
  @Before
  public void setUp() {
    timezone = ZoneId.of("America/New_York");
    context = new CalendarContext("work", timezone);
  }

  @Test
  public void testConstructor() {
    assertNotNull("Context should not be null", context);
    assertEquals("Name should match", "work", context.getName());
    assertEquals("Timezone should match", timezone, context.getTimezone());
    assertNotNull("Model should be initialized", context.getModel());
  }

  @Test
  public void testGetName() {
    assertEquals("work", context.getName());
  }

  @Test
  public void testSetName() {
    context.setName("personal");
    assertEquals("Name should be updated", "personal", context.getName());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetNameNull() {
    context.setName(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetNameEmpty() {
    context.setName("");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetNameWhitespace() {
    context.setName("   ");
  }

  @Test
  public void testSetNameWithWhitespace() {
    context.setName("  valid name  ");
    assertEquals("Should trim whitespace", "  valid name  ", context.getName());
  }

  @Test
  public void testGetTimezone() {
    assertEquals(timezone, context.getTimezone());
  }

  @Test
  public void testSetTimezone() {
    ZoneId newTimezone = ZoneId.of("Europe/London");
    context.setTimezone(newTimezone);
    assertEquals("Timezone should be updated", newTimezone, context.getTimezone());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetTimezoneNull() {
    context.setTimezone(null);
  }

  @Test
  public void testSetTimezoneMultipleTimes() {
    ZoneId tz1 = ZoneId.of("Asia/Tokyo");
    ZoneId tz2 = ZoneId.of("Europe/Paris");

    context.setTimezone(tz1);
    assertEquals(tz1, context.getTimezone());

    context.setTimezone(tz2);
    assertEquals(tz2, context.getTimezone());

    ZoneId tz3 = ZoneId.of("America/Los_Angeles");
    context.setTimezone(tz3);
    assertEquals(tz3, context.getTimezone());
  }

  @Test
  public void testGetModel() {
    IcalendarModel model = context.getModel();
    assertNotNull("Model should not be null", model);
  }

  @Test
  public void testModelIsSameInstance() {
    IcalendarModel model1 = context.getModel();
    IcalendarModel model2 = context.getModel();
    assertEquals("Should return same model instance", model1, model2);
  }

  @Test
  public void testModelIsUsable() {
    IcalendarModel model = context.getModel();

    // Test that the model can perform basic operations
    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);

    model.createEvent("Test Event", start, end);

    List<Ievent> events = model.getByDate(LocalDate.of(2025, 1, 15));
    assertEquals("Should have one event", 1, events.size());
    assertEquals("Event subject should match", "Test Event", events.get(0).getSubject());
  }

  @Test
  public void testMultipleContextsHaveIndependentModels() {
    CalendarContext context1 = new CalendarContext("calendar1", ZoneId.of("UTC"));
    CalendarContext context2 = new CalendarContext("calendar2", ZoneId.of("UTC"));

    LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 15, 11, 0);

    context1.getModel().createEvent("Event 1", start, end);
    context2.getModel().createEvent("Event 2", start, end);

    List<Ievent> events1 = context1.getModel().getByDate(LocalDate.of(2025, 1, 15));
    List<Ievent> events2 = context2.getModel().getByDate(LocalDate.of(2025, 1, 15));

    assertEquals("Context 1 should have 1 event", 1, events1.size());
    assertEquals("Context 2 should have 1 event", 1, events2.size());
    assertEquals("Event 1 subject", "Event 1", events1.get(0).getSubject());
    assertEquals("Event 2 subject", "Event 2", events2.get(0).getSubject());
  }

  @Test
  public void testConstructorWithDifferentTimezones() {
    CalendarContext utcContext = new CalendarContext("utc", ZoneId.of("UTC"));
    CalendarContext estContext = new CalendarContext("est", ZoneId.of("America/New_York"));
    CalendarContext jstContext = new CalendarContext("jst", ZoneId.of("Asia/Tokyo"));

    assertEquals(ZoneId.of("UTC"), utcContext.getTimezone());
    assertEquals(ZoneId.of("America/New_York"), estContext.getTimezone());
    assertEquals(ZoneId.of("Asia/Tokyo"), jstContext.getTimezone());
  }

  @Test
  public void testSetNameDoesNotAffectOtherContexts() {
    CalendarContext context1 = new CalendarContext("original", timezone);
    CalendarContext context2 = new CalendarContext("original", timezone);

    context1.setName("modified");

    assertEquals("Context 1 should be modified", "modified", context1.getName());
    assertEquals("Context 2 should remain original", "original", context2.getName());
  }

  @Test
  public void testSetTimezoneDoesNotAffectOtherContexts() {
    ZoneId timezone1 = ZoneId.of("UTC");
    CalendarContext context1 = new CalendarContext("cal1", timezone1);
    CalendarContext context2 = new CalendarContext("cal2", timezone1);

    ZoneId newTimezone = ZoneId.of("Asia/Tokyo");
    context1.setTimezone(newTimezone);

    assertEquals("Context 1 should have new timezone", newTimezone, context1.getTimezone());
    assertEquals("Context 2 should have original timezone", timezone1, context2.getTimezone());
  }
}