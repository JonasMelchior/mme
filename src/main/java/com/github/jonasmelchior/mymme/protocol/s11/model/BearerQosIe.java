package com.github.jonasmelchior.mymme.protocol.s11.model;

import io.netty.buffer.ByteBuf;

public class BearerQosIe extends AbstractInformationElement {
    private byte qci;
    private byte priorityLevel;
    private boolean preemptionCapability;
    private boolean preemptionVulnerability;

    public BearerQosIe(byte qci, byte priorityLevel, boolean preemptionCapability, boolean preemptionVulnerability, byte instance) {
        super(Gtpv2Constants.IE_TYPE_BEARER_QOS, instance);
        this.qci = qci;
        this.priorityLevel = priorityLevel;
        this.preemptionCapability = preemptionCapability;
        this.preemptionVulnerability = preemptionVulnerability;
    }

    public BearerQosIe(byte instance) {
        super(Gtpv2Constants.IE_TYPE_BEARER_QOS, instance);
    }

    @Override
    public int getLength() {
        return 22; // For simplified version with 0 bitrates
    }

    @Override
    public void encode(ByteBuf buffer) {
        encodeHeader(buffer, 22);
        byte arp = (byte) (priorityLevel << 2);
        if (preemptionCapability) arp |= 0x40;
        if (preemptionVulnerability) arp |= 0x01;
        
        buffer.writeByte(arp);
        buffer.writeByte(qci);
        // Maximum bit rate for Uplink/Downlink (5 bytes each)
        for (int i = 0; i < 10; i++) buffer.writeByte(0);
        // Guaranteed bit rate for Uplink/Downlink (5 bytes each)
        for (int i = 0; i < 10; i++) buffer.writeByte(0);
    }

    @Override
    public void decode(ByteBuf buffer, int length) {
        byte arp = buffer.readByte();
        this.priorityLevel = (byte) ((arp >> 2) & 0x0F);
        this.preemptionCapability = (arp & 0x40) != 0;
        this.preemptionVulnerability = (arp & 0x01) != 0;
        this.qci = buffer.readByte();
        buffer.skipBytes(length - 2);
    }
}
