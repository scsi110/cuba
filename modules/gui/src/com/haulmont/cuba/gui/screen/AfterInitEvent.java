package com.haulmont.cuba.gui.screen;

import java.util.EventObject;

/**
 * JavaDoc
 *
 * Used by UI components to perform actions after UiController initialized
 */
public class AfterInitEvent extends EventObject {
    protected final ScreenOptions options;

    public AfterInitEvent(Screen source, ScreenOptions options) {
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