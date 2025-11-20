package com.example.apprdv;

import java.util.HashMap;
import java.util.Map;

public class Appointment {

    private String appointmentId;
    private String advisorId;
    private String clientId;
    private String selectedDate;
    private String selectedSlot;
    private String status; // Exemple : "Confirmé", "En attente"

    // Constructeur vide nécessaire pour Firebase
    public Appointment() {}

    // Constructeur avec tous les champs
    public Appointment(String appointmentId, String advisorId, String clientId, String selectedDate, String selectedSlot, String status) {
        this.appointmentId = appointmentId;
        this.advisorId = advisorId;
        this.clientId = clientId;
        this.selectedDate = selectedDate;
        this.selectedSlot = selectedSlot;
        this.status = status;
    }

    // Getters et Setters
    public String getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(String appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getAdvisorId() {
        return advisorId;
    }

    public void setAdvisorId(String advisorId) {
        this.advisorId = advisorId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getSelectedDate() {
        return selectedDate;
    }

    public void setSelectedDate(String selectedDate) {
        this.selectedDate = selectedDate;
    }

    public String getSelectedSlot() {
        return selectedSlot;
    }

    public void setSelectedSlot(String selectedSlot) {
        this.selectedSlot = selectedSlot;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Méthode pour convertir l'objet Appointment en une Map, utile pour stocker dans Firebase
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("appointmentId", appointmentId);
        result.put("advisorId", advisorId);
        result.put("clientId", clientId);
        result.put("selectedDate", selectedDate);
        result.put("selectedSlot", selectedSlot);
        result.put("status", status);
        return result;
    }
}
