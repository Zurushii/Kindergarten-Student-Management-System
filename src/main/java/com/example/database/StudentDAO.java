package com.example.database;

import com.example.model.Student;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for the Student model.
 * Handles all CRUD (Create, Read, Update, Delete) operations
 * and search queries for the 'students' database table.
 */
public class StudentDAO {

    // ===== CREATE =====
    public static boolean addStudent(Student student) {
        String sql = "INSERT INTO students (name, birthdate, address, allergy, gender) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Bind values to the SQL query
            stmt.setString(1, student.getName());
            stmt.setDate(2, Date.valueOf(student.getBirthdate()));
            stmt.setString(3, student.getAddress());
            stmt.setString(4, student.getAllergy());
            stmt.setString(5, student.getGender()); // Gender field

            return stmt.executeUpdate() > 0; // Returns true if at least one row is affected
        } catch (Exception e) {
            e.printStackTrace();
            return false; // Insertion failed
        }
    }

    // ===== READ =====
    public static List<Student> getAllStudents() {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM students";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            // Iterate through the result set and create Student objects
            while (rs.next()) {
                students.add(new Student(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDate("birthdate").toLocalDate(),
                        rs.getString("address"),
                        rs.getString("allergy"),
                        rs.getString("gender") // Gender field
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return students;
    }

    // ===== UPDATE =====
    public static boolean updateStudent(Student student) {
        String sql = "UPDATE students SET name=?, birthdate=?, address=?, allergy=?, gender=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Bind updated values to the query
            stmt.setString(1, student.getName());
            stmt.setDate(2, Date.valueOf(student.getBirthdate()));
            stmt.setString(3, student.getAddress());
            stmt.setString(4, student.getAllergy());
            stmt.setString(5, student.getGender()); // Gender field
            stmt.setInt(6, student.getId()); // Identify row by ID

            return stmt.executeUpdate() > 0; // Returns true if at least one row updated
        } catch (Exception e) {
            e.printStackTrace();
            return false; // Update failed
        }
    }

    // ===== DELETE =====
    public static boolean deleteStudent(int id) {
        String sql = "DELETE FROM students WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id); // Bind ID to query
            return stmt.executeUpdate() > 0; // Returns true if a row was deleted
        } catch (Exception e) {
            e.printStackTrace();
            return false; // Deletion failed
        }
    }

    // ===== SEARCH =====
    public static List<Student> searchStudents(String keyword) {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM students WHERE name LIKE ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + keyword + "%"); // Use LIKE for partial matching
            ResultSet rs = stmt.executeQuery();

            // Build student list from results
            while (rs.next()) {
                students.add(new Student(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDate("birthdate").toLocalDate(),
                        rs.getString("address"),
                        rs.getString("allergy"),
                        rs.getString("gender") // Gender field
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return students;
    }
}
