package com.haulmont.cuba.gui.components.sys;

import com.haulmont.cuba.gui.Screen;
import com.haulmont.cuba.gui.WindowManager.LaunchMode;

/**
 * Internal. Provides API for WindowManager implementations.
 */
public interface WindowImplementation {
    void setController(Screen screen);
    Screen getController();

    void setLaunchMode(LaunchMode launchMode);
    LaunchMode getLaunchMode();
}