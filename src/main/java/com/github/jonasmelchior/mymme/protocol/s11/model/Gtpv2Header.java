package com.github.jonasmelchior.mymme.protocol.s11.model;

public class Gtpv2Header {
    private byte version = 2;
    private boolean piggybacking = false;
    private boolean teidPresent = true;
    private byte messageType;
    private int messageLength; // 16 bits
    private int teid; // 32 bits
    private int sequenceNumber; // 24 bits
    private byte spare;

    public Gtpv2Header(byte messageType, int teid, int sequenceNumber) {
        this.messageType = messageType;
        this.teid = teid;
        this.sequenceNumber = sequenceNumber;
    }

    public Gtpv2Header() {}

    // Getters and Setters
    public byte getVersion() { return version; }
    public void setVersion(byte version) { this.version = version; }
    public boolean isPiggybacking() { return piggybacking; }
    public void setPiggybacking(boolean piggybacking) { this.piggybacking = piggybacking; }
    public boolean isTeidPresent() { return teidPresent; }
    public void setTeidPresent(boolean teidPresent) { this.teidPresent = teidPresent; }
    public byte getMessageType() { return messageType; }
    public void setMessageType(byte messageType) { this.messageType = messageType; }
    public int getMessageLength() { return messageLength; }
    public void setMessageLength(int messageLength) { this.messageLength = messageLength; }
    public int getTeid() { return teid; }
    public void setTeid(int teid) { this.teid = teid; }
    public int getSequenceNumber() { return sequenceNumber; }
    public void setSequenceNumber(int sequenceNumber) { this.sequenceNumber = sequenceNumber; }
    public byte getSpare() { return spare; }
    public void setSpare(byte spare) { this.spare = spare; }

    public byte getFlags() {
        byte flags = (byte) (version << 5);
        if (piggybacking) flags |= 0x10;
        if (teidPresent) flags |= 0x08;
        return flags;
    }

    public void setFlags(byte flags) {
        this.version = (byte) ((flags >> 5) & 0x07);
        this.piggybacking = (flags & 0x10) != 0;
        this.teidPresent = (flags & 0x08) != 0;
    }
}
