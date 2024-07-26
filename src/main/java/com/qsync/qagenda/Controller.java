package com.qsync.qagenda;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Controller {

    public ToggleButton show_events_button;
    public ToggleButton show_notes_button;
    @FXML
    private DatePicker datePicker;

    @FXML
    private ComboBox<String> dateRangeSelector;

    @FXML
    private ListView<String> eventList;

    @FXML
    private TabPane mainTabPane;

    @FXML
    private DatePicker agendaDatePicker;

    @FXML
    private TextField timePicker;

    @FXML
    private TextArea eventDescription;

    @FXML
    private TextArea notesArea;

    @FXML
    private VBox topContent;

    @FXML
    private ToggleGroup displayToggleGroup = new ToggleGroup();

    private SimpleDateFormat icsDateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
    private SimpleDateFormat displayDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private Date filterDate;

    private final String AGENDA_PATH = "calendrier/agenda.ics";
    private final String NOTES_PATH = "notes";
    @FXML
    private void initialize() {
        displayToggleGroup.getToggles().forEach(toggle -> ((ToggleButton) toggle).setToggleGroup(displayToggleGroup));

        datePicker.setValue(LocalDate.now());
        // Add listeners to update the event list when the date or date range is changed
        datePicker.valueProperty().addListener((observable, oldValue, newValue) -> displayEvents());

        dateRangeSelector.valueProperty().addListener((observable, oldValue, newValue) -> displayEvents());


        Calendar cl = Calendar.getInstance();
        timePicker.setText( Long.toString(cl.get(Calendar.HOUR_OF_DAY))+":"+Long.toString(Calendar.MINUTE));
        checkDirsArePresents();

        dateRangeSelector.valueProperty().setValue("Day");

        // Initialize the event list display
        displayEvents();
    }

    @FXML
    private void handleAddEvent() {
        try {
            String date = agendaDatePicker.getValue().toString();
            String time = timePicker.getText();
            String eventSummary = eventDescription.getText();

            String dateTimeString = date + " " + time;
            Date eventDate = displayDateFormat.parse(dateTimeString);

            writeDateToIcsFile(eventDate.getTime(), eventSummary);
            displayEvents(); // Update the event list after adding a new event
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSaveNotes() {
        // Logic to save notes

        Calendar cl = Calendar.getInstance();
        File noteFile = new File(NOTES_PATH,Long.toString(cl.getTime().getTime())+".txt");

        try {
            noteFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(noteFile);
            fos.write(notesArea.getText().getBytes(StandardCharsets.UTF_8));
            fos.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void showAgenda() {
        mainTabPane.getSelectionModel().select(0);
    }


    @FXML
    private void showSettings() {
        mainTabPane.getSelectionModel().select(2);
    }

    @FXML
    private void showEvents() {
        topContent.getChildren().clear();
        HBox eventControls = new HBox(10);
        eventControls.getChildren().addAll(new Label("Select Date:"), datePicker, new Label("Max Date Range:"), dateRangeSelector);
        topContent.getChildren().addAll(eventControls, eventList);
        displayEvents();
        show_notes_button.setSelected(false);
        show_events_button.setSelected(true);
    }

    @FXML
    private void showNotes() {
        topContent.getChildren().clear();

        TilePane notesPane = new TilePane();
        notesPane.setHgap(10);
        notesPane.setVgap(10);

        File notesDir = new File(NOTES_PATH);


        // Populate notesPane with note cards
        for (String noteFileName : notesDir.list()) {

            Calendar cl = Calendar.getInstance();
            String[] parts = noteFileName.split("\\.");
            if(parts.length == 2) {
                Instant instant = Instant.ofEpochMilli(Long.parseLong(parts[0]));
                LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String readableDate = dateTime.format(formatter);


                File noteFile = new File(notesDir, noteFileName);

                try {
                    FileInputStream fis = new FileInputStream(noteFile);
                    VBox noteCard = new VBox(5);
                    noteCard.setPadding(new Insets(10));
                    noteCard.setStyle("-fx-border-color: black; -fx-border-width: 1;");
                    TextArea noteContent = new TextArea(new String(fis.readAllBytes()));


                    fis.close();

                    Button saveButton = new Button("Save");

                    saveButton.setOnAction(e -> {
                        try {
                            FileOutputStream fos = new FileOutputStream(noteFile);
                            fos.write(noteContent.getText().getBytes(StandardCharsets.UTF_8));
                            fos.close();
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    });

                    // Create the Remove button
                    Button removeButton = new Button("Remove");

                    removeButton.setOnAction(e -> {
                        if (noteFile.delete()) {
                            notesPane.getChildren().remove(noteCard);
                        } else {
                            // Handle the case where the file couldn't be deleted
                            System.err.println("Failed to delete the file: " + noteFile.getPath());
                        }
                    });

                    noteCard.getChildren().addAll(new Label(readableDate), noteContent, saveButton, removeButton);
                    notesPane.getChildren().add(noteCard);

                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

        }

        // Wrap the TilePane in a ScrollPane
        ScrollPane scrollPane = new ScrollPane(notesPane);
        scrollPane.setFitToWidth(true); // Makes the ScrollPane fit the width of its content
        //scrollPane.setFitToHeight(true); // Makes the ScrollPane fit the height of its content
        scrollPane.setPrefViewportHeight(300);

        topContent.getChildren().add(scrollPane);

        show_notes_button.setSelected(true);
        show_events_button.setSelected(false);
    }


    private void writeDateToIcsFile(long timeInMillis, String eventSummary) {
        Date date = new Date(timeInMillis);
        String formattedDate = icsDateFormat.format(date);

        String eventContent = "BEGIN:VEVENT\n" +
                "UID:" + timeInMillis + "@qsync.com\n" +
                "DTSTAMP:" + formattedDate + "\n" +
                "DTSTART:" + formattedDate + "\n" +
                "DTEND:" + formattedDate + "\n" +
                "SUMMARY:" + eventSummary + "\n" +
                "END:VEVENT\n";

        File icsFile = new File(AGENDA_PATH);

        try {
            RandomAccessFile raf = new RandomAccessFile(icsFile, "rw");
            FileChannel channel = raf.getChannel();
            ByteBuffer eventData = ByteBuffer.wrap(eventContent.getBytes(StandardCharsets.UTF_8));

            if (icsFile.length() == 0) {
                String icsHeader = "BEGIN:VCALENDAR\n" +
                        "VERSION:2.0\n" +
                        "PRODID:-//qsync//qagenda\n";
                ByteBuffer headerData = ByteBuffer.wrap(icsHeader.getBytes(StandardCharsets.UTF_8));
                channel.write(headerData);
            } else {
                channel.position(channel.size() - "END:VCALENDAR\n".getBytes(StandardCharsets.UTF_8).length);
            }

            channel.write(eventData);
            channel.write(ByteBuffer.wrap("END:VCALENDAR\n".getBytes(StandardCharsets.UTF_8)));
            channel.close();
            raf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<Event> readIcsFile() {
        List<Event> events = new ArrayList<>();
        File icsFile = new File(AGENDA_PATH);

        try {
            BufferedReader reader = new BufferedReader(new FileReader(icsFile));
            String line;
            Event currentEvent = null;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("BEGIN:VEVENT")) {
                    currentEvent = new Event();
                } else if (line.startsWith("DTSTART:")) {
                    if (currentEvent != null) {
                        String dateStr = line.substring("DTSTART:".length());
                        currentEvent.setStartDate(icsDateFormat.parse(dateStr));
                    }
                } else if (line.startsWith("SUMMARY:")) {
                    if (currentEvent != null) {
                        currentEvent.setSummary(line.substring("SUMMARY:".length()));
                    }
                } else if (line.startsWith("END:VEVENT")) {
                    if (currentEvent != null) {
                        events.add(currentEvent);
                        currentEvent = null;
                    }
                }
            }
            reader.close();
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return events;
    }

    private void displayEvents() {
        eventList.getItems().clear();

        if (datePicker.getValue() == null || dateRangeSelector.getValue() == null) {
            return;
        }

        Date selectedDate = null;
        try {
            selectedDate = displayDateFormat.parse(datePicker.getValue().toString()+" 00:00");
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(selectedDate);

        switch (dateRangeSelector.getValue()) {
            case "Day":
                calendar.add(Calendar.DAY_OF_YEAR, 1);
                break;
            case "Week":
                calendar.add(Calendar.DAY_OF_YEAR, 7);
                break;
            case "Month":
                calendar.add(Calendar.MONTH, 1);
                break;
            case "Year":
                calendar.add(Calendar.YEAR, 1);
                break;
            default:
                calendar.add(Calendar.DAY_OF_YEAR, 1);
                break;
        }

        filterDate = calendar.getTime();

        List<Event> events = readIcsFile();
        Collections.sort(events);

        for (Event event : events) {
            if (event.getStartDate().before(filterDate) && !event.getStartDate().before(selectedDate)) {
                String eventText = displayDateFormat.format(event.getStartDate()) + " - " + event.getSummary();
                eventList.getItems().add(eventText);
            }
        }
    }


    private void checkDirsArePresents(){
        File agendaDir = new File(AGENDA_PATH.split("/")[0]);

        if(!agendaDir.exists()){
            agendaDir.mkdirs();

        }
        File ics = new File(agendaDir,"agenda.ics");

        if(!ics.exists()){
            try {
                ics.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        File notesDir = new File(NOTES_PATH);

        if(!notesDir.exists()){
            notesDir.mkdirs();
        }
    }
}
