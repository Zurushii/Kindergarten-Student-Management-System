package com.example.model;

import javafx.beans.property.*;
import java.time.LocalDate;

public class Student {
    private final IntegerProperty id;
    private final StringProperty name;
    private final ObjectProperty<LocalDate> birthdate;
    private final StringProperty address;
    private final StringProperty allergy;
    private final StringProperty gender;

    // Constructor with ID
    public Student(int id, String name, LocalDate birthdate, String address, String allergy, String gender) {
        this.id = new SimpleIntegerProperty(id);
        this.name = new SimpleStringProperty(name);
        this.birthdate = new SimpleObjectProperty<>(birthdate);
        this.address = new SimpleStringProperty(address);
        this.allergy = new SimpleStringProperty(allergy);
        this.gender = new SimpleStringProperty(gender);
    }

    // Constructor without ID (for inserts)
    public Student(String name, LocalDate birthdate, String address, String allergy, String gender) {
        this(0, name, birthdate, address, allergy, gender);
    }

    // ===== Getters =====
    public int getId() { return id.get(); }
    public String getName() { return name.get(); }
    public LocalDate getBirthdate() { return birthdate.get(); }
    public String getAddress() { return address.get(); }
    public String getAllergy() { return allergy.get(); }
    public String getGender() { return gender.get(); }

    // ===== Properties =====
    public IntegerProperty idProperty() { return id; }
    public StringProperty nameProperty() { return name; }
    public ObjectProperty<LocalDate> birthdateProperty() { return birthdate; }
    public StringProperty addressProperty() { return address; }
    public StringProperty allergyProperty() { return allergy; }
    public StringProperty genderProperty() { return gender; }

    // ===== Setters =====
    public void setName(String name) { this.name.set(name); }
    public void setBirthdate(LocalDate birthdate) { this.birthdate.set(birthdate); }
    public void setAddress(String address) { this.address.set(address); }
    public void setAllergy(String allergy) { this.allergy.set(allergy); }
    public void setGender(String gender) { this.gender.set(gender); }

    // ===== Dynamic Age Calculation =====
    public int getAge() {
        if (birthdate.get() == null) return 0;
        int currentYear = LocalDate.now().getYear();
        return currentYear - birthdate.get().getYear();
    }
}
