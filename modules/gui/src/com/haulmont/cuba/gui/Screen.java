package com.haulmont.cuba.gui;

import com.haulmont.bali.events.EventHub;
import com.haulmont.cuba.gui.components.Window;

import static com.haulmont.bali.util.Preconditions.checkNotNullArgument;

public abstract class Screen {
    private String id;

    private Window window;

    private EventHub eventHub = new EventHub();

    protected EventHub getEventHub() {
        return eventHub;
    }

    public String getId() {
        return id;
    }

    /**
     * JavaDoc
     *
     * @param id
     */
    protected void setId(String id) {
        this.id = id;
    }

    protected void setWindow(Window window) {
        checkNotNullArgument(window);

        if (this.window != null) {
            throw new IllegalStateException("Screen already has Window");
        }
        this.window = window;
    }

    protected Window getWindow() {
        return window;
    }
}