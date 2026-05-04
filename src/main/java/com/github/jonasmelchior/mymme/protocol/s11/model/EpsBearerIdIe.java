package com.github.jonasmelchior.mymme.protocol.s11.model;

import io.netty.buffer.ByteBuf;

public class EpsBearerIdIe extends AbstractInformationElement {
    private byte ebi;

    public EpsBearerIdIe(byte ebi, byte instance) {
        super(Gtpv2Constants.IE_TYPE_EBI, instance);
        this.ebi = ebi;
    }

    public EpsBearerIdIe(byte instance) {
        super(Gtpv2Constants.IE_TYPE_EBI, instance);
    }

    public byte getEbi() { return ebi; }

    @Override
    public int getLength() {
        return 1;
    }

    @Override
    public void encode(ByteBuf buffer) {
        encodeHeader(buffer, 1);
        buffer.writeByte(ebi & 0x0F);
    }

    @Override
    public void decode(ByteBuf buffer, int length) {
        this.ebi = (byte) (buffer.readByte() & 0x0F);
    }
}
