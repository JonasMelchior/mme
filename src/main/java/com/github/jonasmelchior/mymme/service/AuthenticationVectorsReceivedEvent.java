package com.github.jonasmelchior.mymme.service;

public class AuthenticationVectorsReceivedEvent {
    private final String imsi;
    private final byte[] rand;
    private final byte[] autn;
    private final byte[] xres;
    private final byte[] kAsme;

    public AuthenticationVectorsReceivedEvent(String imsi, byte[] rand, byte[] autn, byte[] xres, byte[] kAsme) {
        this.imsi = imsi;
        this.rand = rand;
        this.autn = autn;
        this.xres = xres;
        this.kAsme = kAsme;
    }

    public String getImsi() { return imsi; }
    public byte[] getRand() { return rand; }
    public byte[] getAutn() { return autn; }
    public byte[] getXres() { return xres; }
    public byte[] getkAsme() { return kAsme; }
}
