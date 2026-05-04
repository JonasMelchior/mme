package com.github.jonasmelchior.mymme.protocol.s11.model;

import io.netty.buffer.ByteBuf;

public class CauseIe extends AbstractInformationElement {
    private byte causeValue;

    public CauseIe(byte causeValue, byte instance) {
        super(Gtpv2Constants.IE_TYPE_CAUSE, instance);
        this.causeValue = causeValue;
    }

    public CauseIe(byte instance) {
        super(Gtpv2Constants.IE_TYPE_CAUSE, instance);
    }

    public byte getCauseValue() {
        return causeValue;
    }

    @Override
    public int getLength() {
        return 2; // Cause value + flags
    }

    @Override
    public void encode(ByteBuf buffer) {
        encodeHeader(buffer, getLength());
        buffer.writeByte(causeValue);
        buffer.writeByte(0); // Flags: PCE=0, BCE=0, CS=0
    }

    @Override
    public void decode(ByteBuf buffer, int length) {
        this.causeValue = buffer.readByte();
        if (length > 1) {
            buffer.readByte(); // Flags
        }
        if (length > 2) {
            buffer.skipBytes(length - 2);
        }
    }
}
