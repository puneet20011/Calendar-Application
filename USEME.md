# Calendar Application - User Guide

## Misc.md
**Misc.md** is a document in **res** folder which outlines all the changes in the design that we made when we transitioned from assignment 5 to assignment 6.

## Running the Application

### GUI Based Mode
```bash
java -jar build/libs/calendar-1.0.jar
```
### Interactive Mode
```bash
java -jar build/libs/calendar-1.0.jar --mode interactive
```

### Headless Mode
```bash
java -jar build/libs/calendar-1.0.jar --mode headless res/Commands.txt
```
**Note:** Command file must end with `exit`.

---


### GUI Based Calendar

#### **View Active Calendar**
- Look at the top-right corner of the window
- The active calendar name and timezone are displayed in a colored box
- Example: "Active: Default (America/New_York)"

#### **Create New Calendar**
- Click the **"New Calendar"** button in the top-left section
- Enter calendar name in the dialog
- Enter timezone (e.g., `America/New_York`, `Europe/London`, `Asia/Tokyo`)
- Click **OK**
- Success message will confirm creation

#### **Switch Between Calendars**
- Use the **dropdown menu** at the top-left (labeled "Calendar:")
- Click the dropdown to see all available calendars
- Select the calendar you want to use
- The view automatically refreshes to show that calendar's events
- The active calendar indicator updates to show the new calendar and timezone

#### **Edit Calendar Name**
- Click the **"Edit Calendar"** button in the top section
- Select **"Edit Name"** from the options
- Enter the new calendar name
- Click **OK**
- The dropdown updates with the new name

#### **Edit Calendar Timezone**
- Click the **"Edit Calendar"** button in the top section
- Select **"Edit Timezone"** from the options
- Enter the new timezone (e.g., `America/Los_Angeles`)
- Click **OK**
- All events are automatically converted to maintain the same absolute time

---

### Navigation (GUI)

#### **Navigate Between Months**
- **Previous Month**: Click the **"<- Previous"** button
- **Next Month**: Click the **"Next ->"** button
- The month and year label in the center updates automatically
- Calendar grid refreshes to show the selected month

#### **View Events on a Specific Day**
- Click on any **day button** in the calendar grid(month view)
- The selected day is highlighted in light yellow
- Events for that day appear in the right panel under "Events on Selected Day"
- Event count is shown below each day number (e.g., "(3 events)")

#### **View Today's Date**
- Today's date is automatically highlighted with a **red border**
- Click on today's date to see today's events

---

### Creating Events (GUI)

#### **Create Single Event**
- Click on the day you want to create an event
- Click **"Create Event on Selected Day"** button at the bottom-right
- In the dialog, select the **"Single/All-Day Event"** tab
- Fill in the fields:
    - **Subject**: Enter event name (required)
    - **All-Day Event checkbox**: Check this for all-day events (8am-5pm)
    - **Start Time**: Enter time in format HH:mm (e.g., 09:00)
    - **End Time**: Enter time in format HH:mm (e.g., 10:00)
    - **Description**: Optional event details
    - **Location**: Optional location
    - **Status**: Select "public" or "private"
- Click **"Create Event"**
- Success message confirms creation
- Calendar refreshes to show the new event

#### **Create All-Day Event**
- Click on the day you want to create an all-day event
- Click **"Create Event on Selected Day"**
- In the **"Single/All-Day Event"** tab, check **"All-Day Event (8am - 5pm)"** checkbox
- The start and end time fields automatically update to 08:00 and 17:00
- The time fields are disabled when all-day is selected
- Fill in subject and optional fields
- Click **"Create Event"**

#### **Create Event Series/Recurring Event**
- Click on the starting day for your recurring event
- Click **"Create Event on Selected Day"**
- Select the **"Recurring Event"** tab
- Fill in the fields:
    - **Subject**: Enter event name (required)
    - **All-Day Event checkbox**: Check for all-day recurring events (8am-5pm)
    - **Start Time**: Enter start time
    - **End Time**: Enter end time
    - **Description**: Optional
    - **Location**: Optional
    - **Repeat On**: Check the weekdays (Mon, Tue, Wed, Thu, Fri, Sat, Sun)
    - **Recurrence End**:
        - Select **"Count"** radio button and enter number of occurrences
        - OR select **"Until"** radio button and enter end date (YYYY-MM-DD format)
- Click **"Create Recurring Event"**
- All events in the series are created



---

### Editing Events (GUI)

#### **Edit Single Event**
- Click on the day containing the event
- Find the event in the right panel ("Events on Selected Day")
- Click the **"Edit"** button on the event card
- Select **"Edit this event only"**
- Choose which property to edit: subject, start, end, description, location, status
- Enter the new value:
    - **Subject/Description/Location**: Type new text
    - **Start/End**: Enter new datetime (YYYY-MM-DDTHH:mm format)
    - **Status**: Select from dropdown (public/private)
- Click **OK**
- Only that specific event instance is updated

#### **Edit Multiple Events (This and Future)**
- Click on the day with the event you want to edit
- Click the **"Edit"** button on the event card
- Select **"Edit this and future events in series"**
- Choose property to edit
- Enter new value
- Click **OK**
- The selected event and all future events in the series are updated

#### **Edit Entire Series**
- Click on any day with an event from the series
- Click the **"Edit"** button on the event card
- Select **"Edit entire series (all events)"**
- Choose property to edit
- Enter new value
- Click **OK**
- All events in the series (past, present, and future) are updated

---

### Viewing Event Details (GUI)

#### **Event Cards**
Each event is displayed as a card in the right panel with:
- **Event title** (bold, larger font)
- **Time range** (e.g., "Time: 09:00 - 10:00")
- **Location** (if specified, in gray text)
- **Description** (if specified, truncated to 80 characters with "...")
- **[Private] indicator** (if status is private, shown in red)
- **Edit button** at the bottom of the card

#### **No Events Message**
- If no events are scheduled on the selected day, you'll see: "No events scheduled"

#### **Event Count on Calendar Days**
- Each day shows the number of events in small blue text
- Example: "(3 events)" below the day number
- Days with no events show just the day number

---

### Copying Events (GUI)

#### **Access Copy Dialog**
- Click on the day with events you want to copy
- Click **"Copy Events..."** button at the bottom-right
- A menu appears with three options

#### **Copy Single Event**
- Select **"Copy single event"** from the menu
- Fill in the dialog:
    - **Event Subject**: Name of the event to copy
    - **Source Start Time**: Event's start time (HH:mm format)
    - **Target Calendar**: Select destination calendar from dropdown
    - **Target Date**: Enter destination date (YYYY-MM-DD)
    - **Target Start Time**: Enter destination time (HH:mm)
- Click **OK**
- Event is copied with timezone conversion if needed

#### **Copy All Events on Selected Day**
- Select **"Copy all events on selected day"** from the menu
- Fill in the dialog:
    - **Source Date**: Pre-filled with selected date (can edit)
    - **Target Calendar**: Select destination calendar
    - **Target Date**: Enter destination date
- Click **OK**
- All events on that day are copied
- Times remain the same but are interpreted in target calendar's timezone

#### **Copy Events in Date Range**
- Select **"Copy events in date range"** from the menu
- Fill in the dialog:
    - **Start Date**: Enter range start (YYYY-MM-DD)
    - **End Date (inclusive)**: Enter range end (YYYY-MM-DD)
    - **Target Calendar**: Select destination calendar
    - **Target Start Date**: Enter where the range should start in destination
- Click **OK**
- All events in the date range are copied


## Calendar Management (Commands)

### Create Calendar
Creates a new calendar with a unique name and timezone.
```
create calendar --name Work --timezone America/New_York
```

**Common Timezones:**
- `America/New_York` (EST/EDT)
- `America/Los_Angeles` (PST/PDT)
- `America/Chicago` (CST/CDT)
- `Europe/London` (GMT/BST)
- `Asia/Tokyo` (JST)
- `UTC`

### Use Calendar
Sets the active calendar. **Required before creating/editing events.**
```
use calendar --name Work
```

### Edit Calendar Name
Renames an existing calendar.
```
edit calendar --name Work --property name WorkCalendar
```

### Edit Calendar Timezone
Changes the calendar's timezone. All events are converted to maintain the same absolute UTC time.
```
edit calendar --name Work --property timezone America/Los_Angeles
```
Example: 10:00 AM EST becomes 7:00 AM PST.

---

## Creating Events

### Create Single Event
Creates one event at a specific time.
```
create event Team Meeting from 2025-01-15T10:00 to 2025-01-15T11:00
```

### Create All-Day Event
Creates an event from 8:00 AM to 5:00 PM.
```
create event Birthday on 2025-05-10
```

### Create Recurring Event Series (Count)
Creates multiple events on specific weekdays.
```
create event Standup from 2025-01-06T09:00 to 2025-01-06T09:15 repeats MWF for 10 times
```
**Weekday codes:** M=Monday, T=Tuesday, W=Wednesday, R=Thursday, F=Friday, S=Saturday, U=Sunday

### Create Recurring Event Series (Until Date)
Creates multiple events until a specific date.
```
create event Weekly Review from 2025-01-03T15:00 to 2025-01-03T16:00 repeats F until 2025-03-31
```

### Create Recurring All-Day Series (Count)
```
create event Team Lunch on 2025-01-06 repeats M for 12 times
```

### Create Recurring All-Day Series (Until Date)
```
create event Gym Day on 2025-01-06 repeats MWF until 2025-06-30
```

### Using Quotes for Multi-Word Subjects
```
create event "Team Meeting: Q1 Planning" from 2025-01-15T10:00 to 2025-01-15T11:00
```

---

## Editing Events

### Edit Event (Single Instance Only)
Edits only the specific event identified by subject, start time, and end time.
```
edit event subject Team Meeting from 2025-01-15T10:00 to 2025-01-15T11:00 with Leadership Sync
```
This changes only the event on January 15 at 10:00-11:00. If this event is part of a series, only this one instance changes.

### Edit Events (Starting From)
Edits the identified event and all events in the series that occur after it.
```
edit events subject Meeting from 2025-01-15T10:00 with Daily Standup
```
This changes the event on January 15 and all future events in the same series.

### Edit Series (All Events in Series)
Edits all events in the series (before and after the identified event).
```
edit series subject Weekly Review from 2025-01-03T15:00 with Sprint Retrospective
```
This changes all events in the recurring series, both past and future instances.

---

## Editing Example Walkthrough

This example demonstrates how editing works with recurring series:

**Step 1:** Create an event series
```
create event First from 2025-05-05T10:00 to 2025-05-05T11:00 repeats MW for 6 times
```
This creates 6 events on May 5 (M), 7 (W), 12 (M), 14 (W), 19 (M), 21 (W).

**Step 2:** Edit events starting from May 12
```
edit events subject First from 2025-05-12T10:00 with Second
```
This changes 4 events (May 12, 14, 19, 21) to "Second". The May 5 and 7 events remain "First".

**Step 3:** Edit the entire series
```
edit series subject First from 2025-05-05T10:00 with Third
```
This changes all 6 events back to "Third".

**Step 4:** Change start time for events starting from May 12
```
edit events start Third from 2025-05-12T10:00 with 2025-05-12T10:30
```
This changes the start time of May 12, 14, 19, 21 events to 10:30 AM. Now these 4 events form a different series because they have a different start time.

**Step 5:** Edit the 10:00 AM series
```
edit series subject Third from 2025-05-05T10:00 with Fourth
```
This only changes May 5 and 7 (the events that still start at 10:00 AM).

**Step 6:** Edit the 10:30 AM series
```
edit series subject Third from 2025-05-12T10:30 with Fifth
```
This changes May 12, 14, 19, 21 (the events that now start at 10:30 AM).

**Result:** Changing the start time split the original series into two separate series.

---

## Editable Properties

You can edit these properties: `subject`, `start`, `end`, `location`, `description`, `status`

### Create Example Event
```
create event Meeting from 2025-01-15T10:00 to 2025-01-15T11:00
```

### Editing Start Time
```
edit event start Meeting from 2025-01-15T10:00 to 2025-01-15T11:00 with 2025-01-15T10:30
```

### Editing End Time
```
edit event end Meeting from 2025-01-15T10:30 to 2025-01-15T11:30 with 2025-01-15T12:30
```

### Editing Subject
```
edit event subject Meeting from 2025-01-15T10:30 to 2025-01-15T12:30 with Standup
```

### Editing Location
```
edit event location Standup from 2025-01-15T10:30 to 2025-01-15T12:30 with Conference Room A
```

### Editing Description
```
edit event description Standup from 2025-01-15T10:30 to 2025-01-15T12:30 with Discuss Q1 goals
```

### Editing Status
```
edit event status Standup from 2025-01-15T10:30 to 2025-01-15T12:30 with private
```

You can use `edit events` or `edit series` with any of these properties.

---

## Copying Events

### Copy Event
Copies all matching events to another calendar at a specified date/time.
```
copy event Meeting on 2025-01-15T10:00 --target Personal to 2025-02-20T10:00
```
If multiple events match (same subject and start time), all are copied.

### Copy All Events on Date
Copies all events from one date to another date.
```
copy events on 2025-01-15 --target Personal to 2025-02-20
```
Times remain the same but are interpreted in the target calendar's timezone.

### Copy Events in Range
Copies all events in a date range to another calendar starting at a specified date.
```
copy events between 2025-01-01 and 2025-01-31 --target Archive to 2025-07-01
```
The endpoint dates are inclusive. Relative spacing between events is preserved.

**Partial Series Copy:** When copying a range that includes part of a series, only events within the range are copied, but their series status is retained.

---

## Querying Events

### Print Events on Date
```
print events on 2025-01-15
```
Shows all events on January 15, 2025.

### Print Events in Range
```
print events from 2025-01-01T00:00 to 2025-01-31T23:59
```
Shows all events in January 2025.

### Show Status
```
show status on 2025-01-15T11:00
```
Shows "busy" if you have an event at that time, "available" if free.

---

## Export Calendar

### Export as CSV
```
export cal my-calendar.csv
```
Creates a CSV file that can be imported to Google Calendar.

### Export as iCal
```
export cal my-calendar.ical
```
or
```
export cal my-calendar.ics
```
Creates an iCal file with timezone information. The application auto-detects format based on file extension.

Both commands print the absolute file path.

---

## Exit
```
exit
```
Exits the application. Required at the end of headless mode files.

---

## Date and Time Formats

**Date:** `YYYY-MM-DD` (example: 2025-01-15)

**Time:** `HH:MM` in 24-hour format (example: 14:30)

**DateTime:** `YYYY-MM-DDTHH:MM` (example: 2025-01-15T14:30)

---

## Comments and Empty Lines (Headless Mode)

Lines starting with `#` are comments:
```
# This is a comment
create event Meeting from 2025-01-15T10:00 to 2025-01-15T11:00
```

Empty lines are ignored.

---

## Example Multi-Calendar Workflow
```
# Create calendars
create calendar --name Work --timezone America/New_York
create calendar --name Personal --timezone America/New_York

# Set up work calendar
use calendar --name Work
create event "Daily Standup" from 2025-01-06T09:00 to 2025-01-06T09:15 repeats MTWRF for 20 times
create event "Team Lunch" on 2025-01-10 repeats F for 8 times
edit series location "Daily Standup" from 2025-01-06T09:00 with Zoom

# Set up personal calendar
use calendar --name Personal
create event Gym on 2025-01-06 repeats MWF for 12 times
create event "Doctor Appointment" from 2025-01-15T14:00 to 2025-01-15T15:00

# Copy work event to personal reminder
use calendar --name Work
copy event "Team Lunch" on 2025-01-10T08:00 --target Personal to 2025-01-10T08:00

# Query schedules
use calendar --name Work
print events from 2025-01-01T00:00 to 2025-01-31T23:59

# Export calendars
use calendar --name Work
export cal work-january.ical

use calendar --name Personal
export cal personal-january.csv

exit
```

---

## Example Interactive Session
```
Calendar Application Started
Enter commands (type 'exit' to quit):
> create calendar --name Work --timezone America/New_York
Calendar created: Work
> use calendar --name Work
Now using calendar: Work
> create event Standup from 2025-01-06T09:00 to 2025-01-06T09:15 repeats MTWRF for 5 times
Event series created: Standup
> print events on 2025-01-06
* Standup starting on 2025-01-06 at 09:00, ending on 2025-01-06 at 09:15
> show status on 2025-01-06T09:00
busy
> exit
Goodbye!
```

---

## Example Headless Mode File

**commands.txt:**
```
# Create and setup calendars
create calendar --name Work --timezone America/New_York
create calendar --name Personal --timezone America/Los_Angeles

# Use work calendar
use calendar --name Work

# Create recurring series
create event First from 2025-05-05T10:00 to 2025-05-05T11:00 repeats MW for 6 times

# Edit some events in series
edit events subject First from 2025-05-12T10:00 with Second

# Edit entire series
edit series subject First from 2025-05-05T10:00 with Third

# Add location to all events
edit series location Third from 2025-05-05T10:00 with Conference Room B

# Copy to personal calendar
copy events between 2025-05-05 and 2025-05-21 --target Personal to 2025-06-01

# Query both calendars
use calendar --name Work
print events from 2025-05-01T00:00 to 2025-05-31T23:59

use calendar --name Personal
print events from 2025-06-01T00:00 to 2025-06-30T23:59

# Export
use calendar --name Work
export cal may-work-calendar.ical

use calendar --name Personal
export cal june-personal-calendar.csv

# Required
exit
```

---

## Important Rules

1. A calendar must be in use before creating/editing events. Use `use calendar --name <name>`.
2. Two events cannot have the same subject, start time, and end time.
3. Changing the start time of events in a series can split them into separate series.
4. All events in a series must have the same start time to remain in the same series.
5. Series events must span only a single day (cannot cross midnight).
6. Headless mode command files must end with `exit`.
7. When copying events across calendars with different timezones, times are interpreted in the target calendar's timezone.
8. Partial series copies retain series status in the destination calendar.

---

## Common Errors

**No calendar in use:**
```
ERROR: No calendar in use. Use 'use calendar --name <name>' first
```

**Calendar already exists:**
```
ERROR: Calendar with name 'Work' already exists
```

**Calendar not found:**
```
ERROR: Calendar 'Personal' not found
```

**Invalid timezone:**
```
ERROR: Invalid timezone: Invalid/Zone
```

**Invalid date format:**
```
ERROR: Error: Text '01/15/2025' could not be parsed
```
Use format: `2025-01-15`

**Missing exit in headless mode:**
```
Error: Command file must end with 'exit' command
```

**Duplicate event:**
```
ERROR: Event with same subject, start, and end time already exists
```

**Cannot copy - duplicates exist:**
```
ERROR: All matching events already exist in target calendar
```

**Invalid command:**
```
ERROR: Invalid command: create event Meeting
```