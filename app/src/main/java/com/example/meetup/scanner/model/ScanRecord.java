package com.example.meetup.scanner.model;

public class ScanRecord {
    private int maxUsages;
    private String eventId;
    private String memberId;
    private long timestamp;
    private boolean success;

    public ScanRecord() {}

    public ScanRecord(String eventId, String memberId,
                      int maxUsages, boolean success) {
        this.eventId = eventId;
        this.memberId = memberId;
        this.timestamp = System.currentTimeMillis();
        this.maxUsages = maxUsages;
        this.success = success;
    }
    public String getEventId() { return eventId; }
    public String getMemberId() { return memberId; }
    public long getTimestamp() { return timestamp; }
    public boolean isSuccess() { return success; }


    public void setEventId(String eventId) { this.eventId = eventId; }
    public void setMemberId(String memberId) { this.memberId = memberId; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setSuccess(boolean success) { this.success = success; }
}