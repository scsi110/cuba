package com.haulmont.cuba.gui.screen;

import com.haulmont.cuba.gui.WindowManager.ScreenOptions;

import java.util.EventObject;

/**
 * JavaDoc
 */
public class InitEvent extends EventObject {
    protected final ScreenOptions options;

    public InitEvent(Screen source, ScreenOptions options) {
        super(source);
        this.options = options;
    }

    @Override
    public Screen getSource() {
        return (Screen) super.getSource();
    }

    public ScreenOptions getOptions() {
        return options;
    }
}