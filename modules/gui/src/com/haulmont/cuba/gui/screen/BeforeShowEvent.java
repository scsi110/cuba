package com.haulmont.cuba.gui.screen;

import java.util.EventObject;

/**
 * JavaDoc
 */
public class BeforeShowEvent extends EventObject {
    public BeforeShowEvent(Screen source) {
        super(source);
    }

    @Override
    public Screen getSource() {
        return (Screen) super.getSource();
    }
}