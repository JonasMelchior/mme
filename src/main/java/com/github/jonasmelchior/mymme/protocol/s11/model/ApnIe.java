package com.github.jonasmelchior.mymme.protocol.s11.model;

import io.netty.buffer.ByteBuf;
import java.nio.charset.StandardCharsets;

public class ApnIe extends AbstractInformationElement {
    private String apn;

    public ApnIe(String apn, byte instance) {
        super(Gtpv2Constants.IE_TYPE_APN, instance);
        this.apn = apn;
    }

    public ApnIe(byte instance) {
        super(Gtpv2Constants.IE_TYPE_APN, instance);
    }

    public String getApn() { return apn; }

    @Override
    public int getLength() {
        // Each label preceded by length byte. 
        // e.g. "internet" -> 8 "internet" -> 9 bytes
        // "internet.mnc001" -> 8 "internet" 6 "mnc001" -> 16 bytes
        int len = 0;
        String[] labels = apn.split("\\.");
        for (String label : labels) {
            len += 1 + label.length();
        }
        return len;
    }

    @Override
    public void encode(ByteBuf buffer) {
        encodeHeader(buffer, getLength());
        String[] labels = apn.split("\\.");
        for (String label : labels) {
            buffer.writeByte(label.length());
            buffer.writeBytes(label.getBytes(StandardCharsets.UTF_8));
        }
    }

    @Override
    public void decode(ByteBuf buffer, int length) {
        StringBuilder sb = new StringBuilder();
        int read = 0;
        while (read < length) {
            int labelLen = buffer.readUnsignedByte();
            read++;
            byte[] label = new byte[labelLen];
            buffer.readBytes(label);
            read += labelLen;
            sb.append(new String(label, StandardCharsets.UTF_8));
            if (read < length) {
                sb.append(".");
            }
        }
        this.apn = sb.toString();
    }
}
