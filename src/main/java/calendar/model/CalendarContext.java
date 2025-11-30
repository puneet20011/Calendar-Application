package calendar.model;

import java.time.ZoneId;

/**
 * Context wrapper for a calendar instance.
 * Wraps a single calendar with the context of timezone and name.
 * Package-private utility class only used by CalendarSystem.
 */
class CalendarContext {
  private String name;
  private ZoneId timezone;
  private final IcalendarModel model;

  /**
   * Creates a calendar context.
   *
   * @param name calendar name
   * @param timezone calendar timezone
   */
  CalendarContext(String name, ZoneId timezone) {
    this.name = name;
    this.timezone = timezone;
    this.model = new CalendarModel();
  }

  String getName() {
    return name;
  }

  void setName(String name) {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Calendar name cannot be empty");
    }
    this.name = name;
  }

  ZoneId getTimezone() {
    return timezone;
  }

  void setTimezone(ZoneId timezone) {
    if (timezone == null) {
      throw new IllegalArgumentException("Timezone cannot be null");
    }
    this.timezone = timezone;
  }

  /**
   * Gets the underlying calendar model for this calendar.
   * The model handles all event operations.
   *
   * @return the calendar model instance
   */
  IcalendarModel getModel() {
    return model;
  }
}