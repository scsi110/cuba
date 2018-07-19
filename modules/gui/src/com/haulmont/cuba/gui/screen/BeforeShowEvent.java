package com.haulmont.cuba.gui.screen;

import com.haulmont.cuba.gui.Screen;

import java.util.EventObject;

public class BeforeShowEvent extends EventObject {
    public BeforeShowEvent(Screen source) {
        super(source);
    }

    @Override
    public Screen getSource() {
        return (Screen) super.getSource();
    }
}