package com.example.meetup;

import java.io.Serializable;

public class Member  implements Serializable {
    private String name;
    private String number;
    private int maxUsages;
    public Member(){
    }
    public Member (String name, String number, int maxUseges){
        this.name = name;
        this.number = number;
        this.maxUsages = maxUseges;
    }

    public String getName(){return name;}
    public String getNumber(){return number;}
    public int getMaxUsages(){return maxUsages;}

    public void setName(String name) { this.name = name; }
    public void setNumber(String number) { this.number = number; }
    public void setMaxUseges(int maxUseges) { this .maxUsages = maxUseges; }
}
