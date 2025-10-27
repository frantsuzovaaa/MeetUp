package com.example.meetup;

public class Users {
    private String name, last_name, email;


    public Users(){

    }
    public Users(String name, String last_name, String email){
        this.name = name;
        this.last_name = last_name;
        this.email = email;
    }
    public String getEmail() { return email; }
    public String getFirstName() { return name; }
    public String getLastName() { return last_name; }
}
