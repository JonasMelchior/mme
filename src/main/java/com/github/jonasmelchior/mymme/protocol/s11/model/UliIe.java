package com.github.jonasmelchior.mymme.protocol.s11.model;

import io.netty.buffer.ByteBuf;

public class UliIe extends AbstractInformationElement {
    private String mcc;
    private String mnc;
    private int tac;
    private int cellId;

    public UliIe(String mcc, String mnc, int tac, int cellId, byte instance) {
        super(Gtpv2Constants.IE_TYPE_ULI, instance);
        this.mcc = mcc;
        this.mnc = mnc;
        this.tac = tac;
        this.cellId = cellId;
    }

    public UliIe(byte instance) {
        super(Gtpv2Constants.IE_TYPE_ULI, instance);
    }

    @Override
    public int getLength() {
        return 1 + 5 + 7; // flags + TAI + ECGI
    }

    @Override
    public void encode(ByteBuf buffer) {
        encodeHeader(buffer, getLength());
        buffer.writeByte(0x01 | 0x02); // TAI=1, ECGI=1
        
        // TAI
        encodeTai(buffer);
        // ECGI
        encodeEcgi(buffer);
    }

    private void encodeTai(ByteBuf buffer) {
        encodePlmn(buffer);
        buffer.writeShort(tac);
    }

    private void encodeEcgi(ByteBuf buffer) {
        encodePlmn(buffer);
        buffer.writeInt(cellId & 0x0FFFFFFF); // 28 bits
    }

    private void encodePlmn(ByteBuf buffer) {
        int mcc1 = Character.digit(mcc.charAt(0), 10);
        int mcc2 = Character.digit(mcc.charAt(1), 10);
        int mcc3 = Character.digit(mcc.charAt(2), 10);
        int mnc1 = Character.digit(mnc.charAt(0), 10);
        int mnc2 = Character.digit(mnc.charAt(1), 10);
        int mnc3 = (mnc.length() > 2) ? Character.digit(mnc.charAt(2), 10) : 0xF;
        
        buffer.writeByte((mcc2 << 4) | mcc1);
        buffer.writeByte((mnc3 << 4) | mcc3);
        buffer.writeByte((mnc2 << 4) | mnc1);
    }

    @Override
    public void decode(ByteBuf buffer, int length) {
        byte flags = buffer.readByte();
        // Simplified decode: assume TAI and ECGI are present
        if ((flags & 0x01) != 0) { // TAI
            decodePlmn(buffer); // sets this.mcc/mnc
            this.tac = buffer.readUnsignedShort();
        }
        if ((flags & 0x02) != 0) { // ECGI
            decodePlmn(buffer);
            this.cellId = buffer.readInt() & 0x0FFFFFFF;
        }
    }

    private void decodePlmn(ByteBuf buffer) {
        byte b1 = buffer.readByte();
        byte b2 = buffer.readByte();
        byte b3 = buffer.readByte();
        int mcc1 = b1 & 0x0F;
        int mcc2 = (b1 >> 4) & 0x0F;
        int mcc3 = b2 & 0x0F;
        int mnc3 = (b2 >> 4) & 0x0F;
        int mnc1 = b3 & 0x0F;
        int mnc2 = (b3 >> 4) & 0x0F;
        this.mcc = "" + mcc1 + mcc2 + mcc3;
        this.mnc = (mnc3 == 0xF) ? "" + mnc1 + mnc2 : "" + mnc1 + mnc2 + mnc3;
    }
}
