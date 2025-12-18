package com.example.meetup.new_file;

import java.util.HashMap;
import java.util.Map;

public class Events_new {
    private String nameEvent;
    private String codeWord;
    private String place;
    private long dataTime;
    private String creatorId;
    private Map<String, Member_new> members;

    public Events_new() {}

    public Events_new(String nameEvent, String codeWord, String place, long dataTime, String creatorId) {
        this.nameEvent = nameEvent;
        this.codeWord = codeWord;
        this.place = place;
        this.dataTime = dataTime;
        this.creatorId = creatorId;
        this.members = new HashMap<>();
    }

    public String getNameEvent() { return nameEvent; }
    public String getCodeWord() { return codeWord; }
    public String getPlace() { return place; }
    public long getDataTime() { return dataTime; }
    public String getCreatorId() { return creatorId; }
    public Map<String, Member_new> getMembers() { return members; }


    public void setNameEvent(String nameEvent) { this.nameEvent = nameEvent; }
    public void setCodeWord(String codeWord) { this.codeWord = codeWord; }
    public void setPlace(String place) { this.place = place; }
    public void setDataTime(long dataTime) { this.dataTime = dataTime; }
    public void setCreatorId(String creatorId) { this.creatorId = creatorId; }
    public void setMembers(Map<String, Member_new> members) { this.members = members; }

    public Member_new getMember(String memberId) {
        if (members != null) {
            return members.get(memberId);
        }
        return null;
    }

    public void addMember(String memberId, Member_new member) {
        if (members == null) {
            members = new HashMap<>();
        }
        members.put(memberId, member);
    }

    public void removeMember(String memberId) {
        if (members != null) {
            members.remove(memberId);
        }
    }

    public void updateMemberName(String memberId, String newName) {
        Member_new member = getMember(memberId);
        if (member != null) {
            member.setName(newName);
        }
    }


    public void updateMemberNumber(String memberId, String newNumber) {
        Member_new member = getMember(memberId);
        if (member != null) {
            member.setNumber(newNumber);
        }
    }

    public void updateMemberMaxUsages(String memberId, int newMaxUsages) {
        Member_new member = getMember(memberId);
        if (member != null) {
            member.setMaxUseges(newMaxUsages);
        }
    }

    public boolean hasMember(String memberId) {
        return members != null && members.containsKey(memberId);
    }

    public int getMembersCount() {
        return members != null ? members.size() : 0;
    }
}