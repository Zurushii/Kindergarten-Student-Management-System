package com.example.controller;

import com.example.database.StudentDAO;
import com.example.model.Student;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import java.io.File;
import java.io.IOException;

/**
 * Controller class for managing Kindergarten Student data.
 * Handles add, update, delete, filter, reset, and PDF export operations.
 */
public class StudentController {

    // ===== Form Fields =====
    @FXML private TextField txtName;
    @FXML private DatePicker dateBirth;
    @FXML private TextField txtAge;
    @FXML private TextArea txtAddress;
    @FXML private TextField txtAllergy;
    @FXML private ComboBox<String> cmbGender;

    // ===== Buttons =====
    @FXML private Button btnAdd;
    @FXML private Button btnUpdate;
    @FXML private Button btnDelete;
    @FXML private Button btnReset;

    // ===== TableView and Columns =====
    @FXML private TableView<Student> tableStudents;
    @FXML private TableColumn<Student, Number> colId;
    @FXML private TableColumn<Student, String> colName;
    @FXML private TableColumn<Student, LocalDate> colBirth;
    @FXML private TableColumn<Student, Number> colAge;
    @FXML private TableColumn<Student, String> colAddress;
    @FXML private TableColumn<Student, String> colAllergy;
    @FXML private TableColumn<Student, String> colGender;

    // ===== Error Labels =====
    @FXML private Label lblErrorName;
    @FXML private Label lblErrorBirth;
    @FXML private Label lblErrorAddress;
    @FXML private Label lblErrorGender;

    // ===== Filter Controls =====
    @FXML private ComboBox<String> cmbFilterType;
    @FXML private TextField txtFilterValue;
    @FXML private Button btnApplyFilter;
    @FXML private Button btnClearFilter;

    // ===== Summary Panel =====
    @FXML private Label lblTotalStudents;
    @FXML private Label lblAgeGroups;
    @FXML private Label lblNextBirthdays;
    @FXML private Label lblAllergyCount;
    @FXML private Label lblGenderDistribution;

    // ===== ObservableList for TableView =====
    private ObservableList<Student> studentList;

    // ===== Initialization =====
    @FXML
    public void initialize() {
        // Constrain columns to fit table width
        tableStudents.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Set filter options
        cmbFilterType.setItems(FXCollections.observableArrayList("Name", "Age", "Allergy", "Gender"));
        cmbFilterType.setValue("Name");

        // Set gender options
        cmbGender.setItems(FXCollections.observableArrayList("Male", "Female"));
        cmbGender.setValue("Select Gender");

        // Auto-generate ID column based on row index
        colId.setCellValueFactory(cellData ->
            new javafx.beans.property.ReadOnlyObjectWrapper<>(tableStudents.getItems().indexOf(cellData.getValue()) + 1)
        );

        // Refresh table when items change
        tableStudents.getItems().addListener((javafx.collections.ListChangeListener<Student>) c -> tableStudents.refresh());

        // Bind columns to Student properties
        colName.setCellValueFactory(data -> data.getValue().nameProperty());
        colBirth.setCellValueFactory(data -> data.getValue().birthdateProperty());
        colAge.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getAge()));
        colAddress.setCellValueFactory(data -> data.getValue().addressProperty());

        // Allergy column: display "None" if empty
        colAllergy.setCellValueFactory(data -> data.getValue().allergyProperty());
        colAllergy.setCellFactory(column -> new TableCell<Student, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= tableStudents.getItems().size()) {
                    setText(null);
                } else if (item == null || item.trim().isEmpty()) {
                    setText("None");
                } else {
                    setText(item);
                }
            }
        });

        colGender.setCellValueFactory(data -> data.getValue().genderProperty());

        // Load initial data
        loadStudents();

        // Force DatePicker to use dd/MM/yyyy format
        java.time.format.DateTimeFormatter dateFormatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
        dateBirth.setConverter(new javafx.util.StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                return (date != null) ? dateFormatter.format(date) : "";
            }

            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.trim().isEmpty()) {
                    try {
                        return LocalDate.parse(string, dateFormatter);
                    } catch (java.time.format.DateTimeParseException e) {
                        return null; // Validation handles errors
                    }
                }
                return null;
            }
        });
        dateBirth.setPromptText("dd/MM/yyyy");

        // Update age automatically when birthdate changes
        dateBirth.valueProperty().addListener((obs, oldDate, newDate) -> {
            if (newDate != null) {
                int age = LocalDate.now().getYear() - newDate.getYear();
                txtAge.setText(String.valueOf(age));
            }
        });

        // Populate form when a table row is selected
        tableStudents.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                txtName.setText(newSel.getName());
                dateBirth.setValue(newSel.getBirthdate());
                txtAge.setText(String.valueOf(newSel.getAge()));
                txtAddress.setText(newSel.getAddress());
                txtAllergy.setText(newSel.getAllergy() == null ? "" : newSel.getAllergy());
                cmbGender.setValue(newSel.getGender());
            }
        });

        // Update summary panel
        updateSummary();
    }

    // ===== Load students from DAO =====
    private void loadStudents() {
        studentList = FXCollections.observableArrayList(StudentDAO.getAllStudents());
        tableStudents.setItems(studentList);
        updateSummary();
    }

    // ===== Update summary panel =====
    private void updateSummary() {
        if (studentList == null || studentList.isEmpty()) {
            lblTotalStudents.setText("Total Students: 0");
            lblAgeGroups.setText("Age Groups: -");
            lblAllergyCount.setText("Allergies Reported: 0");
            lblNextBirthdays.setText("Birthdays This Month: 0");
            return;
        }

        // Total students
        int total = studentList.size();
        lblTotalStudents.setText("Total Students: " + total);

        // Gender distribution
        long maleCount = studentList.stream().filter(s -> "Male".equalsIgnoreCase(s.getGender())).count();
        long femaleCount = studentList.stream().filter(s -> "Female".equalsIgnoreCase(s.getGender())).count();
        lblGenderDistribution.setText("Gender: Male: " + maleCount + " | Female: " + femaleCount);

        // Age groups
        Map<Integer, Long> ageGroups = studentList.stream()
                .collect(Collectors.groupingBy(Student::getAge, Collectors.counting()));
        StringBuilder groupText = new StringBuilder("Age Groups: ");
        ageGroups.forEach((age, count) -> groupText.append(age).append(" yrs: ").append(count).append(" | "));
        if (!ageGroups.isEmpty()) groupText.setLength(groupText.length() - 3); // remove last "|"
        lblAgeGroups.setText(groupText.toString());

        // Allergy count
        long allergyCount = studentList.stream()
                .filter(s -> s.getAllergy() != null && !s.getAllergy().trim().isEmpty())
                .count();
        lblAllergyCount.setText("Allergies Reported: " + allergyCount);

        // Birthdays this month
        int currentMonth = LocalDate.now().getMonthValue();
        long birthdayCount = studentList.stream()
                .filter(s -> s.getBirthdate() != null && s.getBirthdate().getMonthValue() == currentMonth)
                .count();
        lblNextBirthdays.setText("Birthdays This Month: " + birthdayCount);
    }

    // ===== Button Handlers =====
    @FXML
    void handleAdd(ActionEvent event) {
        try {
            if (!validateForm()) return;

            Student student = new Student(
                    txtName.getText(),
                    dateBirth.getValue(),
                    txtAddress.getText(),
                    txtAllergy.getText().isEmpty() ? null : txtAllergy.getText(),
                    cmbGender.getValue()
            );

            if (StudentDAO.addStudent(student)) {
                loadStudents();
                handleReset(null);
                showAlert("Success", "Student added successfully!");
            } else {
                showAlert("Error", "Failed to add student.");
            }

        } catch (Exception e) {
            showAlert("Error", "Invalid input: " + e.getMessage());
        }
    }

    @FXML
    void handleUpdate(ActionEvent event) {
        Student selected = tableStudents.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (!validateForm()) return;

            selected.setName(txtName.getText());
            selected.setBirthdate(dateBirth.getValue());
            selected.setAddress(txtAddress.getText());
            selected.setAllergy(txtAllergy.getText().isEmpty() ? null : txtAllergy.getText());
            selected.setGender(cmbGender.getValue());

            if (StudentDAO.updateStudent(selected)) {
                loadStudents();
                handleReset(null);
                showAlert("Success", "Student updated successfully!");
            } else {
                showAlert("Error", "Failed to update student.");
            }
        } else {
            showAlert("Warning", "No student selected.");
        }
    }

    @FXML
    void handleDelete(ActionEvent event) {
        Student selected = tableStudents.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (StudentDAO.deleteStudent(selected.getId())) {
                loadStudents();
                handleReset(null);
                showAlert("Success", "Student deleted successfully!");
            } else {
                showAlert("Error", "Failed to delete student.");
            }
        } else {
            showAlert("Warning", "No student selected.");
        }
    }

    // ===== Error Handling =====
    private void showError(Label lbl, String message) {
        lbl.setText(message);
        lbl.setVisible(true);
        lbl.setManaged(true);
    }

    private void hideError(Label lbl) {
        lbl.setVisible(false);
        lbl.setManaged(false);
    }

    // ===== Reset Form =====
    @FXML
    void handleReset(ActionEvent event) {
        txtName.clear();
        dateBirth.setValue(null);
        txtAge.clear();
        txtAddress.clear();
        txtAllergy.clear();
        cmbGender.setValue("Select Gender");

        clearError(txtName);
        clearError(dateBirth);
        clearError(txtAddress);

        hideError(lblErrorName);
        hideError(lblErrorBirth);
        hideError(lblErrorGender);
        hideError(lblErrorAddress);
    }

    // ===== Form Validation =====
    private boolean validateForm() {
        boolean valid = true;

        // Name validation
        if (txtName.getText().trim().isEmpty()) {
            showError(lblErrorName, "❌ Name is required");
            valid = false;
        } else hideError(lblErrorName);

        // ===== Birthdate validation =====
        LocalDate birthdate = dateBirth.getValue();
        if (birthdate == null) {
            showError(lblErrorBirth, "❌ Birth date required");
            valid = false;
        } else {
            // Calculate exact age using Period
            int age = java.time.Period.between(birthdate, LocalDate.now()).getYears();

            if (age < 4) {
                showError(lblErrorBirth, "❌ Must be at least 4 years old");
                valid = false;
            } else if (age > 6) {
                showError(lblErrorBirth, "❌ Must not be older than 6 years old");
                valid = false;
            } else {
                hideError(lblErrorBirth);
            }
        }


        // Gender validation
        if (cmbGender.getValue() == null || cmbGender.getValue().equals("Select Gender")) {
            showError(lblErrorGender, "❌ Gender is required");
            valid = false;
        } else hideError(lblErrorGender);

        // Address validation
        if (txtAddress.getText().trim().isEmpty()) {
            showError(lblErrorAddress, "❌ Address is required");
            valid = false;
        } else hideError(lblErrorAddress);

        return valid;
    }

    // ===== Alert Helper =====
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ===== Input Error Styling =====
    private void markError(Control control, String message) {
        if (!control.getStyleClass().contains("input-error")) control.getStyleClass().add("input-error");
        control.setTooltip(new Tooltip(message));
    }

    private void clearError(Control control) {
        control.getStyleClass().remove("input-error");
        control.setTooltip(null);
    }

    // ===== Filter Handlers =====
    @FXML
    void handleApplyFilter(ActionEvent event) {
        String type = cmbFilterType.getValue();
        String value = txtFilterValue.getText().trim();

        if (type == null || value.isEmpty()) {
            showAlert("Filter Error", "Please select a filter type and enter a value.");
            return;
        }

        ObservableList<Student> filteredList = FXCollections.observableArrayList();

        switch (type) {
            case "Name" -> studentList.stream()
                    .filter(s -> s.getName().toLowerCase().contains(value.toLowerCase()))
                    .forEach(filteredList::add);
            case "Age" -> {
                try {
                    int age = Integer.parseInt(value);
                    studentList.stream()
                            .filter(s -> s.getAge() == age)
                            .forEach(filteredList::add);
                } catch (NumberFormatException e) {
                    showAlert("Filter Error", "Please enter a valid number for Age.");
                    return;
                }
            }
            case "Allergy" -> studentList.stream()
                    .filter(s -> {
                        String allergy = (s.getAllergy() == null || s.getAllergy().trim().isEmpty()) ? "None" : s.getAllergy();
                        return allergy.toLowerCase().contains(value.toLowerCase());
                    })
                    .forEach(filteredList::add);
            case "Gender" -> studentList.stream()
                    .filter(s -> s.getGender() != null && s.getGender().equalsIgnoreCase(value))
                    .forEach(filteredList::add);
        }

        tableStudents.setItems(filteredList);
    }

    @FXML
    void handleClearFilter(ActionEvent event) {
        txtFilterValue.clear();
        cmbFilterType.setValue("Name");
        tableStudents.setItems(studentList);
    }

    // ===== PDF Export =====
    @FXML
    void handleExportPDF(ActionEvent event) {
        File file = choosePDFFile();
        if (file == null) return;

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            try (PDPageContentStream content = new PDPageContentStream(doc, page)) {
                drawPDFTitle(content);
                float[] colWidths = calculateColumnWidths(new float[]{190, 40, 200, 100, 100}, 500);
                float yPosition = 730;

                // Draw table header
                yPosition = drawTableHeader(content, yPosition, colWidths, new String[]{"Name", "Age", "Allergy", "Birthday", "Gender"});

                // Draw table rows
                yPosition = drawStudentRows(doc, content, yPosition, colWidths, new ArrayList<>(tableStudents.getItems()));
            }

            doc.save(file);
            showAlert("Success", "PDF exported successfully:\n" + file.getAbsolutePath());

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to export PDF: " + e.getMessage());
        }
    }

    // ===== Helper: File chooser =====
    private File choosePDFFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Student List as PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        fileChooser.setInitialFileName("ListOfStudent.pdf");
        return fileChooser.showSaveDialog(tableStudents.getScene().getWindow());
    }

    // ===== Helper: Draw PDF title =====
    private void drawPDFTitle(PDPageContentStream content) throws IOException {
        content.beginText();
        content.setFont(PDType1Font.HELVETICA_BOLD, 16);
        content.newLineAtOffset(200, 770);
        content.showText("Kindergarten Student List");
        content.endText();
    }

    // ===== Helper: Calculate proportional column widths =====
    private float[] calculateColumnWidths(float[] origWidths, float tableWidth) {
        float total = 0;
        for (float w : origWidths) total += w;
        float[] colWidths = new float[origWidths.length];
        for (int i = 0; i < origWidths.length; i++) colWidths[i] = origWidths[i] / total * tableWidth;
        return colWidths;
    }

    // ===== Helper: Draw table header =====
    private float drawTableHeader(PDPageContentStream content, float yPosition,
            float[] colWidths, String[] headers) throws IOException {
			float rowHeight = 25;
			float margin = 50;
			
			content.setLineWidth(1);
			content.setFont(PDType1Font.HELVETICA_BOLD, 12);
			
			// 🔹 Draw outer rectangle for header row
			content.addRect(margin, yPosition - rowHeight, 500, rowHeight);
			content.stroke();
			
			float textX = margin;
			for (int i = 0; i < headers.length; i++) {
			float colX = textX;
			float colW = colWidths[i];
			float textWidth = (PDType1Font.HELVETICA_BOLD.getStringWidth(headers[i]) / 1000) * 12;
			float centerX = colX + (colW - textWidth) / 2;
			float textY = yPosition - rowHeight / 2 - 6;
			
			content.beginText();
			content.newLineAtOffset(centerX, textY);
			content.showText(headers[i]);
			content.endText();
			
			textX += colWidths[i];
			}
			
			// 🔹 Draw vertical column lines
			float nextX = margin;
			for (float w : colWidths) {
			content.moveTo(nextX, yPosition);
			content.lineTo(nextX, yPosition - rowHeight);
			content.stroke();
			nextX += w;
			}
			content.moveTo(margin + 500, yPosition);
			content.lineTo(margin + 500, yPosition - rowHeight);
			content.stroke();
			
			// 🔹 Draw bottom border for header row
			content.moveTo(margin, yPosition - rowHeight);
			content.lineTo(margin + 500, yPosition - rowHeight);
			content.stroke();
			
			// Reset font for rows
			content.setFont(PDType1Font.HELVETICA, 12);
			
			return yPosition - rowHeight;
			}

 // ===== Helper: Draw student rows (with proper page breaks) =====
    private float drawStudentRows(PDDocument doc, PDPageContentStream content,
                                  float yPosition, float[] colWidths, List<Student> students) throws IOException {
        float rowHeight = 25;
        float margin = 50;
        float bottomMargin = 50; // safe margin at the bottom
        content.setFont(PDType1Font.HELVETICA, 12);

        for (Student s : students) {
            float rowTop = yPosition;
            float maxRowHeight = rowHeight;

            String[] data = {
                safe(s.getName()),
                String.valueOf(s.getAge()),
                (s.getAllergy() == null || s.getAllergy().isEmpty()) ? "-" : s.getAllergy(),
                (s.getBirthdate() == null) ? "-" : s.getBirthdate().toString(),
                (s.getGender() == null || s.getGender().isEmpty()) ? "-" : s.getGender()
            };

            // Wrap text for each cell
            List<List<String>> wrappedCells = wrapRowCells(data, colWidths);

            // Compute max row height based on wrapped text
            for (List<String> lines : wrappedCells) {
                float cellHeight = lines.size() * 15;
                if (cellHeight > maxRowHeight) maxRowHeight = cellHeight;
            }

            // 🔹 Check if this row fits on the current page
            if (yPosition - maxRowHeight < bottomMargin) {
                // Close current page
                content.close();

                // Create a new page
                PDPage newPage = new PDPage(PDRectangle.A4);
                doc.addPage(newPage);
                content = new PDPageContentStream(doc, newPage);

                // Reset yPosition for new page
                yPosition = 730;

                // Redraw the title
                drawPDFTitle(content);

                // Draw the header (this will return yPosition below the header row)
                yPosition = drawTableHeader(content, yPosition, colWidths,
                        new String[]{"Name", "Age", "Allergy", "Birthday", "Gender"});

                // Update rowTop for this row
                rowTop = yPosition;
            }

            // Draw row borders and text
            drawRowBorders(content, yPosition, maxRowHeight, colWidths, margin);
            drawRowText(content, wrappedCells, colWidths, rowTop, maxRowHeight, margin);

            // Move down for the next row
            yPosition -= maxRowHeight;
        }

        content.close(); // close the last page’s content stream
        return yPosition;
    }

    // ===== Helper: Wrap row cells =====
    private List<List<String>> wrapRowCells(String[] data, float[] colWidths) throws IOException {
        List<List<String>> wrappedCells = new ArrayList<>();
        for (int i = 0; i < data.length; i++) {
            String cellText = data[i];
            float availableWidth = colWidths[i] - 10;
            List<String> lines = new ArrayList<>();
            StringBuilder currentLine = new StringBuilder();

            for (String word : cellText.split(" ")) {
                String testLine = currentLine + (currentLine.length() == 0 ? "" : " ") + word;
                float textWidth = (PDType1Font.HELVETICA.getStringWidth(testLine) / 1000) * 12;
                if (textWidth < availableWidth) currentLine = new StringBuilder(testLine);
                else {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                }
            }
            if (currentLine.length() > 0) lines.add(currentLine.toString());
            wrappedCells.add(lines);
        }
        return wrappedCells;
    }

    // ===== Helper: Draw row borders =====
    private void drawRowBorders(PDPageContentStream content, float yPosition, float rowHeight, float[] colWidths, float margin) throws IOException {
        float rowTop = yPosition;

        content.moveTo(margin, rowTop);
        content.lineTo(margin + 500, rowTop);
        content.stroke();
        content.moveTo(margin, rowTop - rowHeight);
        content.lineTo(margin + 500, rowTop - rowHeight);
        content.stroke();

        float colX = margin;
        for (float w : colWidths) {
            content.moveTo(colX, rowTop);
            content.lineTo(colX, rowTop - rowHeight);
            content.stroke();
            colX += w;
        }
        content.moveTo(margin + 500, rowTop);
        content.lineTo(margin + 500, rowTop - rowHeight);
        content.stroke();
    }

    // ===== Helper: Draw row text =====
    private void drawRowText(PDPageContentStream content, List<List<String>> wrappedCells, float[] colWidths, float rowTop, float maxRowHeight, float margin) throws IOException {
        float colX = margin;
        for (int i = 0; i < wrappedCells.size(); i++) {
            List<String> lines = wrappedCells.get(i);
            float totalTextHeight = lines.size() * 15;
            float startY = rowTop - (maxRowHeight - totalTextHeight) / 2 - 12;

            for (String line : lines) {
                float textWidth = (PDType1Font.HELVETICA.getStringWidth(line) / 1000) * 12;
                float textStartX = colX + (colWidths[i] - textWidth) / 2;
                content.beginText();
                content.newLineAtOffset(textStartX, startY);
                content.showText(line);
                content.endText();
                startY -= 15;
            }
            colX += colWidths[i];
        }
    }
    // ===== Helper: safely handle null strings =====
    private String safe(String s) {
        return (s == null) ? "-" : s;
    }
}
