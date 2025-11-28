package com.example.meetup;

public class Users {
    private String email;
    private String firstName;
    private String lastName;

    public Users() {}
    public Users(String name, String last_name, String email){
        this.firstName = name;
        this.lastName = last_name;
        this.email = email;
    }


    public String getEmail() { return email; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }

    public void setEmail(String email) { this.email = email; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
}
