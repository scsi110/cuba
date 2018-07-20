package com.haulmont.cuba.gui.screen;

import com.haulmont.cuba.gui.Screen;
import com.haulmont.cuba.gui.WindowManager.ScreenOptions;

import java.util.EventObject;

/**
 * JavaDoc
 *
 * Used by UI components to perform actions after ScreenController initialized
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