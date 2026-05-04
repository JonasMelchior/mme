package com.github.jonasmelchior.mymme.service;

public class AuthenticationSuccessEvent {
    private final String imsi;

    public AuthenticationSuccessEvent(String imsi) {
        this.imsi = imsi;
    }

    public String getImsi() {
        return imsi;
    }
}
