package com.haulmont.cuba.gui.screen;

import com.haulmont.cuba.gui.Screen;

import java.util.EventObject;

public class AfterShowEvent extends EventObject {
    public AfterShowEvent(Screen source) {
        super(source);
    }

    @Override
    public Screen getSource() {
        return (Screen) super.getSource();
    }
}