package com.example.mytrip;

public class Trip {

    // Trip attributes
    private int id;
    private String destination;
    private String date;

    // Constructor
    public Trip(int id, String destination, String date) {
        this.id = id;
        this.destination = destination;
        this.date = date;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getDestination() {
        return destination;
    }

    public String getDate() {
        return date;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public void setDate(String date) {
        this.date = date;
    }
}