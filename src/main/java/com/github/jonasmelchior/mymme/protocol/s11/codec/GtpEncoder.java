package com.github.jonasmelchior.mymme.protocol.s11.codec;

import com.github.jonasmelchior.mymme.protocol.s11.model.Gtpv2Header;
import com.github.jonasmelchior.mymme.protocol.s11.model.Gtpv2Message;
import com.github.jonasmelchior.mymme.protocol.s11.model.InformationElement;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class GtpEncoder {
    public static byte[] encode(Gtpv2Message message) {
        ByteBuf buffer = Unpooled.buffer();
        
        Gtpv2Header header = message.getHeader();
        buffer.writeByte(header.getFlags());
        buffer.writeByte(header.getMessageType());
        
        int lengthPlaceholderIndex = buffer.writerIndex();
        buffer.writeShort(0); // Length (placeholder)
        
        if (header.isTeidPresent()) {
            buffer.writeInt(header.getTeid());
        }
        
        int seq = header.getSequenceNumber();
        buffer.writeByte((seq >> 16) & 0xFF);
        buffer.writeByte((seq >> 8) & 0xFF);
        buffer.writeByte(seq & 0xFF);
        buffer.writeByte(0); // Spare
        
        int bodyStartIndex = buffer.writerIndex();
        
        for (InformationElement ie : message.getElements()) {
            ie.encode(buffer);
        }
        
        int totalLength = buffer.writerIndex();
        int messageLength = totalLength - 4; // Length excludes first 4 bytes
        buffer.setShort(lengthPlaceholderIndex, messageLength);
        
        byte[] result = new byte[totalLength];
        buffer.readBytes(result);
        return result;
    }
}
