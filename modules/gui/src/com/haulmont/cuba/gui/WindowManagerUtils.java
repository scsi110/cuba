package com.haulmont.cuba.gui;

import com.haulmont.bali.events.EventHub;
import com.haulmont.cuba.gui.WindowManager.ScreenOptions;
import com.haulmont.cuba.gui.components.Window;
import com.haulmont.cuba.gui.config.WindowInfo;

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

    public static EventHub getEventHub(Screen screen) {
        return screen.getEventHub();
    }

    public static void setWindowInfo(Screen screen, WindowInfo windowInfo) {
        screen.setWindowInfo(windowInfo);
    }

    public static WindowInfo getWindowInfo(Screen screen) {
        return screen.getWindowInfo();
    }

    public static ScreenOptions getScreenOptions(Screen screen) {
        return screen.getScreenOptions();
    }

    public static void setScreenOptions(Screen screen, ScreenOptions screenOptions) {
        screen.setScreenOptions(screenOptions);
    }
}