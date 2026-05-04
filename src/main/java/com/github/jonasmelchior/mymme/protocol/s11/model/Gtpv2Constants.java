package com.github.jonasmelchior.mymme.protocol.s11.model;

public class Gtpv2Constants {
    // Message Types (TS 29.274 Table 6.1-1)
    public static final byte TYPE_CREATE_SESSION_REQUEST = 32;
    public static final byte TYPE_CREATE_SESSION_RESPONSE = 33;
    public static final byte TYPE_MODIFY_BEARER_REQUEST = 34;
    public static final byte TYPE_MODIFY_BEARER_RESPONSE = 35;

    // IE Types (TS 29.274 Table 8.1-1)
    public static final byte IE_TYPE_IMSI = 1;
    public static final byte IE_TYPE_CAUSE = 2;
    public static final byte IE_TYPE_RECOVERY = 3;
    public static final byte IE_TYPE_APN = 71;
    public static final byte IE_TYPE_AMBR = 72;
    public static final byte IE_TYPE_EBI = 73;
    public static final byte IE_TYPE_RAT_TYPE = 82;
    public static final byte IE_TYPE_SERVING_NETWORK = 83;
    public static final byte IE_TYPE_ULI = 86;
    public static final byte IE_TYPE_F_TEID = 87;
    public static final byte IE_TYPE_BEARER_CONTEXT = 93;
    public static final byte IE_TYPE_PDN_TYPE = 99;
    public static final byte IE_TYPE_PAA = 79;
    public static final byte IE_TYPE_BEARER_QOS = 80;
    public static final byte IE_TYPE_SELECTION_MODE = (byte) 128;
}
