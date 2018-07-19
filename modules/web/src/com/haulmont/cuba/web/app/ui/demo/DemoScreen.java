package com.haulmont.cuba.web.app.ui.demo;

import com.haulmont.cuba.gui.Screen;
import com.haulmont.cuba.gui.components.Label;
import com.haulmont.cuba.gui.icons.CubaIcon;
import com.haulmont.cuba.gui.screen.Design;
import com.haulmont.cuba.gui.screen.InitEvent;
import com.haulmont.cuba.gui.screen.Subscribe;
import com.haulmont.cuba.gui.screen.UIController;
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory;
import com.haulmont.cuba.web.theme.HaloTheme;

import javax.inject.Inject;

@UIController("demo-screen")
@Design("/com/haulmont/cuba/web/app/ui/demo/demo.xml")
public class DemoScreen extends Screen {

    @Inject
    protected ComponentsFactory componentsFactory;

    @Subscribe
    public void init(@SuppressWarnings("unused") InitEvent event) {
        Label<String> label = componentsFactory.createComponent(Label.NAME);
        label.setValue("IT IS ALIVE!");
        label.setIconFromSet(CubaIcon.MONEY);
        label.addStyleName(HaloTheme.LABEL_H2);

        getWindow().add(label);
    }
}