package com.github.jonasmelchior.mymme.protocol.s11.model;

import io.netty.buffer.ByteBuf;

public interface InformationElement {
    byte getType();
    int getLength();
    byte getInstance();
    void encode(ByteBuf buffer);
    void decode(ByteBuf buffer, int length);
}
