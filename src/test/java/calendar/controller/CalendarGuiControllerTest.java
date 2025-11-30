package calendar.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import calendar.mocks.MockCalendarGuiView;
import calendar.mocks.MockCalendarSystemModel;
import calendar.model.Event;
import java.lang.reflect.Constructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for CalendarGuiController functionality.
 * Tests GUI controller behavior, view updates, and feature implementations.
 */
public class CalendarGuiControllerTest {

  private MockCalendarSystemModel mockModel;
  private MockCalendarGuiView mockView;
  private CalendarGuiController controller;
  private StringBuilder modelLog;
  private StringBuilder viewLog;

  /**
   * Sets up test fixtures before each test.
   */
  @Before
  public void setUp() {
    modelLog = new StringBuilder();
    viewLog = new StringBuilder();
    mockModel = new MockCalendarSystemModel(modelLog);
    mockView = new MockCalendarGuiView(viewLog);
    controller = new CalendarGuiController(mockModel, mockView);
  }

  /**
   * Cleans up after each test.
   */
  @After
  public void tearDown() {
    modelLog.setLength(0);
    viewLog.setLength(0);
  }

  // Helper method to create events
  private Event createEvent(String subject, LocalDateTime start, LocalDateTime end)
      throws Exception {
    Class<?> eventSeriesClass = Class.forName("calendar.model.EventSeries");
    Constructor<Event> constructor = Event.class.getDeclaredConstructor(
        String.class, LocalDateTime.class, LocalDateTime.class,
        eventSeriesClass, String.class, String.class, String.class);
    constructor.setAccessible(true);
    return constructor.newInstance(subject, start, end, null, null, null, "public");
  }

  @Test
  public void testRunInitializesDefaultCalendar() {
    controller.run();

    String log = modelLog.toString();
    assertEquals(true, log.contains("createCalendar:Default"));
    assertEquals(true, log.contains("useCalendar:Default"));
  }

  @Test
  public void testRunRefreshesCalendarList() {
    controller.run();

    String log = viewLog.toString();
    assertEquals(true, log.contains("refreshCalendarList"));
    assertEquals(true, log.contains("Default"));
  }

  @Test
  public void testRunNavigatesToCurrentMonth() {
    controller.run();

    LocalDate today = LocalDate.now();
    String log = viewLog.toString();
    assertEquals(true, log.contains("refreshMonthView:" + today.getYear() + "-"
        + today.getMonthValue()));
  }

  @Test
  public void testRunSelectsCurrentDate() {
    controller.run();

    LocalDate today = LocalDate.now();
    String log = viewLog.toString();
    assertEquals(true, log.contains("refreshDayView:" + today));
  }

  @Test
  public void testRunCallsDisplay() {
    controller.run();

    String log = viewLog.toString();
    assertEquals(true, log.contains("display called"));
  }

  @Test
  public void testRunSetsFeatures() {
    controller.run();

    String log = viewLog.toString();
    assertEquals(true, log.contains("setFeatures called"));
    assertNotNull("Features should be set",
        mockView.getFeatures());
  }

  @Test
  public void testCreateCalendar() {
    controller.run();
    viewLog.setLength(0);

    controller.createCalendar("work", "America/New_York");

    assertEquals(true, modelLog.toString().contains("createCalendar:work,America/New_York"));
    assertEquals(true, viewLog.toString().contains("refreshCalendarList"));
    assertEquals(true, viewLog.toString().contains("showSuccess:Calendar created: work"));
  }

  @Test
  public void testCreateCalendarError() {
    controller.run();
    viewLog.setLength(0);

    mockModel = new MockCalendarSystemModel(modelLog) {
      @Override
      public void createCalendar(String name, String timezone) {
        throw new IllegalArgumentException("Calendar already exists");
      }
    };
    controller = new CalendarGuiController(mockModel, mockView);
    controller.run();

    controller.createCalendar("duplicate", "UTC");

    assertEquals(true, viewLog.toString().contains("showError:Calendar already exists"));
  }

  @Test
  public void testSelectCalendar() {
    controller.run();
    viewLog.setLength(0);
    modelLog.setLength(0);

    mockModel.setCurrentCalendarTimezone("America/New_York");
    controller.selectCalendar("work");

    assertEquals(true, modelLog.toString().contains("useCalendar:work"));
    assertEquals(true, modelLog.toString().contains("getCurrentCalendarTimezone"));
    assertEquals(true, viewLog.toString().contains("refreshCalendarList"));
  }

  @Test
  public void testSelectCalendarNavigatesToMonth() {
    controller.run();
    mockView.setCurrentMonth(YearMonth.of(2025, 5));
    viewLog.setLength(0);

    mockModel.setCurrentCalendarTimezone("UTC");
    controller.selectCalendar("personal");

    assertEquals(true, viewLog.toString().contains("refreshMonthView:2025-5"));
  }

  @Test
  public void testSelectCalendarRefreshesDay() {
    controller.run();
    LocalDate testDate = LocalDate.of(2025, 5, 15);
    mockView.setSelectedDate(testDate);
    viewLog.setLength(0);

    mockModel.setCurrentCalendarTimezone("UTC");
    controller.selectCalendar("personal");

    assertEquals(true, viewLog.toString().contains("refreshDayView:" + testDate));
  }

  @Test
  public void testSelectCalendarError() {
    controller.run();
    viewLog.setLength(0);

    mockModel = new MockCalendarSystemModel(modelLog) {
      @Override
      public void useCalendar(String name) {
        throw new IllegalArgumentException("Calendar not found");
      }
    };
    controller = new CalendarGuiController(mockModel, mockView);
    controller.run();

    controller.selectCalendar("nonexistent");

    assertEquals(true, viewLog.toString().contains("showError:Calendar not found"));
  }

  @Test
  public void testCreateSingleEvent() {
    controller.run();
    viewLog.setLength(0);
    modelLog.setLength(0);

    LocalDateTime start = LocalDateTime.of(2025, 5, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 15, 11, 0);

    controller.createSingleEvent("Meeting", start, end,
        "Important meeting", "Room 101", "private");

    assertEquals(true, modelLog.toString().contains("createEvent:Meeting"));
    assertEquals(true, modelLog.toString()
        .contains("editEvent:Meeting,2025-05-15T10:00,description,Important meeting"));
    assertEquals(true, modelLog.toString()
        .contains("editEvent:Meeting,2025-05-15T10:00,location,Room 101"));
    assertEquals(true, modelLog.toString()
        .contains("editEvent:Meeting,2025-05-15T10:00,status,private"));
    assertEquals(true, viewLog.toString()
        .contains("showSuccess:Event created: Meeting"));
  }

  @Test
  public void testCreateSingleEventWithNullOptionalFields() {
    controller.run();
    viewLog.setLength(0);
    modelLog.setLength(0);

    LocalDateTime start = LocalDateTime.of(2025, 5, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 15, 11, 0);

    controller.createSingleEvent("Meeting", start, end, null, null, null);

    assertEquals(true, modelLog.toString().contains("createEvent:Meeting"));
    assertEquals(true, viewLog.toString().contains("showSuccess:Event created: Meeting"));
  }

  @Test
  public void testCreateSingleEventWithEmptyOptionalFields() {
    controller.run();
    viewLog.setLength(0);
    modelLog.setLength(0);

    LocalDateTime start = LocalDateTime.of(2025, 5, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 15, 11, 0);

    controller.createSingleEvent("Meeting", start, end, "", "", "public");

    assertEquals(true, modelLog.toString().contains("createEvent:Meeting"));
    assertEquals(false, modelLog.toString().contains("description,"));
    assertEquals(false, modelLog.toString().contains("location,"));
  }

  @Test
  public void testCreateSingleEventError() {
    controller.run();
    viewLog.setLength(0);

    mockModel = new MockCalendarSystemModel(modelLog) {
      @Override
      public void createEvent(String subject, LocalDateTime start, LocalDateTime end) {
        throw new IllegalArgumentException("Duplicate event");
      }
    };
    controller = new CalendarGuiController(mockModel, mockView);
    controller.run();

    LocalDateTime start = LocalDateTime.of(2025, 5, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 15, 11, 0);

    controller.createSingleEvent("Meeting", start, end, null, null, null);

    assertEquals(true, viewLog.toString().contains("showError:Duplicate event"));
  }

  @Test
  public void testCreateRecurringEvent() {
    controller.run();
    viewLog.setLength(0);
    modelLog.setLength(0);

    LocalDateTime start = LocalDateTime.of(2025, 5, 5, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 5, 11, 0);
    LocalDate untilDate = LocalDate.of(2025, 5, 30);

    controller.createRecurringEvent("Weekly Meeting", start, end,
        "MWF", 5, untilDate, "Team sync", "Conference Room", "public");

    assertEquals(true, modelLog.toString()
        .contains("createEventSeries:Weekly Meeting"));
    assertEquals(true, viewLog.toString()
        .contains("showSuccess:Event series created: Weekly Meeting"));
  }

  @Test
  public void testCreateRecurringEventWithCountOnly() {
    controller.run();
    viewLog.setLength(0);
    modelLog.setLength(0);

    LocalDateTime start = LocalDateTime.of(2025, 5, 5, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 5, 11, 0);

    controller.createRecurringEvent("Daily", start, end,
        "MTWRF", 10, null, null, null, null);

    assertEquals(true, modelLog.toString().contains("createEventSeries:Daily"));
    assertEquals(true, modelLog.toString().contains(",10,null"));
  }

  @Test
  public void testCreateRecurringEventWithUntilDate() {
    controller.run();
    viewLog.setLength(0);
    modelLog.setLength(0);

    LocalDateTime start = LocalDateTime.of(2025, 5, 5, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 5, 11, 0);
    LocalDate untilDate = LocalDate.of(2025, 5, 30);

    controller.createRecurringEvent("Weekly", start, end,
        "W", -1, untilDate, null, null, null);

    String log = modelLog.toString();
    assertEquals(true, log.contains("createEventSeries:Weekly"));
    assertEquals(true, log.contains("2025-05-30T23:59"));
  }

  @Test
  public void testEditSingleEvent() {
    controller.run();
    viewLog.setLength(0);
    modelLog.setLength(0);

    LocalDateTime start = LocalDateTime.of(2025, 5, 15, 10, 0);
    controller.editSingleEvent("Meeting", start, "location", "Room 202");

    assertEquals(true, modelLog.toString()
        .contains("editEvent:Meeting,2025-05-15T10:00,location,Room 202"));
    assertEquals(true, viewLog.toString().contains("refreshMonthView"));
    assertEquals(true, viewLog.toString().contains("showSuccess:Event updated"));
  }

  @Test
  public void testEditSingleEventError() {
    controller.run();
    viewLog.setLength(0);

    mockModel = new MockCalendarSystemModel(modelLog) {
      @Override
      public void editEvent(String subject, LocalDateTime start,
                            String property, String newValue) {
        throw new IllegalArgumentException("Event not found");
      }
    };
    controller = new CalendarGuiController(mockModel, mockView);
    controller.run();

    LocalDateTime start = LocalDateTime.of(2025, 5, 15, 10, 0);
    controller.editSingleEvent("Meeting", start, "location", "Room 202");

    assertEquals(true, viewLog.toString().contains("showError:Event not found"));
  }

  @Test
  public void testEditMultipleEvents() {
    controller.run();
    viewLog.setLength(0);
    modelLog.setLength(0);

    LocalDateTime start = LocalDateTime.of(2025, 5, 15, 10, 0);
    controller.editMultipleEvents("Weekly", start, "location", "New Room");

    assertEquals(true, modelLog.toString()
        .contains("editEvents:Weekly,2025-05-15T10:00,location,New Room"));
    assertEquals(true, viewLog.toString().contains("refreshMonthView"));
    assertEquals(true, viewLog.toString().contains("showSuccess:Events updated"));
  }

  @Test
  public void testNavigateToMonth() {
    controller.run();
    viewLog.setLength(0);
    modelLog.setLength(0);

    mockModel.setEventsForRange(Collections.emptyList());

    controller.navigateToMonth(2025, 6);

    assertEquals(true, modelLog.toString().contains("getByRange"));
    assertEquals(true, viewLog.toString().contains("refreshMonthView:2025-6"));
  }

  @Test
  public void testNavigateToMonthWithEvents() throws Exception {
    controller.run();
    viewLog.setLength(0);
    modelLog.setLength(0);

    Event event = createEvent("Meeting",
        LocalDateTime.of(2025, 6, 15, 10, 0),
        LocalDateTime.of(2025, 6, 15, 11, 0));
    mockModel.setEventsForRange(List.of(event));

    controller.navigateToMonth(2025, 6);

    assertEquals(true, viewLog.toString().contains("refreshMonthView:2025-6,events=1"));
  }

  @Test
  public void testNavigateToMonthError() {
    controller.run();
    viewLog.setLength(0);

    mockModel = new MockCalendarSystemModel(modelLog) {
      @Override
      public List<calendar.model.Ievent> getByRange(LocalDateTime start, LocalDateTime end) {
        throw new IllegalArgumentException("Invalid range");
      }
    };
    controller = new CalendarGuiController(mockModel, mockView);
    controller.run();

    controller.navigateToMonth(2025, 6);

    assertEquals(true, viewLog.toString().contains("showError:Invalid range"));
  }

  @Test
  public void testSelectDate() {
    controller.run();
    viewLog.setLength(0);
    modelLog.setLength(0);

    LocalDate date = LocalDate.of(2025, 5, 15);
    mockModel.setEventsForDate(Collections.emptyList());

    controller.selectDate(date);

    assertEquals(true, modelLog.toString().contains("getByDate:2025-05-15"));
    assertEquals(true, viewLog.toString().contains("refreshDayView:2025-05-15,events=0"));
  }

  @Test
  public void testSelectDateWithEvents() throws Exception {
    controller.run();
    viewLog.setLength(0);
    modelLog.setLength(0);

    LocalDate date = LocalDate.of(2025, 5, 15);
    Event event = createEvent("Meeting",
        LocalDateTime.of(2025, 5, 15, 10, 0),
        LocalDateTime.of(2025, 5, 15, 11, 0));
    mockModel.setEventsForDate(List.of(event));

    controller.selectDate(date);

    assertEquals(true, viewLog.toString().contains("refreshDayView:2025-05-15,events=1"));
  }

  @Test
  public void testSelectDateError() {
    controller.run();
    viewLog.setLength(0);

    mockModel = new MockCalendarSystemModel(modelLog) {
      @Override
      public List<calendar.model.Ievent> getByDate(LocalDate date) {
        throw new IllegalArgumentException("Invalid date");
      }
    };
    controller = new CalendarGuiController(mockModel, mockView);
    controller.run();

    controller.selectDate(LocalDate.of(2025, 5, 15));

    assertEquals(true, viewLog.toString().contains("showError:Invalid date"));
  }

  @Test
  public void testGetAvailableCalendars() {
    controller.run();
    controller.createCalendar("work", "UTC");
    controller.createCalendar("personal", "UTC");

    String[] calendars = controller.getAvailableCalendars();

    assertNotNull("Should return calendar array", calendars);
    assertEquals("Should have 3 calendars (Default + 2 created)", 3, calendars.length);
  }

  @Test
  public void testEditCalendarPropertyName() {
    controller.run();
    viewLog.setLength(0);
    modelLog.setLength(0);

    mockModel.setCurrentCalendarTimezone("UTC");
    controller.editCalendarProperty("work", "name", "office");

    assertEquals(true, modelLog.toString()
        .contains("editCalendar:work,name,office"));
    assertEquals(true, viewLog.toString().contains("refreshCalendarList"));
    assertEquals(true, viewLog.toString().contains("showSuccess:Calendar updated"));
  }

  @Test
  public void testEditCalendarPropertyTimezone() {
    controller.run();
    viewLog.setLength(0);
    modelLog.setLength(0);

    mockView.setCurrentMonth(YearMonth.of(2025, 5));
    mockModel.setCurrentCalendarTimezone("America/Los_Angeles");

    controller.editCalendarProperty("work", "timezone", "America/Los_Angeles");

    assertEquals(true, modelLog.toString()
        .contains("editCalendar:work,timezone,America/Los_Angeles"));
    assertEquals(true, modelLog.toString().contains("getCurrentCalendarTimezone"));
    assertEquals(true, viewLog.toString().contains("refreshCalendarList"));
    assertEquals(true, viewLog.toString().contains("refreshMonthView:2025-5"));
  }

  @Test
  public void testEditCalendarPropertyError() {
    controller.run();
    viewLog.setLength(0);

    mockModel = new MockCalendarSystemModel(modelLog) {
      @Override
      public void editCalendar(String calendarName, String property, String newValue) {
        throw new IllegalArgumentException("Invalid property");
      }
    };
    controller = new CalendarGuiController(mockModel, mockView);
    controller.run();

    controller.editCalendarProperty("work", "invalid", "value");

    assertEquals(true, viewLog.toString().contains("showError:Invalid property"));
  }

  @Test
  public void testCopySingleEvent() {
    controller.run();
    viewLog.setLength(0);
    modelLog.setLength(0);

    LocalDateTime eventStart = LocalDateTime.of(2025, 5, 15, 10, 0);
    LocalDateTime targetStart = LocalDateTime.of(2025, 5, 20, 10, 0);

    controller.copySingleEvent("Meeting", eventStart, "target", targetStart);

    assertEquals(true, modelLog.toString()
        .contains("copyEvent:Meeting,2025-05-15T10:00,target,2025-05-20T10:00"));
    assertEquals(true, viewLog.toString().contains("showSuccess:Event copied to target"));
  }

  @Test
  public void testCopySingleEventError() {
    controller.run();
    viewLog.setLength(0);

    mockModel = new MockCalendarSystemModel(modelLog) {
      @Override
      public void copyEvent(String eventName, LocalDateTime eventStart,
                            String targetCalendarName, LocalDateTime targetStart) {
        throw new IllegalArgumentException("Event not found");
      }
    };
    controller = new CalendarGuiController(mockModel, mockView);
    controller.run();

    LocalDateTime eventStart = LocalDateTime.of(2025, 5, 15, 10, 0);
    LocalDateTime targetStart = LocalDateTime.of(2025, 5, 20, 10, 0);

    controller.copySingleEvent("Meeting", eventStart, "target", targetStart);

    assertEquals(true, viewLog.toString().contains("showError:Event not found"));
  }

  @Test
  public void testCopyEventsOnDate() {
    controller.run();
    viewLog.setLength(0);
    modelLog.setLength(0);

    LocalDate sourceDate = LocalDate.of(2025, 5, 15);
    LocalDate targetDate = LocalDate.of(2025, 5, 20);

    controller.copyEventsOnDate(sourceDate, "target", targetDate);

    assertEquals(true, modelLog.toString()
        .contains("copyEventsOnDate:2025-05-15,target,2025-05-20"));
    assertEquals(true, viewLog.toString()
        .contains("showSuccess:Events copied to target"));
  }

  @Test
  public void testCopyEventsOnDateError() {
    controller.run();
    viewLog.setLength(0);

    mockModel = new MockCalendarSystemModel(modelLog) {
      @Override
      public void copyEventsOnDate(LocalDate sourceDate, String targetCalendarName,
                                   LocalDate targetDate) {
        throw new IllegalArgumentException("Target calendar not found");
      }
    };
    controller = new CalendarGuiController(mockModel, mockView);
    controller.run();

    controller.copyEventsOnDate(LocalDate.of(2025, 5, 15), "target",
        LocalDate.of(2025, 5, 20));

    assertEquals(true, viewLog.toString().contains("showError:Target calendar not found"));
  }

  @Test
  public void testCopyEventsInRange() {
    controller.run();
    viewLog.setLength(0);
    modelLog.setLength(0);

    LocalDate startDate = LocalDate.of(2025, 5, 1);
    LocalDate endDate = LocalDate.of(2025, 5, 7);
    LocalDate targetStartDate = LocalDate.of(2025, 6, 1);

    controller.copyEventsInRange(startDate, endDate, "target", targetStartDate);

    assertEquals(true, modelLog.toString()
        .contains("copyEventsInRange:2025-05-01,2025-05-07,target,2025-06-01"));
    assertEquals(true, viewLog.toString()
        .contains("showSuccess:Events copied to target"));
  }

  @Test
  public void testCopyEventsInRangeError() {
    controller.run();
    viewLog.setLength(0);

    mockModel = new MockCalendarSystemModel(modelLog) {
      @Override
      public void copyEventsInRange(LocalDate startDate, LocalDate endDate,
                                    String targetCalendarName, LocalDate targetStartDate) {
        throw new IllegalArgumentException("Invalid range");
      }
    };
    controller = new CalendarGuiController(mockModel, mockView);
    controller.run();

    controller.copyEventsInRange(LocalDate.of(2025, 5, 1), LocalDate.of(2025, 5, 7),
        "target", LocalDate.of(2025, 6, 1));

    assertEquals(true, viewLog.toString().contains("showError:Invalid range"));
  }

  @Test
  public void testCreateSingleEventRefreshesViews() {
    controller.run();
    viewLog.setLength(0);

    LocalDateTime start = LocalDateTime.of(2025, 5, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 15, 11, 0);

    controller.createSingleEvent("Meeting", start, end, null, null, null);

    assertEquals(true, viewLog.toString().contains("refreshMonthView:2025-5"));
    assertEquals(true, viewLog.toString().contains("refreshDayView:2025-05-15"));
  }

  @Test
  public void testCreateRecurringEventRefreshesViews() {
    controller.run();
    viewLog.setLength(0);

    LocalDateTime start = LocalDateTime.of(2025, 5, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 15, 11, 0);

    controller.createRecurringEvent("Weekly", start, end,
        "W", 3, null, null, null, null);

    assertEquals(true, viewLog.toString().contains("refreshMonthView:2025-5"));
    assertEquals(true, viewLog.toString().contains("refreshDayView:2025-05-15"));
  }

  @Test
  public void testEditSingleEventRefreshesCorrectDate() {
    controller.run();
    viewLog.setLength(0);

    LocalDateTime start = LocalDateTime.of(2025, 7, 20, 14, 0);
    controller.editSingleEvent("Meeting", start, "description", "Updated");

    assertEquals(true, viewLog.toString().contains("refreshMonthView:2025-7"));
    assertEquals(true, viewLog.toString().contains("refreshDayView:2025-07-20"));
  }

  @Test
  public void testEditMultipleEventsRefreshesCorrectDate() {
    controller.run();
    viewLog.setLength(0);

    LocalDateTime start = LocalDateTime.of(2025, 8, 10, 9, 0);
    controller.editMultipleEvents("Series", start, "location", "Room B");

    assertEquals(true, viewLog.toString().contains("refreshMonthView:2025-8"));
    assertEquals(true, viewLog.toString().contains("refreshDayView:2025-08-10"));
  }

  @Test
  public void testCreateEventWithPublicStatus() {
    controller.run();
    viewLog.setLength(0);
    modelLog.setLength(0);

    LocalDateTime start = LocalDateTime.of(2025, 5, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 15, 11, 0);

    controller.createSingleEvent("Meeting", start, end,
        "Description", "Location", "public");

    String log = modelLog.toString();
    assertEquals(false, log
        .contains("editEvent:Meeting,2025-05-15T10:00,status,public"));
    assertEquals(true, log.contains("description,Description"));
    assertEquals(true, log.contains("location,Location"));
  }

  @Test
  public void testCreateRecurringEventWithPrivateStatus() {
    controller.run();
    viewLog.setLength(0);
    modelLog.setLength(0);

    LocalDateTime start = LocalDateTime.of(2025, 5, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 15, 11, 0);

    controller.createRecurringEvent("Weekly", start, end,
        "W", 3, null, "Desc", "Loc", "private");

    String log = modelLog.toString();
    assertEquals(true, log
        .contains("editEvent:Weekly,2025-05-15T10:00,status,private"));
  }

  @Test
  public void testRunHandlesModelException() {
    mockModel = new MockCalendarSystemModel(modelLog) {
      @Override
      public void createCalendar(String name, String timezone) {
        throw new RuntimeException("System error");
      }
    };
    controller = new CalendarGuiController(mockModel, mockView);

    controller.run();

    assertEquals(true, viewLog.toString()
        .contains("showError:Failed to initialize: System error"));
  }

  @Test
  public void testNavigateToMonthCalculatesCorrectRange() {
    controller.run();
    modelLog.setLength(0);

    mockModel.setEventsForRange(Collections.emptyList());
    controller.navigateToMonth(2025, 2);

    String log = modelLog.toString();
    assertEquals(true, log.contains("getByRange:2025-02-01T00:00"));
    assertEquals(true, log.contains("2025-02-28T23:59:59"));
  }

  @Test
  public void testNavigateToMonthLeapYear() {
    controller.run();
    modelLog.setLength(0);

    mockModel.setEventsForRange(Collections.emptyList());
    controller.navigateToMonth(2024, 2);

    String log = modelLog.toString();
    assertEquals(true, log.contains("2024-02-29T23:59:59"));
  }

  @Test
  public void testSelectCalendarRefreshesWithCorrectTimezone() {
    controller.run();
    viewLog.setLength(0);

    mockModel.setCurrentCalendarTimezone("America/New_York");
    controller.selectCalendar("work");

    String log = viewLog.toString();
    assertEquals(true, log.contains("timezone=America/New_York"));
  }

  @Test
  public void testEditCalendarNameRefreshesListWithNewName() {
    controller.run();
    viewLog.setLength(0);

    mockModel.setCurrentCalendarTimezone("UTC");
    controller.editCalendarProperty("oldname", "name", "newname");

    String log = viewLog.toString();
    assertEquals(true, log.contains("current=newname"));
  }

  @Test
  public void testCreateSingleEventWithOnlyDescription() {
    controller.run();
    modelLog.setLength(0);

    LocalDateTime start = LocalDateTime.of(2025, 5, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 15, 11, 0);

    controller.createSingleEvent("Meeting", start, end,
        "Only description", null, null);

    String log = modelLog.toString();
    assertEquals(true, log.contains("description,Only description"));
    assertEquals(false, log.contains("location,null"));
  }

  @Test
  public void testCreateSingleEventWithOnlyLocation() {
    controller.run();
    modelLog.setLength(0);

    LocalDateTime start = LocalDateTime.of(2025, 5, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 15, 11, 0);

    controller.createSingleEvent("Meeting", start, end,
        null, "Only location", null);

    String log = modelLog.toString();
    assertEquals(true, log.contains("location,Only location"));
    assertEquals(false, log.contains("description,null"));
  }

  @Test
  public void testOptionalFieldsEditContinuesOnException() {
    controller.run();
    modelLog.setLength(0);

    mockModel = new MockCalendarSystemModel(modelLog) {
      @Override
      public void editEvent(String subject, LocalDateTime start,
                            String property, String newValue) {
        throw new RuntimeException("Edit failed");
      }
    };
    controller = new CalendarGuiController(mockModel, mockView);
    controller.run();

    LocalDateTime start = LocalDateTime.of(2025, 5, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 15, 11, 0);

    controller.createSingleEvent("Meeting", start, end,
        "Description", "Location", "private");

    assertEquals(true, modelLog.toString().contains("createEvent:Meeting"));
  }

  @Test
  public void testNavigateToMonthForDecember() {
    controller.run();
    modelLog.setLength(0);

    mockModel.setEventsForRange(Collections.emptyList());
    controller.navigateToMonth(2025, 12);

    String log = modelLog.toString();
    assertEquals(true, log.contains("getByRange:2025-12-01T00:00"));
    assertEquals(true, log.contains("2025-12-31T23:59:59"));
  }

  @Test
  public void testNavigateToMonthForJanuary() {
    controller.run();
    modelLog.setLength(0);

    mockModel.setEventsForRange(Collections.emptyList());
    controller.navigateToMonth(2025, 1);

    String log = modelLog.toString();
    assertEquals(true, log.contains("getByRange:2025-01-01T00:00"));
    assertEquals(true, log.contains("2025-01-31T23:59:59"));
  }

  @Test
  public void testMultipleSelectCalendarCalls() {
    controller.run();
    viewLog.setLength(0);
    modelLog.setLength(0);

    mockModel.setCurrentCalendarTimezone("UTC");

    controller.selectCalendar("cal1");
    controller.selectCalendar("cal2");
    controller.selectCalendar("cal3");

    String modelLogStr = modelLog.toString();
    assertEquals(true, modelLogStr.contains("useCalendar:cal1"));
    assertEquals(true, modelLogStr.contains("useCalendar:cal2"));
    assertEquals(true, modelLogStr.contains("useCalendar:cal3"));
  }

  @Test
  public void testCreateMultipleEventsRefreshesEachTime() {
    controller.run();
    viewLog.setLength(0);

    LocalDateTime start1 = LocalDateTime.of(2025, 5, 15, 10, 0);
    LocalDateTime end1 = LocalDateTime.of(2025, 5, 15, 11, 0);
    LocalDateTime start2 = LocalDateTime.of(2025, 5, 15, 14, 0);
    LocalDateTime end2 = LocalDateTime.of(2025, 5, 15, 15, 0);

    controller.createSingleEvent("Event1", start1, end1, null, null, null);
    controller.createSingleEvent("Event2", start2, end2, null, null, null);

    String log = viewLog.toString();
    int refreshCount = 0;
    int index = 0;
    while ((index = log.indexOf("refreshMonthView", index)) != -1) {
      refreshCount++;
      index += "refreshMonthView".length();
    }

    assertEquals(true, refreshCount >= 2);
  }

  @Test
  public void testEditSingleEventMultipleTimes() {
    controller.run();
    modelLog.setLength(0);

    LocalDateTime start = LocalDateTime.of(2025, 5, 15, 10, 0);

    controller.editSingleEvent("Meeting", start, "location", "Room 1");
    controller.editSingleEvent("Meeting", start, "description", "Updated");
    controller.editSingleEvent("Meeting", start, "status", "private");

    String log = modelLog.toString();
    int editCount = 0;
    int index = 0;
    while ((index = log.indexOf("editEvent", index)) != -1) {
      editCount++;
      index += "editEvent".length();
    }

    assertEquals("Should perform 3 edits", 3, editCount);
  }

  @Test
  public void testRunInitializationSequence() {
    controller.run();

    String log = viewLog.toString();

    int setFeaturesIdx = log.indexOf("setFeatures called");
    int refreshListIdx = log.indexOf("refreshCalendarList");
    int refreshMonthIdx = log.indexOf("refreshMonthView");
    int refreshDayIdx = log.indexOf("refreshDayView");
    int displayIdx = log.indexOf("display called");

    assertEquals(true, setFeaturesIdx >= 0
        && refreshListIdx >= 0
        && refreshMonthIdx >= 0
        && refreshDayIdx >= 0
        && displayIdx >= 0);

    assertEquals(true, setFeaturesIdx < refreshListIdx
        && refreshListIdx < refreshMonthIdx
        && refreshMonthIdx < refreshDayIdx
        && refreshDayIdx < displayIdx);
  }

  @Test
  public void testNavigateToMonthWithEventsSpanningMonth() throws Exception {
    controller.run();
    viewLog.setLength(0);

    Event event = createEvent("Multi-Day",
        LocalDateTime.of(2025, 5, 30, 10, 0),
        LocalDateTime.of(2025, 6, 2, 11, 0));
    mockModel.setEventsForRange(List.of(event));

    controller.navigateToMonth(2025, 6);

    assertEquals(true, viewLog.toString()
        .contains("refreshMonthView:2025-6,events=1"));
  }

  @Test
  public void testCreateRecurringEventMonday() {
    controller.run();
    viewLog.setLength(0);
    modelLog.setLength(0);

    LocalDateTime start = LocalDateTime.of(2025, 5, 5, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 5, 11, 0);

    controller.createRecurringEvent("Monday Meeting", start, end,
        "M", 3, null, null, null, null);

    assertEquals(true, modelLog.toString()
        .contains("createEventSeries:Monday Meeting"));
    assertEquals(true, modelLog.toString().contains(",M,3,"));
  }

  @Test
  public void testCreateRecurringEventTuesday() {
    controller.run();
    viewLog.setLength(0);
    modelLog.setLength(0);

    LocalDateTime start = LocalDateTime.of(2025, 5, 6, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 6, 11, 0);

    controller.createRecurringEvent("Tuesday Meeting", start, end,
        "T", 3, null, null, null, null);

    assertEquals(true, modelLog.toString()
        .contains("createEventSeries:Tuesday Meeting"));
    assertEquals(true, modelLog.toString().contains(",T,3,"));
  }

  @Test
  public void testCreateRecurringEventWednesday() {
    controller.run();
    viewLog.setLength(0);
    modelLog.setLength(0);

    LocalDateTime start = LocalDateTime.of(2025, 5, 7, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 7, 11, 0);

    controller.createRecurringEvent("Wednesday Meeting", start, end,
        "W", 3, null, null, null, null);

    assertEquals(true, modelLog.toString()
        .contains("createEventSeries:Wednesday Meeting"));
    assertEquals(true, modelLog.toString().contains(",W,3,"));
  }

  @Test
  public void testCreateRecurringEventThursday() {
    controller.run();
    viewLog.setLength(0);
    modelLog.setLength(0);

    LocalDateTime start = LocalDateTime.of(2025, 5, 8, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 8, 11, 0);

    controller.createRecurringEvent("Thursday Meeting", start, end,
        "R", 3, null, null, null, null);

    assertEquals(true, modelLog.toString()
        .contains("createEventSeries:Thursday Meeting"));
    assertEquals(true, modelLog.toString().contains(",R,3,"));
  }

  @Test
  public void testCreateRecurringEventFriday() {
    controller.run();
    viewLog.setLength(0);
    modelLog.setLength(0);

    LocalDateTime start = LocalDateTime.of(2025, 5, 9, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 9, 11, 0);

    controller.createRecurringEvent("Friday Meeting", start, end,
        "F", 3, null, null, null, null);

    assertEquals(true, modelLog.toString()
        .contains("createEventSeries:Friday Meeting"));
    assertEquals(true, modelLog.toString().contains(",F,3,"));
  }

  @Test
  public void testCreateRecurringEventSaturday() {
    controller.run();
    viewLog.setLength(0);
    modelLog.setLength(0);

    LocalDateTime start = LocalDateTime.of(2025, 5, 10, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 10, 11, 0);

    controller.createRecurringEvent("Saturday Meeting", start, end,
        "S", 3, null, null, null, null);

    assertEquals(true, modelLog.toString()
        .contains("createEventSeries:Saturday Meeting"));
    assertEquals(true, modelLog.toString().contains(",S,3,"));
  }

  @Test
  public void testCreateRecurringEventSunday() {
    controller.run();
    viewLog.setLength(0);
    modelLog.setLength(0);

    LocalDateTime start = LocalDateTime.of(2025, 5, 11, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 11, 11, 0);

    controller.createRecurringEvent("Sunday Meeting", start, end,
        "U", 3, null, null, null, null);

    assertEquals(true, modelLog.toString()
        .contains("createEventSeries:Sunday Meeting"));
    assertEquals(true, modelLog.toString().contains(",U,3,"));
  }

  @Test
  public void testCreateRecurringEventWeekdaysOnly() {
    controller.run();
    viewLog.setLength(0);
    modelLog.setLength(0);

    LocalDateTime start = LocalDateTime.of(2025, 5, 5, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 5, 11, 0);

    controller.createRecurringEvent("Weekday Meeting", start, end,
        "MTWRF", 10, null, null, null, null);

    assertEquals(true, modelLog.toString()
        .contains("createEventSeries:Weekday Meeting"));
    assertEquals(true, modelLog.toString().contains(",MTWRF,10,"));
  }

  @Test
  public void testCreateRecurringEventWeekendsOnly() {
    controller.run();
    viewLog.setLength(0);
    modelLog.setLength(0);

    LocalDateTime start = LocalDateTime.of(2025, 5, 10, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 10, 11, 0);

    controller.createRecurringEvent("Weekend Event", start, end,
        "SU", 5, null, null, null, null);

    assertEquals(true, modelLog.toString()
        .contains("createEventSeries:Weekend Event"));
    assertEquals(true, modelLog.toString().contains(",SU,5,"));
  }

  @Test
  public void testCreateRecurringEventAllDays() {
    controller.run();
    viewLog.setLength(0);
    modelLog.setLength(0);

    LocalDateTime start = LocalDateTime.of(2025, 5, 5, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 5, 11, 0);

    controller.createRecurringEvent("Daily Meeting", start, end,
        "MTWRFSU", 7, null, null, null, null);

    assertEquals(true, modelLog.toString()
        .contains("createEventSeries:Daily Meeting"));
    assertEquals(true, modelLog.toString().contains(",MTWRFSU,7,"));
  }

  @Test
  public void testEditEventSubject() {
    controller.run();
    viewLog.setLength(0);
    modelLog.setLength(0);

    LocalDateTime start = LocalDateTime.of(2025, 5, 15, 10, 0);
    controller.editSingleEvent("Old Subject", start, "subject", "New Subject");

    assertEquals(true, modelLog.toString()
        .contains("editEvent:Old Subject,2025-05-15T10:00,subject,New Subject"));
    assertEquals(true, viewLog.toString().contains("showSuccess:Event updated"));
  }

  @Test
  public void testEditEventStart() {
    controller.run();
    viewLog.setLength(0);
    modelLog.setLength(0);

    LocalDateTime start = LocalDateTime.of(2025, 5, 15, 10, 0);
    controller.editSingleEvent("Meeting", start, "start", "2025-05-15T11:00");

    assertEquals(true, modelLog.toString()
        .contains("editEvent:Meeting,2025-05-15T10:00,start,2025-05-15T11:00"));
    assertEquals(true, viewLog.toString().contains("showSuccess:Event updated"));
  }

  @Test
  public void testEditEventEnd() {
    controller.run();
    viewLog.setLength(0);
    modelLog.setLength(0);

    LocalDateTime start = LocalDateTime.of(2025, 5, 15, 10, 0);
    controller.editSingleEvent("Meeting", start, "end", "2025-05-15T12:00");

    assertEquals(true, modelLog.toString()
        .contains("editEvent:Meeting,2025-05-15T10:00,end,2025-05-15T12:00"));
    assertEquals(true, viewLog.toString().contains("showSuccess:Event updated"));
  }

  @Test
  public void testEditEventDescription() {
    controller.run();
    viewLog.setLength(0);
    modelLog.setLength(0);

    LocalDateTime start = LocalDateTime.of(2025, 5, 15, 10, 0);
    controller.editSingleEvent("Meeting", start, "description", "Updated desc");

    assertEquals(true, modelLog.toString()
        .contains("editEvent:Meeting,2025-05-15T10:00,description,Updated desc"));
    assertEquals(true, viewLog.toString().contains("showSuccess:Event updated"));
  }

  @Test
  public void testEditEventLocation() {
    controller.run();
    viewLog.setLength(0);
    modelLog.setLength(0);

    LocalDateTime start = LocalDateTime.of(2025, 5, 15, 10, 0);
    controller.editSingleEvent("Meeting", start, "location", "Room 303");

    assertEquals(true, modelLog.toString()
        .contains("editEvent:Meeting,2025-05-15T10:00,location,Room 303"));
    assertEquals(true, viewLog.toString().contains("showSuccess:Event updated"));
  }

  @Test
  public void testEditEventStatus() {
    controller.run();
    viewLog.setLength(0);
    modelLog.setLength(0);

    LocalDateTime start = LocalDateTime.of(2025, 5, 15, 10, 0);
    controller.editSingleEvent("Meeting", start, "status", "private");

    assertEquals(true, modelLog.toString()
        .contains("editEvent:Meeting,2025-05-15T10:00,status,private"));
    assertEquals(true, viewLog.toString().contains("showSuccess:Event updated"));
  }

  @Test
  public void testEditMultipleEventsSubject() {
    controller.run();
    viewLog.setLength(0);
    modelLog.setLength(0);

    LocalDateTime start = LocalDateTime.of(2025, 5, 15, 10, 0);
    controller.editMultipleEvents("Weekly", start, "subject", "Updated Weekly");

    assertEquals(true, modelLog.toString()
        .contains("editEvents:Weekly,2025-05-15T10:00,subject,Updated Weekly"));
    assertEquals(true, viewLog.toString().contains("showSuccess:Events updated"));
  }

  @Test
  public void testEditMultipleEventsDescription() {
    controller.run();
    viewLog.setLength(0);
    modelLog.setLength(0);

    LocalDateTime start = LocalDateTime.of(2025, 5, 15, 10, 0);
    controller.editMultipleEvents("Weekly", start, "description", "New desc");

    assertEquals(true, modelLog.toString()
        .contains("editEvents:Weekly,2025-05-15T10:00,description,New desc"));
    assertEquals(true, viewLog.toString().contains("showSuccess:Events updated"));
  }

  @Test
  public void testEditMultipleEventsStatus() {
    controller.run();
    viewLog.setLength(0);
    modelLog.setLength(0);

    LocalDateTime start = LocalDateTime.of(2025, 5, 15, 10, 0);
    controller.editMultipleEvents("Weekly", start, "status", "private");

    assertEquals(true, modelLog.toString()
        .contains("editEvents:Weekly,2025-05-15T10:00,status,private"));
    assertEquals(true, viewLog.toString().contains("showSuccess:Events updated"));
  }

  @Test
  public void testNavigateToMonthMarch() {
    controller.run();
    modelLog.setLength(0);

    mockModel.setEventsForRange(Collections.emptyList());
    controller.navigateToMonth(2025, 3);

    String log = modelLog.toString();
    assertEquals(true, log.contains("getByRange:2025-03-01T00:00"));
    assertEquals(true, log.contains("2025-03-31T23:59:59"));
  }

  @Test
  public void testNavigateToMonthApril() {
    controller.run();
    modelLog.setLength(0);

    mockModel.setEventsForRange(Collections.emptyList());
    controller.navigateToMonth(2025, 4);

    String log = modelLog.toString();
    assertEquals(true, log.contains("getByRange:2025-04-01T00:00"));
    assertEquals(true, log.contains("2025-04-30T23:59:59"));
  }

  @Test
  public void testNavigateFromDecemberToJanuary() {
    controller.run();
    viewLog.setLength(0);

    mockModel.setEventsForRange(Collections.emptyList());
    controller.navigateToMonth(2025, 12);
    viewLog.setLength(0);
    controller.navigateToMonth(2026, 1);

    assertEquals(true, viewLog.toString().contains("refreshMonthView:2026-1"));
  }

  @Test
  public void testNavigateFromJanuaryToDecember() {
    controller.run();
    viewLog.setLength(0);

    mockModel.setEventsForRange(Collections.emptyList());
    controller.navigateToMonth(2025, 1);
    viewLog.setLength(0);
    controller.navigateToMonth(2024, 12);

    assertEquals(true, viewLog.toString().contains("refreshMonthView:2024-12"));
  }

  @Test
  public void testSelectCalendarThenCreateEvent() {
    controller.run();
    viewLog.setLength(0);
    modelLog.setLength(0);

    mockModel.setCurrentCalendarTimezone("UTC");
    controller.selectCalendar("work");

    LocalDateTime start = LocalDateTime.of(2025, 5, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 15, 11, 0);
    controller.createSingleEvent("Meeting", start, end, null, null, null);

    assertEquals(true, modelLog.toString().contains("useCalendar:work"));
    assertEquals(true, modelLog.toString().contains("createEvent:Meeting"));
  }

  @Test
  public void testRapidCalendarSwitching() {
    controller.run();
    viewLog.setLength(0);
    modelLog.setLength(0);

    mockModel.setCurrentCalendarTimezone("UTC");

    controller.selectCalendar("cal1");
    controller.selectCalendar("cal2");
    controller.selectCalendar("cal3");
    controller.selectCalendar("cal1");

    String log = modelLog.toString();
    assertEquals(true, log.contains("useCalendar:cal1"));
    assertEquals(true, log.contains("useCalendar:cal2"));
    assertEquals(true, log.contains("useCalendar:cal3"));
  }

  @Test
  public void testEditCalendarNameWhileActive() {
    controller.run();
    viewLog.setLength(0);
    modelLog.setLength(0);

    mockModel.setCurrentCalendarTimezone("UTC");
    controller.selectCalendar("oldname");
    viewLog.setLength(0);

    controller.editCalendarProperty("oldname", "name", "newname");

    assertEquals(true, viewLog.toString().contains("current=newname"));
  }

  @Test
  public void testEditCalendarTimezoneRefreshesMonthView() {
    controller.run();
    viewLog.setLength(0);

    mockView.setCurrentMonth(YearMonth.of(2025, 6));
    mockModel.setCurrentCalendarTimezone("America/New_York");

    controller.editCalendarProperty("work", "timezone", "America/New_York");

    assertEquals(true, viewLog.toString().contains("refreshMonthView:2025-6"));
  }

  @Test
  public void testSelectDateAfterNavigatingToMonth() {
    controller.run();
    viewLog.setLength(0);
    modelLog.setLength(0);

    mockModel.setEventsForRange(Collections.emptyList());
    controller.navigateToMonth(2025, 7);

    viewLog.setLength(0);
    mockModel.setEventsForDate(Collections.emptyList());
    controller.selectDate(LocalDate.of(2025, 7, 15));

    assertEquals(true, viewLog.toString().contains("refreshDayView:2025-07-15"));
  }

  @Test
  public void testCreateEventRefreshesCurrentMonth() {
    controller.run();
    viewLog.setLength(0);

    mockView.setCurrentMonth(YearMonth.of(2025, 5));

    LocalDateTime start = LocalDateTime.of(2025, 5, 20, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 20, 11, 0);
    controller.createSingleEvent("Event", start, end, null, null, null);

    assertEquals(true, viewLog.toString().contains("refreshMonthView:2025-5"));
  }

  @Test
  public void testCreateEventRefreshesSelectedDate() {
    controller.run();
    viewLog.setLength(0);

    mockView.setSelectedDate(LocalDate.of(2025, 5, 15));

    LocalDateTime start = LocalDateTime.of(2025, 5, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 15, 11, 0);
    controller.createSingleEvent("Event", start, end, null, null, null);

    assertEquals(true, viewLog.toString().contains("refreshDayView:2025-05-15"));
  }

  @Test
  public void testEditEventRefreshesCurrentMonth() {
    controller.run();
    viewLog.setLength(0);

    mockView.setCurrentMonth(YearMonth.of(2025, 6));

    LocalDateTime start = LocalDateTime.of(2025, 6, 10, 10, 0);
    controller.editSingleEvent("Meeting", start, "location", "Room A");

    assertEquals(true, viewLog.toString().contains("refreshMonthView:2025-6"));
  }

  @Test
  public void testEditEventRefreshesSelectedDate() {
    controller.run();
    viewLog.setLength(0);

    mockView.setSelectedDate(LocalDate.of(2025, 5, 15));

    LocalDateTime start = LocalDateTime.of(2025, 5, 15, 10, 0);
    controller.editSingleEvent("Meeting", start, "description", "Updated");

    assertEquals(true, viewLog.toString().contains("refreshDayView:2025-05-15"));
  }

  @Test
  public void testSelectCalendarRefreshesCurrentMonthView() {
    controller.run();
    viewLog.setLength(0);

    mockView.setCurrentMonth(YearMonth.of(2025, 8));
    mockModel.setCurrentCalendarTimezone("UTC");

    controller.selectCalendar("work");

    assertEquals(true, viewLog.toString().contains("refreshMonthView:2025-8"));
  }

  @Test
  public void testSelectCalendarRefreshesCurrentDayView() {
    controller.run();
    viewLog.setLength(0);

    mockView.setSelectedDate(LocalDate.of(2025, 8, 20));
    mockModel.setCurrentCalendarTimezone("UTC");

    controller.selectCalendar("work");

    assertEquals(true, viewLog.toString().contains("refreshDayView:2025-08-20"));
  }

  @Test
  public void testNavigateToMonthWithNoEvents() {
    controller.run();
    viewLog.setLength(0);
    modelLog.setLength(0);

    mockModel.setEventsForRange(Collections.emptyList());

    controller.navigateToMonth(2025, 5);

    assertEquals(true, viewLog.toString().contains("refreshMonthView:2025-5,events=0"));
  }

  @Test
  public void testSelectDateWithNoEvents() {
    controller.run();
    viewLog.setLength(0);
    modelLog.setLength(0);

    LocalDate date = LocalDate.of(2025, 5, 15);
    mockModel.setEventsForDate(Collections.emptyList());

    controller.selectDate(date);

    assertEquals(true, viewLog.toString()
        .contains("refreshDayView:2025-05-15,events=0"));
  }

  @Test
  public void testCreateRecurringEventRefreshesCorrectMonth() {
    controller.run();
    viewLog.setLength(0);

    LocalDateTime start = LocalDateTime.of(2025, 7, 5, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 7, 5, 11, 0);

    controller.createRecurringEvent("Weekly", start, end,
        "W", 4, null, null, null, null);

    assertEquals(true, viewLog.toString().contains("refreshMonthView:2025-7"));
  }

  @Test
  public void testCreateRecurringEventRefreshesCorrectDate() {
    controller.run();
    viewLog.setLength(0);

    LocalDateTime start = LocalDateTime.of(2025, 7, 5, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 7, 5, 11, 0);

    controller.createRecurringEvent("Weekly", start, end,
        "W", 4, null, null, null, null);

    assertEquals(true, viewLog.toString().contains("refreshDayView:2025-07-05"));
  }

  @Test
  public void testEditMultipleEventsRefreshesMonth() {
    controller.run();
    viewLog.setLength(0);

    LocalDateTime start = LocalDateTime.of(2025, 9, 5, 10, 0);
    controller.editMultipleEvents("Series", start, "location", "Room Z");

    assertEquals(true, viewLog.toString().contains("refreshMonthView:2025-9"));
  }

  @Test
  public void testEditMultipleEventsRefreshesDate() {
    controller.run();
    viewLog.setLength(0);

    LocalDateTime start = LocalDateTime.of(2025, 9, 5, 10, 0);
    controller.editMultipleEvents("Series", start, "description", "Updated");

    assertEquals(true, viewLog.toString().contains("refreshDayView:2025-09-05"));
  }

  @Test
  public void testGetAvailableCalendarsAfterMultipleCreations() {
    controller.run();

    controller.createCalendar("cal1", "UTC");
    controller.createCalendar("cal2", "UTC");
    controller.createCalendar("cal3", "UTC");

    String[] calendars = controller.getAvailableCalendars();

    assertEquals("Should have 4 calendars", 4, calendars.length);
  }

  @Test
  public void testGetAvailableCalendarsInitialState() {
    controller.run();

    String[] calendars = controller.getAvailableCalendars();

    assertEquals("Should have 1 default calendar", 1, calendars.length);
  }

  @Test
  public void testRefreshViewsAfterCalendarEdit() {
    controller.run();
    viewLog.setLength(0);

    mockView.setCurrentMonth(YearMonth.of(2025, 5));
    mockModel.setCurrentCalendarTimezone("UTC");

    controller.editCalendarProperty("work", "timezone", "UTC");

    assertEquals(true, viewLog.toString().contains("refreshCalendarList"));
    assertEquals(true, viewLog.toString().contains("refreshMonthView"));
  }

  @Test
  public void testCreateCalendarRefreshesListOnly() {
    controller.run();
    viewLog.setLength(0);

    controller.createCalendar("newcal", "UTC");

    assertEquals(true, viewLog.toString().contains("refreshCalendarList"));
    assertEquals(false, viewLog.toString().contains("refreshMonthView"));
    assertEquals(false, viewLog.toString().contains("refreshDayView"));
  }

  @Test
  public void testEditCalendarNameRefreshesListOnly() {
    controller.run();
    viewLog.setLength(0);

    mockModel.setCurrentCalendarTimezone("UTC");
    controller.editCalendarProperty("old", "name", "new");

    assertEquals(true, viewLog.toString().contains("refreshCalendarList"));
  }

  @Test
  public void testSuccessMessageForCreateCalendar() {
    controller.run();
    viewLog.setLength(0);

    controller.createCalendar("test", "UTC");

    assertEquals(true, viewLog.toString()
        .contains("showSuccess:Calendar created: test"));
  }

  @Test
  public void testSuccessMessageForEditCalendar() {
    controller.run();
    viewLog.setLength(0);

    mockModel.setCurrentCalendarTimezone("UTC");
    controller.editCalendarProperty("work", "name", "office");

    assertEquals(true, viewLog.toString().contains("showSuccess:Calendar updated"));
  }

  @Test
  public void testSuccessMessageForCopyEvent() {
    controller.run();
    viewLog.setLength(0);

    LocalDateTime eventStart = LocalDateTime.of(2025, 5, 15, 10, 0);
    LocalDateTime targetStart = LocalDateTime.of(2025, 5, 20, 10, 0);

    controller.copySingleEvent("Event", eventStart, "target", targetStart);

    assertEquals(true, viewLog.toString()
        .contains("showSuccess:Event copied to target"));
  }

  @Test
  public void testSuccessMessageForCopyEventsOnDate() {
    controller.run();
    viewLog.setLength(0);

    controller.copyEventsOnDate(LocalDate.of(2025, 5, 15), "target",
        LocalDate.of(2025, 5, 20));

    assertEquals(true, viewLog.toString()
        .contains("showSuccess:Events copied to target"));
  }

  @Test
  public void testSuccessMessageForCopyEventsInRange() {
    controller.run();
    viewLog.setLength(0);

    controller.copyEventsInRange(LocalDate.of(2025, 5, 1),
        LocalDate.of(2025, 5, 7), "target", LocalDate.of(2025, 6, 1));

    assertEquals(true, viewLog.toString()
        .contains("showSuccess:Events copied to target"));
  }

  @Test
  public void testNavigateToMonthJune() {
    controller.run();
    modelLog.setLength(0);

    mockModel.setEventsForRange(Collections.emptyList());
    controller.navigateToMonth(2025, 6);

    String log = modelLog.toString();
    assertEquals(true, log.contains("getByRange:2025-06-01T00:00"));
    assertEquals(true, log.contains("2025-06-30T23:59:59"));
  }

  @Test
  public void testNavigateToMonthSeptember() {
    controller.run();
    modelLog.setLength(0);

    mockModel.setEventsForRange(Collections.emptyList());
    controller.navigateToMonth(2025, 9);

    String log = modelLog.toString();
    assertEquals(true, log.contains("getByRange:2025-09-01T00:00"));
    assertEquals(true, log.contains("2025-09-30T23:59:59"));
  }

  @Test
  public void testNavigateToMonthNovember() {
    controller.run();
    modelLog.setLength(0);

    mockModel.setEventsForRange(Collections.emptyList());
    controller.navigateToMonth(2025, 11);

    String log = modelLog.toString();
    assertEquals(true, log.contains("getByRange:2025-11-01T00:00"));
    assertEquals(true, log.contains("2025-11-30T23:59:59"));
  }

  @Test
  public void testCreateEventWithAllOptionalFields() {
    controller.run();
    viewLog.setLength(0);
    modelLog.setLength(0);

    LocalDateTime start = LocalDateTime.of(2025, 5, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 15, 11, 0);

    controller.createSingleEvent("Full Event", start, end,
        "Full description", "Full location", "private");

    String log = modelLog.toString();
    assertEquals(true, log.contains("createEvent:Full Event"));
    assertEquals(true, log.contains("description,Full description"));
    assertEquals(true, log.contains("location,Full location"));
    assertEquals(true, log.contains("status,private"));
  }

  @Test
  public void testCreateRecurringEventWithAllOptionalFields() {
    controller.run();
    viewLog.setLength(0);
    modelLog.setLength(0);

    LocalDateTime start = LocalDateTime.of(2025, 5, 5, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 5, 11, 0);

    controller.createRecurringEvent("Full Series", start, end,
        "MW", 3, null, "Series description", "Series location", "private");

    String log = modelLog.toString();
    assertEquals(true, log.contains("createEventSeries:Full Series"));
    assertEquals(true, log.contains("description,Series description"));
    assertEquals(true, log.contains("location,Series location"));
    assertEquals(true, log.contains("status,private"));
  }

  @Test
  public void testSelectDateMultipleTimes() {
    controller.run();
    viewLog.setLength(0);
    modelLog.setLength(0);

    mockModel.setEventsForDate(Collections.emptyList());

    controller.selectDate(LocalDate.of(2025, 5, 1));
    controller.selectDate(LocalDate.of(2025, 5, 2));
    controller.selectDate(LocalDate.of(2025, 5, 3));

    String log = modelLog.toString();
    assertEquals(true, log.contains("getByDate:2025-05-01"));
    assertEquals(true, log.contains("getByDate:2025-05-02"));
    assertEquals(true, log.contains("getByDate:2025-05-03"));
  }

  @Test
  public void testNavigateToMonthMultipleTimes() {
    controller.run();
    viewLog.setLength(0);

    mockModel.setEventsForRange(Collections.emptyList());

    controller.navigateToMonth(2025, 1);
    controller.navigateToMonth(2025, 6);
    controller.navigateToMonth(2025, 12);

    String log = viewLog.toString();
    assertEquals(true, log.contains("refreshMonthView:2025-1"));
    assertEquals(true, log.contains("refreshMonthView:2025-6"));
    assertEquals(true, log.contains("refreshMonthView:2025-12"));
  }

  @Test
  public void testCreateCalendarWithDifferentTimezones() {
    controller.run();
    modelLog.setLength(0);

    controller.createCalendar("est", "America/New_York");
    controller.createCalendar("pst", "America/Los_Angeles");
    controller.createCalendar("utc", "UTC");

    String log = modelLog.toString();
    assertEquals(true, log.contains("createCalendar:est,America/New_York"));
    assertEquals(true, log.contains("createCalendar:pst,America/Los_Angeles"));
    assertEquals(true, log.contains("createCalendar:utc,UTC"));
  }

  @Test
  public void testRefreshCalendarListWithTimezone() {
    controller.run();
    viewLog.setLength(0);

    mockModel.setCurrentCalendarTimezone("America/New_York");
    controller.selectCalendar("work");

    String log = viewLog.toString();
    assertEquals(true, log.contains("timezone=America/New_York"));
    assertEquals(true, log.contains("current=work"));
  }

  @Test
  public void testRefreshCalendarListAfterNameChange() {
    controller.run();
    viewLog.setLength(0);

    mockModel.setCurrentCalendarTimezone("UTC");
    controller.editCalendarProperty("old", "name", "new");

    String log = viewLog.toString();
    assertEquals(true, log.contains("refreshCalendarList"));
    assertEquals(true, log.contains("current=new"));
  }

  @Test
  public void testRefreshCalendarListAfterTimezoneChange() {
    controller.run();
    viewLog.setLength(0);

    mockView.setCurrentMonth(YearMonth.of(2025, 5));
    mockModel.setCurrentCalendarTimezone("Europe/London");

    controller.editCalendarProperty("work", "timezone", "Europe/London");

    String log = viewLog.toString();
    assertEquals(true, log.contains("refreshCalendarList"));
    assertEquals(true, log.contains("timezone=Europe/London"));
  }

  @Test
  public void testEditMultipleEventsEnd() {
    controller.run();
    viewLog.setLength(0);
    modelLog.setLength(0);

    LocalDateTime start = LocalDateTime.of(2025, 5, 15, 10, 0);
    controller.editMultipleEvents("Weekly", start, "end", "2025-05-15T12:00");

    assertEquals(true, modelLog.toString()
        .contains("editEvents:Weekly,2025-05-15T10:00,end,2025-05-15T12:00"));
    assertEquals(true, viewLog.toString().contains("showSuccess:Events updated"));
  }

  @Test
  public void testEditMultipleEventsStart() {
    controller.run();
    viewLog.setLength(0);
    modelLog.setLength(0);

    LocalDateTime start = LocalDateTime.of(2025, 5, 15, 10, 0);
    controller.editMultipleEvents("Weekly", start, "start", "2025-05-15T11:00");

    assertEquals(true, modelLog.toString()
        .contains("editEvents:Weekly,2025-05-15T10:00,start,2025-05-15T11:00"));
    assertEquals(true, viewLog.toString().contains("showSuccess:Events updated"));
  }

  @Test
  public void testCopyEventsOnDatePreservesTime() {
    controller.run();
    modelLog.setLength(0);

    LocalDate sourceDate = LocalDate.of(2025, 5, 15);
    LocalDate targetDate = LocalDate.of(2025, 6, 20);

    controller.copyEventsOnDate(sourceDate, "target", targetDate);

    assertEquals(true, modelLog.toString()
        .contains("copyEventsOnDate:2025-05-15,target,2025-06-20"));
  }

  @Test
  public void testCopyEventsInRangePreservesOffset() {
    controller.run();
    modelLog.setLength(0);

    LocalDate start = LocalDate.of(2025, 5, 1);
    LocalDate end = LocalDate.of(2025, 5, 7);
    LocalDate targetStart = LocalDate.of(2025, 6, 10);

    controller.copyEventsInRange(start, end, "target", targetStart);

    assertEquals(true, modelLog.toString()
        .contains("copyEventsInRange:2025-05-01,2025-05-07,target,2025-06-10"));
  }

  @Test
  public void testCreateSingleEventWithWhitespaceInFields() {
    controller.run();
    modelLog.setLength(0);

    LocalDateTime start = LocalDateTime.of(2025, 5, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 15, 11, 0);

    controller.createSingleEvent("Meeting", start, end,
        "  description  ", "  location  ", null);

    String log = modelLog.toString();
    assertEquals(true, log.contains("description,  description  "));
    assertEquals(true, log.contains("location,  location  "));
  }

  @Test
  public void testRunInitializesWithSystemTimezone() {
    controller.run();

    String log = modelLog.toString();
    assertEquals(true, log.contains("createCalendar:Default"));
  }

  @Test
  public void testRunDisplaysViewAfterInitialization() {
    controller.run();

    String log = viewLog.toString();
    int displayIdx = log.indexOf("display called");
    int refreshIdx = log.lastIndexOf("refresh");

    assertEquals(true, displayIdx > refreshIdx);
  }

  @Test
  public void testCreateRecurringEventError() {
    controller.run();
    viewLog.setLength(0);

    mockModel = new MockCalendarSystemModel(modelLog) {
      @Override
      public void createEventSeries(String subject, LocalDateTime start,
                                    LocalDateTime end, String weekdays, int occurrences,
                                    LocalDateTime untilDate) {
        throw new IllegalArgumentException("Invalid series");
      }
    };
    controller = new CalendarGuiController(mockModel, mockView);
    controller.run();

    LocalDateTime start = LocalDateTime.of(2025, 5, 5, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 5, 11, 0);

    controller.createRecurringEvent("Series", start, end,
        "M", 3, null, null, null, null);

    assertEquals(true, viewLog.toString().contains("showError:Invalid series"));
  }

  @Test
  public void testEditMultipleEventsError() {
    controller.run();
    viewLog.setLength(0);

    mockModel = new MockCalendarSystemModel(modelLog) {
      @Override
      public void editEvents(String subject, LocalDateTime start,
                             String property, String newValue) {
        throw new IllegalArgumentException("Cannot edit events");
      }
    };
    controller = new CalendarGuiController(mockModel, mockView);
    controller.run();

    LocalDateTime start = LocalDateTime.of(2025, 5, 15, 10, 0);
    controller.editMultipleEvents("Weekly", start, "location", "Room");

    assertEquals(true, viewLog.toString().contains("showError:Cannot edit events"));
  }

  @Test
  public void testNavigateToMonthMay() {
    controller.run();
    modelLog.setLength(0);

    mockModel.setEventsForRange(Collections.emptyList());
    controller.navigateToMonth(2025, 5);

    String log = modelLog.toString();
    assertEquals(true, log.contains("getByRange:2025-05-01T00:00"));
    assertEquals(true, log.contains("2025-05-31T23:59:59"));
  }

  @Test
  public void testNavigateToMonthAugust() {
    controller.run();
    modelLog.setLength(0);

    mockModel.setEventsForRange(Collections.emptyList());
    controller.navigateToMonth(2025, 8);

    String log = modelLog.toString();
    assertEquals(true, log.contains("getByRange:2025-08-01T00:00"));
    assertEquals(true, log.contains("2025-08-31T23:59:59"));
  }

  @Test
  public void testNavigateToMonthOctober() {
    controller.run();
    modelLog.setLength(0);

    mockModel.setEventsForRange(Collections.emptyList());
    controller.navigateToMonth(2025, 10);

    String log = modelLog.toString();
    assertEquals(true, log.contains("getByRange:2025-10-01T00:00"));
    assertEquals(true, log.contains("2025-10-31T23:59:59"));
  }

  @Test
  public void testSelectDateInDifferentMonth() {
    controller.run();
    viewLog.setLength(0);
    modelLog.setLength(0);

    mockModel.setEventsForDate(Collections.emptyList());

    controller.selectDate(LocalDate.of(2025, 7, 25));

    assertEquals(true, modelLog.toString().contains("getByDate:2025-07-25"));
    assertEquals(true, viewLog.toString()
        .contains("refreshDayView:2025-07-25,events=0"));
  }

  @Test
  public void testCreateEventThenNavigateToSameMonth() {
    controller.run();
    viewLog.setLength(0);

    LocalDateTime start = LocalDateTime.of(2025, 5, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 15, 11, 0);

    controller.createSingleEvent("Event", start, end, null, null, null);
    viewLog.setLength(0);

    mockModel.setEventsForRange(Collections.emptyList());
    controller.navigateToMonth(2025, 5);

    assertEquals(true, viewLog.toString().contains("refreshMonthView:2025-5"));
  }

  @Test
  public void testEditEventThenSelectSameDate() {
    controller.run();
    viewLog.setLength(0);

    LocalDateTime start = LocalDateTime.of(2025, 5, 15, 10, 0);
    controller.editSingleEvent("Meeting", start, "location", "Room");

    viewLog.setLength(0);
    mockModel.setEventsForDate(Collections.emptyList());
    controller.selectDate(LocalDate.of(2025, 5, 15));

    assertEquals(true, viewLog.toString().contains("refreshDayView:2025-05-15"));
  }

  @Test
  public void testMultipleOperationsOnSameCalendar() {
    controller.run();
    modelLog.setLength(0);

    mockModel.setCurrentCalendarTimezone("UTC");
    controller.selectCalendar("work");

    LocalDateTime start = LocalDateTime.of(2025, 5, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 15, 11, 0);
    controller.createSingleEvent("Event1", start, end, null, null, null);

    LocalDateTime start2 = LocalDateTime.of(2025, 5, 16, 10, 0);
    LocalDateTime end2 = LocalDateTime.of(2025, 5, 16, 11, 0);
    controller.createSingleEvent("Event2", start2, end2, null, null, null);

    controller.editSingleEvent("Event1", start, "location", "Room A");

    String log = modelLog.toString();
    assertEquals(true, log.contains("useCalendar:work"));
    assertEquals(true, log.contains("createEvent:Event1"));
    assertEquals(true, log.contains("createEvent:Event2"));
    assertEquals(true, log.contains("editEvent:Event1"));
  }

  @Test
  public void testViewRefreshSequenceAfterEventCreation() {
    controller.run();
    viewLog.setLength(0);

    LocalDateTime start = LocalDateTime.of(2025, 5, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 15, 11, 0);

    controller.createSingleEvent("Event", start, end, null, null, null);

    String log = viewLog.toString();
    int monthIdx = log.indexOf("refreshMonthView");
    int dayIdx = log.indexOf("refreshDayView");

    assertEquals(true, monthIdx >= 0);
    assertEquals(true, dayIdx >= 0);
    assertEquals(true, monthIdx < dayIdx);
  }

  @Test
  public void testViewRefreshSequenceAfterEventEdit() {
    controller.run();
    viewLog.setLength(0);

    LocalDateTime start = LocalDateTime.of(2025, 5, 15, 10, 0);
    controller.editSingleEvent("Event", start, "subject", "Updated");

    String log = viewLog.toString();
    int monthIdx = log.indexOf("refreshMonthView");
    int dayIdx = log.indexOf("refreshDayView");

    assertEquals(true, monthIdx >= 0);
    assertEquals(true, dayIdx >= 0);
    assertEquals(true, monthIdx < dayIdx);
  }

  @Test
  public void testGetAvailableCalendarsReturnsArrayCopy() {
    controller.run();

    String[] calendars1 = controller.getAvailableCalendars();
    String[] calendars2 = controller.getAvailableCalendars();

    assertEquals(true, calendars1.length == calendars2.length);
    assertEquals(false, calendars1 == calendars2);
  }

  @Test
  public void testRunInitializesDefaultTimezoneFromSystem() {
    controller.run();

    String log = modelLog.toString();
    assertEquals(true, log.contains("createCalendar:Default,"));
  }

  @Test
  public void testSelectCalendarUpdatesTimezoneDisplay() {
    controller.run();
    viewLog.setLength(0);

    mockModel.setCurrentCalendarTimezone("Asia/Tokyo");
    controller.selectCalendar("tokyo");

    assertEquals(true, viewLog.toString().contains("timezone=Asia/Tokyo"));
  }

  @Test
  public void testEditCalendarPropertyUpdatesDisplay() {
    controller.run();
    viewLog.setLength(0);

    mockModel.setCurrentCalendarTimezone("Europe/Paris");
    controller.editCalendarProperty("work", "timezone", "Europe/Paris");

    assertEquals(true, viewLog.toString().contains("refreshCalendarList"));
  }

  @Test
  public void testCreateEventOnDifferentDates() {
    controller.run();
    modelLog.setLength(0);

    LocalDateTime start1 = LocalDateTime.of(2025, 5, 1, 10, 0);
    LocalDateTime end1 = LocalDateTime.of(2025, 5, 1, 11, 0);

    LocalDateTime start2 = LocalDateTime.of(2025, 5, 15, 10, 0);
    LocalDateTime end2 = LocalDateTime.of(2025, 5, 15, 11, 0);

    LocalDateTime start3 = LocalDateTime.of(2025, 5, 30, 10, 0);
    LocalDateTime end3 = LocalDateTime.of(2025, 5, 30, 11, 0);

    controller.createSingleEvent("Event1", start1, end1, null, null, null);
    controller.createSingleEvent("Event2", start2, end2, null, null, null);
    controller.createSingleEvent("Event3", start3, end3, null, null, null);

    String log = modelLog.toString();
    assertEquals(true, log.contains("createEvent:Event1"));
    assertEquals(true, log.contains("createEvent:Event2"));
    assertEquals(true, log.contains("createEvent:Event3"));
  }

  @Test
  public void testNavigateAcrossMultipleYears() {
    controller.run();
    viewLog.setLength(0);

    mockModel.setEventsForRange(Collections.emptyList());

    controller.navigateToMonth(2024, 12);
    controller.navigateToMonth(2025, 1);
    controller.navigateToMonth(2026, 1);

    String log = viewLog.toString();
    assertEquals(true, log.contains("refreshMonthView:2024-12"));
    assertEquals(true, log.contains("refreshMonthView:2025-1"));
    assertEquals(true, log.contains("refreshMonthView:2026-1"));
  }

  @Test
  public void testSelectDateAcrossMultipleMonths() {
    controller.run();
    modelLog.setLength(0);

    mockModel.setEventsForDate(Collections.emptyList());

    controller.selectDate(LocalDate.of(2025, 1, 15));
    controller.selectDate(LocalDate.of(2025, 6, 15));
    controller.selectDate(LocalDate.of(2025, 12, 15));

    String log = modelLog.toString();
    assertEquals(true, log.contains("getByDate:2025-01-15"));
    assertEquals(true, log.contains("getByDate:2025-06-15"));
    assertEquals(true, log.contains("getByDate:2025-12-15"));
  }
}