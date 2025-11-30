package calendar.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Defines all command patterns for the calendar application.
 * This class contains the regex patterns used to match user commands.
 * Package private class only used inside controller package.
 */
class CommandPatterns {

  /**
   * Gets all command patterns in priority order (more specific patterns first).
   */
  static List<CommandPattern> getAllPatterns() {
    List<CommandPattern> patterns = new ArrayList<>();

    patterns.add(new CommandPattern(
        "CREATE_CALENDAR",
        Pattern.compile("create calendar --name (\\S+) --timezone ([\\w/]+)")
    ));

    patterns.add(new CommandPattern(
        "USE_CALENDAR",
        Pattern.compile("use calendar --name (\\S+)")
    ));

    patterns.add(new CommandPattern(
        "EDIT_CALENDAR",
        Pattern.compile("edit calendar --name (\\S+) --property (\\w+) (.+)")
    ));

    patterns.add(new CommandPattern(
        "COPY_EVENT",
        Pattern.compile("copy event (.+?) on (\\S+) --target (\\S+) to (\\S+)")
    ));

    patterns.add(new CommandPattern(
        "COPY_EVENTS_ON_DATE",
        Pattern.compile("copy events on (\\S+) --target (\\S+) to (\\S+)")
    ));

    patterns.add(new CommandPattern(
        "COPY_EVENTS_RANGE",
        Pattern.compile("copy events between (\\S+) and (\\S+) --target (\\S+) to (\\S+)")
    ));

    patterns.add(new CommandPattern(
        "CREATE_EVENT_SERIES_COUNT",
        Pattern.compile(
            "create event (.+?) from (\\S+) to (\\S+) repeats ([MTWRFSU]+) for (\\d+) times")
    ));

    patterns.add(new CommandPattern(
        "CREATE_EVENT_SERIES_UNTIL",
        Pattern.compile(
            "create event (.+?) from (\\S+) to (\\S+) repeats ([MTWRFSU]+) until (\\S+)")
    ));

    patterns.add(new CommandPattern(
        "CREATE_ALL_DAY_SERIES_COUNT",
        Pattern.compile("create event (.+?) on (\\S+) repeats ([MTWRFSU]+) for (\\d+) times")
    ));

    patterns.add(new CommandPattern(
        "CREATE_ALL_DAY_SERIES_UNTIL",
        Pattern.compile("create event (.+?) on (\\S+) repeats ([MTWRFSU]+) until (\\S+)")
    ));

    patterns.add(new CommandPattern(
        "CREATE_EVENT",
        Pattern.compile("create event (.+?) from (\\S+) to (\\S+)")
    ));

    patterns.add(new CommandPattern(
        "CREATE_ALL_DAY_EVENT",
        Pattern.compile("create event (.+?) on (\\S+)")
    ));

    patterns.add(new CommandPattern(
        "EDIT_EVENT",
        Pattern.compile("edit event (\\w+) (.+?) from (\\S+) to (\\S+) with (.+)")
    ));

    patterns.add(new CommandPattern(
        "EDIT_EVENTS",
        Pattern.compile("edit events (\\w+) (.+?) from (\\S+) with (.+)")
    ));

    patterns.add(new CommandPattern(
        "EDIT_SERIES",
        Pattern.compile("edit series (\\w+) (.+?) from (\\S+) with (.+)")
    ));

    patterns.add(new CommandPattern(
        "PRINT_EVENTS_ON_DATE",
        Pattern.compile("print events on (\\S+)")
    ));

    patterns.add(new CommandPattern(
        "PRINT_EVENTS_IN_RANGE",
        Pattern.compile("print events from (\\S+) to (\\S+)")
    ));

    patterns.add(new CommandPattern(
        "SHOW_STATUS",
        Pattern.compile("show status on (\\S+)")
    ));

    patterns.add(new CommandPattern(
        "EXPORT",
        Pattern.compile("export cal (.+)")
    ));

    return patterns;
  }

  /**
   * Represents a command pattern with a name and regex pattern.
   */
  static class CommandPattern {
    private final String name;
    private final Pattern pattern;

    CommandPattern(String name, Pattern pattern) {
      this.name = name;
      this.pattern = pattern;
    }

    String getName() {
      return name;
    }

    Pattern getPattern() {
      return pattern;
    }
  }
}