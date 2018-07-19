package com.haulmont.cuba.gui;

import com.haulmont.cuba.gui.components.Window;

/**
 * Internal methods used in WindowManager implementations.
 */
public final class WindowManagerUtils {

    public static void setWindow(Screen screen, Window window) {
        screen.setWindow(window);
    }

    public static <E> void fireEvent(Screen screen, Class<E> eventType, E event) {
        screen.fireEvent(eventType, event);
    }
}