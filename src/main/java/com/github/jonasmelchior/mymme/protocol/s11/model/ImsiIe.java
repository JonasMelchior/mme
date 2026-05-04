package com.github.jonasmelchior.mymme.protocol.s11.model;

import io.netty.buffer.ByteBuf;

public class ImsiIe extends AbstractInformationElement {
    private String imsi;

    public ImsiIe(String imsi, byte instance) {
        super(Gtpv2Constants.IE_TYPE_IMSI, instance);
        this.imsi = imsi;
    }

    public ImsiIe(byte instance) {
        super(Gtpv2Constants.IE_TYPE_IMSI, instance);
    }

    public String getImsi() {
        return imsi;
    }

    @Override
    public int getLength() {
        return (imsi.length() + 1) / 2;
    }

    @Override
    public void encode(ByteBuf buffer) {
        int length = getLength();
        encodeHeader(buffer, length);
        
        for (int i = 0; i < imsi.length(); i += 2) {
            int low = Character.digit(imsi.charAt(i), 10);
            int high = 0xF;
            if (i + 1 < imsi.length()) {
                high = Character.digit(imsi.charAt(i + 1), 10);
            }
            buffer.writeByte((high << 4) | low);
        }
    }

    @Override
    public void decode(ByteBuf buffer, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            byte b = buffer.readByte();
            int low = b & 0x0F;
            int high = (b >> 4) & 0x0F;
            sb.append(low);
            if (high != 0x0F) {
                sb.append(high);
            }
        }
        this.imsi = sb.toString();
    }
}
