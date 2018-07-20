/*
 * Copyright (c) 2008-2016 Haulmont.
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
 *
 */
package com.haulmont.cuba.web.sys;

import com.haulmont.bali.events.EventHub;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.gui.Screen;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.WindowManagerUtils;
import com.haulmont.cuba.gui.components.Window;
import com.haulmont.cuba.gui.components.sys.WindowImplementation;
import com.haulmont.cuba.gui.config.WindowConfig;
import com.haulmont.cuba.gui.config.WindowInfo;
import com.haulmont.cuba.gui.screen.*;
import com.haulmont.cuba.gui.sys.ScreenViewsLoader;
import com.haulmont.cuba.gui.sys.UIControllerDependencyInjector;
import com.haulmont.cuba.gui.sys.UIControllerUtils;
import com.haulmont.cuba.gui.xml.layout.*;
import com.haulmont.cuba.gui.xml.layout.loaders.ComponentLoaderContext;
import com.haulmont.cuba.security.entity.PermissionType;
import com.haulmont.cuba.web.AppUI;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Locale;

@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Component(WindowManager.NAME)
public class WebWindowManager implements WindowManager {
    @Inject
    protected WindowConfig windowConfig;
    @Inject
    protected Security security;
    @Inject
    protected ComponentsFactory componentsFactory;
    @Inject
    protected BeanLocator beanLocator;
    @Inject
    protected ScreenXmlLoader screenXmlLoader;
    @Inject
    protected LayoutLoaderConfig layoutLoaderConfig;
    @Inject
    protected UserSessionSource userSessionSource;
    @Inject
    protected ScreenViewsLoader screenViewsLoader;

    protected AppUI ui;

    public WebWindowManager(AppUI ui) {
        this.ui = ui;
    }

    @Override
    public <T extends Screen> T create(Class<T> requiredScreenClass, LaunchMode launchMode, ScreenOptions options) {
        WindowInfo windowInfo = getScreenInfo(requiredScreenClass);

        checkPermissions(launchMode, windowInfo);

        // todo perf4j stop watches for lifecycle

        @SuppressWarnings("unchecked")
        Class<T> resolvedScreenClass = (Class<T>) windowInfo.getScreenClass();

        Window window = createWindow(windowInfo, resolvedScreenClass, launchMode);

        T controller = createController(windowInfo, window, resolvedScreenClass, launchMode);

        loadScreenXml(windowInfo, window, controller, options);

        WindowManagerUtils.setWindow(controller, window);

        WindowImplementation windowImpl = (WindowImplementation) window;
        windowImpl.setController(controller);
        windowImpl.setLaunchMode(launchMode);

        // todo legacy datasource layer

        UIControllerDependencyInjector dependencyInjector =
                beanLocator.getPrototype(UIControllerDependencyInjector.NAME, controller, options);
        dependencyInjector.inject();

        InitEvent initEvent = new InitEvent(controller, options);
        WindowManagerUtils.fireEvent(controller, InitEvent.class, initEvent);

        AfterInitEvent afterInitEvent = new AfterInitEvent(controller, options);
        WindowManagerUtils.fireEvent(controller, AfterInitEvent.class, afterInitEvent);

        return controller;
    }

    protected <T extends Screen> void loadScreenXml(WindowInfo windowInfo, Window window, T controller,
                                                    ScreenOptions options) {
        String templatePath = windowInfo.getTemplate();

        if (StringUtils.isNotEmpty(templatePath)) {
            // todo support relative design path

            Element element = screenXmlLoader.load(templatePath, windowInfo.getId(),
                    Collections.emptyMap()); // todo support legacy params map

            // todo load XML markup if annotation present, or screen is legacy screen

            ComponentLoaderContext componentLoaderContext =
                    new ComponentLoaderContext(Collections.emptyMap()); // todo support legacy parameters map
            componentLoaderContext.setFullFrameId(windowInfo.getId());
            componentLoaderContext.setCurrentFrameId(windowInfo.getId());

            ComponentLoader windowLoader = createLayout(windowInfo, window, element, componentLoaderContext);

            screenViewsLoader.deployViews(element); // todo will be removed from new screens

            // todo load datasources here

            windowLoader.loadComponent();

            EventHub eventHub = WindowManagerUtils.getEventHub(controller);
            eventHub.subscribe(AfterInitEvent.class, event -> {
                componentLoaderContext.setFrame(window);
                componentLoaderContext.executePostInitTasks();
            });
        }
    }

    protected ComponentLoader createLayout(WindowInfo windowInfo, Window window, Element rootElement,
                                           ComponentLoader.Context context) {
        String descriptorPath = windowInfo.getTemplate();

        LayoutLoader layoutLoader = new LayoutLoader(context, componentsFactory, layoutLoaderConfig);
        layoutLoader.setLocale(getLocale());

        // todo should we load messages depending on Class ?
        if (StringUtils.isNotEmpty(descriptorPath)) {
            if (descriptorPath.contains("/")) {
                descriptorPath = StringUtils.substring(descriptorPath, 0, descriptorPath.lastIndexOf("/"));
            }

            String path = descriptorPath.replaceAll("/", ".");
            int start = path.startsWith(".") ? 1 : 0;
            path = path.substring(start);

            layoutLoader.setMessagesPack(path);
        }
        //noinspection UnnecessaryLocalVariable
        ComponentLoader windowLoader = layoutLoader.createWindowContent(window, rootElement, windowInfo.getId());
        return windowLoader;
    }

    protected Locale getLocale() {
        return userSessionSource.getUserSession().getLocale();
    }

    @Override
    public void show(Screen screen) {
        checkMultiOpen(screen);

        // todo load and apply settings

        // todo UI security

        BeforeShowEvent beforeShowEvent = new BeforeShowEvent(screen);
        WindowManagerUtils.fireEvent(screen, BeforeShowEvent.class, beforeShowEvent);

        WindowImplementation windowImpl = (WindowImplementation) screen.getWindow();

        if (windowImpl.getLaunchMode() instanceof OpenMode) {
            OpenMode openMode = (OpenMode) windowImpl.getLaunchMode();

            switch (openMode) {
                case TOP_LEVEL:
                    // todo
                    ui.setTopLevelWindow((Window.TopLevelWindow) screen.getWindow());

                    break;

                case DIALOG:
                    // todo
                    break;

                case NEW_WINDOW:
                case NEW_TAB:
                    // todo
                    break;

                default:
                    throw new UnsupportedOperationException("Unsupported OpenMode " + openMode);
            }
        }

        AfterShowEvent afterShowEvent = new AfterShowEvent(screen);
        WindowManagerUtils.fireEvent(screen, AfterShowEvent.class, afterShowEvent);
    }

    @Override
    public void remove(Screen screen) {
        // todo remove event
    }

    protected <T extends Screen> T createController(WindowInfo windowInfo, Window window,
                                                    Class<T> screenClass, LaunchMode launchMode) {
        Constructor<T> constructor;
        try {
            constructor = screenClass.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new DevelopmentException("No public constructor for screen class " + screenClass);
        }

        T controller;
        try {
            controller = constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Unable to create instance of screen class " + screenClass);
        }

        return controller;
    }

    protected Window createWindow(WindowInfo windowInfo, Class<? extends Screen> screenClass, LaunchMode launchMode) {
        // todo here we should create TabWindow / DialogWindow / TopLevelWindow UI components depending on launchMode
        Window window = componentsFactory.createComponent(Window.class);
        if (launchMode == OpenMode.TOP_LEVEL) {
            window.setSizeFull();
        }

        return window;
    }

    protected void checkMultiOpen(Screen screen) {
        // todo check if already opened, replace buggy int hash code
    }

    protected void checkPermissions(LaunchMode launchMode, WindowInfo windowInfo) {
        // TOP_LEVEL windows are always permitted
        if (launchMode != OpenMode.TOP_LEVEL) {
            boolean permitted = security.isScreenPermitted(windowInfo.getId());
            if (!permitted) {
                throw new AccessDeniedException(PermissionType.SCREEN, windowInfo.getId());
            }
        }
    }

    protected WindowInfo getScreenInfo(Class<? extends Screen> screenClass) {
        UIController uiController = screenClass.getAnnotation(UIController.class);
        // todo legacy screens
        if (uiController == null) {
            throw new IllegalArgumentException("No @UIController annotation for class " + screenClass);
        }

        String screenId = UIControllerUtils.getInferredScreenId(uiController, screenClass);

        return windowConfig.getWindowInfo(screenId);
    }
}