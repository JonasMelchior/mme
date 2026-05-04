package com.github.jonasmelchior.mymme.protocol.s11.model;

import io.netty.buffer.ByteBuf;

public class ServingNetworkIe extends AbstractInformationElement {
    private String mcc;
    private String mnc;

    public ServingNetworkIe(String mcc, String mnc, byte instance) {
        super(Gtpv2Constants.IE_TYPE_SERVING_NETWORK, instance);
        this.mcc = mcc;
        this.mnc = mnc;
    }

    public ServingNetworkIe(byte instance) {
        super(Gtpv2Constants.IE_TYPE_SERVING_NETWORK, instance);
    }

    public String getMcc() { return mcc; }
    public String getMnc() { return mnc; }

    @Override
    public int getLength() {
        return 3;
    }

    @Override
    public void encode(ByteBuf buffer) {
        encodeHeader(buffer, 3);
        
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
        if (mnc3 == 0xF) {
            this.mnc = "" + mnc1 + mnc2;
        } else {
            this.mnc = "" + mnc1 + mnc2 + mnc3;
        }
    }
}
