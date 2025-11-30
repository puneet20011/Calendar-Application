package calendar.controller;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Test;

/**
 * Tests for CommandPatterns.
 */
public class CommandPatternsTest {

  private CommandPatterns.CommandPattern getPatternByName(String name) {
    List<CommandPatterns.CommandPattern> patterns = CommandPatterns.getAllPatterns();
    for (CommandPatterns.CommandPattern pattern : patterns) {
      if (pattern.getName().equals(name)) {
        return pattern;
      }
    }
    return null;
  }

  @Test
  public void testGetAllPatternsNotEmpty() {
    List<CommandPatterns.CommandPattern> patterns = CommandPatterns.getAllPatterns();
    assertNotNull(patterns);
    assertFalse(patterns.isEmpty());
  }

  @Test
  public void testCommandPatternConstructor() {
    Pattern pattern = Pattern.compile("sample");
    CommandPatterns.CommandPattern commandPattern =
        new CommandPatterns.CommandPattern("SAMPLE", pattern);

    assertTrue(commandPattern.getName().equals("SAMPLE"));
    assertTrue(commandPattern.getPattern() == pattern);
  }

  @Test
  public void testCreateCalendarPattern() {
    CommandPatterns.CommandPattern pattern = getPatternByName("CREATE_CALENDAR");
    assertNotNull("CREATE_CALENDAR pattern should be defined", pattern);

    Matcher matcher = pattern.getPattern()
        .matcher("create calendar --name work --timezone America/New_York");
    assertTrue("Valid command must match CREATE_CALENDAR pattern", matcher.matches());
  }

  @Test
  public void testUseCalendarPattern() {
    CommandPatterns.CommandPattern pattern = getPatternByName("USE_CALENDAR");
    assertNotNull(pattern);

    Matcher matcher = pattern.getPattern().matcher("use calendar --name personal");
    assertTrue(matcher.matches());
  }

  @Test
  public void testCopyEventPattern() {
    CommandPatterns.CommandPattern pattern = getPatternByName("COPY_EVENT");
    assertNotNull(pattern);

    Matcher matcher = pattern.getPattern().matcher(
        "copy event Project Demo on 2025-02-01T09:00 --target remote to 2025-02-02T09:00");
    assertTrue(matcher.matches());
  }

  @Test
  public void testCopyEventsRangePattern() {
    CommandPatterns.CommandPattern pattern = getPatternByName("COPY_EVENTS_RANGE");
    assertNotNull(pattern);

    Matcher matcher = pattern.getPattern().matcher(
        "copy events between 2025-02-01 and 2025-02-07 --target work to 2025-03-01");
    assertTrue(matcher.matches());
  }

  @Test
  public void testCreateEventPatternWithQuotes() {
    CommandPatterns.CommandPattern pattern = getPatternByName("CREATE_EVENT");
    assertNotNull(pattern);

    Matcher matcher = pattern.getPattern().matcher(
        "create event \"Team Sync\" from 2025-01-15T10:00 to 2025-01-15T11:00");
    assertTrue(matcher.matches());
  }

  @Test
  public void testCreateEventSeriesCountPattern() {
    CommandPatterns.CommandPattern pattern = getPatternByName("CREATE_EVENT_SERIES_COUNT");
    assertNotNull(pattern);

    String command = "create event Weekly Sync from 2025-01-15T10:00 to 2025-01-15T11:00 "
        + "repeats MWF for 3 times";
    Matcher matcher = pattern.getPattern().matcher(command);
    assertTrue(matcher.matches());
  }

  @Test
  public void testEditEventPattern() {
    CommandPatterns.CommandPattern pattern = getPatternByName("EDIT_EVENT");
    assertNotNull(pattern);

    String command = "edit event subject Weekly Sync from 2025-01-15T10:00 to "
        + "2025-01-15T11:00 with Updated Sync";
    Matcher matcher = pattern.getPattern().matcher(command);
    assertTrue(matcher.matches());
  }

  @Test
  public void testPrintEventsOnDatePattern() {
    CommandPatterns.CommandPattern pattern = getPatternByName("PRINT_EVENTS_ON_DATE");
    assertNotNull(pattern);

    Matcher matcher = pattern.getPattern().matcher("print events on 2025-01-15");
    assertTrue(matcher.matches());
  }

  @Test
  public void testExportPattern() {
    CommandPatterns.CommandPattern pattern = getPatternByName("EXPORT");
    assertNotNull(pattern);

    Matcher matcher = pattern.getPattern().matcher("export cal calendar.ics");
    assertTrue(matcher.matches());
  }
}
