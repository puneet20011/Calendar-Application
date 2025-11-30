import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Permission;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Integration tests for CalendarRunner.
 * Tests the full application including command-line argument parsing.
 */
public class CalendarRunnerTest {

  private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
  private final ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
  private final PrintStream originalOut = System.out;
  private final PrintStream originalErr = System.err;
  private SecurityManager originalSecurityManager;

  /**
   * Security manager that prevents System.exit() from actually terminating the JVM.
   */
  private static class NoExitSecurityManager extends SecurityManager {
    @Override
    public void checkPermission(Permission perm) {
      // Allow all permissions
    }

    @Override
    public void checkPermission(Permission perm, Object context) {
      // Allow all permissions
    }

    @Override
    public void checkExit(int status) {
      super.checkExit(status);
      throw new ExitException(status);
    }
  }

  /**
   * Exception thrown instead of actually exiting.
   */
  private static class ExitException extends SecurityException {
    public final int status;

    public ExitException(int status) {
      super("System.exit(" + status + ")");
      this.status = status;
    }
  }

  /**
   * Set up test environment.
   */
  @Before
  public void setUp() {
    System.setOut(new PrintStream(outputStream));
    System.setErr(new PrintStream(errorStream));
    originalSecurityManager = System.getSecurityManager();
    System.setSecurityManager(new NoExitSecurityManager());
  }

  /**
   * Clean up test environment.
   */
  @After
  public void tearDown() {
    System.setSecurityManager(originalSecurityManager);
    System.setOut(originalOut);
    System.setErr(originalErr);
  }

  @Test
  public void testHeadlessModeWithValidFile() throws Exception {
    Path tempFile = Files.createTempFile("test_commands", ".txt");
    try {
      String content =
          "create calendar --name work --timezone UTC\n"
              + "use calendar --name work\n"
              + "create event Test from 2025-01-15T10:00 to 2025-01-15T11:00\n"
              + "print events on 2025-01-15\n"
              + "exit\n";
      Files.write(tempFile, content.getBytes());

      String[] args = {"--mode", "headless", tempFile.toString()};
      CalendarRunner.main(args);

      String output = outputStream.toString();
      assertTrue("Should start application",
          output.contains("Calendar Application Started"));
      assertTrue("Should create calendar",
          output.contains("Calendar created: work"));
      assertTrue("Should use calendar",
          output.contains("Now using calendar: work"));
      assertTrue("Should create event",
          output.contains("Event created: Test"));
      assertTrue("Should print event",
          output.contains("* Test"));
      assertTrue("Should say goodbye",
          output.contains("Goodbye!"));

      String errors = errorStream.toString();
      assertTrue("Should not have errors: " + errors, errors.isEmpty());

    } finally {
      Files.deleteIfExists(tempFile);
    }
  }

  @Test
  public void testHeadlessModeWithCommentsAndEmptyLines() throws Exception {
    Path tempFile = Files.createTempFile("test_commands", ".txt");
    try {
      String content =
          "# This is a comment\n"
              + "\n"
              + "create calendar --name test --timezone UTC\n"
              + "use calendar --name test\n"
              + "create event Test from 2025-01-15T10:00 to 2025-01-15T11:00\n"
              + "\n"
              + "# Another comment\n"
              + "exit\n";
      Files.write(tempFile, content.getBytes());

      String[] args = {"--mode", "headless", tempFile.toString()};
      CalendarRunner.main(args);

      String output = outputStream.toString();
      assertTrue("Should create event despite comments",
          output.contains("Event created: Test"));

      String errors = errorStream.toString();
      assertTrue("Should not have errors: " + errors, errors.isEmpty());

    } finally {
      Files.deleteIfExists(tempFile);
    }
  }

  @Test
  public void testHeadlessModeFileNotFound() {
    String[] args = {"--mode", "headless", "nonexistent_file.txt"};

    try {
      CalendarRunner.main(args);
      fail("Should have called System.exit(1)");
    } catch (ExitException e) {
      assertEquals("Should exit with status 1", 1, e.status);
      String errors = errorStream.toString();
      assertTrue("Should report file error",
          errors.contains("Error reading command file")
              || errors.contains("nonexistent_file.txt"));
    }
  }

  @Test
  public void testHeadlessModeMissingFilePath() {
    String[] args = {"--mode", "headless"};

    try {
      CalendarRunner.main(args);
      fail("Should have called System.exit(1)");
    } catch (ExitException e) {
      assertEquals("Should exit with status 1", 1, e.status);
      String errors = errorStream.toString();
      assertTrue("Should report missing file path",
          errors.contains("Headless mode requires a file path"));
    }
  }

  @Test
  public void testInvalidModeFlag() {
    String[] args = {"--invalid", "interactive"};

    try {
      CalendarRunner.main(args);
      fail("Should have called System.exit(1)");
    } catch (ExitException e) {
      assertEquals("Should exit with status 1", 1, e.status);
      String errors = errorStream.toString();
      assertTrue("Should report invalid flag",
          errors.contains("Invalid argument. Expected --mode"));
    }
  }

  @Test
  public void testInvalidMode() {
    String[] args = {"--mode", "invalid"};

    try {
      CalendarRunner.main(args);
      fail("Should have called System.exit(1)");
    } catch (ExitException e) {
      assertEquals("Should exit with status 1", 1, e.status);
      String errors = errorStream.toString();
      assertTrue("Should report invalid mode",
          errors.contains("Invalid mode: invalid"));
    }
  }

  @Test
  public void testInsufficientArguments() {
    String[] args = {"--mode"};

    try {
      CalendarRunner.main(args);
      fail("Should have called System.exit(1)");
    } catch (ExitException e) {
      assertEquals("Should exit with status 1", 1, e.status);
      String errors = errorStream.toString();
      assertTrue("Should report usage",
          errors.contains("Usage:") || errors.contains("CalendarApp"));
    }
  }

  @Test
  public void testHeadlessModeWithMultipleEvents() throws Exception {
    Path tempFile = Files.createTempFile("test_commands", ".txt");
    try {
      String content =
          "create calendar --name work --timezone UTC\n"
              + "use calendar --name work\n"
              + "create event Morning Standup from 2025-01-15T09:00 to 2025-01-15T09:15\n"
              + "create event Code Review from 2025-01-15T14:00 to 2025-01-15T15:00\n"
              + "create event Team Retro from 2025-01-15T16:00 to 2025-01-15T17:00\n"
              + "print events on 2025-01-15\n"
              + "show status on 2025-01-15T14:30\n"
              + "exit\n";
      Files.write(tempFile, content.getBytes());

      String[] args = {"--mode", "headless", tempFile.toString()};
      CalendarRunner.main(args);

      String output = outputStream.toString();

      assertTrue(output.contains("Event created: Morning Standup"));
      assertTrue(output.contains("Event created: Code Review"));
      assertTrue(output.contains("Event created: Team Retro"));

      assertTrue(output.contains("* Morning Standup"));
      assertTrue(output.contains("* Code Review"));
      assertTrue(output.contains("* Team Retro"));

      assertTrue(output.contains("busy"));

      assertTrue(errorStream.toString().isEmpty());

    } finally {
      Files.deleteIfExists(tempFile);
    }
  }

  @Test
  public void testHeadlessModeWithEventSeries() throws Exception {
    Path tempFile = Files.createTempFile("test_commands", ".txt");
    try {
      String content =
          "create calendar --name work --timezone UTC\n"
              + "use calendar --name work\n"
              + "create event Weekly Meeting from 2025-01-15T10:00 to 2025-01-15T11:00 "
              + "repeats MWF for 3 times\n"
              + "print events on 2025-01-15\n"
              + "exit\n";
      Files.write(tempFile, content.getBytes());

      String[] args = {"--mode", "headless", tempFile.toString()};
      CalendarRunner.main(args);

      String output = outputStream.toString();
      assertTrue("Should create series",
          output.contains("Event series created: Weekly Meeting"));
      assertTrue("Should print events",
          output.contains("* Weekly Meeting"));

      assertTrue(errorStream.toString().isEmpty());

    } finally {
      Files.deleteIfExists(tempFile);
    }
  }

  @Test
  public void testHeadlessModeWithoutExitCommand() throws Exception {
    Path tempFile = Files.createTempFile("test_commands", ".txt");
    try {
      String content =
          "create calendar --name work --timezone UTC\n"
              + "use calendar --name work\n"
              + "create event Test from 2025-01-15T10:00 to 2025-01-15T11:00\n";
      Files.write(tempFile, content.getBytes());

      String[] args = {"--mode", "headless", tempFile.toString()};
      CalendarRunner.main(args);

      String output = outputStream.toString();
      assertTrue("Should start application",
          output.contains("Calendar Application Started"));

      String errors = errorStream.toString();
      assertTrue("Should report missing exit command",
          errors.contains("File should have Exit command at the end"));

    } finally {
      Files.deleteIfExists(tempFile);
    }
  }

  @Test
  public void testHeadlessModeWithInvalidCommands() throws Exception {
    Path tempFile = Files.createTempFile("test_commands", ".txt");
    try {
      String content =
          "create calendar --name work --timezone UTC\n"
              + "use calendar --name work\n"
              + "invalid command here\n"
              + "create event Test from 2025-01-15T10:00 to 2025-01-15T11:00\n"
              + "exit\n";
      Files.write(tempFile, content.getBytes());

      String[] args = {"--mode", "headless", tempFile.toString()};
      CalendarRunner.main(args);

      String output = outputStream.toString();
      assertTrue("Should still create calendar",
          output.contains("Calendar created: work"));
      assertTrue("Should still create event",
          output.contains("Event created: Test"));

      String errors = errorStream.toString();
      assertTrue("Should report invalid command",
          errors.contains("Invalid command: invalid command here"));

    } finally {
      Files.deleteIfExists(tempFile);
    }
  }

  @Test
  public void testHeadlessModeWithExport() throws Exception {
    Path tempFile = Files.createTempFile("test_commands", ".txt");
    Path exportFile = Files.createTempFile("exported", ".csv");
    try {
      String content =
          "create calendar --name work --timezone UTC\n"
              + "use calendar --name work\n"
              + "create event Test from 2025-01-15T10:00 to 2025-01-15T11:00\n"
              + "export cal " + exportFile.toString() + "\n"
              + "exit\n";
      Files.write(tempFile, content.getBytes());

      String[] args = {"--mode", "headless", tempFile.toString()};
      CalendarRunner.main(args);

      String output = outputStream.toString();
      assertTrue("Should export successfully",
          output.contains("Calendar exported successfully"));
      assertTrue("Should show file location",
          output.contains("File location:"));

      assertTrue("Export file should exist",
          Files.exists(exportFile));

    } finally {
      Files.deleteIfExists(tempFile);
      Files.deleteIfExists(exportFile);
    }
  }

  @Test
  public void testHeadlessModeUpperCase() throws Exception {
    Path tempFile = Files.createTempFile("test_commands", ".txt");
    try {
      String content =
          "create calendar --name work --timezone UTC\n"
              + "use calendar --name work\n"
              + "exit\n";
      Files.write(tempFile, content.getBytes());

      String[] args = {"--mode", "HEADLESS", tempFile.toString()};
      CalendarRunner.main(args);

      String output = outputStream.toString();
      assertTrue("Should handle uppercase mode",
          output.contains("Calendar created: work"));

    } finally {
      Files.deleteIfExists(tempFile);
    }
  }

  @Test
  public void testHeadlessModeWithCalendarOperations() throws Exception {
    Path tempFile = Files.createTempFile("test_commands", ".txt");
    try {
      String content =
          "create calendar --name personal --timezone America/New_York\n"
              + "create calendar --name work --timezone America/Los_Angeles\n"
              + "use calendar --name personal\n"
              + "create event Lunch from 2025-01-15T12:00 to 2025-01-15T13:00\n"
              + "use calendar --name work\n"
              + "create event Meeting from 2025-01-15T10:00 to 2025-01-15T11:00\n"
              + "print events on 2025-01-15\n"
              + "exit\n";
      Files.write(tempFile, content.getBytes());

      String[] args = {"--mode", "headless", tempFile.toString()};
      CalendarRunner.main(args);

      String output = outputStream.toString();
      assertTrue("Should create personal calendar",
          output.contains("Calendar created: personal"));
      assertTrue("Should create work calendar",
          output.contains("Calendar created: work"));
      assertTrue("Should use personal calendar",
          output.contains("Now using calendar: personal"));
      assertTrue("Should use work calendar",
          output.contains("Now using calendar: work"));
      assertTrue("Should create events",
          output.contains("Event created:"));

    } finally {
      Files.deleteIfExists(tempFile);
    }
  }

  @Test
  public void testHeadlessModeWithCopyOperations() throws Exception {
    Path tempFile = Files.createTempFile("test_commands", ".txt");
    try {
      String content =
          "create calendar --name source --timezone UTC\n"
              + "create calendar --name target --timezone UTC\n"
              + "use calendar --name source\n"
              + "create event Meeting from 2025-01-15T10:00 to 2025-01-15T11:00\n"
              + "copy event Meeting on 2025-01-15T10:00 --target target to 2025-01-20T10:00\n"
              + "use calendar --name target\n"
              + "print events on 2025-01-20\n"
              + "exit\n";
      Files.write(tempFile, content.getBytes());

      String[] args = {"--mode", "headless", tempFile.toString()};
      CalendarRunner.main(args);

      String output = outputStream.toString();
      assertTrue("Should copy event successfully",
          output.contains("Event copied successfully"));
      assertTrue("Should print copied event",
          output.contains("* Meeting"));

    } finally {
      Files.deleteIfExists(tempFile);
    }
  }

  @Test
  public void testHeadlessModeWithEditOperations() throws Exception {
    Path tempFile = Files.createTempFile("test_commands", ".txt");
    try {
      String content =
          "create calendar --name work --timezone UTC\n"
              + "use calendar --name work\n"
              + "create event Meeting from 2025-01-15T10:00 to 2025-01-15T11:00\n"
              + "edit event description Meeting from 2025-01-15T10:00 to 2025-01-15T11:00 "
              + "with Important discussion\n"
              + "edit event location Meeting from 2025-01-15T10:00 to 2025-01-15T11:00 "
              + "with Room 101\n"
              + "print events on 2025-01-15\n"
              + "exit\n";
      Files.write(tempFile, content.getBytes());

      String[] args = {"--mode", "headless", tempFile.toString()};
      CalendarRunner.main(args);

      String output = outputStream.toString();
      assertTrue("Should edit event successfully",
          output.contains("Event edited successfully"));
      assertTrue("Should show edited properties",
          output.contains("description: Important discussion"));
      assertTrue("Should show edited properties",
          output.contains("location: Room 101"));

    } finally {
      Files.deleteIfExists(tempFile);
    }
  }

  @Test
  public void testGuiModeWithNoArguments() throws Exception {
    String[] args = {};

    final boolean[] guiLaunched = {false};
    final Exception[] caughtException = {null};

    Thread testThread = new Thread(() -> {
      try {
        CalendarRunner.main(args);
        guiLaunched[0] = true;
      } catch (ExitException e) {
        if (e.status == 0) {
          guiLaunched[0] = true;
        } else {
          caughtException[0] = e;
        }
      } catch (Exception e) {
        caughtException[0] = e;
      }
    });

    testThread.start();
    Thread.sleep(1000);
    testThread.interrupt();
    testThread.join(2000);

    assertEquals(true, guiLaunched[0]);
    assertEquals(null, caughtException[0]);
  }

  @Test
  public void testGuiModeDoesNotPrintUsageErrors() throws Exception {
    String[] args = {};

    Thread testThread = new Thread(() -> {
      try {
        CalendarRunner.main(args);
      } catch (ExitException e) {
        // Expected - GUI calls setDefaultCloseOperation which triggers exit(0)
      }
    });

    testThread.start();
    Thread.sleep(500);
    testThread.interrupt();
    testThread.join(2000);

    String errors = errorStream.toString();

    assertEquals(false, errors.contains("Invalid"));
    assertEquals(false, errors.contains("Usage:"));
  }

  @Test
  public void testSingleArgumentShowsUsage() {
    String[] args = {"--mode"};

    try {
      CalendarRunner.main(args);
      fail("Should have called System.exit(1)");
    } catch (ExitException e) {
      assertEquals("Should exit with status 1", 1, e.status);
      String errors = errorStream.toString();
      assertEquals(true, errors.contains("Usage:"));
    }
  }

  @Test
  public void testInvalidFlagShowsUsage() {
    String[] args = {"--invalid", "interactive"};

    try {
      CalendarRunner.main(args);
      fail("Should have called System.exit(1)");
    } catch (ExitException e) {
      assertEquals("Should exit with status 1", 1, e.status);
      String errors = errorStream.toString();
      assertEquals(true, errors.contains("Invalid argument"));
      assertEquals(true, errors.contains("Expected --mode"));
    }
  }

  @Test
  public void testUsageMessageContainsAllModes() {
    String[] args = {"--mode"};

    try {
      CalendarRunner.main(args);
      fail("Should have called System.exit(1)");
    } catch (ExitException e) {
      String errors = errorStream.toString();
      assertEquals(true, errors.contains("GUI mode"));
      assertEquals(true, errors.contains("interactive"));
      assertEquals(true, errors.contains("headless"));
    }
  }

  @Test
  public void testUsageMessageShowsJarCommand() {
    String[] args = {"--mode"};

    try {
      CalendarRunner.main(args);
      fail("Should have called System.exit(1)");
    } catch (ExitException e) {
      String errors = errorStream.toString();
      assertEquals(true, errors.contains("java -jar"));
      assertEquals(true, errors.contains("CalendarApp.jar"));
    }
  }

  @Test
  public void testInteractiveModeWithExitCommand() throws Exception {
    String input = "exit\n";
    System.setIn(new java.io.ByteArrayInputStream(input.getBytes()));

    String[] args = {"--mode", "interactive"};
    CalendarRunner.main(args);

    String output = outputStream.toString();
    assertEquals(true, output.contains("Calendar Application Started"));
    assertEquals(true, output.contains("Goodbye!"));
  }

  @Test
  public void testInteractiveModeWithCommands() throws Exception {
    String input = String.join("\n",
        "create calendar --name test --timezone UTC",
        "use calendar --name test",
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00",
        "print events on 2025-05-01",
        "exit"
    );
    System.setIn(new java.io.ByteArrayInputStream(input.getBytes()));

    String[] args = {"--mode", "interactive"};
    CalendarRunner.main(args);

    String output = outputStream.toString();
    assertEquals(true, output.contains("Calendar created: test"));
    assertEquals(true, output.contains("Now using calendar: test"));
    assertEquals(true, output.contains("Event created: Meeting"));
    assertEquals(true, output.contains("* Meeting"));
  }

  @Test
  public void testInteractiveModeUpperCase() throws Exception {
    String input = "exit\n";
    System.setIn(new java.io.ByteArrayInputStream(input.getBytes()));

    String[] args = {"--mode", "INTERACTIVE"};
    CalendarRunner.main(args);

    String output = outputStream.toString();
    assertEquals(true, output.contains("Calendar Application Started"));
  }
}