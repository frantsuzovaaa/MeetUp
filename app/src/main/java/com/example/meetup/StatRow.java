package com.example.meetup;

public class StatRow {
    private String name;
    private String phone;

    public StatRow(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }

    public String getName() { return name; }
    public String getPhone() { return phone;}
}