# Calendar Application

A feature-rich calendar management system built with Java Swing that supports multiple calendars, recurring events with flexible patterns, timezone-aware operations, and seamless event copying across calendars.       


## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Architecture](#architecture)
- [Installation](#installation)
- [Usage](#usage)
- [Command Reference](#command-reference)
- [Examples](#examples)
- [Testing](#testing)
- [Technical Details](#technical-details)
- [Contributing](#contributing)
- [License](#license)

## Overview

This calendar application provides a robust solution for managing multiple calendars with different timezones. Whether you're coordinating across time zones, managing team schedules, or organizing personal events, this application handles complex scheduling scenarios with ease.

Built using the Model-View-Controller (MVC) architecture, the application offers three distinct modes of operation: an interactive GUI, a command-line interface, and a headless mode for automated command execution.

## Features

### Multi-Calendar Management
- Create unlimited calendars with independent timezones
- Switch between calendars seamlessly
- Edit calendar properties (name, timezone) with automatic event conversion
- Isolated event storage per calendar

### Event Types
- **Single Events**: One-time events with specific start and end times
- **All-Day Events**: Events spanning 8 AM to 5 PM
- **Recurring Event Series**: Events that repeat on specific weekdays

### Recurring Events
- Flexible weekday patterns using MTWRFSU codes (Monday through Sunday)
- Count-based termination (e.g., "repeat 10 times")
- Date-based termination (e.g., "repeat until 2025-12-31")
- Support for complex patterns (weekdays only, weekends only, custom combinations)

### Intelligent Event Editing
- **Edit Single Instance**: Modify one occurrence without affecting others
- **Edit Future Events**: Update an event and all subsequent occurrences
- **Edit Entire Series**: Modify all events in a series simultaneously
- Automatic series splitting when start times change
- Removal from series when editing single instance start time

### Timezone Operations
- Automatic timezone conversion maintains absolute UTC moments
- Events preserve real-world timing across timezone changes
- Timezone conversion when copying between calendars
- Support for all IANA timezone identifiers

### Event Copying
- **Copy Single Event**: Copy one event to another calendar at a specified time
- **Copy Events on Date**: Copy all events from one date to another
- **Copy Events in Range**: Copy all events within a date range to target calendar
- Maintains relative time offsets
- Preserves optional properties (description, location, status)
- Intelligent handling of series events with weekday pattern preservation

### Query and Status
- Query events by specific date
- Query events within date/time ranges
- Check busy/available status at any date and time
- Events sorted by start time

### Export Functionality
- **CSV Export**: Google Calendar compatible format
- **iCal/ICS Export**: RFC 5545 compliant format
- Proper escaping for special characters
- Timezone information included in iCal exports

### Optional Event Properties
- Description
- Location
- Status (public/private)

## Architecture

The application follows the **Model-View-Controller (MVC)** design pattern:

### Model Layer
- `CalendarSystem`: Facade managing multiple calendar contexts
- `CalendarModel`: Core calendar operations for a single calendar
- `Event`: Immutable event objects with builder pattern
- `EventSeries`: Manages recurring event occurrences
- `EventCalendar`: Event storage and retrieval
- Utility classes: `EventCopier`, `CalendarTimezoneConverter`, `SeriesCreator`, `SeriesEditor`, `EventUpdater`, `SystemValidator`

### View Layer
- `CalendarSwingView`: Interactive GUI with Swing components
- `CalendarView`: Text-based output for CLI mode
- Dialog components for event/calendar creation and editing

### Controller Layer
- `CalendarGuiController`: Handles GUI interactions and callbacks
- `CalendarController`: Processes text commands from CLI or files
- `CommandExecutor`: Parses and executes commands
- `CommandPatterns`: Regex patterns for command matching

## Installation

### Prerequisites
- Java 11 or higher
- Gradle or Maven (for building from source)

### Build from Source

Using Gradle:
```bash
./gradlew build
```

Using Maven:
```bash
mvn clean package
```

The compiled JAR will be in `build/libs/` or `target/` directory.

## Usage

### GUI Mode (Default)

Launch the application without arguments for the interactive GUI:
```bash
java -jar CalendarApp.jar
```

The GUI provides:
- Month view with event counts
- Day view with detailed event information
- Dialogs for creating/editing calendars and events
- Visual calendar selection with timezone display

### Interactive CLI Mode

Run commands interactively in the terminal:
```bash
java -jar CalendarApp.jar --mode interactive
```

Type commands directly and see immediate results. Type `exit` to quit.

### Headless Mode

Execute commands from a text file:
```bash
java -jar CalendarApp.jar --mode headless commands.txt
```

Useful for automated workflows, testing, or batch operations.

## Command Reference

### Calendar Management

**Create Calendar**
```
create calendar --name <calendar_name> --timezone <timezone>
```
Example: `create calendar --name work --timezone America/New_York`

**Use Calendar** (switch to a calendar)
```
use calendar --name <calendar_name>
```
Example: `use calendar --name work`

**Edit Calendar Name**
```
edit calendar --name <calendar_name> --property name <new_name>
```
Example: `edit calendar --name work --property name office`

**Edit Calendar Timezone**
```
edit calendar --name <calendar_name> --property timezone <new_timezone>
```
Example: `edit calendar --name work --property timezone America/Los_Angeles`

### Event Creation

**Create Single Event**
```
create event <subject> from <start_datetime> to <end_datetime>
```
Example: `create event "Team Meeting" from 2025-05-15T10:00 to 2025-05-15T11:00`

**Create All-Day Event**
```
create event <subject> on <date>
```
Example: `create event "Holiday" on 2025-05-15`

**Create Recurring Event (Count-based)**
```
create event <subject> from <start_datetime> to <end_datetime> repeats <weekdays> for <count> times
```
Example: `create event "Daily Standup" from 2025-05-05T09:00 to 2025-05-05T09:30 repeats MTWRF for 10 times`

**Create Recurring Event (Date-based)**
```
create event <subject> from <start_datetime> to <end_datetime> repeats <weekdays> until <end_date>
```
Example: `create event "Weekly Review" from 2025-05-05T14:00 to 2025-05-05T15:00 repeats F until 2025-12-31`

**Create All-Day Recurring Event (Count-based)**
```
create event <subject> on <date> repeats <weekdays> for <count> times
```
Example: `create event "Gym Day" on 2025-05-05 repeats MWF for 15 times`

**Create All-Day Recurring Event (Date-based)**
```
create event <subject> on <date> repeats <weekdays> until <end_date>
```
Example: `create event "Weekend Activity" on 2025-05-03 repeats SU until 2025-08-31`

**Weekday Codes**: M (Monday), T (Tuesday), W (Wednesday), R (Thursday), F (Friday), S (Saturday), U (Sunday)

### Event Editing

**Edit Single Event Instance**
```
edit event <property> <subject> from <start_datetime> to <end_datetime> with <new_value>
```
Example: `edit event location "Team Meeting" from 2025-05-15T10:00 to 2025-05-15T11:00 with "Room 202"`

**Edit Future Events in Series**
```
edit events <property> <subject> from <start_datetime> with <new_value>
```
Example: `edit events subject "Standup" from 2025-05-12T09:00 with "Daily Sync"`

**Edit Entire Series**
```
edit series <property> <subject> from <start_datetime> with <new_value>
```
Example: `edit series location "Weekly Review" from 2025-05-05T14:00 with "Conference Room A"`

**Editable Properties**: subject, start, end, description, location, status

### Event Copying

**Copy Single Event**
```
copy event <subject> on <source_datetime> --target <target_calendar> to <target_datetime>
```
Example: `copy event "Meeting Template" on 2025-05-01T10:00 --target shared to 2025-05-15T10:00`

**Copy All Events on Date**
```
copy events on <source_date> --target <target_calendar> to <target_date>
```
Example: `copy events on 2025-05-01 --target backup to 2025-06-01`

**Copy Events in Range**
```
copy events between <start_date> and <end_date> --target <target_calendar> to <target_start_date>
```
Example: `copy events between 2025-05-01 and 2025-05-07 --target shared to 2025-06-01`

### Queries

**Print Events on Specific Date**
```
print events on <date>
```
Example: `print events on 2025-05-15`

**Print Events in Date/Time Range**
```
print events from <start_datetime> to <end_datetime>
```
Example: `print events from 2025-05-01T00:00 to 2025-05-31T23:59`

**Check Availability Status**
```
show status on <datetime>
```
Example: `show status on 2025-05-15T10:30`
Returns: `busy` or `available`

### Export

**Export Calendar**
```
export cal <filename>
```
Examples:
- `export cal my_calendar.csv` - Google Calendar compatible CSV
- `export cal my_calendar.ics` - iCal format
- `export cal my_calendar.ical` - iCal format (alternative extension)

### Date/Time Formats

- **Date**: `yyyy-MM-dd` (e.g., `2025-05-15`)
- **DateTime**: `yyyy-MM-dd'T'HH:mm` (e.g., `2025-05-15T10:30`)
- **Timezone**: IANA timezone identifier (e.g., `America/New_York`, `Europe/London`, `Asia/Tokyo`)

### Special Commands

- **Exit**: `exit` - Quit the application
- **Comments**: Lines starting with `#` are ignored
- **Empty Lines**: Ignored for readability

## Examples

### Example 1: Basic Calendar Setup
```
create calendar --name work --timezone America/New_York
use calendar --name work
create event "Team Meeting" from 2025-05-15T10:00 to 2025-05-15T11:00
edit event location "Team Meeting" from 2025-05-15T10:00 to 2025-05-15T11:00 with "Conference Room A"
print events on 2025-05-15
exit
```

### Example 2: Recurring Events
```
create calendar --name personal --timezone America/Los_Angeles
use calendar --name personal

# Create weekday standup
create event "Daily Standup" from 2025-05-05T09:00 to 2025-05-05T09:30 repeats MTWRF for 20 times

# Create all-day weekend events
create event "Gym Day" on 2025-05-03 repeats SU until 2025-08-31

# Print all May events
print events from 2025-05-01T00:00 to 2025-05-31T23:59
exit
```

### Example 3: Series Editing
```
create calendar --name team --timezone UTC
use calendar --name team

# Create initial series
create event "Sprint Planning" from 2025-05-05T10:00 to 2025-05-05T11:00 repeats MW for 6 times

# Edit entire series location
edit series location "Sprint Planning" from 2025-05-05T10:00 with "Room 101"

# Edit only future events' description
edit events description "Sprint Planning" from 2025-05-12T10:00 with "Updated planning session"

# Edit single instance
edit event start "Sprint Planning" from 2025-05-14T10:00 to 2025-05-14T11:00 with 2025-05-14T14:00

print events from 2025-05-05T00:00 to 2025-05-20T23:59
exit
```

### Example 4: Multi-Calendar with Copying
```
# Setup calendars
create calendar --name engineering --timezone America/New_York
create calendar --name design --timezone America/New_York
create calendar --name shared --timezone UTC

# Add events to engineering calendar
use calendar --name engineering
create event "Code Review" from 2025-05-05T14:00 to 2025-05-05T15:00 repeats MTWRF for 10 times
create event "Sprint Planning" from 2025-05-05T10:00 to 2025-05-05T11:00

# Add events to design calendar
use calendar --name design
create event "Design Critique" from 2025-05-06T11:00 to 2025-05-06T12:00 repeats T for 5 times

# Copy important meetings to shared calendar
use calendar --name engineering
copy event "Sprint Planning" on 2025-05-05T10:00 --target shared to 2025-05-05T15:00

use calendar --name design
copy events between 2025-05-06 and 2025-06-03 --target shared to 2025-05-06

# View shared calendar
use calendar --name shared
print events from 2025-05-05T00:00 to 2025-06-10T23:59
exit
```

### Example 5: Timezone Conversion
```
# Create calendar in EST
create calendar --name eastcoast --timezone America/New_York
use calendar --name eastcoast
create event "Morning Standup" from 2025-05-05T09:00 to 2025-05-05T09:30 repeats MTWRF for 5 times

# Check current times
print events on 2025-05-05

# Convert to PST
edit calendar --name eastcoast --property timezone America/Los_Angeles

# Events automatically converted (9 AM EST → 6 AM PST)
print events on 2025-05-05
exit
```

### Example 6: Cross-Calendar Event Migration
```
create calendar --name old_calendar --timezone UTC
create calendar --name new_calendar --timezone UTC

use calendar --name old_calendar
create event "Meeting 1" from 2025-05-01T10:00 to 2025-05-01T11:00
create event "Meeting 2" from 2025-05-02T14:00 to 2025-05-02T15:00
create event "Weekly Sync" from 2025-05-05T09:00 to 2025-05-05T10:00 repeats MTWRF for 10 times

# Migrate all events to new calendar
copy events between 2025-05-01 and 2025-05-20 --target new_calendar to 2025-05-01

use calendar --name new_calendar
print events from 2025-05-01T00:00 to 2025-05-20T23:59
exit
```

### Example 7: Template Event Copying
```
create calendar --name templates --timezone UTC
create calendar --name schedule --timezone UTC

# Create a template event
use calendar --name templates
create event "Standard Meeting" from 2025-05-01T10:00 to 2025-05-01T11:00
edit event location "Standard Meeting" from 2025-05-01T10:00 to 2025-05-01T11:00 with "Conference Room A"
edit event description "Standard Meeting" from 2025-05-01T10:00 to 2025-05-01T11:00 with "Regular team sync"

# Copy template to multiple dates
copy event "Standard Meeting" on 2025-05-01T10:00 --target schedule to 2025-05-05T10:00
copy event "Standard Meeting" on 2025-05-01T10:00 --target schedule to 2025-05-12T10:00
copy event "Standard Meeting" on 2025-05-01T10:00 --target schedule to 2025-05-19T10:00

use calendar --name schedule
print events from 2025-05-05T00:00 to 2025-05-20T23:59
exit
```

### Example 8: Export Calendar
```
create calendar --name work --timezone America/New_York
use calendar --name work

# Add some events
create event "Morning Standup" from 2025-05-05T09:00 to 2025-05-05T09:30 repeats MTWRF for 20 times
create event "Sprint Planning" from 2025-05-05T14:00 to 2025-05-05T16:00

# Export to CSV (Google Calendar compatible)
export cal work_calendar.csv

# Export to iCal format
export cal work_calendar.ics

exit
```

### Example 9: Complex Workflow
```
# Setup team calendars
create calendar --name dev_team --timezone America/New_York
create calendar --name qa_team --timezone America/New_York
create calendar --name all_hands --timezone UTC

# Dev team schedule
use calendar --name dev_team
create event "Daily Standup" from 2025-05-05T09:00 to 2025-05-05T09:30 repeats MTWRF for 10 times
create event "Sprint Planning" from 2025-05-05T14:00 to 2025-05-05T16:00
create event "Sprint Retrospective" from 2025-05-16T14:00 to 2025-05-16T15:00

# Copy dev schedule to QA team
copy events between 2025-05-05 and 2025-05-16 --target qa_team to 2025-05-05

# QA adds their own review meeting
use calendar --name qa_team
create event "QA Review" from 2025-05-08T11:00 to 2025-05-08T12:00 repeats RF for 3 times

# Copy important meetings to all-hands calendar
use calendar --name dev_team
copy event "Sprint Planning" on 2025-05-05T14:00 --target all_hands to 2025-05-05T18:00
copy event "Sprint Retrospective" on 2025-05-16T14:00 --target all_hands to 2025-05-16T18:00

# Change dev team timezone
edit calendar --name dev_team --property timezone America/Los_Angeles

# Export all calendars
use calendar --name dev_team
export cal dev_team.ics

use calendar --name qa_team
export cal qa_team.csv

use calendar --name all_hands
export cal all_hands.ics

exit
```

## Testing

The project includes comprehensive test coverage:

### Test Statistics
- **100+ JUnit tests** across all layers
- **95%+ code coverage**
- Unit, integration, and end-to-end tests

### Test Categories

**Controller Tests**
- `CalendarControllerTest`: CLI controller behavior, command processing, error handling
- `CalendarGuiControllerTest`: GUI controller callbacks, view updates, feature implementations
- `CommandExecutorTest`: Command parsing, execution, pattern matching
- `CommandPatternsTest`: Regex pattern validation
- `CsvExporterTest`: CSV export formatting, escaping
- `IcalExporterTest`: iCal format compliance, RFC 5545 validation
- `ResponseFormatterTest`: Event formatting for display

**Model Tests**
- `CalendarSystemTest`: Multi-calendar management, context switching
- `CalendarModelTest`: Event creation, editing, series management
- `CalendarTimezoneConverterTest`: Timezone conversion accuracy
- `EventCalendarTest`: Event storage, retrieval, queries
- `EventCopierTest`: Event copying with timezone handling
- `EventTest`: Event construction, equality, immutability
- `EventSeriesTest`: Series creation, occurrence management
- `SeriesCreatorTest`: Recurrence pattern generation
- `SeriesEditorTest`: Series editing strategies
- `EventUpdaterTest`: Property updates, duration preservation
- `SystemValidatorTest`: Validation rules, constraint enforcement

**End-to-End Tests**
- `EndToEndTests`: Complete workflows, integration scenarios
- `NewEndToEndTests`: Multi-calendar operations, copying, timezone scenarios

**Mock Objects**
- `MockCalendarSystemModel`: Model test double
- `MockCalendarGuiView`: GUI view test double
- `MockCalendarView`: CLI view test double

### Running Tests

Using Gradle:
```bash
./gradlew test
```

Using Maven:
```bash
mvn test
```

View detailed test reports:
- Gradle: `build/reports/tests/test/index.html`
- Maven: `target/surefire-reports/`

## Technical Details

### Design Patterns

**Builder Pattern**
- Used for creating immutable `Event` and `EventSeries` objects
- Provides flexible construction with optional fields
- Ensures validation at build time

**Command Pattern**
- CLI commands parsed using regex patterns
- Command execution delegated to model operations
- Supports preprocessing and validation

**Facade Pattern**
- `CalendarSystem` provides unified interface for multi-calendar operations
- Simplifies controller interaction with complex model layer

**Strategy Pattern**
- `SeriesEditor` uses different strategies for series editing (keep, split, replace)
- Determined by edit scope and property being modified

### Key Classes

**Model Layer**
- `CalendarSystem`: Multi-calendar facade, calendar context management
- `CalendarContext`: Wraps calendar with name and timezone metadata
- `CalendarModel`: Single calendar operations
- `Event`: Immutable event with builder pattern
- `EventSeries`: Manages recurring event occurrences
- `EventCalendar`: Event storage with date indexing
- `EventCopier`: Handles cross-calendar event copying with timezone conversion
- `CalendarTimezoneConverter`: Converts events when calendar timezone changes
- `SeriesCreator`: Generates recurring event occurrences
- `SeriesEditor`: Manages series editing with different strategies
- `EventUpdater`: Updates event properties maintaining invariants
- `SystemValidator`: Validates all operations, prevents duplicates and conflicts

**View Layer**
- `CalendarSwingView`: Main GUI frame with month/day views
- `CalendarView`: Text-based output for CLI
- Dialog classes: `CreateEventDialog`, `EditEventDialog`, `CreateCalendarDialog`, `EditCalendarDialog`, `CopyEventsDialog`

**Controller Layer**
- `CalendarGuiController`: Handles GUI callbacks implementing `CalendarFeatures`
- `CalendarController`: Processes text commands, manages application flow
- `CommandExecutor`: Parses and executes commands using patterns
- `CommandPatterns`: Defines regex patterns for all commands
- `CsvExporter`: Exports to CSV format
- `IcalExporter`: Exports to iCal/ICS format
- `ResponseFormatter`: Formats events for display

### Validation Rules

- Events must have start time before end time
- No duplicate events (same subject, start, and end)
- Series events must span a single day
- Cannot change date for series events (only time)
- Maximum 365 occurrences per series
- Calendar names must be unique
- Timezone must be valid IANA identifier

### Timezone Handling

The application uses Java's `ZoneId` and `ZonedDateTime` for accurate timezone operations:

1. **Event Storage**: Events stored in local date/time (naive)
2. **Interpretation**: Times interpreted in calendar's timezone
3. **Conversion**: When copying or changing timezone, maintains absolute UTC moment
4. **Example**: 2 PM EST (7 PM UTC) → 11 AM PST (still 7 PM UTC)

### File Formats

**CSV Export**
- Compatible with Google Calendar import
- Headers: Subject, Start Date, Start Time, End Date, End Time, All Day Event, Description, Location, Private
- Proper escaping for commas, quotes, newlines
- Date format: MM/dd/yyyy
- Time format: hh:mm a (12-hour)

**iCal/ICS Export**
- RFC 5545 compliant
- Includes VTIMEZONE block
- Unique UIDs for each event
- Supports DESCRIPTION, LOCATION, CLASS (PUBLIC/PRIVATE)
- Proper text escaping (backslash, semicolon, comma, newline)

### Event Uniqueness

Events are uniquely identified by the combination of:
- Subject (case-sensitive)
- Start date and time
- End date and time

This allows multiple events with the same subject at different times.

### Series Editing Behavior

**Edit Event** (single instance):
- If in series and editing start time: removes from series, creates standalone event
- If in series and editing other properties: keeps in series, updates that instance

**Edit Events** (from date forward):
- If not editing start time: keeps all in same series
- If editing start time: creates new series for future events

**Edit Series** (all occurrences):
- If not editing start time: updates all in same series
- If editing start time: creates new series with updated times

## Project Structure
```
calendar/
├── controller/
│   ├── CalendarController.java          # CLI controller
│   ├── CalendarGuiController.java       # GUI controller
│   ├── CommandExecutor.java             # Command parsing and execution
│   ├── CommandPatterns.java             # Regex patterns
│   ├── CsvExporter.java                 # CSV export
│   ├── IcalExporter.java                # iCal export
│   ├── ResponseFormatter.java           # Event formatting
│   ├── CalendarFeatures.java            # GUI feature interface
│   └── IcalendarController.java         # Controller interface
├── model/
│   ├── CalendarSystem.java              # Multi-calendar facade
│   ├── CalendarContext.java             # Calendar wrapper
│   ├── CalendarModel.java               # Single calendar model
│   ├── Event.java                       # Immutable event
│   ├── EventSeries.java                 # Recurring series
│   ├── EventCalendar.java               # Event storage
│   ├── EventCopier.java                 # Cross-calendar copying
│   ├── CalendarTimezoneConverter.java   # Timezone conversion
│   ├── SeriesCreator.java               # Series generation
│   ├── SeriesEditor.java                # Series editing
│   ├── EventUpdater.java                # Event property updates
│   ├── SystemValidator.java             # Validation logic
│   ├── EventKey.java                    # Event uniqueness key
│   ├── CalConstrains.java               # Constants
│   ├── IcalendarModel.java              # Model interface
│   ├── CalendarSystemModel.java         # System model interface
│   └── Ievent.java                      # Event interface
├── view/
│   ├── CalendarSwingView.java           # Swing GUI
│   ├── CalendarView.java                # CLI view
│   ├── IcalendarGuiView.java            # GUI view interface
│   ├── IcalendarView.java               # View interface
│   └── dialogs/
│       ├── CreateEventDialog.java
│       ├── EditEventDialog.java
│       ├── CreateCalendarDialog.java
│       ├── EditCalendarDialog.java
│       └── CopyEventsDialog.java
└── CalendarRunner.java                  # Main entry point
```

## Requirements

- **Java Version**: 11 or higher
- **Dependencies**:
    - JUnit 4 (for testing)
    - Java Swing (included in JDK)
    - Java Time API (included in JDK)

## Building

### Using Gradle
```bash
# Build project
./gradlew build

# Run tests
./gradlew test

# Create JAR
./gradlew jar

# Clean build
./gradlew clean
```

### Using Maven
```bash
# Build project
mvn clean package

# Run tests
mvn test

# Skip tests
mvn package -DskipTests
```

## Limitations and Constraints

- Maximum 365 occurrences per event series
- Series events must span a single day (no multi-day recurring events)
- Cannot change date for series events (only time component)
- Event times stored in calendar's timezone (not UTC)
- File export creates files in application's working directory

## Known Issues

None currently reported. Please submit issues via GitHub Issues.

## Future Enhancements

Potential improvements for future versions:
- Event reminders and notifications
- Conflict detection and resolution
- Calendar sharing and permissions
- Web interface
- Database persistence
- Attachment support
- Calendar subscription (read-only external calendars)
- Meeting invitations
- Recurring event exceptions (skip specific occurrences)
- Multi-day recurring events

## Contributing

Contributions are welcome! Please follow these guidelines:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/your-feature`)
3. Write tests for new functionality
4. Ensure all tests pass (`./gradlew test`)
5. Follow existing code style and patterns
6. Commit your changes (`git commit -am 'Add new feature'`)
7. Push to the branch (`git push origin feature/your-feature`)
8. Create a Pull Request

### Code Style
- Follow Java naming conventions
- Use meaningful variable names
- Add Javadoc comments for public methods
- Keep methods focused and concise
- Write tests for all new functionality

### Testing Requirements
- All new features must include unit tests
- Integration tests for cross-component functionality
- End-to-end tests for complete workflows
- Maintain minimum 90% code coverage

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Author

Puneet Singh Puri 

## Acknowledgments

- Built as part of Object-Oriented Design 
- Implements principles of clean architecture and SOLID design
- Uses Java Time API for robust date/time handling
- GUI built with Java Swing framework

## Support

For questions, issues, or suggestions:
- Open an issue on GitHub
- Contact: puri.pu@northeastern.edu / puneet.puri999@gmail.com

---

**Note**: This is an educational project demonstrating object-oriented design principles, MVC architecture, and comprehensive testing practices.