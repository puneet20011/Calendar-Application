# Misc.md - Calendar Application GUI Implementation

## Design Changes
## **Changes to controller**
### 1. **Added a CalendarGUIController and CalendarFeatures Interface**
New GUIController is added that takes care of the GUI based interactions, so now this controller will be passed with the GUI view. It implements and offers all the features through the CalendarFeatures Interface, so the GUI can call the features in the GUI controller and it will call the correct method in the model with the set of parameters and return the response back to the GUI View for display.
It was needed as Older Calendar model only takes care of the headless and interactive mode, but this controller provides a different feature set for GUI based View.

## **Changes to view**
### 2. **Added new IcalendarGuiView interface**
- **Change**: Separate interface for GUI based view:
Added a new interface that represents the GUI based view, it is implemented by the CalendarSwingView that creates a Java Swing View and uses command callbacks design pattern to call the feature in the controller that takes care of the GUI based interaction.
- **Justification**: This interface defines that set of functionalities that GUI based view displays. This is allows a separation of concern between the normal(Interactive and headless view) from GUI Based view.

### 3. **Dialogs**
New dialogs added to the View that define the Swing view for each dialog and call the feature corresponding to that 
dialog in the controller. Each dialog corresponds to each functionality like create, edit and copy events and create calendar and edit calendar.
---

## Feature Status

### * **Working GUI Features**

#### **Calendar Management**
- * Create calendars with custom names and timezones
- * Select/switch between multiple calendars
- * Edit calendar properties (name, timezone)
- * Default calendar creation in user's system timezone

#### **Event Creation**
- * Create single events with start/end times
- * Create all-day events (8am-5pm)
- * Create recurring events with:
    - Weekday selection (M, T, W, R, F, S, U)
    - Count-based recurrence (e.g., "5 times")
    - Date-based recurrence (e.g., "until 2025-12-31")
- * All-day event toggle for both single and recurring events
- * Optional fields: description, location, status (public/private)

#### **Event Editing**
- * Edit single event occurrence
- * Edit multiple events (this and future in series)
- * Edit entire event series
- * Editable properties: subject, start, end, description, location, status

#### **Event Viewing**
- * Month view with navigation (previous/next month)
- * Day view showing all events on selected date
- * Event count display on calendar days
- * Visual indicators for selected date and today's date
- * Color-coded calendar identification

#### **Event Copying**
- * Copy single event between calendars
- * Copy all events on a specific date
- * Copy events in date range
- * Timezone conversion when copying across calendars


### * **Other Working Features for interactive and Headless**

#### **Export Functionality**
- * Export to CSV format (Google Calendar compatible)
- * Export to iCal format (.ical/.ics)
- * Automatic format detection by file extension
- * Absolute path display after export

#### **Text-Based Interface**
- * Interactive mode (command-by-command)
- * Headless mode (script file execution)
- * All commands from specifications working:
    - Calendar commands (create, use, edit)
    - Event commands (create, edit single/multiple/series)
    - Query commands (print events, show status)
    - Copy commands (single event, date, range)
    - Export command

