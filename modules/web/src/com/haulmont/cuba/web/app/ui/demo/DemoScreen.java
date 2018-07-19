package com.haulmont.cuba.web.app.ui.demo;

import com.haulmont.cuba.gui.Screen;
import com.haulmont.cuba.gui.screen.Design;
import com.haulmont.cuba.gui.screen.InitEvent;
import com.haulmont.cuba.gui.screen.Subscribe;
import com.haulmont.cuba.gui.screen.UIController;

// fixme DEMO ONLY !
@UIController("demo-screen")
@Design("demo.xml")
public class DemoScreen extends Screen {

    @Subscribe
    public void init(InitEvent event) {

    }
}