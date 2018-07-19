package com.haulmont.cuba.web.app.ui.demo;

import com.haulmont.cuba.gui.Screen;
import com.haulmont.cuba.gui.components.Label;
import com.haulmont.cuba.gui.icons.CubaIcon;
import com.haulmont.cuba.gui.screen.*;
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory;
import com.haulmont.cuba.web.theme.HaloTheme;

import javax.inject.Inject;

@UIController("demo-screen")
@Design("/com/haulmont/cuba/web/app/ui/demo/demo.xml")
public class DemoScreen extends Screen {
    @Inject
    protected ComponentsFactory componentsFactory;

    @Subscribe
    public void init(InitEvent event) {
        Label<String> label = componentsFactory.createComponent(Label.NAME);
        label.setValue("IT IS ALIVE!");
        label.setIconFromSet(CubaIcon.MONEY);
        label.addStyleName(HaloTheme.LABEL_H2);

        getWindow().add(label);
    }

    @Subscribe
    public void afterShow(AfterShowEvent event) {
        Label<String> label = componentsFactory.createComponent(Label.NAME);
        label.setValue("It is shown");

        getWindow().add(label);
    }
}