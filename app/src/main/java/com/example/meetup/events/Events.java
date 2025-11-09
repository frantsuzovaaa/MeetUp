package com.example.meetup.events;

public class Events {
    private String nameEvent;
    private String codeWord;
    private String place;
    private long dataTime;
    private String creatorId;
    public Events(){

    }
    public  Events (String nameEvent, String codeWord, String place, long dataTime, String creatorId){
        this.nameEvent = nameEvent;
        this.codeWord = codeWord;
        this.place = place;
        this.dataTime = dataTime;
        this.creatorId = creatorId;
    }
    public String getNameEvent(){return nameEvent;}
    public String getCodeWord(){return codeWord;}
    public String getPlace(){return place;}
    public long getDataTime(){return dataTime;}
    public String getCreatorId(){return creatorId;}

    public void setNameEvent(String nameEvent) { this.nameEvent = nameEvent; }
    public void setCodeWord(String codeWord) { this.codeWord = codeWord; }
    public void setPlace(String place) { this.place = place; }
    public void setDataTime(long dataTime) { this .dataTime = dataTime; }
    public void setCreatorId(String creatorId) { this.creatorId = creatorId; }






}
