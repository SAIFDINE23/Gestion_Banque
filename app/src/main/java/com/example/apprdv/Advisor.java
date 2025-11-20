package com.example.apprdv;

import java.util.List;
import java.util.Map;

public class Advisor {
    private String advisorId; // Clé Firebase (par exemple : "-OZpTWIz-hSoSGbH_tBp")
    private String id; // ID interne de l'advisor ("at1411199066")
    private String name;
    private String surname;
    private String agency;
    private String phone;
    private String address;
    private String email;
    private String password;
    private String role;
    private String birthday;
    private boolean firstConnection;
    private List<String> availableSlots;
    private Map<String, Map<String, Boolean>> availability;

    // 🔹 Constructeur vide nécessaire pour Firebase
    public Advisor() {}

    // 🔹 Constructeur complet
    public Advisor(String advisorId, String id, String name, String surname, String agency,
                   String phone, String address, String email, String password, String role,
                   String birthday, boolean firstConnection,
                   List<String> availableSlots, Map<String, Map<String, Boolean>> availability) {

        this.advisorId = advisorId;
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.agency = agency;
        this.phone = phone;
        this.address = address;
        this.email = email;
        this.password = password;
        this.role = role;
        this.birthday = birthday;
        this.firstConnection = firstConnection;
        this.availableSlots = availableSlots;
        this.availability = availability;
    }

    // 🔹 Getters et Setters
    public String getAdvisorId() {
        return advisorId;
    }

    public void setAdvisorId(String advisorId) {
        this.advisorId = advisorId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getAgency() {
        return agency;
    }

    public void setAgency(String agency) {
        this.agency = agency;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public boolean isFirstConnection() {
        return firstConnection;
    }

    public void setFirstConnection(boolean firstConnection) {
        this.firstConnection = firstConnection;
    }

    public List<String> getAvailableSlots() {
        return availableSlots;
    }

    public void setAvailableSlots(List<String> availableSlots) {
        this.availableSlots = availableSlots;
    }

    public Map<String, Map<String, Boolean>> getAvailability() {
        return availability;
    }

    public void setAvailability(Map<String, Map<String, Boolean>> availability) {
        this.availability = availability;
    }
}
