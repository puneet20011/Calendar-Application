package calendar.view;

import calendar.controller.CalendarFeatures;
import calendar.model.Ievent;
import calendar.view.dialogs.CopyEventsDialog;
import calendar.view.dialogs.CreateCalendarDialog;
import calendar.view.dialogs.CreateEventDialog;
import calendar.view.dialogs.EditCalendarDialog;
import calendar.view.dialogs.EditEventDialog;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

/**
 * Swing GUI - now cleaner with extracted dialogs.
 */
public class CalendarSwingView extends JFrame implements IcalendarGuiView {

  private CalendarFeatures features;

  private JComboBox<String> calendarSelector;
  private JLabel monthLabel;
  private JLabel currentCalendarLabel;
  private JPanel monthGrid;
  private JPanel eventsPanel;

  private YearMonth currentMonth;
  private LocalDate selectedDate;

  private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

  private static final Color[] CALENDAR_COLORS = {
      new Color(173, 216, 230),
      new Color(255, 218, 185),
      new Color(221, 160, 221),
      new Color(152, 251, 152),
      new Color(255, 228, 181)
  };

  /**
   * Constructs a CalendarSwingView with default settings.
   */
  public CalendarSwingView() {
    super("Calendar Application");
    this.currentMonth = YearMonth.now();
    this.selectedDate = LocalDate.now();

    setupUi();

    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(1100, 750);
    setLocationRelativeTo(null);
  }

  private void setupUi() {
    setLayout(new BorderLayout(10, 10));

    add(createTopPanel(), BorderLayout.NORTH);
    add(createCenterPanel(), BorderLayout.CENTER);
    add(createRightPanel(), BorderLayout.EAST);
  }

  private JPanel createTopPanel() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));


    JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
    leftPanel.add(new JLabel("Calendar:"));
    calendarSelector = new JComboBox<>();
    calendarSelector.setPreferredSize(new Dimension(150, 25));
    leftPanel.add(calendarSelector);

    JButton newCalBtn = new JButton("New Calendar");
    newCalBtn.addActionListener(e -> CreateCalendarDialog.show(this, features));
    leftPanel.add(newCalBtn);

    JButton editCalBtn = new JButton("Edit Calendar");
    editCalBtn.addActionListener(e -> EditCalendarDialog.show(this, features,
        (String) calendarSelector.getSelectedItem()));
    leftPanel.add(editCalBtn);


    currentCalendarLabel = new JLabel();
    currentCalendarLabel.setOpaque(true);
    currentCalendarLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
    rightPanel.add(currentCalendarLabel);

    JPanel firstRow = new JPanel(new BorderLayout(10, 0));
    firstRow.add(leftPanel, BorderLayout.WEST);
    firstRow.add(rightPanel, BorderLayout.EAST);

    JPanel secondRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));

    JButton prevBtn = new JButton("<- Previous");
    prevBtn.addActionListener(e -> previousMonth());
    secondRow.add(prevBtn);

    monthLabel = new JLabel("", SwingConstants.CENTER);
    monthLabel.setFont(new Font("Arial", Font.BOLD, 16));
    monthLabel.setPreferredSize(new Dimension(150, 25));
    secondRow.add(monthLabel);

    JButton nextBtn = new JButton("Next ->");
    nextBtn.addActionListener(e -> nextMonth());
    secondRow.add(nextBtn);

    panel.add(firstRow);
    panel.add(secondRow);

    return panel;
  }

  private JPanel createCenterPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createTitledBorder("Month View"));

    monthGrid = new JPanel(new GridLayout(0, 7, 3, 3));
    monthGrid.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    panel.add(monthGrid, BorderLayout.CENTER);

    return panel;
  }

  private JPanel createRightPanel() {
    JPanel panel = new JPanel(new BorderLayout(5, 5));
    panel.setPreferredSize(new Dimension(320, 0));
    panel.setBorder(BorderFactory.createTitledBorder("Events on Selected Day"));

    eventsPanel = new JPanel();
    eventsPanel.setLayout(new BoxLayout(eventsPanel, BoxLayout.Y_AXIS));
    JScrollPane scroll = new JScrollPane(eventsPanel);
    panel.add(scroll, BorderLayout.CENTER);

    JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 5, 5));

    JButton createEventBtn = new JButton("Create Event/Series on Selected Day");
    createEventBtn.addActionListener(e -> CreateEventDialog.show(this, features, selectedDate));
    buttonPanel.add(createEventBtn);

    JButton copyBtn = new JButton("Copy Events...");
    copyBtn.addActionListener(e -> CopyEventsDialog.showMenu(this, features, selectedDate));
    buttonPanel.add(copyBtn);

    panel.add(buttonPanel, BorderLayout.SOUTH);

    return panel;
  }

  @Override
  public void setFeatures(CalendarFeatures features) {
    this.features = features;
    calendarSelector.addActionListener(e -> {
      String selected = (String) calendarSelector.getSelectedItem();
      if (selected != null && features != null) {
        features.selectCalendar(selected);
      }
    });
  }

  @Override
  public void display() {
    setVisible(true);
  }

  @Override
  public LocalDate getSelectedDate() {
    return selectedDate;
  }

  @Override
  public YearMonth getCurrentMonth() {
    return currentMonth;
  }

  @Override
  public void refreshCalendarList(Set<String> calendarNames, String currentCalendar,
                                  String timezone) {
    calendarSelector.removeAllItems();
    for (String name : calendarNames) {
      calendarSelector.addItem(name);
    }
    if (currentCalendar != null) {
      calendarSelector.setSelectedItem(currentCalendar);

      int index = getCalendarIndex(currentCalendar, calendarNames);
      Color color = CALENDAR_COLORS[index % CALENDAR_COLORS.length];
      currentCalendarLabel.setBackground(color);

      if (timezone != null && !timezone.isEmpty()) {
        currentCalendarLabel.setText("Active: " + currentCalendar + " (" + timezone + ")");
      } else {
        currentCalendarLabel.setText("Active: " + currentCalendar);
      }
    }
  }

  @Override
  public void refreshMonthView(int year, int month, List<Ievent> events) {
    this.currentMonth = YearMonth.of(year, month);
    monthLabel.setText(currentMonth.getMonth() + " " + year);

    monthGrid.removeAll();
    addDayHeaders();
    addEmptyCellsForAlignment();
    addDayButtons(events);

    monthGrid.revalidate();
    monthGrid.repaint();
  }

  private void addDayHeaders() {
    String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    for (String day : days) {
      JLabel label = new JLabel(day, SwingConstants.CENTER);
      label.setFont(label.getFont().deriveFont(Font.BOLD));
      monthGrid.add(label);
    }
  }

  private void addEmptyCellsForAlignment() {
    LocalDate firstDay = currentMonth.atDay(1);
    int startDay = firstDay.getDayOfWeek().getValue() % 7;
    for (int i = 0; i < startDay; i++) {
      monthGrid.add(new JLabel(""));
    }
  }

  private void addDayButtons(List<Ievent> events) {
    for (int day = 1; day <= currentMonth.lengthOfMonth(); day++) {
      LocalDate date = currentMonth.atDay(day);
      long eventCount = countEventsOnDate(date, events);
      JButton btn = createDayButton(date, day, eventCount);
      monthGrid.add(btn);
    }
  }

  private long countEventsOnDate(LocalDate date, List<Ievent> events) {
    return events.stream()
        .filter(e -> !e.getStart().toLocalDate().isAfter(date)
            && !e.getEnd().toLocalDate().isBefore(date))
        .count();
  }

  private JButton createDayButton(LocalDate date, int day, long eventCount) {
    JButton btn = new JButton("<html><center>" + day
        + (eventCount > 0 ? "<br><font size=2 color=blue>(" + eventCount + " events)</font>"
        : "") + "</center></html>");

    if (date.equals(selectedDate)) {
      btn.setBackground(new Color(255, 255, 200));
      btn.setOpaque(true);
      btn.setBorderPainted(true);
    }

    if (date.equals(LocalDate.now())) {
      btn.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
    }

    btn.addActionListener(e -> features.selectDate(date));
    return btn;
  }

  @Override
  public void refreshDayView(LocalDate date, List<Ievent> events) {
    this.selectedDate = date;

    eventsPanel.removeAll();

    JLabel dateLabel = new JLabel("Events on " + date);
    dateLabel.setFont(dateLabel.getFont().deriveFont(Font.BOLD, 14f));
    eventsPanel.add(dateLabel);
    eventsPanel.add(Box.createVerticalStrut(10));

    if (events.isEmpty()) {
      eventsPanel.add(new JLabel("No events scheduled"));
    } else {
      for (Ievent event : events) {
        eventsPanel.add(createEventCard(event));
        eventsPanel.add(Box.createVerticalStrut(5));
      }
    }

    eventsPanel.revalidate();
    eventsPanel.repaint();
  }

  private JPanel createEventCard(Ievent event) {
    JPanel card = new JPanel();
    card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
    card.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(Color.GRAY, 1),
        BorderFactory.createEmptyBorder(8, 8, 8, 8)));
    card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

    addEventTitle(card, event);
    addEventTime(card, event);
    addEventLocation(card, event);
    addEventDescription(card, event);
    addEventStatus(card, event);
    addEditButton(card, event);

    return card;
  }

  private void addEventTitle(JPanel card, Ievent event) {
    JLabel title = new JLabel(event.getSubject());
    title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));
    title.setAlignmentX(Component.LEFT_ALIGNMENT);
    card.add(title);
    card.add(Box.createVerticalStrut(5));
  }

  private void addEventTime(JPanel card, Ievent event) {
    JLabel time = new JLabel("Time: " + event.getStart().toLocalTime().format(TIME_FMT)
        + " - " + event.getEnd().toLocalTime().format(TIME_FMT));
    time.setFont(time.getFont().deriveFont(11f));
    time.setAlignmentX(Component.LEFT_ALIGNMENT);
    card.add(time);
  }

  private void addEventLocation(JPanel card, Ievent event) {
    if (event.getLocation() != null && !event.getLocation().isEmpty()) {
      card.add(Box.createVerticalStrut(3));
      JLabel loc = new JLabel("Location: " + event.getLocation());
      loc.setFont(loc.getFont().deriveFont(11f));
      loc.setForeground(new Color(100, 100, 100));
      loc.setAlignmentX(Component.LEFT_ALIGNMENT);
      card.add(loc);
    }
  }

  private void addEventDescription(JPanel card, Ievent event) {
    if (event.getDescription() != null && !event.getDescription().isEmpty()) {
      card.add(Box.createVerticalStrut(3));

      String desc = event.getDescription();
      if (desc.length() > 80) {
        desc = desc.substring(0, 77) + "...";
      }

      JTextArea descArea = new JTextArea(desc);
      descArea.setFont(descArea.getFont().deriveFont(10f));
      descArea.setForeground(new Color(80, 80, 80));
      descArea.setLineWrap(true);
      descArea.setWrapStyleWord(true);
      descArea.setEditable(false);
      descArea.setOpaque(false);
      descArea.setAlignmentX(Component.LEFT_ALIGNMENT);
      card.add(descArea);
    }
  }

  private void addEventStatus(JPanel card, Ievent event) {
    if ("private".equalsIgnoreCase(event.getStatus())) {
      card.add(Box.createVerticalStrut(3));
      JLabel status = new JLabel("[Private]");
      status.setFont(status.getFont().deriveFont(Font.BOLD, 10f));
      status.setForeground(new Color(150, 0, 0));
      status.setAlignmentX(Component.LEFT_ALIGNMENT);
      card.add(status);
    }
  }

  private void addEditButton(JPanel card, Ievent event) {
    card.add(Box.createVerticalStrut(5));
    JButton editBtn = new JButton("Edit");
    editBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
    editBtn.addActionListener(e -> EditEventDialog.show(this, features, event));
    card.add(editBtn);
  }

  @Override
  public void showSuccess(String message) {
    JOptionPane.showMessageDialog(this, message, "Success",
        JOptionPane.INFORMATION_MESSAGE);
  }

  @Override
  public void showError(String errorMessage) {
    JOptionPane.showMessageDialog(this, errorMessage, "Error",
        JOptionPane.ERROR_MESSAGE);
  }

  @Override
  public boolean showConfirmation(String message) {
    return JOptionPane.showConfirmDialog(this, message, "Confirm",
        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
  }

  private void previousMonth() {
    YearMonth prev = currentMonth.minusMonths(1);
    features.navigateToMonth(prev.getYear(), prev.getMonthValue());
  }

  private void nextMonth() {
    YearMonth next = currentMonth.plusMonths(1);
    features.navigateToMonth(next.getYear(), next.getMonthValue());
  }

  private int getCalendarIndex(String calendarName, Set<String> allNames) {
    int index = 0;
    for (String name : allNames) {
      if (name.equals(calendarName)) {
        return index;
      }
      index++;
    }
    return 0;
  }
}