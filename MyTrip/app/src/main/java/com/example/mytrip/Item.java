package com.example.mytrip;

public class Item {

    // Item attributes
    private int id;
    private int tripId;
    private String itemName;
    private String priority;
    private int isChecked;

    // Constructor
    public Item(int id, int tripId, String itemName, String priority, int isChecked) {
        this.id = id;
        this.tripId = tripId;
        this.itemName = itemName;
        this.priority = priority;
        this.isChecked = isChecked;
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getTripId() {
        return tripId;
    }

    public String getItemName() {
        return itemName;
    }

    public String getPriority() {
        return priority;
    }

    public int getIsChecked() {
        return isChecked;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setTripId(int tripId) {
        this.tripId = tripId;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public void setIsChecked(int isChecked) {
        this.isChecked = isChecked;
    }
}