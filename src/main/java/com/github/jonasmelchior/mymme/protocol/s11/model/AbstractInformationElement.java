package com.github.jonasmelchior.mymme.protocol.s11.model;

import io.netty.buffer.ByteBuf;

public abstract class AbstractInformationElement implements InformationElement {
    protected byte type;
    protected byte instance;

    protected AbstractInformationElement(byte type, byte instance) {
        this.type = type;
        this.instance = instance;
    }

    @Override
    public byte getType() {
        return type;
    }

    @Override
    public byte getInstance() {
        return instance;
    }

    protected void encodeHeader(ByteBuf buffer, int length) {
        buffer.writeByte(type);
        buffer.writeShort(length);
        buffer.writeByte(instance & 0x0F);
    }
}
