package com.github.jonasmelchior.mymme.protocol.s11.model;

import io.netty.buffer.ByteBuf;

public class RatTypeIe extends AbstractInformationElement {
    private byte ratType;

    public RatTypeIe(byte ratType, byte instance) {
        super(Gtpv2Constants.IE_TYPE_RAT_TYPE, instance);
        this.ratType = ratType;
    }

    public RatTypeIe(byte instance) {
        super(Gtpv2Constants.IE_TYPE_RAT_TYPE, instance);
    }

    public byte getRatType() {
        return ratType;
    }

    @Override
    public int getLength() {
        return 1;
    }

    @Override
    public void encode(ByteBuf buffer) {
        encodeHeader(buffer, 1);
        buffer.writeByte(ratType);
    }

    @Override
    public void decode(ByteBuf buffer, int length) {
        this.ratType = buffer.readByte();
    }
}
