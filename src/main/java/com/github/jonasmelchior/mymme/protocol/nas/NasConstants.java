package com.github.jonasmelchior.mymme.protocol.nas;

public class NasConstants {

    // Protocol Discriminators (TS 24.007)
    public static final byte PD_ESM = 0x02; // EPS session management messages
    public static final byte PD_EMM = 0x07; // EPS mobility management messages

    // Security Header Types (TS 24.301, 9.3.1)
    public static final byte SECURITY_PLAIN = 0x00;
    public static final byte SECURITY_INTEGRITY = 0x01;
    public static final byte SECURITY_INTEGRITY_CIPHERED = 0x02;
    public static final byte SECURITY_INTEGRITY_NEW = 0x03;
    public static final byte SECURITY_INTEGRITY_CIPHERED_NEW = 0x04;

    // --- EMM Message Types (TS 24.301, 9.8.1) ---
    public static final byte TYPE_ATTACH_REQUEST = 0x41;
    public static final byte TYPE_ATTACH_ACCEPT = 0x42;
    public static final byte TYPE_ATTACH_COMPLETE = 0x43;
    public static final byte TYPE_ATTACH_REJECT = 0x44;
    public static final byte TYPE_DETACH_REQUEST = 0x45;
    public static final byte TYPE_DETACH_ACCEPT = 0x46;
    public static final byte TYPE_TRACKING_AREA_UPDATE_REQUEST = 0x48;
    public static final byte TYPE_TRACKING_AREA_UPDATE_ACCEPT = 0x49;
    public static final byte TYPE_TRACKING_AREA_UPDATE_COMPLETE = 0x4a;
    public static final byte TYPE_TRACKING_AREA_UPDATE_REJECT = 0x4b;
    public static final byte TYPE_EXTENDED_SERVICE_REQUEST = 0x4c;
    public static final byte TYPE_SERVICE_REJECT = 0x4e;
    public static final byte TYPE_GUTI_REALLOCATION_COMMAND = 0x50;
    public static final byte TYPE_GUTI_REALLOCATION_COMPLETE = 0x51;
    public static final byte TYPE_AUTHENTICATION_REQUEST = 0x52;
    public static final byte TYPE_AUTHENTICATION_RESPONSE = 0x53;
    public static final byte TYPE_AUTHENTICATION_REJECT = 0x54;
    public static final byte TYPE_AUTHENTICATION_FAILURE = 0x5c;
    public static final byte TYPE_IDENTITY_REQUEST = 0x55;
    public static final byte TYPE_IDENTITY_RESPONSE = 0x56;
    public static final byte TYPE_SECURITY_MODE_COMMAND = 0x5d;
    public static final byte TYPE_SECURITY_MODE_COMPLETE = 0x5e;
    public static final byte TYPE_SECURITY_MODE_REJECT = 0x5f;
    public static final byte TYPE_EMM_STATUS = 0x60;
    public static final byte TYPE_EMM_INFORMATION = 0x61;
    public static final byte TYPE_DOWNLINK_NAS_TRANSPORT = 0x62;
    public static final byte TYPE_UPLINK_NAS_TRANSPORT = 0x63;
    public static final byte TYPE_CS_SERVICE_NOTIFICATION = 0x64;

    // --- ESM Message Types (TS 24.301, 9.8.2) ---
    public static final byte TYPE_ACTIVATE_DEFAULT_EPS_BEARER_CONTEXT_REQUEST = (byte) 0xc1;
    public static final byte TYPE_ACTIVATE_DEFAULT_EPS_BEARER_CONTEXT_ACCEPT = (byte) 0xc2;
    public static final byte TYPE_ACTIVATE_DEFAULT_EPS_BEARER_CONTEXT_REJECT = (byte) 0xc3;
    public static final byte TYPE_ACTIVATE_DEDICATED_EPS_BEARER_CONTEXT_REQUEST = (byte) 0xc5;
    public static final byte TYPE_ACTIVATE_DEDICATED_EPS_BEARER_CONTEXT_ACCEPT = (byte) 0xc6;
    public static final byte TYPE_ACTIVATE_DEDICATED_EPS_BEARER_CONTEXT_REJECT = (byte) 0xc7;
    public static final byte TYPE_MODIFY_EPS_BEARER_CONTEXT_REQUEST = (byte) 0xc9;
    public static final byte TYPE_MODIFY_EPS_BEARER_CONTEXT_ACCEPT = (byte) 0xca;
    public static final byte TYPE_MODIFY_EPS_BEARER_CONTEXT_REJECT = (byte) 0xcb;
    public static final byte TYPE_DEACTIVATE_EPS_BEARER_CONTEXT_REQUEST = (byte) 0xcd;
    public static final byte TYPE_DEACTIVATE_EPS_BEARER_CONTEXT_ACCEPT = (byte) 0xce;
    public static final byte TYPE_PDN_CONNECTIVITY_REQUEST = (byte) 0xd0;
    public static final byte TYPE_PDN_CONNECTIVITY_REJECT = (byte) 0xd1;
    public static final byte TYPE_PDN_DISCONNECT_REQUEST = (byte) 0xd2;
    public static final byte TYPE_PDN_DISCONNECT_REJECT = (byte) 0xd3;
    public static final byte TYPE_BEARER_RESOURCE_ALLOCATION_REQUEST = (byte) 0xd4;
    public static final byte TYPE_BEARER_RESOURCE_ALLOCATION_REJECT = (byte) 0xd5;
    public static final byte TYPE_BEARER_RESOURCE_MODIFICATION_REQUEST = (byte) 0xd6;
    public static final byte TYPE_BEARER_RESOURCE_MODIFICATION_REJECT = (byte) 0xd7;
    public static final byte TYPE_ESM_INFORMATION_REQUEST = (byte) 0xd9;
    public static final byte TYPE_ESM_INFORMATION_RESPONSE = (byte) 0xda;
    public static final byte TYPE_ESM_STATUS = (byte) 0xe8;
}
