# Calendar Application - Design Changes

## Overview
This highlights changes from 4th to 5th assignment.
This document outlines the design changes made when extending the calendar application from a single-calendar system to a multi-calendar system with timezone support.

## Design Changes by MVC Layer

### Model Layer Changes

#### * New Wrapper Class: CalendarContext

This was added to wrap the existing Calendar Model(Representing a Single Calendar and its operations) with the context of timeZone and name. This package-private class contains the calendar name, timezone, and IcalendarModel instance, serving as an internal implementation detail that is not exposed to external code.
This Change by using a Wrapper class allowed us to move forward by reusing all the previous code as it is.

#### * New Interface: CalendarSystemModel

Created CalendarSystemModel interface that extends IcalendarModel to provide a single interface for the controller. The controller now only has to interact with the CalendarSystemModel interface, which represents the complete calendar system.

#### * CalendarSystem

Added CalendarSystem class implementing CalendarSystemModel to manage multiple calendars and delegate operations to appropriate calendar instances. This implementation follows the Facade Pattern and is responsible for maintaining a collection of calendars using a Map<String, CalendarContext>, tracking the currently active calendar, enforcing the that a calendar must be in use rule, and delegating event operations to the active calendar.


#### * New Abstraction: IEvent Interface

Created IEvent interface that represents a Single Event. 
This allowed us to use the Interface Type abstraction while returning a list of events to the controller for display.


#### * New Utility Classes added

New Utility Package-Private classes are added that take care of copying events from one calendar to another while accounting for timezone changes on events and coverting timezones of events in case of timezone changes for a calendar.

#### * Moved the Export Logic out of the model

Now the export logic is moved to the controller, so the model gives all the calendar related data to the controller and controller will use that data for the export as ical or csv.

#### * No Changes to Existing Classes
No Changes to Our existing model utility classes were needed.

### View Layer Changes

#### No Changes
No Changes had to be made to the view. Model changes did not require any changes to the view.

### Controller Layer Changes

#### * Added new Commands
New Command patterns were added to match the new functionality and also their handler were added that call the correct method in model based on user commands.

#### * Export Logic 
New as well as all export are now moved to the controller so controller queries that model for that calendar data and uses that data for the export functionality.
It identifies the export type by file extension gets data from the model and formats it for the export.
