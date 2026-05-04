package com.github.jonasmelchior.mymme.protocol.s11.model;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.List;

public class BearerContextIe extends AbstractInformationElement {
    private List<InformationElement> elements = new ArrayList<>();

    public BearerContextIe(byte instance) {
        super(Gtpv2Constants.IE_TYPE_BEARER_CONTEXT, instance);
    }

    public void addElement(InformationElement ie) {
        elements.add(ie);
    }

    public List<InformationElement> getElements() {
        return elements;
    }

    @Override
    public int getLength() {
        int length = 0;
        for (InformationElement ie : elements) {
            length += 4 + ie.getLength(); // IE Header + IE Data
        }
        return length;
    }

    @Override
    public void encode(ByteBuf buffer) {
        encodeHeader(buffer, getLength());
        for (InformationElement ie : elements) {
            ie.encode(buffer);
        }
    }

    @Override
    public void decode(ByteBuf buffer, int length) {
        // Decoding grouped IE is tricky because we need to know all IE types.
        // For now, I'll just keep the raw buffer or implement a generic decoder.
        // Let's assume we use a GtpDecoder to help here.
    }
}
