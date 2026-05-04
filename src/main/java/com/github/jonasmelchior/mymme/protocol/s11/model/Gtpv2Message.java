package com.github.jonasmelchior.mymme.protocol.s11.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Gtpv2Message {
    private Gtpv2Header header;
    private List<InformationElement> elements = new ArrayList<>();

    public Gtpv2Message(Gtpv2Header header) {
        this.header = header;
    }

    public Gtpv2Header getHeader() { return header; }
    public List<InformationElement> getElements() { return elements; }

    public void addElement(InformationElement ie) {
        elements.add(ie);
    }

    public <T extends InformationElement> Optional<T> getElement(Class<T> clazz, int instance) {
        return elements.stream()
            .filter(ie -> clazz.isInstance(ie) && ie.getInstance() == instance)
            .map(clazz::cast)
            .findFirst();
    }
}
