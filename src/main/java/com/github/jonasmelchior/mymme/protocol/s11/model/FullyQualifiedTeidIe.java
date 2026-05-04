package com.github.jonasmelchior.mymme.protocol.s11.model;

import io.netty.buffer.ByteBuf;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class FullyQualifiedTeidIe extends AbstractInformationElement {
    private byte interfaceType;
    private int teid;
    private InetAddress ipAddress;

    public FullyQualifiedTeidIe(byte interfaceType, int teid, InetAddress ipAddress, byte instance) {
        super(Gtpv2Constants.IE_TYPE_F_TEID, instance);
        this.interfaceType = interfaceType;
        this.teid = teid;
        this.ipAddress = ipAddress;
    }

    public FullyQualifiedTeidIe(byte instance) {
        super(Gtpv2Constants.IE_TYPE_F_TEID, instance);
    }

    public byte getInterfaceType() { return interfaceType; }
    public int getTeid() { return teid; }
    public InetAddress getIpAddress() { return ipAddress; }

    @Override
    public int getLength() {
        int len = 1 + 4; // flags + teid
        if (ipAddress instanceof Inet4Address) len += 4;
        else len += 16;
        return len;
    }

    @Override
    public void encode(ByteBuf buffer) {
        int length = getLength();
        encodeHeader(buffer, length);
        
        byte flags = (byte) (interfaceType & 0x3F);
        if (ipAddress instanceof Inet4Address) flags |= 0x80;
        else flags |= 0x40;
        
        buffer.writeByte(flags);
        buffer.writeInt(teid);
        buffer.writeBytes(ipAddress.getAddress());
    }

    @Override
    public void decode(ByteBuf buffer, int length) {
        byte flags = buffer.readByte();
        this.interfaceType = (byte) (flags & 0x3F);
        boolean v4 = (flags & 0x80) != 0;
        boolean v6 = (flags & 0x40) != 0;
        
        this.teid = buffer.readInt();
        byte[] addr;
        if (v4) {
            addr = new byte[4];
            buffer.readBytes(addr);
        } else if (v6) {
            addr = new byte[16];
            buffer.readBytes(addr);
        } else {
            addr = new byte[0];
        }
        
        try {
            this.ipAddress = InetAddress.getByAddress(addr);
        } catch (UnknownHostException e) {
            // Should not happen
        }
    }
}
