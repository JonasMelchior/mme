package com.github.jonasmelchior.mymme.protocol.s11.codec;

import com.github.jonasmelchior.mymme.protocol.s11.model.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.List;

public class GtpDecoder {
    public static Gtpv2Message decode(byte[] data) {
        ByteBuf buffer = Unpooled.wrappedBuffer(data);
        byte flags = buffer.readByte();
        byte msgType = buffer.readByte();
        int length = buffer.readUnsignedShort();
        
        Gtpv2Header header = new Gtpv2Header();
        header.setFlags(flags);
        header.setMessageType(msgType);
        header.setMessageLength(length);
        
        if (header.isTeidPresent()) {
            header.setTeid(buffer.readInt());
        }
        
        int seq = (buffer.readUnsignedByte() << 16) | (buffer.readUnsignedByte() << 8) | buffer.readUnsignedByte();
        header.setSequenceNumber(seq);
        buffer.readByte(); // Spare
        
        Gtpv2Message message = new Gtpv2Message(header);
        
        while (buffer.readableBytes() > 0) {
            InformationElement ie = decodeIe(buffer);
            if (ie != null) {
                message.addElement(ie);
            }
        }
        
        return message;
    }

    private static InformationElement decodeIe(ByteBuf buffer) {
        if (buffer.readableBytes() < 4) return null;
        
        byte type = buffer.readByte();
        int length = buffer.readUnsignedShort();
        byte instance = (byte) (buffer.readByte() & 0x0F);
        
        InformationElement ie;
        switch (type) {
            case Gtpv2Constants.IE_TYPE_IMSI: ie = new ImsiIe(instance); break;
            case Gtpv2Constants.IE_TYPE_CAUSE: ie = new CauseIe(instance); break;
            case Gtpv2Constants.IE_TYPE_RAT_TYPE: ie = new RatTypeIe(instance); break;
            case Gtpv2Constants.IE_TYPE_F_TEID: ie = new FullyQualifiedTeidIe(instance); break;
            case Gtpv2Constants.IE_TYPE_SERVING_NETWORK: ie = new ServingNetworkIe(instance); break;
            case Gtpv2Constants.IE_TYPE_APN: ie = new ApnIe(instance); break;
            case Gtpv2Constants.IE_TYPE_ULI: ie = new UliIe(instance); break;
            case Gtpv2Constants.IE_TYPE_EBI: ie = new EpsBearerIdIe(instance); break;
            case Gtpv2Constants.IE_TYPE_BEARER_QOS: ie = new BearerQosIe(instance); break;
            case Gtpv2Constants.IE_TYPE_BEARER_CONTEXT: ie = decodeBearerContext(buffer, length, instance); return ie;
            default:
                // Generic IE for unknown types
                buffer.skipBytes(length);
                return null;
        }
        
        if (ie != null) {
            ie.decode(buffer, length);
        }
        return ie;
    }

    private static BearerContextIe decodeBearerContext(ByteBuf buffer, int length, byte instance) {
        BearerContextIe context = new BearerContextIe(instance);
        int end = buffer.readerIndex() + length;
        while (buffer.readerIndex() < end) {
            InformationElement ie = decodeIe(buffer);
            if (ie != null) {
                context.addElement(ie);
            }
        }
        return context;
    }
}
