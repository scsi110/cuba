package com.haulmont.cuba.gui;

import com.haulmont.bali.events.EventHub;
import com.haulmont.bali.events.Subscription;
import com.haulmont.cuba.gui.WindowManager.ScreenOptions;
import com.haulmont.cuba.gui.components.Window;
import com.haulmont.cuba.gui.config.WindowInfo;
import com.haulmont.cuba.gui.screen.AfterInitEvent;
import com.haulmont.cuba.gui.screen.InitEvent;

import java.util.function.Consumer;

import static com.haulmont.bali.util.Preconditions.checkNotNullArgument;

/**
 * JavaDoc
 */
public abstract class Screen {
    private String id;

    private WindowInfo windowInfo;
    private ScreenOptions screenOptions;

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

    protected void setWindowInfo(WindowInfo windowInfo) {
        this.windowInfo = windowInfo;
    }

    protected WindowInfo getWindowInfo() {
        return windowInfo;
    }

    protected void setScreenOptions(ScreenOptions screenOptions) {
        this.screenOptions = screenOptions;
    }

    protected ScreenOptions getScreenOptions() {
        return screenOptions;
    }

    protected <E> void fireEvent(Class<E> eventType, E event) {
        eventHub.publish(eventType, event);
    }

    public Window getWindow() {
        return window;
    }

    protected Subscription addInitListener(Consumer<InitEvent> listener) {
        return eventHub.subscribe(InitEvent.class, listener);
    }

    protected Subscription addAfterInitListener(Consumer<AfterInitEvent> listener) {
        return eventHub.subscribe(AfterInitEvent.class, listener);
    }
}