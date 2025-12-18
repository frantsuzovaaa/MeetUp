package com.example.meetup.new_file;

import java.io.Serializable;

public class Member_new  implements Serializable {
    private String name;
    private String number;
    private int maxUseges;

    private String TextForQR;
    public Member_new(){
    }
    public Member_new (String name, String number, int maxUseges){
        this.name = name;
        this.number = number;
        this.maxUseges = maxUseges;
    }

    public String getName(){return name;}
    public String getNumber(){return number;}
    public int getMaxUsages(){return maxUseges;}

    public void setName(String name) { this.name = name; }
    public void setNumber(String number) { this.number = number; }
    public void setMaxUseges(int maxUseges) { this .maxUseges = maxUseges; }
}
