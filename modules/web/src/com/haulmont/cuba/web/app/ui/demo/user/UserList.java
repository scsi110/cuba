/*
 * Copyright (c) 2008-2018 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.haulmont.cuba.web.app.ui.demo.user;

import com.haulmont.cuba.gui.Screen;
import com.haulmont.cuba.gui.components.Label;
import com.haulmont.cuba.gui.screen.InitEvent;
import com.haulmont.cuba.gui.screen.UiController;
import com.haulmont.cuba.gui.screen.Subscribe;
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory;

import javax.inject.Inject;

@UiController("user-list")
public class UserList extends Screen {
    @Inject
    protected ComponentsFactory componentsFactory;

    @Subscribe
    protected void init(InitEvent event) {
        Label<String> label = componentsFactory.createComponent(Label.NAME);
        label.setValue("Demo");

        getWindow().add(label);

        getWindow().setCaption("Users");
    }
}