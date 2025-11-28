package com.example.meetup;

import java.io.Serializable;

public class Member  implements Serializable {
    private String name;
    private String number;
    private String eventId;
    private int maxUseges;

    private String TextForQR;
    public Member(){
    }
    public Member (String name, String number, String eventId, int maxUseges){
        this.name = name;
        this.number = number;
        this.eventId = eventId;
        this.maxUseges = maxUseges;
    }

    public String getName(){return name;}
    public String getNumber(){return number;}
    public String getEventId(){return eventId;}
    public int getMaxUsages(){return maxUseges;}

    public void setName(String name) { this.name = name; }
    public void setNumber(String number) { this.number = number; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public void setMaxUseges(int maxUseges) { this .maxUseges = maxUseges; }
}
