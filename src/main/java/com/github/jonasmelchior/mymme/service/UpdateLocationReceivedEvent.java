package com.github.jonasmelchior.mymme.service;

public class UpdateLocationReceivedEvent {
    private final String imsi;
    private final String apn;
    private final int qci;

    public UpdateLocationReceivedEvent(String imsi, String apn, int qci) {
        this.imsi = imsi;
        this.apn = apn;
        this.qci = qci;
    }

    public String getImsi() { return imsi; }
    public String getApn() { return apn; }
    public int getQci() { return qci; }
}
