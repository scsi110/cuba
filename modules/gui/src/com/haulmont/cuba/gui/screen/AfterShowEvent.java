package com.haulmont.cuba.gui.screen;

import java.util.EventObject;

/**
 * JavaDoc
 */
public class AfterShowEvent extends EventObject {
    public AfterShowEvent(Screen source) {
        super(source);
    }

    @Override
    public Screen getSource() {
        return (Screen) super.getSource();
    }
}